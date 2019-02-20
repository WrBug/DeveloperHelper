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

import com.github.megatronking.netbare.net.Session;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link VirtualGatewayFactory} that produces the {@link DefaultVirtualGateway}.
 *
 * @author Megatron King
 * @since 2018-11-01 23:29
 */
public final class DefaultVirtualGatewayFactory implements VirtualGatewayFactory {

    private List<InterceptorFactory> mFactories;

    private DefaultVirtualGatewayFactory(@NonNull List<InterceptorFactory> factories) {
        this.mFactories = factories;
    }

    @Override
    public VirtualGateway create(Session session, Request request, Response response) {
        return new DefaultVirtualGateway(session, request, response, new ArrayList<>(mFactories));
    }

    /**
     * Create a {@link VirtualGatewayFactory} instance with a collection of
     * {@link InterceptorFactory}.
     *
     * @param factories a collection of {@link InterceptorFactory}.
     * @return A instance of {@link DefaultVirtualGatewayFactory}.
     */
    public static VirtualGatewayFactory create(@NonNull List<InterceptorFactory> factories) {
        return new DefaultVirtualGatewayFactory(factories);
    }

    /**
     * Create a {@link VirtualGatewayFactory} instance that not contains {@link Interceptor}.
     *
     * @return A instance of {@link VirtualGatewayFactory}.
     */
    public static VirtualGatewayFactory create() {
        return create(new ArrayList<InterceptorFactory>());
    }

}
