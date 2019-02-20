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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * A chain with multiple {@link Interceptor} in series. The interceptors process net packets one by
 * one, and send the modified packets to tunnel in the end.
 *
 * @param <T> An implementation of {@link TunnelFlow}, responsible for sending data to tunnel.
 * @param <I> An implementation of {@link Interceptor}.
 *
 * @author Megatron King
 * @since 2018-11-13 23:00
 */
public abstract class InterceptorChain<T extends TunnelFlow, I extends Interceptor> {

    private T mFlow;
    private List<I> mInterceptors;
    private int mIndex;

    /**
     * Hand the net packets to the next {@link Interceptor}.
     *
     * @param buffer A buffer contains net packet data.
     * @param flow A {@link TunnelFlow} implementation.
     * @param interceptors A collection of all interceptors in chain.
     * @param index The next interceptor index.
     * @throws IOException If an I/O error has occurred.
     */
    protected abstract void processNext(ByteBuffer buffer, T flow, List<I> interceptors, int index)
            throws IOException;

    /**
     * Constructs an intercept chain with a tunnel flow instance and a collection of interceptors.
     *
     * @param flow A {@link TunnelFlow} implementation.
     * @param interceptors A collection of interceptors.
     */
    public InterceptorChain(T flow, List<I> interceptors) {
        this(flow, interceptors, 0);
    }

    /**
     * Constructs a new intercept chain with the tunnel flow instance and a collection of
     * interceptors. The chain will start from the given index.
     *
     * @param flow A {@link TunnelFlow} implementation.
     * @param interceptors A collection of interceptors.
     * @param index The head index.
     */
    public InterceptorChain(T flow, List<I> interceptors, int index) {
        this.mFlow = flow;
        this.mInterceptors = interceptors;
        this.mIndex = index;
    }

    /**
     * Finish the interception and send the packet to tunnel.
     *
     * @param buffer A buffer contains net packet data.
     * @throws IOException If an I/O error has occurred.
     */
    public void processFinal(ByteBuffer buffer) throws IOException {
        mFlow.process(buffer);
    }

    /**
     * Hand the net packets to the next. If all interceptors have been processed, the packets will
     * be sent to tunnel, otherwise hand it to the next interceptor.
     *
     * @param buffer A buffer contains net packet data.
     * @throws IOException If an I/O error has occurred.
     */
    public void process(ByteBuffer buffer) throws IOException {
        if (mIndex >= mInterceptors.size()) {
            processFinal(buffer);
        } else {
            processNext(buffer, mFlow, mInterceptors, mIndex);
        }
    }

}
