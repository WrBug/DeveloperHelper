/*  NetBare - An android network capture and injection library.
 *  Copyright (C) 2018-2019 Megatron King
 *  Copyright (C) 2018-2019 GuoShi
 *
 *  NetBare is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU General Public License as published by the Free Software Found-
 *  ation, either version 3 of the License, or (at your option) any later version.
 *
 *  NetBare is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 *  PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with NetBare.
 *  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.megatronking.netbare.proxy;

import android.net.VpnService;

import com.github.megatronking.netbare.NetBareLog;
import com.github.megatronking.netbare.NetBareUtils;
import com.github.megatronking.netbare.gateway.VirtualGateway;
import com.github.megatronking.netbare.net.Session;
import com.github.megatronking.netbare.net.SessionProvider;
import com.github.megatronking.netbare.tunnel.ConnectionShutdownException;
import com.github.megatronking.netbare.tunnel.NioCallback;
import com.github.megatronking.netbare.tunnel.NioTunnel;
import com.github.megatronking.netbare.tunnel.TcpProxyTunnel;
import com.github.megatronking.netbare.tunnel.TcpRemoteTunnel;
import com.github.megatronking.netbare.tunnel.TcpTunnel;
import com.github.megatronking.netbare.tunnel.TcpVATunnel;

import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

/**
 * The TCP proxy server is a nio {@link ServerSocketChannel}, it listens the connections from
 * {@link VpnService} and forwards request packets to real remote server. This server uses
 * {@link TcpVATunnel} to bind {@link VirtualGateway} and {@link NioTunnel} together. Every TCP
 * connection has two channels: {@link TcpProxyTunnel} and {@link TcpRemoteTunnel}.
 * The {@link TcpProxyTunnel} is responsible for sending remote server response packets to VPN
 * service, and the {@link TcpRemoteTunnel} is responsible for communicating with remote server.
 *
 * @author Megatron King
 * @since 2018-10-11 17:35
 */
/* package */ class TcpProxyServer extends BaseProxyServer implements Runnable {

    private final VpnService mVpnService;

    private final Selector mSelector;
    private final ServerSocketChannel mServerSocketChannel;

    private int mIp;
    private short mPort;
    private int mMtu;

    private SessionProvider mSessionProvider;

    /* package */ TcpProxyServer(VpnService vpnService, String ip, int mtu)
            throws IOException {
        super("TcpProxyServer");
        this.mVpnService = vpnService;

        this.mSelector = Selector.open();
        this.mServerSocketChannel = ServerSocketChannel.open();
        this.mServerSocketChannel.configureBlocking(false);
        this.mServerSocketChannel.socket().bind(new InetSocketAddress(0));
        this.mServerSocketChannel.register(mSelector, SelectionKey.OP_ACCEPT);

        this.mIp = NetBareUtils.convertIp(ip);
        this.mPort = (short) mServerSocketChannel.socket().getLocalPort();
        this.mMtu = mtu;

        NetBareLog.v("[TCP]proxy server: %s:%d", ip, NetBareUtils.convertPort(mPort));
    }

    void setSessionProvider(SessionProvider sessionProvider) {
        this.mSessionProvider = sessionProvider;
    }


    @Override
    int getIp() {
        return mIp;
    }

    @Override
    short getPort() {
        return mPort;
    }

    @Override
    public void run() {
        NetBareLog.i("[TCP]Server starts running.");
        super.run();
        NetBareUtils.closeQuietly(mSelector);
        NetBareUtils.closeQuietly(mServerSocketChannel);
        NetBareLog.i("[TCP]Server stops running.");
    }

    @Override
    protected void process() throws IOException {
        int select = mSelector.select();
        if (select == 0) {
            return;
        }
        Set<SelectionKey> selectedKeys = mSelector.selectedKeys();
        if (selectedKeys == null) {
            return;
        }
        Iterator<SelectionKey> iterator = selectedKeys.iterator();
        while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            try {
                if (key.isValid()) {
                    if (key.isAcceptable()) {
                        onAccept();
                    } else {
                        Object attachment = key.attachment();
                        if (attachment instanceof NioCallback) {
                            NioCallback callback = (NioCallback) attachment;
                            try {
                                if (key.isConnectable()) {
                                    callback.onConnected();
                                } else if (key.isReadable()) {
                                    callback.onRead();
                                } else if (key.isWritable()) {
                                    callback.onWrite();
                                }
                            } catch (IOException e) {
                                NioTunnel tunnel = callback.getTunnel();
                                if (!tunnel.isClosed()) {
                                    handleException(e);
                                }
                                callback.onClosed();
                            }
                        }
                    }
                }
            } finally {
                iterator.remove();
            }

        }
    }

    private void onAccept() throws IOException {
        SocketChannel clientChannel = mServerSocketChannel.accept();
        Socket clientSocket = clientChannel.socket();

        // The client ip is the remote server ip
        // The client port is the local port(it is the vpn port not the proxy server port)
        String ip = clientSocket.getInetAddress().getHostAddress();
        int port = clientSocket.getPort();

        // The session should have be saved before the tcp packets be forwarded to proxy server. So
        // we can query it by client port.
        Session session = mSessionProvider.query((short) port);
        if (session == null) {
            throw new IOException("No session saved with key: " + port);
        }

        int remotePort = NetBareUtils.convertPort(session.remotePort);

        // Connect remote server and dispatch data.
        TcpTunnel proxyTunnel = null;
        TcpTunnel remoteTunnel = null;
        try {
            proxyTunnel = new TcpProxyTunnel(clientChannel, mSelector, remotePort);
            remoteTunnel = new TcpRemoteTunnel(mVpnService, SocketChannel.open(),
                    mSelector, ip, remotePort);
            TcpVATunnel gatewayTunnel = new TcpVATunnel(session, proxyTunnel,
                    remoteTunnel, mMtu);
            gatewayTunnel.connect(new InetSocketAddress(ip, remotePort));
        } catch (IOException e){
            NetBareUtils.closeQuietly(proxyTunnel);
            NetBareUtils.closeQuietly(remoteTunnel);
            throw e;
        }
    }

    private void handleException(IOException e) {
        if (e == null || e.getMessage() == null) {
            return;
        }
        if (e instanceof SSLHandshakeException) {
            // Client doesn't accept the MITM CA certificate.
            NetBareLog.e(e.getMessage());
        } else if (e instanceof ConnectionShutdownException) {
            // Connection exception, do not mind this.
            NetBareLog.e(e.getMessage());
        } else if (e instanceof ConnectException) {
            // Connection timeout
            NetBareLog.e(e.getMessage());
        } else if (e instanceof SSLException && (e.getCause() instanceof EOFException)) {
            // Connection shutdown manually
            NetBareLog.e(e.getMessage());
        } else {
            NetBareLog.wtf(e);
        }
    }

}
