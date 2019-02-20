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

/**
 * A factory that produces the {@link VirtualGateway}.
 *
 * @author Megatron King
 * @since 2018-11-01 23:23
 */
public interface VirtualGatewayFactory {

    /**
     * Returns a new {@link VirtualGateway} for the given arguments.
     *
     * @param session A network session.
     * @param request A request connects to the remote server tunnel.
     * @param response A response connects to VPN file descriptor
     */
    VirtualGateway create(Session session, Request request, Response response);

}
