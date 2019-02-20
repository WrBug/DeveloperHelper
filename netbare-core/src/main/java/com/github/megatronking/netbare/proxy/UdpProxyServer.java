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
import android.os.SystemClock;

import com.github.megatronking.netbare.NetBareLog;
import com.github.megatronking.netbare.NetBareUtils;
import com.github.megatronking.netbare.gateway.VirtualGateway;
import com.github.megatronking.netbare.ip.IpHeader;
import com.github.megatronking.netbare.ip.UdpHeader;
import com.github.megatronking.netbare.net.Session;
import com.github.megatronking.netbare.net.SessionProvider;
import com.github.megatronking.netbare.tunnel.NioCallback;
import com.github.megatronking.netbare.tunnel.NioTunnel;
import com.github.megatronking.netbare.tunnel.Tunnel;
import com.github.megatronking.netbare.tunnel.UdpRemoteTunnel;
import com.github.megatronking.netbare.tunnel.UdpVATunnel;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The UDP proxy server is a virtual server, every packet from {@link UdpProxyServerForwarder} is
 * saw as a connection. It use {@link UdpVATunnel} to bind {@link VirtualGateway} and
 * {@link NioTunnel} together. Not like TCP, UDP only use {@link UdpRemoteTunnel} to communicate with
 * real remote server.
 *
 * @author Megatron King
 * @since 2018-10-11 17:35
 */
/* package */ class UdpProxyServer extends BaseProxyServer {

    private static final int SELECTOR_WAIT_TIME = 50;

    private final VpnService mVpnService;

    private int mMtu;

    private final Selector mSelector;
    private final Map<Short, UdpVATunnel> mTunnels;

    private SessionProvider mSessionProvider;

    /* package */ UdpProxyServer(VpnService vpnService, int mtu) throws IOException {
        super("UdpProxyServer");
        this.mVpnService = vpnService;

        this.mMtu = mtu;

        this.mSelector = Selector.open();
        this.mTunnels = new ConcurrentHashMap<>();
    }

    @Override
    int getIp() {
        return 0;
    }

    @Override
    short getPort() {
        return 0;
    }

    void setSessionProvider(SessionProvider sessionProvider) {
        this.mSessionProvider = sessionProvider;
    }

    void send(UdpHeader header, OutputStream output) throws IOException {
        short localPort = header.getSourcePort();
        UdpVATunnel tunnel = mTunnels.get(localPort);
        try {
            if (tunnel == null) {
                Session session = mSessionProvider.query(localPort);
                if (session == null) {
                    throw new IOException("No session saved with key: " + localPort);
                }

                IpHeader ipHeader = header.getIpHeader();
                NioTunnel remoteTunnel = new UdpRemoteTunnel(mVpnService, DatagramChannel.open(),
                        mSelector, NetBareUtils.convertIp(session.remoteIp), session.remotePort);
                tunnel = new UdpVATunnel(session, remoteTunnel, output, mMtu);
                tunnel.connect(new InetSocketAddress(NetBareUtils.convertIp(ipHeader.getDestinationIp()),
                        NetBareUtils.convertPort(header.getDestinationPort())));
                mTunnels.put(header.getSourcePort(), tunnel);
            }
            tunnel.send(header);
        } catch (IOException e) {
            mTunnels.remove(localPort);
            NetBareUtils.closeQuietly(tunnel);
            throw e;
        }
    }

    @Override
    public void run() {
        NetBareLog.i("[UDP]Server starts running.");
        super.run();
        NetBareUtils.closeQuietly(mSelector);
        NetBareLog.i("[UDP]Server stops running.");
    }

    @Override
    protected void process() throws IOException {
        int select = mSelector.select();
        if (select == 0) {
            // Wait a short time to let the selector register or interest.
            SystemClock.sleep(SELECTOR_WAIT_TIME);
            return;
        }
        Set<SelectionKey> selectedKeys = mSelector.selectedKeys();
        if (selectedKeys == null) {
            return;
        }
        Iterator<SelectionKey> iterator = selectedKeys.iterator();
        while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            if (key.isValid()) {
                Object attachment = key.attachment();
                if (attachment instanceof NioCallback) {
                    NioCallback callback = (NioCallback) attachment;
                    try {
                        if (key.isReadable()) {
                            callback.onRead();
                        } else if (key.isWritable()) {
                            callback.onWrite();
                        } else if (key.isConnectable()) {
                            callback.onConnected();
                        }
                    } catch (IOException e) {
                        callback.onClosed();
                        removeTunnel(callback.getTunnel());
                    }
                }
            }
            iterator.remove();
        }
    }

    @Override
    void stopServer() {
        super.stopServer();
        for (UdpVATunnel tunnel : mTunnels.values()) {
            NetBareUtils.closeQuietly(tunnel);
        }
    }

    private void removeTunnel(Tunnel tunnel) {
        Map<Short, UdpVATunnel> tunnels = new HashMap<>(mTunnels);
        for (short key : tunnels.keySet()) {
            if (tunnels.get(key).getRemoteChannel() == tunnel) {
                mTunnels.remove(key);
            }
        }
    }

}
