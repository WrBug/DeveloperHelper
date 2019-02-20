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
package com.github.megatronking.netbare.gateway;

import com.github.megatronking.netbare.net.Session;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link VirtualGateway} provides the interception service. Interceptors are organized as a list
 * in chain, can observe and modify packets. Use {@link DefaultVirtualGatewayFactory} to create an
 * instance.
 *
 * @author Megatron King
 * @since 2018-11-01 23:35
 */
/* package */ class DefaultVirtualGateway extends VirtualGateway {

    private final List<Interceptor> mInterceptors;

    /* package */ DefaultVirtualGateway(Session session, Request request, Response response,
                                        List<InterceptorFactory> factories) {
        super(session, request, response);
        this.mInterceptors = new ArrayList<>(factories.size());
        for (InterceptorFactory factory : factories) {
            mInterceptors.add(factory.create());
        }
    }

    @Override
    public void sendRequest(ByteBuffer buffer) throws IOException {
        new RequestChain(mRequest, mInterceptors).process(buffer);
    }

    @Override
    public void sendResponse(ByteBuffer buffer) throws IOException {
        new ResponseChain(mResponse, mInterceptors).process(buffer);
    }

    @Override
    public void sendRequestFinished() {
        for (Interceptor interceptor: mInterceptors) {
            interceptor.onRequestFinished(mRequest);
        }
    }

    @Override
    public void sendResponseFinished() {
        for (Interceptor interceptor: mInterceptors) {
            interceptor.onResponseFinished(mResponse);
        }
    }

}
