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
package com.github.megatronking.netbare.proxy;

import java.io.OutputStream;

/**
 * An interface needs to be implement by proxy server forwarders.
 *
 * @author Megatron King
 * @since 2018-10-09 01:24
 */
public interface ProxyServerForwarder {

    /**
     * Prepare the forwarder.
     */
    void prepare();

    /**
     * Forward a packet to local proxy server.
     *
     * @param packet A data packet, the array length is MTU.
     * @param len The actual data length in packet array.
     * @param output An output stream connects VPN file descriptor.
     */
    void forward(byte[] packet, int len, OutputStream output);

    /**
     * Release the forwarder.
     */
    void release();

}
