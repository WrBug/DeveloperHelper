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

import android.net.Uri;
import android.os.Process;
import androidx.annotation.NonNull;

import com.github.megatronking.netbare.injector.HttpInjector;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A HTTP inject interceptor holds a {@link HttpInjector}.
 *
 * @author Megatron King
 * @since 2018-12-29 22:25
 */
public final class HttpInjectInterceptor extends HttpIndexInterceptor {

    private boolean mShouldInjectRequest;
    private boolean mShouldInjectResponse;

    private HttpInjector mHttpInjector;

    private HttpInjectInterceptor(HttpInjector httpInjector) {
        this.mHttpInjector = httpInjector;
    }

    @Override
    protected final void intercept(@NonNull final HttpRequestChain chain,
                                   @NonNull ByteBuffer buffer, int index) throws IOException {
        if (chain.request().uid() == Process.myUid()) {
            chain.process(buffer);
            return;
        }
        if (index == 0) {
            mShouldInjectRequest = mHttpInjector.sniffRequest(chain.request());
        }
        if (!mShouldInjectRequest) {
            chain.process(buffer);
            return;
        }
        if (index == 0) {
            mHttpInjector.onRequestInject(buildHeader(chain.request()), new HttpRequestInjectorCallback(chain));
        } else {
            mHttpInjector.onRequestInject(chain.request(), new HttpRawBody(buffer), new HttpRequestInjectorCallback(chain));
        }
    }

    @Override
    protected final void intercept(@NonNull final HttpResponseChain chain,
                                   @NonNull ByteBuffer buffer, int index) throws IOException {
        if (chain.response().uid() == Process.myUid()) {
            chain.process(buffer);
            return;
        }
        if (index == 0) {
            mShouldInjectResponse = mHttpInjector.sniffResponse(chain.response());
        }
        if (!mShouldInjectResponse) {
            chain.process(buffer);
            return;
        }
        if (index == 0) {
            mHttpInjector.onResponseInject(buildHeader(chain.response()),
                    new HttpResponseInjectorCallback(chain));
        } else {
            mHttpInjector.onResponseInject(chain.response(), new HttpRawBody(buffer),
                    new HttpResponseInjectorCallback(chain));
        }
    }

    @Override
    protected void onRequestFinished(@NonNull HttpRequest request) {
        super.onRequestFinished(request);
        if (mShouldInjectRequest) {
            mHttpInjector.onRequestFinished(request);
        }
        mShouldInjectRequest = false;
    }

    @Override
    protected void onResponseFinished(@NonNull HttpResponse response) {
        super.onResponseFinished(response);
        if (mShouldInjectResponse) {
            mHttpInjector.onResponseFinished(response);
        }
        mShouldInjectResponse = false;
    }

    private HttpRequestHeaderPart buildHeader(HttpRequest request) {
        return new HttpRequestHeaderPart.Builder(request.httpProtocol(), Uri.parse(request.url()),
                request.requestHeaders(), request.method())
                .build();
    }

    private HttpResponseHeaderPart buildHeader(HttpResponse response) {
        return new HttpResponseHeaderPart.Builder(response.httpProtocol(), Uri.parse(response.url()),
                response.responseHeaders(), response.code(), response.message())
                .build();
    }

    /**
     * A factory produces {@link HttpInjectInterceptor} instance.
     *
     * @param httpInjector A HTTP injector.
     * @return An instance of {@link HttpInjectInterceptor}.
     */
    public static HttpInterceptorFactory createFactory(final HttpInjector httpInjector) {
        return new HttpInterceptorFactory() {

            @NonNull
            @Override
            public HttpInterceptor create() {
                return new HttpInjectInterceptor(httpInjector);
            }

        };
    }

}
