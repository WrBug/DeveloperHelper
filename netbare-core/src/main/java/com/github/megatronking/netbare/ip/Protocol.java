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
package com.github.megatronking.netbare.ip;

import androidx.annotation.Nullable;

/**
 * The enum defines all supported IP protocols.
 *
 * Internet Protocol numbers see:
 * https://en.wikipedia.org/wiki/List_of_IP_protocol_numbers
 *
 * @author Megatron King
 * @since 2018-10-11 00:13
 */
public enum Protocol {

    /**
     * Internet Control Message Protocol.
     */
    ICMP((byte)1),

    /**
     * 	Transmission Control Protocol.
     */
    TCP((byte)6),

    /**
     * 	User Datagram Protocol.
     */
    UDP((byte)17);

    final byte number;

    Protocol(byte number) {
        this.number = number;
    }

    /**
     * Parse the protocol by number.
     *
     * @param number Protocol number.
     * @return The supported protocol number or null.
     */
    @Nullable
    public static Protocol parse(int number) {
        for (Protocol protocol : values()) {
            if (protocol.number == number) {
                return protocol;
            }
        }
        return null;
    }

}
