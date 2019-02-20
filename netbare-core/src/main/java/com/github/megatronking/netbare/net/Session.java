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

import com.github.megatronking.netbare.ip.Protocol;

import java.util.UUID;

/**
 * This object represents a network session, it contains IPs, ports and IP packet details.
 *
 * @author Megatron King
 * @since 2018-10-14 23:39
 */
public final class Session {

    /**
     * IP protocol.
     */
    public final Protocol protocol;

    /**
     * Local vpn port.
     */
    public final short localPort;

    /**
     * Remote server port.
     */
    public final short remotePort;

    /**
     * Remote server IP.
     */
    public final int remoteIp;

    /**
     * An unique id uses to identify this session.
     */
    public String id;

    /**
     * Session started time.
     */
    public long time;

    /**
     * Remote server host.
     */
    public String host;

    /**
     * The process id that the session belongs to.
     */
    public int uid;

    /**
     * Packet counts.
     */
    public int packetIndex;

    /**
     * The total size of the packets that sends to remote server.
     */
    public int sendDataSize;

    /**
     * The total size of the packets that received from remote server.
     */
    public int receiveDataSize;

    /* package */ Session(Protocol protocol, short localPort, short remotePort, int remoteIp) {
        this.protocol = protocol;
        this.localPort = localPort;
        this.remotePort = remotePort;
        this.remoteIp = remoteIp;
        this.id = UUID.randomUUID().toString();
        this.time = System.currentTimeMillis();
    }

}
