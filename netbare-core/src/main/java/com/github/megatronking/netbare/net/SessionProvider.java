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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.megatronking.netbare.ip.Protocol;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A session provider that provides the session instance query services.
 *
 * @author Megatron King
 * @since 2018-10-15 21:46
 */
public final class SessionProvider {

    private static final int MAX_SESSION = 100;

    private final Map<Short, Session> mSessions;
    private final UidDumper mDumper;

    /**
     * Constructs a session provider with a {@link UidDumper}.
     *
     * @param dumper Use to dump uid, can be null.
     */
    public SessionProvider(UidDumper dumper) {
        this.mSessions = new ConcurrentHashMap<>(MAX_SESSION);
        this.mDumper = dumper;
    }

    /**
     * Query a session by local VPN port.
     *
     * @param localPort The local VPN port.
     * @return The instance of {@link Session} if it exists, or null.
     */
    @Nullable
    public Session query(short localPort) {
        Session session = mSessions.get(localPort);
        if (mDumper != null && session != null && session.uid == 0) {
            // Query uid again.
            mDumper.request(session);
        }
        return session;
    }

    /**
     * Query or create a session by protocol, ports and remote server IP.
     *
     * @param protocol IP protocol.
     * @param localPort Local VPN port.
     * @param remotePort Remote server port.
     * @param remoteIp Remote server IP.
     * @return An instance of {@link Session}, if the instance not exists, will create a new one.
     */
    @NonNull
    public Session ensureQuery(Protocol protocol, short localPort, short remotePort, int remoteIp) {
        Session session = mSessions.get(localPort);
        if (session != null) {
            if (session.protocol != protocol || session.localPort != localPort ||
                    session.remotePort != remotePort || session.remoteIp != remoteIp) {
                session = null;
            }
        }
        if (session == null) {
            session = new Session(protocol, localPort, remotePort, remoteIp);
            mSessions.put(localPort, session);
            // Dump uid from /proc/net/
            if (mDumper != null) {
                mDumper.request(session);
            }
        }
        return session;
    }

}
