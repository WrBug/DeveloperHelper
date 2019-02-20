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

import com.github.megatronking.netbare.NetBareConfig;
import com.github.megatronking.netbare.NetBareUtils;
import com.github.megatronking.netbare.ip.Protocol;
import com.github.megatronking.netbare.net.Session;

/**
 * A tunnel flow contains the session information.
 *
 * @author Megatron King
 * @since 2018-11-05 21:43
 */
public abstract class SessionTunnelFlow implements TunnelFlow {

    private Session mSession;

    /* package */ void setSession(Session session) {
        mSession = session;
    }

    /**
     * Returns the session's unique id.
     *
     * @return The session id.
     */
    public String id() {
        return mSession.id;
    }

    /**
     * Returns the session created time, you can think of it as the start time of the request.
     *
     * @return Session created time.
     */
    public long time() {
        return mSession.time;
    }

    /**
     * Returns the identifier of this session's process uid. This value is not guaranteed, it is up
     * to {@link NetBareConfig#dumpUid}. And if dumps the uid failed, it will return 0.
     *
     * @return The session's process uid.
     */
    public int uid() {
        return mSession.uid;
    }

    /**
     * Returns the remote server's IPV4 address.
     *
     * @return The remote server's IPV4 address.
     */
    public String ip() {
        return NetBareUtils.convertIp(mSession.remoteIp);
    }

    /**
     * Returns the remote server's host name.
     *
     * @return The remote server's host name.
     */
    public String host() {
        return mSession.host;
    }

    /**
     * Returns the remote server's port.
     *
     * @return The remote server's port.
     */
    public int port() {
        return NetBareUtils.convertPort(mSession.remotePort);
    }

    /**
     * Returns the IP protocol.
     *
     * @return IP protocol.
     */
    public Protocol protocol() {
        return mSession.protocol;
    }

}
