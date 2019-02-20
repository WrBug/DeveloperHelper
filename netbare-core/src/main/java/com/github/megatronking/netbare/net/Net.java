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
package com.github.megatronking.netbare.net;

/**
 * A dumped net info class contains IPs, ports and uid.
 *
 * @author Megatron King
 * @since 2018-12-01 22:33
 */
public class Net {

    /**
     * The identifier of a process's uid.
     */
    public int uid;

    /**
     * The local IP.
     */
    public String localIp;

    /**
     * The local port.
     */
    public int localPort;

    /**
     * The remote server IP.
     */
    public String remoteIp;

    /**
     * The remote server port.
     */
    public int remotePort;

    /* package */ Net(int uid, String localIp, int localPort, String remoteIp, int remotePort) {
        this.uid = uid;
        this.localIp = localIp;
        this.localPort = localPort;
        this.remoteIp = remoteIp;
        this.remotePort = remotePort;
    }

    @Override
    public String toString() {
        return uid + " " + localIp + ":" + localPort + " -> " + remoteIp + ":" + remotePort;
    }

}
