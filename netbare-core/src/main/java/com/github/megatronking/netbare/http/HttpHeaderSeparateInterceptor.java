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

import com.github.megatronking.netbare.NetBareXLog;
import com.github.megatronking.netbare.NetBareUtils;
import com.github.megatronking.netbare.ip.Protocol;
import com.google.common.primitives.Bytes;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Separate HTTP header part and body part into different packets.
 *
 * @author Megatron King
 * @since 2018-12-08 15:36
 */
/* package */ final class HttpHeaderSeparateInterceptor extends HttpPendingInterceptor {

    private boolean mRequestHeaderHandled;
    private boolean mResponseHeaderHandled;

    private NetBareXLog mLog;

    @Override
    protected void intercept(@NonNull HttpRequestChain chain, @NonNull ByteBuffer buffer, int index)
            throws IOException {
        if (mLog == null) {
            mLog = new NetBareXLog(Protocol.TCP, chain.request().ip(), chain.request().port());
        }
        if (mRequestHeaderHandled) {
            chain.process(buffer);
            return;
        }
        if (!buffer.hasRemaining()) {
            chain.process(buffer);
            return;
        }
        buffer = mergeRequestBuffer(buffer);
        // Check the part end line.
        int headerEndIndex = Bytes.indexOf(buffer.array(), NetBareUtils.PART_END_BYTES);
        if (headerEndIndex < 0) {
            mLog.w("Http request header data is not enough.");
            // Not found the part end line, maybe the data is not enough, wait next buffer coming.
            pendRequestBuffer(buffer);
        } else {
            mRequestHeaderHandled = true;
            // Check whether the header and the body are in the same buffer.
            boolean hasMultiPart = headerEndIndex < buffer.limit() - NetBareUtils.PART_END_BYTES.length;
            if (hasMultiPart) {
                mLog.w("Multiple http request parts are founded.");
                // Separate the header and body data to two buffers.
                int offset = headerEndIndex + NetBareUtils.PART_END_BYTES.length;
                ByteBuffer headerBuffer = ByteBuffer.wrap(buffer.array(), buffer.position(), offset);
                // Allocate a new buffer, do not use wrap, different buffers will share the same array.
                ByteBuffer bodyBuffer = ByteBuffer.allocate(buffer.limit() - offset);
                bodyBuffer.put(buffer.array(), offset, buffer.limit() - offset);
                bodyBuffer.flip();
                chain.process(headerBuffer);
                chain.process(bodyBuffer);
            } else {
                chain.process(buffer);
            }
        }
    }

    @Override
    protected void intercept(@NonNull HttpResponseChain chain, @NonNull ByteBuffer buffer, int index)
            throws IOException {
        if (mLog == null) {
            mLog = new NetBareXLog(Protocol.TCP, chain.response().ip(), chain.response().port());
        }
        if (mResponseHeaderHandled) {
            chain.process(buffer);
            return;
        }
        if (!buffer.hasRemaining()) {
            chain.process(buffer);
            return;
        }
        buffer = mergeResponseBuffer(buffer);
        // Check the part end line.
        int headerEndIndex = Bytes.indexOf(buffer.array(), NetBareUtils.PART_END_BYTES);
        if (headerEndIndex < 0) {
            mLog.w("Http response header data is not enough.");
            // Not found the part end line, maybe the data is not enough, wait next buffer coming.
            pendResponseBuffer(buffer);
        } else {
            mResponseHeaderHandled = true;
            // Check whether the header and the body are in the same buffer.
            boolean hasMultiPart = headerEndIndex < buffer.limit() - NetBareUtils.PART_END_BYTES.length;
            if (hasMultiPart) {
                mLog.w("Multiple http response parts are founded.");
                // Separate the header and body data to two buffers.
                int offset = headerEndIndex + NetBareUtils.PART_END_BYTES.length;
                ByteBuffer headerBuffer = ByteBuffer.wrap(buffer.array(), buffer.position(), offset);
                // Allocate a new buffer, do not use wrap, different buffers will share the same array.
                ByteBuffer bodyBuffer = ByteBuffer.allocate(buffer.limit() - offset);
                bodyBuffer.put(buffer.array(), offset, buffer.limit() - offset);
                bodyBuffer.flip();
                chain.process(headerBuffer);
                chain.process(bodyBuffer);
            } else {
                chain.process(buffer);
            }
        }
    }

    @Override
    protected void onRequestFinished(@NonNull HttpRequest request) {
        super.onRequestFinished(request);
        mRequestHeaderHandled = false;
    }

    @Override
    protected void onResponseFinished(@NonNull HttpResponse response) {
        super.onResponseFinished(response);
        mResponseHeaderHandled = false;
    }

}
