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
package com.github.megatronking.netbare;

import com.github.megatronking.netbare.net.Session;
import com.github.megatronking.netbare.ip.Protocol;

/**
 * A log util using in NetBare, it uses the protocol, ip and port as the prefix.
 *
 * @author Megatron King
 * @since 2018-10-14 10:25
 */
public final class NetBareXLog {

    private final String mPrefix;

    /**
     * Constructs a NetBareXLog instance with the net information.
     *
     * @param protocol The IP protocol.
     * @param ip The ip address, a string value.
     * @param port The port, a short value.
     */
    public NetBareXLog(Protocol protocol, String ip, short port) {
        this(protocol, ip, NetBareUtils.convertPort(port));
    }

    /**
     * Constructs a NetBareXLog instance with the net information.
     *
     * @param protocol The IP protocol.
     * @param ip The ip address, a int value.
     * @param port The port, a short value.
     */
    public NetBareXLog(Protocol protocol, int ip, short port) {
        this(protocol, NetBareUtils.convertIp(ip), port);
    }

    /**
     * Constructs a NetBareXLog instance with the net information.
     *
     * @param protocol The IP protocol.
     * @param ip The ip address, a int value.
     * @param port The port, a int value.
     */
    public NetBareXLog(Protocol protocol, int ip, int port) {
        this(protocol, NetBareUtils.convertIp(ip), port);
    }

    /**
     * Constructs a NetBareXLog instance with the net information.
     *
     * @param session The session contains net information.
     */
    public NetBareXLog(Session session) {
        this(session.protocol, session.remoteIp, session.remotePort);
    }

    /**
     * Constructs a NetBareXLog instance with the net information.
     *
     * @param protocol The IP protocol.
     * @param ip The ip address, a string value.
     * @param port The port, a int value.
     */
    public NetBareXLog(Protocol protocol, String ip, int port) {
        this.mPrefix = "[" + protocol.name() + "][" + ip + ":" + port + "]";
    }

    /**
     * Print a verbose level log in console, format is '[protocol][ip:port]message'.
     *
     * @param msg The message you would like logged.
     */
    public void v(String msg) {
        NetBareLog.v(mPrefix + msg);
    }


    public void v(String msg, Object... args) {
        NetBareLog.v(mPrefix + msg, args);
    }

    /**
     * Print a debug level log in console, format is '[protocol][ip:port]message'.
     *
     * @param msg The message you would like logged.
     */
    public void d(String msg) {
        NetBareLog.d(mPrefix + msg);
    }

    /**
     * Print a verbose level log in console, format is '[protocol][ip:port]message'.
     *
     * @param msg The message you would like logged.
     * @param args Arguments referenced by the format specifiers in the format string.
     */
    public void d(String msg, Object... args) {
        NetBareLog.d(mPrefix + msg, args);
    }

    /**
     * Print a info level log in console, format is '[protocol][ip:port]message'.
     *
     * @param msg The message you would like logged.
     */
    public void i(String msg) {
        NetBareLog.i(mPrefix + msg);
    }

    /**
     * Print a info level log in console, format is '[protocol][ip:port]message'.
     *
     * @param msg The message you would like logged.
     * @param args Arguments referenced by the format specifiers in the format string.
     */
    public void i(String msg, Object... args) {
        NetBareLog.i(mPrefix + msg, args);
    }

    /**
     * Print a error level log in console, format is '[protocol][ip:port]message'.
     *
     * @param msg The message you would like logged.
     */
    public void e(String msg) {
        NetBareLog.e(mPrefix + msg);
    }

    /**
     * Print a error level log in console, format is '[protocol][ip:port]message'.
     *
     * @param msg The message you would like logged.
     * @param args Arguments referenced by the format specifiers in the format string.
     */
    public void e(String msg, Object... args) {
        NetBareLog.e(mPrefix + msg, args);
    }

    /**
     * Print a warning level log in console, format is '[protocol][ip:port]message'.
     *
     * @param msg The message you would like logged.
     */
    public void w(String msg) {
        NetBareLog.w(mPrefix + msg);
    }

    /**
     * Print a warning level log in console, format is '[protocol][ip:port]message'.
     *
     * @param msg The message you would like logged.
     * @param args Arguments referenced by the format specifiers in the format string.
     */
    public void w(String msg, Object... args) {
        NetBareLog.w(mPrefix + msg, args);
    }

}
