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
package com.github.megatronking.netbare.stream;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;

/**
 * A stream creates by a byte array.
 *
 * @author Megatron King
 * @since 2018-12-23 11:15
 */
public class ByteStream implements Stream {

    private final ByteBuffer mBuffer;

    /**
     * Constructs a new stream by a byte array.
     *
     * @param bytes The bytes to be wrapped into {@link ByteBuffer}.
     */
    public ByteStream(byte[] bytes) {
        this(bytes, 0, bytes.length);
    }

    /**
     * Constructs a new stream by a byte array.
     *
     * @param bytes The bytes to be wrapped into {@link ByteBuffer}.
     * @param offset The index of the first byte in array.
     * @param  length The number of bytes.
     */
    public ByteStream(byte[] bytes, int offset, int length) {
        // Do not use wrap.
        ByteBuffer buffer = ByteBuffer.allocate(length);
        buffer.put(bytes, offset, length);
        buffer.flip();
        this.mBuffer = buffer;
    }

    @NonNull
    @Override
    public ByteBuffer toBuffer() {
        return mBuffer;
    }

}
