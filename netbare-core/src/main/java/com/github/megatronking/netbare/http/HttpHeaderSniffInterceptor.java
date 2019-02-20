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

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Detect the plaintext packet header to determine is it the HTTP protocol.
 *
 * @author Megatron King
 * @since 2019/1/31 16:13
 */
/* package */ class HttpHeaderSniffInterceptor extends HttpIndexInterceptor {

    private final SSLRefluxCallback mCallback;

    private boolean mRealHttpProtocol;

    /* package */ HttpHeaderSniffInterceptor(SSLRefluxCallback callback) {
        this.mCallback = callback;
    }

    @Override
    protected void intercept(@NonNull HttpRequestChain chain, @NonNull ByteBuffer buffer,
                             int index) throws IOException {
        if (!buffer.hasRemaining()) {
            return;
        }
        if (chain.request().httpProtocol() != null) {
            chain.process(buffer);
            return;
        }
        if (index == 0) {
            if (requestHeaderFirstByteNotPassed(buffer.get(buffer.position()))) {
                mCallback.onRequest(chain.request(), buffer);
                return;
            }
            // Sniff request header method
            if (buffer.remaining() >= 7 && requestHeaderMethodNotPassed(buffer)) {
                mCallback.onRequest(chain.request(), buffer);
                return;
            }
            mRealHttpProtocol = true;
            chain.process(buffer);
        } else {
            if (mRealHttpProtocol) {
                chain.process(buffer);
            }  else {
                mCallback.onRequest(chain.request(), buffer);
            }
        }
    }

    @Override
    protected void intercept(@NonNull HttpResponseChain chain, @NonNull ByteBuffer buffer,
                             int index) throws IOException {
        if (!buffer.hasRemaining()) {
            return;
        }
        if (chain.response().httpProtocol() != null) {
            chain.process(buffer);
            return;
        }
        if (index == 0) {
            if (responseHeaderFirstByteNotPassed(buffer.get(buffer.position()))) {
                mCallback.onResponse(chain.response(), buffer);
                return;
            }
            // Sniff response header protocol
            if (buffer.remaining() >= 8 && responseHeaderProtocolNotPassed(buffer)) {
                mCallback.onResponse(chain.response(), buffer);
                return;
            }
            mRealHttpProtocol = true;
            chain.process(buffer);
        } else {
            if (mRealHttpProtocol) {
                chain.process(buffer);
            }  else {
                mCallback.onResponse(chain.response(), buffer);
            }
        }
    }

    private boolean requestHeaderFirstByteNotPassed(byte first) {
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
                return false;
            default:
                // Unknown first byte data.
                NetBareLog.w("Unknown first request header byte : " + first);
                break;
        }
        return true;
    }

    private boolean requestHeaderMethodNotPassed(ByteBuffer buffer) {
        String headerMethod = new String(buffer.array(), buffer.position(),
                buffer.position() + 7);
        for (HttpMethod method : HttpMethod.values()) {
            if (method == HttpMethod.UNKNOWN) {
                continue;
            }
            if (headerMethod.startsWith(method.name())) {
                return false;
            }
        }
        NetBareLog.w("Unknown request header method : " + headerMethod);
        return true;
    }

    private boolean responseHeaderFirstByteNotPassed(byte first) {
        switch (first) {
                // h2
            case 'h':
                // HTTP1.x
            case 'H':
                return false;
            default:
                // Unknown first byte data.
                NetBareLog.w("Unknown first response header byte : " + first);
                break;
        }
        return true;
    }

    private boolean responseHeaderProtocolNotPassed(ByteBuffer buffer) {
        String headerProtocol = new String(buffer.array(), buffer.position(),
                buffer.position() + 8);
        for (HttpProtocol protocol : HttpProtocol.values()) {
            if (protocol == HttpProtocol.UNKNOWN || protocol == HttpProtocol.H2_PRIOR_KNOWLEDGE
                    || protocol == HttpProtocol.SPDY_3 || protocol == HttpProtocol.QUIC) {
                continue;
            }
            if (headerProtocol.startsWith(protocol.toString())) {
                return false;
            }
        }
        NetBareLog.w("Unknown response header protocol : " + headerProtocol);
        return true;
    }

}
