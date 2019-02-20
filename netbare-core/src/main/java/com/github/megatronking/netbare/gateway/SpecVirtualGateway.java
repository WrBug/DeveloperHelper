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

import com.github.megatronking.netbare.ip.Protocol;
import com.github.megatronking.netbare.net.Session;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * The spec VirtualGateway filter the net packets by {@link Protocol}.
 *
 * @author Megatron King
 * @since 2018-11-03 10:34
 */
public abstract class SpecVirtualGateway extends VirtualGateway {

    private final boolean mIsSpec;

    public SpecVirtualGateway(Protocol protocol, Session session, Request request,
                              Response response) {
        super(session, request, response);
        this.mIsSpec = protocol == session.protocol;
    }

    /**
     * The specific protocol packets sent to server will flow through this method.
     *
     * @param buffer A byte buffer contains the net packet data.
     * @throws IOException If an I/O error has occurred.
     */
    protected abstract void onSpecRequest(ByteBuffer buffer) throws IOException;

    /**
     * The specific protocol packets sent to client will flow through this method.
     *
     * @param buffer A byte buffer contains the net packet data.
     * @throws IOException If an I/O error has occurred.
     */
    protected abstract void onSpecResponse(ByteBuffer buffer) throws IOException;

    /**
     * Notify virtual gateway that no longer has data sent to the server.
     */
    protected abstract void onSpecRequestFinished();

    /**
     * Notify virtual gateway that no longer has data sent to the client.
     */
    protected abstract void onSpecResponseFinished();

    @Override
    public final void sendRequest(ByteBuffer buffer) throws IOException {
        if (mIsSpec) {
            onSpecRequest(buffer);
        } else {
            super.sendRequest(buffer);
        }
    }

    @Override
    public final void sendResponse(ByteBuffer buffer) throws IOException {
        if (mIsSpec) {
            onSpecResponse(buffer);
        } else {
            super.sendResponse(buffer);
        }
    }

    @Override
    public final void sendRequestFinished() {
        if (mIsSpec) {
            onSpecRequestFinished();
        } else {
            super.sendRequestFinished();
        }
    }

    @Override
    public final void sendResponseFinished() {
        if (mIsSpec) {
            onSpecResponseFinished();
        } else {
            super.sendResponseFinished();
        }
    }

}
