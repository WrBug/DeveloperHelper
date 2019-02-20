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

import com.github.megatronking.netbare.gateway.Request;
import com.github.megatronking.netbare.gateway.Response;
import com.github.megatronking.netbare.gateway.SpecVirtualGateway;
import com.github.megatronking.netbare.gateway.VirtualGateway;
import com.github.megatronking.netbare.http2.Http2DecodeInterceptor;
import com.github.megatronking.netbare.http2.Http2EncodeInterceptor;
import com.github.megatronking.netbare.ip.Protocol;
import com.github.megatronking.netbare.net.Session;
import com.github.megatronking.netbare.ssl.JKS;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link VirtualGateway} that is responsible for HTTP(S) packets interception. It integrates
 * several internal {@link HttpInterceptor}s to decode and parse HTTP(S) packets. And also it
 * supports extensional {@link HttpInterceptor}s. Use {@link HttpVirtualGatewayFactory} to
 * create an instance.
 *
 * @author Megatron King
 * @since 2018-11-20 23:43
 */
/* package */ class HttpVirtualGateway extends SpecVirtualGateway {

    private HttpZygoteRequest mHttpZygoteRequest;
    private HttpZygoteResponse mHttpZygoteResponse;

    private List<HttpInterceptor> mInterceptors;

    /* package */ HttpVirtualGateway(Session session, Request request, Response response, JKS jks,
                                     final List<HttpInterceptorFactory> factories) {
        super(Protocol.TCP, session, request, response);

        HttpSessionFactory sessionFactory = new HttpSessionFactory();
        this.mHttpZygoteRequest = new HttpZygoteRequest(request, sessionFactory);
        this.mHttpZygoteResponse = new HttpZygoteResponse(response, sessionFactory);

        // Add default interceptors.
        SSLCodecInterceptor codecInterceptor = new SSLCodecInterceptor(jks, request, response);
        this.mInterceptors = new ArrayList<>(8);

        mInterceptors.add(new HttpSniffInterceptor(sessionFactory.create(session.id)));
        mInterceptors.add(codecInterceptor);
        mInterceptors.add(new Http2SniffInterceptor(codecInterceptor));
        mInterceptors.add(new Http2DecodeInterceptor(codecInterceptor, mHttpZygoteRequest, mHttpZygoteResponse));
        mInterceptors.add(new HttpMultiplexInterceptor(mHttpZygoteRequest, mHttpZygoteResponse));
        mInterceptors.add(new HttpHeaderSniffInterceptor(codecInterceptor));
        mInterceptors.add(new ContainerHttpInterceptor(new HttpInterceptorsFactory() {
            @NonNull
            @Override
            public List<HttpInterceptor> create() {
                List<HttpInterceptor> subs = new ArrayList<>(factories.size() + 2);
                subs.add(new HttpHeaderSeparateInterceptor());
                subs.add(new HttpHeaderParseInterceptor());
                // Add extension interceptors.
                for (HttpInterceptorFactory factory : factories) {
                    subs.add(factory.create());
                }
                return subs;
            }
        }));
        // Goalkeepers.
        mInterceptors.add(mInterceptors.size(), new Http2EncodeInterceptor());
        mInterceptors.add(mInterceptors.size(), new SSLRefluxInterceptor(codecInterceptor));

        //
        // SSL Flow Model:
        //
        //        Request                                  Response
        //
        //     out        in                             in        out
        //      ⇈         ⇊                               ⇊         ⇈
        //       Encrypted                                 Encrypted
        //      ⇈         ⇊                               ⇊         ⇈
        //   -----------------------------------------------------------
        //  |                     Codec Interceptor                     |
        //   -----------------------------------------------------------
        //      ⇈  |      ⇊              ...              ⇊      |  ⇈
        //         |      ⇊              ...              ⇊      |
        //      ⇈  |  Decrypted  |   interceptors  |  Decrypted  |  ⇈
        //         |      ⇊              ...              ⇊      |
        //      ⇈  |      ⇊              ...              ⇊      |  ⇈
        //   -----------------------------------------------------------
        //  |                     Reflux Interceptor                    |
        //   -----------------------------------------------------------
        //      ⇈ ⇇  ⇇  ⇇ ⇊                               ⇊ ⇉  ⇉  ⇉ ⇈
        //
    }

    @Override
    public void onSpecRequest(ByteBuffer buffer) throws IOException {
        new HttpRequestChain(mHttpZygoteRequest, mInterceptors).process(buffer);
    }

    @Override
    public void onSpecResponse(ByteBuffer buffer) throws IOException {
        new HttpResponseChain(mHttpZygoteResponse, mInterceptors).process(buffer);
    }

    @Override
    public void onSpecRequestFinished() {
        for (HttpInterceptor interceptor: mInterceptors) {
            interceptor.onRequestFinished(mHttpZygoteRequest);
        }
    }

    @Override
    public void onSpecResponseFinished() {
        for (HttpInterceptor interceptor: mInterceptors) {
            interceptor.onResponseFinished(mHttpZygoteResponse);
        }
    }

}
