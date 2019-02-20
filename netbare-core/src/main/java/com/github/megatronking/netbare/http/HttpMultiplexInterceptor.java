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
import com.github.megatronking.netbare.ip.Protocol;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * If a HTTP connection is keep-alive, there will be multiple sessions go through the same virtual
 * gateway. Those sessions are saw as one and not distinguished, this will increase the difficulty
 * of interception. We use this interceptor to separate them into independent sessions and
 * intercept them one by one.
 *
 * @author Megatron King
 * @since 2018-12-15 15:17
 */
/* package */ class HttpMultiplexInterceptor extends HttpIndexInterceptor {

    private final HttpZygoteRequest mZygoteRequest;
    private final HttpZygoteResponse mZygoteResponse;

    private int mResponseIndex;
    private NetBareXLog mLog;

    private boolean mWebSocket;

    /* package */ HttpMultiplexInterceptor(HttpZygoteRequest zygoteRequest,
                                           HttpZygoteResponse zygoteResponse) {
        this.mZygoteRequest = zygoteRequest;
        this.mZygoteResponse = zygoteResponse;
    }

    @Override
    protected void intercept(@NonNull HttpRequestChain chain, @NonNull ByteBuffer buffer,
                             int index) throws IOException {
        if (chain.request().httpProtocol() != HttpProtocol.HTTP_1_1) {
            chain.process(buffer);
            return;
        }
        // Check the protocol is web socket
        if (!mWebSocket) {
            mWebSocket = mZygoteResponse.isWebSocket();
        }
        if (mResponseIndex > 0 && !mWebSocket) {
            if (mLog == null) {
                mLog = new NetBareXLog(Protocol.TCP, chain.request().ip(), chain.request().port());
            }
            mResponseIndex = 0;
            mLog.w("Multiplex is found in one connection.");
            // Multiplex sessions.
            HttpId newId = new HttpId();
            mZygoteRequest.zygote(newId);
            mZygoteResponse.zygote(newId);
        }
        chain.process(buffer);
    }

    @Override
    protected void intercept(@NonNull HttpResponseChain chain, @NonNull ByteBuffer buffer,
                             int index) throws IOException {
        mResponseIndex++;
        chain.process(buffer);
    }

    @Override
    protected void onResponseFinished(@NonNull HttpResponse response) {
        mResponseIndex = 0;
        super.onResponseFinished(response);
    }

}
