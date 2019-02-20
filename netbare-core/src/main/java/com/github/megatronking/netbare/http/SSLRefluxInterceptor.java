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

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * An interceptor locates at the last layer of the interceptors. It is responsible for send
 * plaintext packets to {@link SSLCodecInterceptor}.
 *
 * @author Megatron King
 * @since 2018-11-15 15:39
 */
/* package */ class SSLRefluxInterceptor extends HttpInterceptor {

    private SSLRefluxCallback mRefluxCallback;

    /* package */ SSLRefluxInterceptor(SSLRefluxCallback refluxCallback) {
        this.mRefluxCallback = refluxCallback;
    }

    @Override
    protected void intercept(@NonNull HttpRequestChain chain, @NonNull ByteBuffer buffer)
            throws IOException {
        if (chain.request().isHttps()) {
            mRefluxCallback.onRequest(chain.request(), buffer);
        } else {
            chain.process(buffer);
        }
    }

    @Override
    protected void intercept(@NonNull HttpResponseChain chain, @NonNull ByteBuffer buffer)
            throws IOException {
        if (chain.response().isHttps()) {
            mRefluxCallback.onResponse(chain.response(), buffer);
        } else {
            chain.process(buffer);
        }
    }

}
