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

import com.github.megatronking.netbare.gateway.Interceptor;
import com.github.megatronking.netbare.gateway.Request;
import com.github.megatronking.netbare.gateway.RequestChain;
import com.github.megatronking.netbare.gateway.Response;
import com.github.megatronking.netbare.gateway.ResponseChain;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A specific interceptor designed for {@link HttpVirtualGateway}, it focuses on the http protocol
 * packets. The interceptor is an implement of {@link Interceptor}, methods are thread-safety and
 * runs in local proxy server threads.
 *
 * <p>
 * Use {@link HttpInterceptorFactory} to create an http interceptor instance.
 * </p>
 *
 * @author Megatron King
 * @since 2018-11-15 19:40
 */
public abstract class HttpInterceptor implements Interceptor {

    /**
     * Intercept http request packet, and delivery it to next interceptor or the terminal.
     * <p>
     * Remember do not block this method for a long time, because all the connections share the
     * same thread.
     * </p>
     *
     * @param chain The request chain, call {@linkplain HttpRequestChain#process(ByteBuffer)} to
     *                delivery the packet.
     * @param buffer A nio buffer contains the packet data.
     * @throws IOException If an I/O error has occurred.
     */
    protected abstract void intercept(@NonNull HttpRequestChain chain, @NonNull ByteBuffer buffer)
            throws IOException;

    /**
     * Intercept http response packet, and delivery it to next interceptor or the terminal.
     * <p>
     * Remember do not block this method for a long time, because all the connections share the
     * same thread.
     * </p>
     *
     * @param chain The response chain, call {@linkplain HttpResponseChain#process(ByteBuffer)} to
     *                delivery the packet.
     * @param buffer A nio buffer contains the packet data.
     * @throws IOException If an I/O error has occurred.
     */
    protected abstract void intercept(@NonNull HttpResponseChain chain, @NonNull ByteBuffer buffer)
            throws IOException;

    /**
     * Invoked when a session's request has finished. It means the client has no more data sent to
     * server in this session, and it might invoked multi times if a connection is keep-alive.
     *
     * @param request The request.
     */
    protected void onRequestFinished(@NonNull HttpRequest request) {
    }

    /**
     * Invoked when a session's response has finished. It means the server has no more data sent to
     * client in this session, and it might invoked multi times if a connection is keep-alive.
     *
     * @param response The response.
     */
    protected void onResponseFinished(@NonNull HttpResponse response) {
    }

    @Override
    public final void intercept(@NonNull RequestChain chain, @NonNull ByteBuffer buffer) {
        // Override the abstract method instead.
    }

    @Override
    public final void intercept(@NonNull ResponseChain chain, @NonNull ByteBuffer buffer) {
        // Override the abstract method instead.
    }

    @Override
    public final void onRequestFinished(@NonNull Request request) {
        // Override the abstract method instead.
    }

    @Override
    public final void onResponseFinished(@NonNull Response response) {
        // Override the abstract method instead.
    }

}
