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

import com.github.megatronking.netbare.gateway.VirtualGateway;
import com.github.megatronking.netbare.gateway.VirtualGatewayFactory;
import com.github.megatronking.netbare.gateway.Request;
import com.github.megatronking.netbare.gateway.Response;
import com.github.megatronking.netbare.net.Session;
import com.github.megatronking.netbare.ssl.JKS;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link VirtualGatewayFactory} that produces the {@link HttpVirtualGateway}.
 *
 * @author Megatron King
 * @since 2018-11-20 23:50
 */
public class HttpVirtualGatewayFactory implements VirtualGatewayFactory {

    private List<HttpInterceptorFactory> mFactories;
    private JKS mJKS;

    /**
     * Constructs a {@link HttpVirtualGatewayFactory} instance with {@link JKS} and a collection of
     * {@link HttpInterceptorFactory}.
     *
     * @param factories a collection of {@link HttpInterceptorFactory}.
     * @return A instance of {@link HttpVirtualGatewayFactory}.
     */
    public HttpVirtualGatewayFactory(@NonNull JKS jks,
                                     @NonNull List<HttpInterceptorFactory> factories) {
        this.mJKS = jks;
        this.mFactories = factories;
    }

    @Override
    public VirtualGateway create(Session session, Request request, Response response) {
        return new HttpVirtualGateway(session, request, response, mJKS, new ArrayList<>(mFactories));
    }

    /**
     * Create a {@link HttpVirtualGatewayFactory} instance with {@link JKS} and a collection of
     * {@link HttpInterceptorFactory}.
     *
     * @param factories a collection of {@link HttpInterceptorFactory}.
     * @return A instance of {@link HttpVirtualGatewayFactory}.
     */
    public static VirtualGatewayFactory create(@NonNull JKS authority,
                                               @NonNull List<HttpInterceptorFactory> factories) {
        return new HttpVirtualGatewayFactory(authority, factories);
    }

}
