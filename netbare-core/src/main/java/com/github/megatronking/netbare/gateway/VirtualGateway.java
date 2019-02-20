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

/**
 * Virtual Gateway is a virtual net packets interception distributor, all packets will flow through
 * it. We can define our own virtual gateway to decode and encode the packets. The Virtual
 * Gateway wraps a request tunnel {@link Request} and a response tunnel {@link Response}, these
 * tunnels are responsible for communicating with the terminal(client and server).
 *
 * @author Megatron King
 * @since 2018-11-01 23:48
 */
public class VirtualGateway {

    /**
     * The request tunnel connects to the server terminal. We can call
     * {@link Request#process(ByteBuffer)} to send data.
     */
    protected Request mRequest;

    /**
     * The response tunnel connects to the client terminal. We can call
     * {@link Response#process(ByteBuffer)} to send data.
     */
    protected Response mResponse;

    /**
     * Constructs a VirtualGateway object with the net session, request tunnel and response tunnel.
     *
     * @param session The net session contains basic net information such as IPs and ports.
     * @param request The request tunnel connects to the server terminal.
     * @param response The response tunnel connects to the client terminal.
     */
    public VirtualGateway(Session session, Request request, Response response) {
        request.setSession(session);
        response.setSession(session);
        this.mRequest = request;
        this.mResponse = response;
    }

    /**
     * Send a packet to server terminal through the request tunnel.
     *
     * @param buffer A byte buffer contains the net packet data.
     * @throws IOException If an I/O error has occurred.
     */
    public void sendRequest(ByteBuffer buffer) throws IOException {
        mRequest.process(buffer);
    }

    /**
     * Send a packet to client terminal through the response tunnel.
     *
     * @param buffer A byte buffer contains the net packet data.
     * @throws IOException If an I/O error has occurred.
     */
    public void sendResponse(ByteBuffer buffer) throws IOException {
        mResponse.process(buffer);
    }

    /**
     * Notify virtual gateway that no longer has data sent to the server.
     */
    public void sendRequestFinished() {
    }

    /**
     * Notify virtual gateway that no longer has data sent to the client.
     */
    public void sendResponseFinished() {
    }

}
