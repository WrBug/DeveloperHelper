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
/*
 * Copyright (C) 2014 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.megatronking.netbare.ws;

import com.github.megatronking.netbare.NetBareUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.toHexString;

/**
 * A web socket frame reader.
 *
 * @author Megatron King
 * @since 2019/1/18 23:52
 */
public class WebSocketReader {

    private final InputStream mInput;
    private final boolean mClient;
    private final WebSocketCallback mCallback;

    private final byte[] mMaskKey;

    private boolean mClosed;

    private int mOpcode;
    private long mFrameLength;
    private boolean mFinalFrame;
    private boolean mControlFrame;

    private List<byte[]> mMessageSegments;

    public WebSocketReader(InputStream input, boolean client, WebSocketCallback callback) {
        this.mInput = input;
        this.mClient = client;
        this.mCallback = callback;

        // Masks are only a concern for server writers.
        this.mMaskKey = client ? null : new byte[4];

        this.mMessageSegments = new ArrayList<>(1);
    }

    /**
     * Process the next protocol frame.
     */
    public void processNextFrame() throws IOException {
        readHeader();
        if (mControlFrame) {
            readControlFrame();
        } else {
            readMessageFrame();
        }
    }

    /**
     * Close the input stream.
     */
    public void close() {
        mClosed = true;
        NetBareUtils.closeQuietly(mInput);
    }

    private void readHeader() throws IOException {
        if (mClosed) {
            throw new IOException("The stream is closed.");
        }

        // Each frame starts with two bytes of data.
        //
        // 0 1 2 3 4 5 6 7    0 1 2 3 4 5 6 7
        // +-+-+-+-+-------+  +-+-------------+
        // |F|R|R|R| OP    |  |M| LENGTH      |
        // |I|S|S|S| CODE  |  |A|             |
        // |N|V|V|V|       |  |S|             |
        // | |1|2|3|       |  |K|             |
        // +-+-+-+-+-------+  +-+-------------+

        // Read first byte
        int b0 = mInput.read() & 0xff;

        mOpcode = b0 & WebSocketProtocol.B0_MASK_OPCODE;
        mFinalFrame = (b0 & WebSocketProtocol.B0_FLAG_FIN) != 0;
        mControlFrame = (b0 & WebSocketProtocol.OPCODE_FLAG_CONTROL) != 0;

        // Control frames must be final frames (cannot contain continuations).
        if (mControlFrame && !mFinalFrame) {
            throw new ProtocolException("Control frames must be final.");
        }

        boolean reservedFlag1 = (b0 & WebSocketProtocol.B0_FLAG_RSV1) != 0;
        boolean reservedFlag2 = (b0 & WebSocketProtocol.B0_FLAG_RSV2) != 0;
        boolean reservedFlag3 = (b0 & WebSocketProtocol.B0_FLAG_RSV3) != 0;
        if (reservedFlag1 || reservedFlag2 || reservedFlag3) {
            // Reserved flags are for extensions which we currently do not support.
            throw new ProtocolException("Reserved flags are unsupported.");
        }

        int b1 = mInput.read() & 0xff;

        boolean isMasked = (b1 & WebSocketProtocol.B1_FLAG_MASK) != 0;
        if (isMasked == mClient) {
            // Masked payloads must be read on the server. Unmasked payloads must be read on the client.
            throw new ProtocolException(mClient
                    ? "Server-sent frames must not be masked."
                    : "Client-sent frames must be masked.");
        }

        // Get frame length, optionally reading from follow-up bytes if indicated by special values.
        mFrameLength = b1 & WebSocketProtocol.B1_MASK_LENGTH;
        if (mFrameLength == WebSocketProtocol.PAYLOAD_SHORT) {
            mFrameLength = readShort() & 0xffffL; // Value is unsigned.
        } else if (mFrameLength == WebSocketProtocol.PAYLOAD_LONG) {
            mFrameLength = readLong();
            if (mFrameLength < 0) {
                throw new ProtocolException(
                        "Frame length 0x" + Long.toHexString(mFrameLength) + " > 0x7FFFFFFFFFFFFFFF");
            }
        }

        if (mControlFrame && mFrameLength > WebSocketProtocol.PAYLOAD_BYTE_MAX) {
            throw new ProtocolException("Control frame must be less than " +
                    WebSocketProtocol.PAYLOAD_BYTE_MAX + "B.");
        }

        if (isMasked) {
            // Read the masking key as bytes so that they can be used directly for unmasking.
            readFully(mMaskKey);
        }

    }

    private void readControlFrame() throws IOException {
        if (mFrameLength >= Integer.MAX_VALUE) {
            throw new IOException("Not support a frame length > " + Integer.MAX_VALUE);
        }
        WebSocketCallback callback = mCallback;
        if (callback == null) {
            return;
        }
        ByteBuffer byteBuffer;
        if (mFrameLength > 0) {
            byte[] frame = new byte[(int) mFrameLength];
            readFully(frame);
            if (!mClient) {
                WebSocketProtocol.toggleMask(frame, mMaskKey);
            }
            byteBuffer = ByteBuffer.wrap(frame);
        } else {
            byteBuffer = ByteBuffer.allocate(0);
        }
        switch (mOpcode) {
            case WebSocketProtocol.OPCODE_CONTROL_PING:
                callback.onReadPing(readFully(byteBuffer));
                break;
            case WebSocketProtocol.OPCODE_CONTROL_PONG:
                callback.onReadPong(readFully(byteBuffer));
                break;
            case WebSocketProtocol.OPCODE_CONTROL_CLOSE:
                int code = WebSocketProtocol.CLOSE_NO_STATUS_CODE;
                String reason = "";
                long bufferSize = byteBuffer.remaining();
                if (bufferSize == 1) {
                    throw new ProtocolException("Malformed close payload length of 1.");
                } else if (bufferSize != 0) {
                    code = byteBuffer.getShort();
                    reason = new String(byteBuffer.array(), byteBuffer.position(), byteBuffer.remaining());
                    String codeExceptionMessage = WebSocketProtocol.closeCodeExceptionMessage(code);
                    if (codeExceptionMessage != null) {
                        throw new ProtocolException(codeExceptionMessage);
                    }
                }
                callback.onReadClose(code, reason);
                break;
            default:
                throw new ProtocolException("Unknown control opcode: " + toHexString(mOpcode));
        }
    }

    private void readMessageFrame() throws IOException {
        int opcode = this.mOpcode;
        if (opcode != WebSocketProtocol.OPCODE_TEXT && opcode != WebSocketProtocol.OPCODE_BINARY) {
            throw new ProtocolException("Unknown opcode: " + toHexString(opcode));
        }

        readMessage();

        WebSocketCallback callback = mCallback;
        if (callback == null) {
            return;
        }
        if (mMessageSegments.isEmpty()) {
            throw new ProtocolException("Message frame segment is empty!");
        }
        int total = 0;
        for (byte[] segment : mMessageSegments) {
            total+= segment.length;
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(total);
        for (byte[] segment : mMessageSegments) {
            byteBuffer.put(segment);
        }
        byteBuffer.flip();
        mMessageSegments.clear();
        if (opcode == WebSocketProtocol.OPCODE_TEXT) {
            callback.onReadMessage(new String(byteBuffer.array()));
        } else {
            callback.onReadMessage(byteBuffer.array());
        }
    }

    private void readMessage() throws IOException {
        while (true) {
            if (mClosed) {
                throw new IOException("The stream is closed.");
            }
            if (mFrameLength <= 0) {
                return;
            }
            if (mFrameLength >= Integer.MAX_VALUE) {
                throw new IOException("Not support a frame length > " + Integer.MAX_VALUE);
            }
            byte[] frame = new byte[(int) mFrameLength];
            readFully(frame);
            if (!mClient) {
                WebSocketProtocol.toggleMask(frame, mMaskKey);
            }
            mMessageSegments.add(frame);

            if (mFinalFrame) {
                break; // We are exhausted and have no continuations.
            }

            readUntilNonControlFrame();
            if (mOpcode != WebSocketProtocol.OPCODE_CONTINUATION) {
                throw new ProtocolException("Expected continuation opcode. Got: " + toHexString(mOpcode));
            }
        }
    }

    private void readUntilNonControlFrame() throws IOException {
        while (!mClosed) {
            readHeader();
            if (!mControlFrame) {
                break;
            }
            readControlFrame();
        }
    }

    private short readShort() throws IOException {
        return (short) ((mInput.read() & 0xFF) << 8
                | (mInput.read() & 0xFF));
    }

    private long readLong() throws IOException {
        return (mInput.read() & 0xFFL) << 56
                | (mInput.read() & 0xFFL) << 48
                | (mInput.read() & 0xFFL) << 40
                | (mInput.read() & 0xFFL) << 32
                | (mInput.read() & 0xFFL) << 24
                | (mInput.read() & 0xFFL) << 16
                | (mInput.read() & 0xFFL) << 8
                | (mInput.read() & 0xFFL);
    }

    private void readFully(byte[] bytes) throws IOException {
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) mInput.read();
        }
    }

    private byte[] readFully(ByteBuffer byteBuffer) {
        byte[] data = new byte[byteBuffer.remaining()];
        byteBuffer.get(data);
        return data;
    }

}
