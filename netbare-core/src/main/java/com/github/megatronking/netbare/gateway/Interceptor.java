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

/**
 * A virtual gateway interceptor, observes and modifies requests/responses. Interceptors are
 * organized by a virtual gateway, and process net packets one by one.
 * <p>
 * Methods are thread-safety due to interceptors are running in the local proxy server threads.
 * </p>
 * <p>
 * Use {@link InterceptorFactory} to create an interceptor instance.
 * </p>
 * @author Megatron King
 * @since 2018-11-13 23:46
 */
public interface Interceptor {

    /**
     * Intercept request packet, and delivery it to next interceptor or the terminal.
     * <p>
     * Remember do not block this method for a long time, because all the connections share the
     * same thread.
     * </p>
     *
     * @param chain The request chain, call {@linkplain RequestChain#process(ByteBuffer)} to
     *                delivery the packet.
     * @param buffer A nio buffer contains the packet data.
     * @throws IOException If an I/O error has occurred.
     */
    void intercept(@NonNull RequestChain chain, @NonNull ByteBuffer buffer) throws IOException;

    /**
     * Intercept request packet, and delivery it to next interceptor or the terminal.
     * <p>
     * Remember do not block this method for a long time, because all the connections share the
     * same thread.
     *
     * @param chain The response chain, call {@linkplain ResponseChain#process(ByteBuffer)} to
     *                delivery the packet.
     * @param buffer A nio buffer contains the packet data.
     * @throws IOException If an I/O error has occurred.
     */
    void intercept(@NonNull ResponseChain chain, @NonNull ByteBuffer buffer) throws IOException;

    /**
     * Invoked when a session's request has finished. It means the client has no more data sent to
     * server in this session, and it might invoked multi times if a connection is keep-alive.
     *
     * @param request The request.
     */
    void onRequestFinished(@NonNull Request request);

    /**
     * Invoked when a session's response has finished. It means the server has no more data sent to
     * client in this session, and it might invoked multi times if a connection is keep-alive.
     *
     * @param response The response.
     */
    void onResponseFinished(@NonNull Response response);

}
