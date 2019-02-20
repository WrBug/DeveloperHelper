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
package com.github.megatronking.netbare.http;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * An abstract interceptor provides multi-apis for packet pending. The packet will be stored in a
 * queue, and you can merge them with another packet.
 *
 * @author Megatron King
 * @since 2018-12-09 12:07
 */
public abstract class HttpPendingInterceptor extends HttpIndexInterceptor {

    private List<ByteBuffer> mRequestPendingBuffers;
    private List<ByteBuffer> mResponsePendingBuffers;

    /**
     * Constructs a {@link HttpPendingInterceptor} instance.
     */
    public HttpPendingInterceptor() {
        mRequestPendingBuffers = new ArrayList<>();
        mResponsePendingBuffers = new ArrayList<>();
    }

    @Override
    protected void onRequestFinished(@NonNull HttpRequest request) {
        super.onRequestFinished(request);
        mRequestPendingBuffers.clear();
    }

    @Override
    protected void onResponseFinished(@NonNull HttpResponse response) {
        super.onResponseFinished(response);
        mResponsePendingBuffers.clear();
    }

    /**
     * Pend a request packet buffer to waiting queue.
     *
     * @param buffer A request packet.
     */
    protected void pendRequestBuffer(ByteBuffer buffer) {
        mRequestPendingBuffers.add(buffer);
    }

    /**
     * Pend a response packet buffer to waiting queue.
     *
     * @param buffer A response packet.
     */
    protected void pendResponseBuffer(ByteBuffer buffer) {
        mResponsePendingBuffers.add(buffer);
    }

    /**
     * Merge all the request pending buffers and a given buffer, and output a new buffer which
     * contains all data. The pending buffers will be clear after the merge action.
     *
     * @param buffer A fresh packet buffer.
     * @return A new buffer.
     */
    protected ByteBuffer mergeRequestBuffer(ByteBuffer buffer) {
        return merge(mRequestPendingBuffers, buffer);
    }

    /**
     * Merge all the response pending buffers and a given buffer, and output a new buffer which
     * contains all data. The pending buffers will be clear after the merge action.
     *
     * @param buffer A fresh packet buffer.
     * @return A new buffer.
     */
    protected ByteBuffer mergeResponseBuffer(ByteBuffer buffer) {
        return merge(mResponsePendingBuffers, buffer);
    }

    private ByteBuffer merge(List<ByteBuffer> pendingBuffers, ByteBuffer buffer) {
        if (!pendingBuffers.isEmpty()) {
            int total = 0;
            for (ByteBuffer pendingBuffer : pendingBuffers) {
                total += pendingBuffer.remaining();
            }
            total += buffer.remaining();

            // Merge elder buffer first.
            int offset = 0;
            byte[] array = new byte[total];
            for (ByteBuffer pendingBuffer : pendingBuffers) {
                System.arraycopy(pendingBuffer.array(), pendingBuffer.position(), array, offset,
                        pendingBuffer.remaining());
                offset += pendingBuffer.remaining();
            }

            // Merge the incoming buffer
            System.arraycopy(buffer.array(), buffer.position(), array, offset, buffer.remaining());

            buffer = ByteBuffer.wrap(array);
            // Clear all data.
            pendingBuffers.clear();
        }
        return buffer;
    }

}
