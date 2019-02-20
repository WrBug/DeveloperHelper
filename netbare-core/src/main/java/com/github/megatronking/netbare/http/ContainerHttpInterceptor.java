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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * One http virtual gateway may have multi http sessions, but we don't want to share interceptors
 * between them. Use a container to manage different sessions, every session has independent
 * interceptor instances.
 *
 * @author Megatron King
 * @since 2019/1/6 16:13
 */
/* package */ class ContainerHttpInterceptor extends HttpInterceptor {

    private final Map<String, Session> mSessions;
    private final HttpInterceptorsFactory mSubInterceptorsFactory;

    /* package */ ContainerHttpInterceptor(HttpInterceptorsFactory factory) {
        this.mSubInterceptorsFactory = factory;
        this.mSessions = new ConcurrentHashMap<>();
    }

    @Override
    protected void intercept(@NonNull final HttpRequestChain chain, @NonNull ByteBuffer buffer)
            throws IOException {
        HttpRequest request = chain.request();
        Session session = findSessionById(request.id());
        session.request = request;
        if (session.interceptors == null) {
            session.interceptors = mSubInterceptorsFactory.create();
        }
        new HttpContainerRequestChain(chain, session.interceptors).process(buffer);
    }

    @Override
    protected void intercept(@NonNull final HttpResponseChain chain, @NonNull ByteBuffer buffer)
            throws IOException {
        HttpResponse response = chain.response();
        Session session = findSessionById(response.id());
        session.response = response;
        if (session.interceptors == null) {
            session.interceptors = mSubInterceptorsFactory.create();
        }
        new HttpContainerResponseChain(chain, session.interceptors).process(buffer);
    }

    @Override
    protected void onRequestFinished(@NonNull HttpRequest request) {
        if (request instanceof HttpZygoteRequest) {
            // This means the connection is down, finish all.
            for (Session session : mSessions.values()) {
                if (session.request != null && session.interceptors != null) {
                    for (HttpInterceptor interceptor : session.interceptors) {
                        interceptor.onRequestFinished(session.request);
                    }
                }
            }
            mSessions.clear();
        } else {
            Session session = mSessions.remove(request.id());
            if (session != null && session.interceptors != null) {
                for (HttpInterceptor interceptor : session.interceptors) {
                    interceptor.onRequestFinished(session.request);
                }
            }
        }
    }

    @Override
    protected void onResponseFinished(@NonNull HttpResponse response) {
        if (response instanceof HttpZygoteResponse) {
            // This means the connection is down, finish all.
            for (Session session : mSessions.values()) {
                if (session != null && session.response != null && session.interceptors != null) {
                    for (HttpInterceptor interceptor : session.interceptors) {
                        interceptor.onResponseFinished(session.response);
                    }
                }
            }
        } else {
            Session session = mSessions.remove(response.id());
            if (session != null && session.interceptors != null) {
                for (HttpInterceptor interceptor : session.interceptors) {
                    interceptor.onResponseFinished(session.response);
                }
            }
        }
    }

    private Session findSessionById(String id) {
        Session session;
        if (mSessions.containsKey(id)) {
            session = mSessions.get(id);
        } else {
            session = new Session();
            mSessions.put(id, session);
        }
        return session;
    }

    private static final class Session {

        private HttpRequest request;
        private HttpResponse response;
        private List<HttpInterceptor> interceptors;

    }

    private static final class HttpContainerRequestChain extends HttpRequestChain {

        private final HttpRequestChain mChain;
        private final List<HttpInterceptor> mInterceptors;
        private final int mIndex;

        private HttpContainerRequestChain(HttpRequestChain chain, List<HttpInterceptor> interceptors) {
            this(chain, interceptors, 0);
        }

        private HttpContainerRequestChain(HttpRequestChain chain, List<HttpInterceptor> interceptors,
                                          int index) {
            super(chain.zygoteRequest(), interceptors, index);
            this.mChain = chain;
            this.mInterceptors = interceptors;
            this.mIndex = index;
        }

        @Override
        public void process(ByteBuffer buffer) throws IOException {
            if (mIndex >= mInterceptors.size()) {
                mChain.process(buffer);
            } else {
                HttpInterceptor interceptor = mInterceptors.get(mIndex);
                if (interceptor != null) {
                    interceptor.intercept(new HttpContainerRequestChain(mChain, mInterceptors,
                            mIndex + 1), buffer);
                }
            }
        }

    }

    private static final class HttpContainerResponseChain extends HttpResponseChain {

        private final HttpResponseChain mChain;
        private final List<HttpInterceptor> mInterceptors;
        private final int mIndex;

        private HttpContainerResponseChain(HttpResponseChain chain, List<HttpInterceptor> interceptors) {
            this(chain, interceptors, 0);
        }

        private HttpContainerResponseChain(HttpResponseChain chain, List<HttpInterceptor> interceptors,
                                          int index) {
            super(chain.zygoteResponse(), interceptors, index);
            this.mChain = chain;
            this.mInterceptors = interceptors;
            this.mIndex = index;
        }

        @Override
        public void process(ByteBuffer buffer) throws IOException {
            if (mIndex >= mInterceptors.size()) {
                mChain.process(buffer);
            } else {
                HttpInterceptor interceptor = mInterceptors.get(mIndex);
                if (interceptor != null) {
                    interceptor.intercept(new HttpContainerResponseChain(mChain, mInterceptors,
                            mIndex + 1), buffer);
                }
            }
        }

    }

}
