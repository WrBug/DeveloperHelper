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

/**
 * A local proxy server receives net packets from VPN and transfer them to the real remote server.
 * Every local proxy server runs separated threads and handle specific IP protocols like TCP, UDP
 * and so on. The server is managed by {@link ProxyServerForwarder}, use {@link #start()} to
 * establish the server and {@link #stop()} to terminate it.
 *
 * @author Megatron King
 * @since 2018-10-10 00:23
 */
public abstract class ProxyServer {

    /**
     * Establish the server and start receive packets.
     */
    /* package */ abstract void startServer();

    /**
     * Terminate this server.
     */
    /* package */ abstract void stopServer();

    /**
     * Returns the proxy server IP.
     *
     * @return The proxy server IP.
     */
    /* package */ abstract int getIp();

    /**
     * Returns the proxy server port.
     *
     * @return The proxy server port.
     */
    /* package */ abstract short getPort();

    /**
     * Establish the proxy server.
     */
    public final void start() {
        startServer();
    }

    /**
     * Terminate the proxy server, release resources and close IOs.
     */
    public final void stop() {
        stopServer();
    }

}
