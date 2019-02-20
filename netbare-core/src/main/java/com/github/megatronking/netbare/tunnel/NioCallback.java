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
package com.github.megatronking.netbare.tunnel;

import java.io.IOException;

/**
 * A nio selector attachment callback for sending notification to {@link NioTunnel}.
 *
 * @author Megatron King
 * @since 2018-10-15 23:15
 */
public interface NioCallback {

    /**
     * Invoked when the connection is connected with the terminal.
     *
     * @throws IOException If an I/O error has occurred.
     */
    void onConnected() throws IOException;

    /**
     * Invoked when the socket IO is readable.
     *
     * @throws IOException If an I/O error has occurred.
     */
    void onRead() throws IOException;

    /**
     * Invoked when the socket IO is writable.
     *
     * @throws IOException If an I/O error has occurred.
     */
    void onWrite() throws IOException;

    /**
     * Invoked when the socket IO is closed.
     */
    void onClosed();

    /**
     * Returns the tunnel using nio attachment.
     *
     * @return A tunnel.
     */
    NioTunnel getTunnel();

}
