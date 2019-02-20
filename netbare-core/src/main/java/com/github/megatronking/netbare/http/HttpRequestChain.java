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

import com.github.megatronking.netbare.gateway.InterceptorChain;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Http request chain, responsible for intercepting http request packets.
 *
 * @author Megatron King
 * @since 2018-11-16 23:21
 */
public class HttpRequestChain extends InterceptorChain<HttpRequest, HttpInterceptor> {

    private HttpZygoteRequest mZygoteRequest;

    /* package */ HttpRequestChain(HttpZygoteRequest request, List<HttpInterceptor> interceptors) {
        this(request, interceptors, 0);
    }

    /* package */ HttpRequestChain(HttpZygoteRequest request, List<HttpInterceptor> interceptors,
                                   int index) {
        super(request, interceptors, index);
        this.mZygoteRequest = request;
    }

    HttpZygoteRequest zygoteRequest() {
        return mZygoteRequest;
    }

    @Override
    protected void processNext(ByteBuffer buffer, HttpRequest request,
                               List<HttpInterceptor> interceptors, int index) throws IOException {
        HttpInterceptor interceptor = interceptors.get(index);
        if (interceptor != null) {
            interceptor.intercept(new HttpRequestChain(mZygoteRequest, interceptors, ++index), buffer);
        }
    }

    @NonNull
    public HttpRequest request() {
        HttpRequest active = mZygoteRequest.getActive();
        return active != null ? active : mZygoteRequest;
    }

}
