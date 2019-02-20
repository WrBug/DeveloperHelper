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

import com.github.megatronking.netbare.NetBareUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * A tiny file stream, the file will be loaded into memory, the file size should be less than
 * {@link #MAX_FILE_LENGTH}.
 *
 * @author Megatron King
 * @since 2018-12-23 11:39
 */
public class TinyFileStream implements Stream {

    /**
     * The max file size of this stream supports.
     */
    public static final int MAX_FILE_LENGTH = 64 * 1024;

    private final File mFile;

    /**
     * Constructs a stream by a tiny file.
     *
     * @param file A tiny file.
     * @throws IOException
     */
    public TinyFileStream(@NonNull File file) throws IOException {
        if (file.length() > MAX_FILE_LENGTH) {
            throw new IOException("Only support file size < " + MAX_FILE_LENGTH);
        }
        this.mFile = file;
    }

    @NonNull
    @Override
    public ByteBuffer toBuffer() {
        ByteBuffer byteBuffer;
        FileInputStream fis = null;
        FileChannel channel = null;
        try {
            fis = new FileInputStream(mFile);
            channel = fis.getChannel();
            byteBuffer = ByteBuffer.allocate(fis.available());
            channel.read(byteBuffer);
            byteBuffer.flip();
        } catch (IOException e) {
            byteBuffer = ByteBuffer.allocate(0);
        } finally {
            NetBareUtils.closeQuietly(channel);
            NetBareUtils.closeQuietly(fis);
        }
        return byteBuffer;
    }

}
