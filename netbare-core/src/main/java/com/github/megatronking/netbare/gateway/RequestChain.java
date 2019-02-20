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

import androidx.annotation.NonNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * A request chain, responsible for intercepting request packets.
 *
 * @author Megatron King
 * @since 2018-11-14 23:18
 */
public class RequestChain extends InterceptorChain<Request, Interceptor> {

    private Request mRequest;

    /* package */ RequestChain(Request request, List<Interceptor> interceptors) {
        super(request, interceptors);
        mRequest = request;
    }

    private RequestChain(Request request, List<Interceptor> interceptors, int index) {
        super(request, interceptors, index);
        mRequest = request;
    }

    @Override
    protected void processNext(ByteBuffer buffer, Request request, List<Interceptor> interceptors,
                               int index) throws IOException {
        Interceptor interceptor = interceptors.get(index);
        if (interceptor != null) {
            interceptor.intercept(new RequestChain(request, interceptors, ++index), buffer);
        }
    }

    @NonNull
    public Request request() {
        return mRequest;
    }

}
