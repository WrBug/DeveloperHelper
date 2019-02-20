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
package com.github.megatronking.netbare.http2;

import androidx.annotation.Nullable;

/**
 * This specification defines a number of frame types, each identified by a unique 8-bit type code.
 *
 * See https://httpwg.org/specs/rfc7540.html#FrameTypes
 *
 * @author Megatron King
 * @since 2019/1/5 15:30
 */
/* package */ enum FrameType {

    /**
     * DATA frames (type=0x0) convey arbitrary, variable-length sequences of octets associated with
     * a stream. One or more DATA frames are used, for instance, to carry HTTP request or response
     * payloads.
     */
    DATA((byte)0x0),

    /**
     * The HEADERS frame (type=0x1) is used to open a stream (Section 5.1), and additionally
     * carries a header block fragment. HEADERS frames can be sent on a stream in the "idle",
     * "reserved (local)", "open", or "half-closed (remote)" state.
     */
    HEADERS((byte)0x1),

    /**
     * The PRIORITY frame (type=0x2) specifies the sender-advised priority of a stream (Section 5.3).
     * It can be sent in any stream state, including idle or closed streams.
     */
    PRIORITY((byte)0x2),

    /**
     * The RST_STREAM frame (type=0x3) allows for immediate termination of a stream. RST_STREAM is
     * sent to request cancellation of a stream or to indicate that an error condition has occurred.
     */
    RST_STREAM((byte)0x3),

    /**
     * The SETTINGS frame (type=0x4) conveys configuration parameters that affect how endpoints
     * communicate, such as preferences and constraints on peer behavior. The SETTINGS frame is
     * also used to acknowledge the receipt of those parameters. Individually, a SETTINGS parameter
     * can also be referred to as a "setting".
     */
    SETTINGS((byte)0x4),

    /**
     * The PUSH_PROMISE frame (type=0x5) is used to notify the peer endpoint in advance of streams
     * the sender intends to initiate. The PUSH_PROMISE frame includes the unsigned 31-bit
     * identifier of the stream the endpoint plans to create along with a set of headers that
     * provide additional context for the stream.
     */
    PUSH_PROMISE((byte)0x5),

    /**
     * The PING frame (type=0x6) is a mechanism for measuring a minimal round-trip time from the
     * sender, as well as determining whether an idle connection is still functional. PING frames
     * can be sent from any endpoint.
     */
    PING((byte)0x6),

    /**
     * The GOAWAY frame (type=0x7) is used to initiate shutdown of a connection or to signal
     * serious error conditions. GOAWAY allows an endpoint to gracefully stop accepting new streams
     * while still finishing processing of previously established streams. This enables
     * administrative actions, like server maintenance.
     */
    GOAWAY((byte)0x7),

    /**
     * The WINDOW_UPDATE frame (type=0x8) is used to implement flow control.
     */
    WINDOW_UPDATE((byte)0x8),

    /**
     * The CONTINUATION frame (type=0x9) is used to continue a sequence of header block fragments
     * (Section 4.3). Any number of CONTINUATION frames can be sent, as long as the preceding frame
     * is on the same stream and is a HEADERS, PUSH_PROMISE, or CONTINUATION frame without the
     * END_HEADERS flag set.
     */
    CONTINUATION((byte)0x9);

    private final byte value;

    FrameType(byte value) {
        this.value = value;
    }

    /* package */ byte get() {
        return value;
    }

    @Nullable
    /* package */ static FrameType parse(byte value) {
        for (FrameType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return null;
    }

}
