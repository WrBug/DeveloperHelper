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

import com.github.megatronking.netbare.NetBareUtils;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * An abstract base nio tunnel class uses nio operations, the sub class should provides IO
 * operations, such as connect, read and write.
 *
 * @param <T> An implementation class for selectable channels
 * @param <S> A socket protects by VPN service.
 *
 * @author Megatron King
 * @since 2018-11-18 18:34
 */
public abstract class NioTunnel<T extends AbstractSelectableChannel, S> implements Closeable,
        NioCallback, Tunnel {

    /**
     * Let the remote tunnel connects to remote server.
     *
     * @param address The remote server IP socket address.
     * @throws IOException if an I/O error occurs.
     */
    public abstract void connect(InetSocketAddress address) throws IOException;

    /**
     * Returns the socket should be protected by VPN service.
     *
     * @return A socket.
     */
    public abstract S socket();

    /**
     * Write the packet buffer to remote server.
     *
     * @param buffer A packet buffer.
     * @return The wrote length.
     * @throws IOException if an I/O error occurs.
     */
    protected abstract int channelWrite(ByteBuffer buffer) throws IOException;

    /**
     * Read data from remote server and put it into the given buffer.
     *
     * @param buffer A buffer to store data.
     * @return The read length.
     * @throws IOException if an I/O error occurs.
     */
    protected abstract int channelRead(ByteBuffer buffer) throws IOException;

    private final T mChannel;
    private final Selector mSelector;
    private SelectionKey mSelectionKey;

    private Queue<ByteBuffer> mPendingBuffers;

    private NioCallback mCallback;
    private boolean mIsClosed;

    NioTunnel(T channel, Selector selector) {
        this.mChannel = channel;
        this.mSelector = selector;
        this.mPendingBuffers = new ConcurrentLinkedDeque<>();
    }

    @Override
    public void onConnected() throws IOException {
        if (mCallback != null) {
            mCallback.onConnected();
        }
    }

    @Override
    public void onRead() throws IOException {
        if (mCallback != null) {
            mCallback.onRead();
        }
    }

    @Override
    public void onWrite() throws IOException {
        if (mCallback != null) {
            mCallback.onWrite();
        }
        // Write pending buffers.
        while (!mPendingBuffers.isEmpty()) {
            ByteBuffer buffer = mPendingBuffers.poll();
            int remaining = buffer.remaining();
            int sent = channelWrite(buffer);
            if (sent < remaining) {
                // Should wait next onWrite.
                mPendingBuffers.offer(buffer);
                return;
            }
        }
        interestRead();
    }

    @Override
    public void onClosed() {
        if (mCallback != null) {
            mCallback.onClosed();
        }
    }

    @Override
    public NioTunnel getTunnel() {
        return this;
    }

    @Override
    public void close() {
        mIsClosed = true;
        mPendingBuffers.clear();
        NetBareUtils.closeQuietly(mChannel);
    }

    @Override
    public void write(ByteBuffer buffer) throws IOException {
        if (mIsClosed) {
            return;
        }
        if (!buffer.hasRemaining()) {
            return;
        }
        mPendingBuffers.offer(buffer);
        interestWrite();
    }

    public int read(ByteBuffer buffer) throws IOException {
        buffer.clear();
        int len = channelRead(buffer);
        if (len > 0) {
            buffer.flip();
        }
        return len;
    }

    public boolean isClosed() {
        return mIsClosed;
    }

    /* package */ void setNioCallback(NioCallback callback) {
        this.mCallback = callback;
    }

    /* package */ void prepareRead() throws IOException {
        if (mChannel.isBlocking()) {
            mChannel.configureBlocking(false);
        }
        mSelector.wakeup();
        mSelectionKey = mChannel.register(mSelector, SelectionKey.OP_READ, this);
    }

    private void interestWrite() {
        if (mSelectionKey != null) {
            mSelector.wakeup();
            mSelectionKey.interestOps(SelectionKey.OP_WRITE);
        }
    }

    private void interestRead() {
        if (mSelectionKey != null) {
            mSelector.wakeup();
            mSelectionKey.interestOps(SelectionKey.OP_READ);
        }
    }


}
