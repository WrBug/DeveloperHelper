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
 * Add the index parameter in the {@link #intercept(HttpRequestChain, ByteBuffer)} and
 * {@link #intercept(HttpResponseChain, ByteBuffer)}, it indicates the packet index in the session.
 * <p>
 * The index will be reset when the session finished.
 * </p>
 *
 * @author Megatron King
 * @since 2018-12-03 21:00
 */
public abstract class HttpIndexInterceptor extends HttpInterceptor {

    private int mRequestIndex;
    private int mResponseIndex;

    /**
     * The same like {@link #intercept(HttpRequestChain, ByteBuffer)}.
     *
     * @param chain The request chain, call {@linkplain HttpRequestChain#process(ByteBuffer)} to
     *                delivery the packet.
     * @param buffer A nio buffer contains the packet data.
     * @param index The packet index, started from 0.
     * @throws IOException If an I/O error has occurred.
     */
    protected abstract void intercept(@NonNull HttpRequestChain chain, @NonNull ByteBuffer buffer,
                                      int index) throws IOException;

    /**
     * The same like {@link #intercept(HttpResponseChain, ByteBuffer)}.
     *
     * @param chain The response chain, call {@linkplain HttpResponseChain#process(ByteBuffer)} to
     *                delivery the packet.
     * @param buffer A nio buffer contains the packet data.
     * @param index The packet index, started from 0.
     * @throws IOException If an I/O error has occurred.
     */
    protected abstract void intercept(@NonNull HttpResponseChain chain, @NonNull ByteBuffer buffer,
                                      int index) throws IOException;

    @Override
    protected final void intercept(@NonNull HttpRequestChain chain, @NonNull ByteBuffer buffer)
            throws IOException {
        intercept(chain, buffer, mRequestIndex++);
    }

    @Override
    protected final void intercept(@NonNull HttpResponseChain chain, @NonNull ByteBuffer buffer)
            throws IOException {
        intercept(chain, buffer, mResponseIndex++);
    }

    @Override
    protected void onRequestFinished(@NonNull HttpRequest request) {
        super.onRequestFinished(request);
        mRequestIndex = 0;
    }

    @Override
    protected void onResponseFinished(@NonNull HttpResponse response) {
        super.onResponseFinished(response);
        mResponseIndex = 0;
    }

}
