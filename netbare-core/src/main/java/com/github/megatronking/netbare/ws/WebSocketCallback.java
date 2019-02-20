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
package com.github.megatronking.netbare.ws;

import java.io.IOException;

/**
 * A callback to be invoked when the message is decoded.
 *
 * @author Megatron King
 * @since 2019/1/18 23:56
 */
public interface WebSocketCallback {

    /**
     * Invoked when a text message is decoded.
     *
     * @param text A text content.
     * @throws IOException If an I/O error has occurred.
     */
    void onReadMessage(String text) throws IOException;

    /**
     * Invoked when a binary message is decoded.
     *
     * @param binary A binary content.
     * @throws IOException If an I/O error has occurred.
     */
    void onReadMessage(byte[] binary) throws IOException;

    /**
     * Invoked when a ping message is decoded.
     *
     * @param ping The ping message content.
     */
    void onReadPing(byte[] ping);

    /**
     * Invoked when a pong message is decoded.
     *
     * @param pong The pong message content.
     */
    void onReadPong(byte[] pong);

    /**
     * Invoked when the control frame is closed.
     *
     * @param code Status code.
     * @param reason Close reason.
     */
    void onReadClose(int code, String reason);

}
