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

import android.text.TextUtils;

import java.io.Closeable;
import java.io.IOException;

/**
 * A collection of assorted utility classes.
 *
 * @author Megatron King
 * @since 2018-10-08 22:52
 */
public final class NetBareUtils {

    /**
     * Http line end (CRLF) symbol.
     */
    public static final String LINE_END = "\r\n";

    /**
     * Http line end (CRLF) regex.
     */
    public static final String LINE_END_REGEX = "\\r\\n";

    /**
     * A byte array of http line end (CRLF).
     */
    public static final byte[] LINE_END_BYTES = LINE_END.getBytes();

    /**
     * Http double line end (CRLF), it separate the headers and body.
     */
    public static final String PART_END = "\r\n\r\n";

    /**
     * A byte array of double http line end (CRLF).
     */
    public static final byte[] PART_END_BYTES = "\r\n\r\n".getBytes();

    /**
     * Convert a int ip value to ipv4 string.
     *
     * @param ip The ip address.
     * @return A ipv4 string value, format is N.N.N.N
     */
    public static String convertIp(int ip) {
        return String.format("%s.%s.%s.%s", (ip >> 24) & 0x00FF,
                (ip >> 16) & 0x00FF, (ip >> 8) & 0x00FF, (ip & 0x00FF));
    }

    /**
     * Convert a string ip value to int.
     *
     * @param ip The ip address.
     * @return A int ip value.
     */
    public static int convertIp(String ip) {
        String[] arrayStrings = ip.split("\\.");
        return (Integer.parseInt(arrayStrings[0]) << 24)
                | (Integer.parseInt(arrayStrings[1]) << 16)
                | (Integer.parseInt(arrayStrings[2]) << 8)
                | (Integer.parseInt(arrayStrings[3]));
    }

    /**
     * Convert a short ip value to int.
     *
     * @param port The port.
     * @return A int port value.
     */
    public static int convertPort(short port) {
        return port & 0xFFFF;
    }

    /**
     * Closes a closeable object or release resource.
     *
     * @param closeable A closeable object like io stream.
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                NetBareLog.wtf(e);
            }
        }
    }

    /**
     * Parse a string to a integer value. If the string is not a integer value, this will return the
     * default value.
     *
     * @param string The string value.
     * @param defaultValue The default integer value.
     * @return The integer value.
     */
    public static int parseInt(String string, int defaultValue) {
        int result = defaultValue;

        if (TextUtils.isEmpty(string)) {
            return result;
        }

        try {
            result = Integer.parseInt(string);
        } catch (Exception e) {
            // parse error
        }
        return result;
    }

    /**
     * Parse a string to a integer value with a radix. If the string is not a integer value, this
     * will return the default value.
     *
     * @param string The string value.
     * @param radix The radix to be used.
     * @param defaultValue The default integer value.
     * @return The integer value.
     */
    public static int parseInt(String string, int radix, int defaultValue) {
        int result = defaultValue;

        if (TextUtils.isEmpty(string)) {
            return result;
        }

        try {
            result = Integer.parseInt(string, radix);
        } catch (Exception e) {
            // parse error
        }
        return result;
    }

}
