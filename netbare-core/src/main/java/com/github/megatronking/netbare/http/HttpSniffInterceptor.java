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

import com.github.megatronking.netbare.NetBareLog;
import com.github.megatronking.netbare.ssl.SSLCodec;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A fronted interceptor verifies the first net packet in order to determine whether it is a HTTP
 * protocol packet. If the packet is not a valid HTTP packet, it will be sent to tunnel directly,
 * otherwise sent to the next interceptor.
 *
 * @author Megatron King
 * @since 2018-12-04 11:58
 */
/* package */ final class HttpSniffInterceptor extends HttpIndexInterceptor {

    private static final int TYPE_HTTP = 1;
    private static final int TYPE_HTTPS = 2;
    private static final int TYPE_INVALID = 3;

    private final HttpSession mSession;

    private int mType;

    /* package */ HttpSniffInterceptor(HttpSession session) {
        this.mSession = session;
    }

    @Override
    protected void intercept(@NonNull HttpRequestChain chain, @NonNull ByteBuffer buffer,
                             int index) throws IOException {
        if (index == 0) {
            mType = chain.request().host() == null ? TYPE_INVALID : verifyHttpType(buffer);
        }
        if (mType == TYPE_HTTPS) {
            mSession.isHttps = true;
        }
        if (mType == TYPE_INVALID) {
            chain.processFinal(buffer);
            return;
        }
        chain.process(buffer);
    }

    @Override
    protected void intercept(@NonNull HttpResponseChain chain, @NonNull ByteBuffer buffer,
                             int index) throws IOException {
        if (mType == TYPE_INVALID) {
            chain.processFinal(buffer);
            return;
        }
        chain.process(buffer);
    }

    private int verifyHttpType(ByteBuffer buffer) {
        if (!buffer.hasRemaining()) {
            return TYPE_INVALID;
        }
        byte first = buffer.get(buffer.position());
        switch (first) {
            // GET
            case 'G':
                // HEAD
            case 'H':
                // POST, PUT, PATCH
            case 'P':
                // DELETE
            case 'D':
                // OPTIONS
            case 'O':
                // TRACE
            case 'T':
                // CONNECT
            case 'C':
                return TYPE_HTTP;
            // HTTPS
            case SSLCodec.SSL_CONTENT_TYPE_ALERT:
            case SSLCodec.SSL_CONTENT_TYPE_APPLICATION_DATA:
            case SSLCodec.SSL_CONTENT_TYPE_CHANGE_CIPHER_SPEC:
            case SSLCodec.SSL_CONTENT_TYPE_EXTENSION_HEARTBEAT:
            case SSLCodec.SSL_CONTENT_TYPE_HANDSHAKE:
                return TYPE_HTTPS;
            default:
                // Unknown first byte data.
                NetBareLog.e("Unknown first request byte : " + first);
                break;
        }
        return TYPE_INVALID;
    }


}
