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

import com.github.megatronking.netbare.NetBareLog;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * A TCP protocol implementation based with {@link NioTunnel}.
 *
 * @author Megatron King
 * @since 2018-11-21 00:36
 */
public abstract class TcpTunnel extends NioTunnel<SocketChannel, Socket> {

    private final SocketChannel mSocketChannel;
    private final Selector mSelector;

    TcpTunnel(SocketChannel socketChannel, Selector selector) {
        super(socketChannel, selector);
        this.mSocketChannel = socketChannel;
        this.mSelector = selector;
    }

    @Override
    public void connect(InetSocketAddress address) throws IOException {
        NetBareLog.i("TCP connects to: %s:%s",
                address.getAddress().getHostAddress(), address.getPort());
        if (mSocketChannel.isBlocking()) {
            mSocketChannel.configureBlocking(false);
        }
        mSocketChannel.register(mSelector, SelectionKey.OP_CONNECT, this);
        mSocketChannel.connect(address);
    }

    @Override
    public void onConnected() throws IOException {
        if (mSocketChannel.finishConnect()) {
            super.onConnected();
        } else {
            throw new IOException("[TCP]The tunnel socket is not connected.");
        }
    }

    @Override
    public Socket socket() {
        return mSocketChannel.socket();
    }

    @Override
    protected int channelRead(ByteBuffer buffer) throws IOException {
        return mSocketChannel.read(buffer);
    }

    @Override
    protected int channelWrite(ByteBuffer buffer) throws IOException {
        return mSocketChannel.write(buffer);
    }

}
