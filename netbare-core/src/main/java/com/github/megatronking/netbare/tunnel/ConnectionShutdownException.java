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
package com.github.megatronking.netbare.tunnel;

import java.io.IOException;

/**
 * Thrown when the connection has shutdown due to irresistible reason.
 *
 * @author Megatron King
 * @since 2018-12-20 18:50
 */
public class ConnectionShutdownException extends IOException {

    /**
     * Constructs an {@code ConnectionShutdownException} with the specified detail message.
     *
     * @param message The detail message (which is saved for later retrieval by the
     * {@link #getMessage()} method).
     */
    public ConnectionShutdownException(String message) {
        super(message);
    }

}
