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
package com.github.megatronking.netbare.tunnel;

import com.github.megatronking.netbare.NetBareXLog;
import com.github.megatronking.netbare.ip.Protocol;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * A TCP tunnel communicates with the VPN service.
 *
 * @author Megatron King
 * @since 2018-11-21 01:40
 */
public class TcpProxyTunnel extends TcpTunnel {

    private NetBareXLog mLog;

    public TcpProxyTunnel(SocketChannel socketChannel, Selector selector, int remotePort) {
        super(socketChannel, selector);
        Socket socket = socketChannel.socket();
        this.mLog = new NetBareXLog(Protocol.TCP, socket.getInetAddress().getHostAddress(),
                remotePort);
    }

    @Override
    public void connect(InetSocketAddress address) {
        // Nothing to connect
    }

    @Override
    public void onConnected() throws IOException {
        mLog.i("Proxy tunnel is connected.");
        super.onConnected();
    }

    @Override
    public int read(ByteBuffer buffer) throws IOException {
        int len = super.read(buffer);
        mLog.i("Read from proxy: " + len);
        return len;
    }

    @Override
    public void write(ByteBuffer buffer) throws IOException {
        mLog.i("Write to proxy: " + buffer.remaining());
        super.write(buffer);
    }

    @Override
    public void close() {
        mLog.i("Proxy tunnel is closed.");
        super.close();
    }

}
