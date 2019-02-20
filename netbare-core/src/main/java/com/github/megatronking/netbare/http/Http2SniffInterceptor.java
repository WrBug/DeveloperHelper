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
import com.github.megatronking.netbare.http2.Http2;
import com.google.common.primitives.Bytes;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Verifies the HTTP packet and determines whether it is a HTTP2 protocol packets.
 *
 * @author Megatron King
 * @since 2019/1/5 14:02
 */
/* package */ class Http2SniffInterceptor extends HttpIndexInterceptor {

    private SSLRefluxCallback mCallback;
    private NetBareXLog mLog;

    /* package */ Http2SniffInterceptor(SSLRefluxCallback callback) {
        this.mCallback = callback;
    }

    @Override
    protected void intercept(@NonNull HttpRequestChain chain, @NonNull ByteBuffer buffer,
                             int index) throws IOException {
        if (index == 0) {
            HttpRequest request = chain.request();
            if (mLog == null) {
                mLog = new NetBareXLog(request.protocol(), request.ip(), request.port());
            }
            // HTTP2 is forces to use SSL connection.
            if (request.isHttps()) {
                if (buffer.hasRemaining() && Bytes.indexOf(buffer.array(),
                        Http2.CONNECTION_PREFACE) == buffer.position()) {
                    mLog.i("Send a connection preface to remote server.");
                    request.session().protocol = HttpProtocol.HTTP_2;
                    if (buffer.remaining() == Http2.CONNECTION_PREFACE.length) {
                        // Skip preface frame data.
                        mCallback.onRequest(request, buffer);
                        return;
                    } else {
                        ByteBuffer prefaceBuffer = ByteBuffer.allocate(Http2.CONNECTION_PREFACE.length);
                        prefaceBuffer.put(Http2.CONNECTION_PREFACE);
                        prefaceBuffer.flip();
                        mCallback.onRequest(request, prefaceBuffer);
                        // The remaining data continues.
                        buffer.position(buffer.position() + Http2.CONNECTION_PREFACE.length);
                    }
                }
            }
        }
        if (buffer.hasRemaining()) {
            chain.process(buffer);
        }
    }

    @Override
    protected void intercept(@NonNull HttpResponseChain chain, @NonNull ByteBuffer buffer,
                             int index) throws IOException {
        if (index == 0) {
            HttpResponse response = chain.response();
            if (mLog == null) {
                mLog = new NetBareXLog(response.protocol(), response.ip(), response.port());
            }
            // HTTP2 is forces to use SSL connection.
            if (response.isHttps()) {
                if (buffer.hasRemaining() && Bytes.indexOf(buffer.array(),
                        Http2.CONNECTION_PREFACE) == buffer.position()) {
                    mLog.i("Receive a connection preface from remote server.");
                    response.session().protocol = HttpProtocol.HTTP_2;
                    if (buffer.remaining() == Http2.CONNECTION_PREFACE.length) {
                        // Skip preface frame data.
                        mCallback.onResponse(response, buffer);
                        return;
                    } else {
                        ByteBuffer prefaceBuffer = ByteBuffer.allocate(Http2.CONNECTION_PREFACE.length);
                        prefaceBuffer.put(Http2.CONNECTION_PREFACE);
                        prefaceBuffer.flip();
                        mCallback.onResponse(response, prefaceBuffer);
                        // The remaining data continues.
                        buffer.position(buffer.position() + Http2.CONNECTION_PREFACE.length);
                    }
                }
            }
        }
        if (buffer.hasRemaining()) {
            chain.process(buffer);
        }
    }

}
