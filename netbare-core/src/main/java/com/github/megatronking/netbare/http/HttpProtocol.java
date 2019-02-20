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
package com.github.megatronking.netbare.http;

import androidx.annotation.NonNull;

/**
 * Http protocols that NetBare defined.
 *
 * @author Megatron King
 * @since 2018-10-15 19:50
 */
public enum HttpProtocol {

    /**
     * It means NetBare does not know the protocol.
     */
    UNKNOWN("unknown"),

    /**
     * An obsolete plaintext framing that does not use persistent sockets by default.
     */
    HTTP_1_0("HTTP/1.0"),

    /**
     * A plaintext framing that includes persistent connections.
     *
     * <p>This version of OkHttp implements <a href="https://tools.ietf.org/html/rfc7230">RFC
     * 7230</a>, and tracks revisions to that spec.
     */
    HTTP_1_1("HTTP/1.1"),

    /**
     * Chromium's binary-framed protocol that includes header compression, multiplexing multiple
     * requests on the same socket, and server-push. HTTP/1.1 semantics are layered on SPDY/3.
     */
    SPDY_3("spdy/3.1"),

    /**
     * The IETF's binary-framed protocol that includes header compression, multiplexing multiple
     * requests on the same socket, and server-push. HTTP/1.1 semantics are layered on HTTP/2.
     */
    HTTP_2("h2"),

    /**
     * Cleartext HTTP/2 with no "upgrade" round trip. This option requires the client to have prior
     * knowledge that the server supports cleartext HTTP/2.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7540#section-3.4">Starting HTTP/2 with Prior
     * Knowledge</a>
     */
    H2_PRIOR_KNOWLEDGE("h2_prior_knowledge"),

    /**
     * QUIC (Quick UDP Internet Connection) is a new multiplexed and secure transport atop UDP,
     * designed from the ground up and optimized for HTTP/2 semantics.
     * HTTP/1.1 semantics are layered on HTTP/2.
     */
    QUIC("quic");

    private final String protocol;

    HttpProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * Returns the protocol identified by {@code protocol}.
     *
     * @param protocol A string protocol presents in request line and status line.
     * @return A HttpProtocol enum.
     */
    @NonNull
    public static HttpProtocol parse(@NonNull String protocol) {
        if (protocol.equalsIgnoreCase(HTTP_1_0.protocol)) {
            return HTTP_1_0;
        } else if (protocol.equalsIgnoreCase(HTTP_1_1.protocol)) {
            return HTTP_1_1;
        } else if (protocol.equalsIgnoreCase(H2_PRIOR_KNOWLEDGE.protocol)) {
            return H2_PRIOR_KNOWLEDGE;
        } else if (protocol.equalsIgnoreCase(HTTP_2.protocol)) {
            return HTTP_2;
        } else if (protocol.equalsIgnoreCase(SPDY_3.protocol)) {
            return SPDY_3;
        } else if (protocol.equalsIgnoreCase(QUIC.protocol)) {
            return QUIC;
        } else {
            return UNKNOWN;
        }
    }

    /**
     * Returns the protocol string value rather than it's name.
     *
     * @return Protocol value.
     */
    @Override
    public String toString() {
        return this.protocol;
    }

}
