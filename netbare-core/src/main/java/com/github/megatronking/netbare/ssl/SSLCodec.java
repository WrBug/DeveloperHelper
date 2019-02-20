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
package com.github.megatronking.netbare.ssl;

import android.os.Build;
import androidx.annotation.NonNull;

import com.github.megatronking.netbare.NetBareLog;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;

/**
 * A base class for encrypting and decrypting SSL packets. Use {@link CodecCallback} to
 * observe actions and receive output packets.
 *
 * <p>SSL handshake steps:</p>
 *
 *    client          server          message
 *    ======          ======          =======
 *    wrap()          ...             ClientHello
 *    ...             unwrap()        ClientHello
 *    ...             wrap()          ServerHello/Certificate
 *    unwrap()        ...             ServerHello/Certificate
 *    wrap()          ...             ClientKeyExchange
 *    wrap()          ...             ChangeCipherSpec
 *    wrap()          ...             Finished
 *    ...             unwrap()        ClientKeyExchange
 *    ...             unwrap()        ChangeCipherSpec
 *    ...             unwrap()        Finished
 *    ...             wrap()          ChangeCipherSpec
 *    ...             wrap()          Finished
 *    unwrap()        ...             ChangeCipherSpec
 *    unwrap()        ...             Finished
 *
 * @author Megatron King
 * @since 2018-11-15 17:46
 */
public abstract class SSLCodec {

    /**
     * Change cipher spec.
     */
    public static final int SSL_CONTENT_TYPE_CHANGE_CIPHER_SPEC =
            SSLUtils.SSL_CONTENT_TYPE_CHANGE_CIPHER_SPEC;

    /**
     * Alert.
     */
    public static final int SSL_CONTENT_TYPE_ALERT =
            SSLUtils.SSL_CONTENT_TYPE_ALERT;

    /**
     * Handshake.
     */
    public static final int SSL_CONTENT_TYPE_HANDSHAKE =
            SSLUtils.SSL_CONTENT_TYPE_HANDSHAKE;

    /**
     * Application data.
     */
    public static final int SSL_CONTENT_TYPE_APPLICATION_DATA =
            SSLUtils.SSL_CONTENT_TYPE_APPLICATION_DATA;

    /**
     * HeartBeat Extension.
     */
    public static final int SSL_CONTENT_TYPE_EXTENSION_HEARTBEAT =
            SSLUtils.SSL_CONTENT_TYPE_EXTENSION_HEARTBEAT;

    /**
     * Should larger than generated pem certificate file size.
     */
    private static final int DEFAULT_BUFFER_SIZE = 20 * 1024;

    private SSLEngineFactory mSSLEngineFactory;

    private boolean mEngineClosed;
    private boolean mHandshakeStarted;
    private boolean mHandshakeFinished;

    private Queue<ByteBuffer> mPlaintextBuffers;

    SSLCodec(SSLEngineFactory factory) {
        this.mSSLEngineFactory = factory;
        this.mPlaintextBuffers = new ConcurrentLinkedDeque<>();
    }

    /**
     * Create an {@link SSLEngine} instance to encode and decode SSL packets.
     *
     * @param factory A factory produces {@link SSLEngine}.
     * @return An instance of {@link SSLEngine}.
     * @throws IOException If an I/O error has occurred.
     */
    protected abstract SSLEngine createEngine(SSLEngineFactory factory)
            throws IOException;

    /**
     * Handshake with the client or server and try to decode a SSL encrypt packet.
     *
     * @param buffer The SSL encrypt packet.
     * @param callback A callback to observe actions and receive output packets.
     * @throws IOException If an I/O error has occurred.
     */
    public void decode(ByteBuffer buffer, @NonNull CodecCallback callback) throws IOException {
        int verifyResult = SSLUtils.verifyPacket(buffer);
        if (!mHandshakeStarted) {
            if (verifyResult == SSLUtils.PACKET_NOT_ENCRYPTED) {
                callback.onDecrypt(buffer);
                return;
            }
        }
        if (verifyResult == SSLUtils.PACKET_NOT_ENOUGH) {
            callback.onPending(buffer);
            return;
        }
        decode(createEngine(mSSLEngineFactory), buffer, callback);
    }

    /**
     * Try to encrypt a plaintext packet. If SSL handshake has finished, then encode it,
     * otherwise add it to queue and wait handshake finished.
     *
     * @param buffer The plaintext packet.
     * @param callback A callback to observe actions and receive output packets.
     * @throws IOException If an I/O error has occurred.
     */
    public void encode(ByteBuffer buffer, @NonNull CodecCallback callback) throws IOException {
        if (!buffer.hasRemaining()) {
            return;
        }
        if (mHandshakeFinished) {
            wrap(createEngine(mSSLEngineFactory), buffer, callback);
        } else {
            mPlaintextBuffers.offer(buffer);
        }
    }

    private void decode(SSLEngine engine, ByteBuffer input, CodecCallback callback)
            throws IOException {
        // Give up decrypt SSL packet.
        if (engine == null) {
            callback.onProcess(input);
            return;
        }
        startDecode(engine, input, callback);
    }

    private void startDecode(SSLEngine engine, ByteBuffer input, CodecCallback callback)
            throws IOException {
        if (mEngineClosed) {
            return;
        }
        if (mHandshakeFinished) {
            unwrap(engine, input, callback);
        } else {
            handshake(engine, input, callback);
        }
        // Start wrap plaintext to engine if possible.
        if (mHandshakeFinished && !mPlaintextBuffers.isEmpty()) {
            ByteBuffer plaintextBuffer;
            while (!mPlaintextBuffers.isEmpty()) {
                plaintextBuffer = mPlaintextBuffers.poll();
                if (plaintextBuffer != null && plaintextBuffer.hasRemaining()) {
                    wrap(engine, plaintextBuffer, callback);
                }
            }
        }
    }

    /* package */ void handshake(SSLEngine engine, ByteBuffer input, CodecCallback callback)
            throws IOException {
        if (!mHandshakeStarted) {
            engine.beginHandshake();
            mHandshakeStarted = true;
        }
        SSLEngineResult.HandshakeStatus status = engine.getHandshakeStatus();
        while (!mHandshakeFinished) {
            if (mEngineClosed) {
                throw new IOException("Handshake failed: Engine is closed.");
            }
            if (status == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
                // Should never happen
                throw new IOException("Handshake failed: Invalid handshake status: " + status);
            } else if (status == SSLEngineResult.HandshakeStatus.FINISHED) {
                mHandshakeFinished = true;
                NetBareLog.i("SSL handshake finished!");
                if (input.hasRemaining()) {
                    decode(engine, input, callback);
                }
            } else if (status == SSLEngineResult.HandshakeStatus.NEED_WRAP) {
                status = handshakeWrap(engine, callback).getHandshakeStatus();
            } else if (status == SSLEngineResult.HandshakeStatus.NEED_UNWRAP) {
                // Wait next encrypted buffer.
                if (!input.hasRemaining()) {
                    break;
                }
                status = handshakeUnwrap(engine, input, callback).getHandshakeStatus();
            } else if (status == SSLEngineResult.HandshakeStatus.NEED_TASK) {
                runDelegatedTasks(engine);
            }
        }
    }

    private SSLEngineResult handshakeWrap(SSLEngine engine, CodecCallback callback)
            throws IOException {
        SSLEngineResult result;
        SSLEngineResult.Status status;
        ByteBuffer output = allocate();
        while (true) {
            result = engineWrap(engine, allocate(0), output);
            status = result.getStatus();
            output.flip();
            if (output.hasRemaining()) {
                callback.onEncrypt(output);
            }
            if (status == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                output = allocate(engine.getSession().getApplicationBufferSize());
            } else {
                if (status == SSLEngineResult.Status.CLOSED) {
                    mEngineClosed = true;
                }
                break;
            }
        }
        return result;
    }

    private SSLEngineResult handshakeUnwrap(SSLEngine engine, ByteBuffer input,
                                            CodecCallback callback) throws IOException {
        SSLEngineResult result;
        SSLEngineResult.Status status;
        ByteBuffer output = allocate();
        while (true) {
            result = engineUnwrap(engine, input, output);
            status = result.getStatus();
            output.flip();
            int producedSize = output.remaining();
            if (producedSize > 0) {
                callback.onDecrypt(output);
            }
            if (status == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                int bufferSize = engine.getSession().getApplicationBufferSize() - producedSize;
                if (bufferSize < 0) {
                    bufferSize = engine.getSession().getApplicationBufferSize();
                }
                output = allocate(bufferSize);
            } else if (status == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
                // Store the remaining packet and wait next encrypted buffer.
                if (input.hasRemaining()) {
                    callback.onPending(ByteBuffer.wrap(input.array(), input.position(),
                            input.remaining()));
                    // Clear all data.
                    input.position(0);
                    input.limit(0);
                }
                break;
            } else if (status == SSLEngineResult.Status.CLOSED) {
                mEngineClosed = true;
                break;
            } else {
                // It is status OK.
                break;
            }
        }
        return result;
    }

    private void unwrap(SSLEngine engine, ByteBuffer input, CodecCallback callback)
            throws IOException {
        ByteBuffer output = null;
        while (true) {
            if (output == null) {
                output = allocate();
            }
            SSLEngineResult result = engineUnwrap(engine, input, output);
            SSLEngineResult.Status status = result.getStatus();
            output.flip();
            int producedSize = output.remaining();
            if (producedSize > 0) {
                callback.onDecrypt(output);
                output = null;
            }
            if (status == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                int bufferSize = engine.getSession().getApplicationBufferSize() - producedSize;
                if (bufferSize < 0) {
                    bufferSize = engine.getSession().getApplicationBufferSize();
                }
                output = allocate(bufferSize);
            } else if (status == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
                // Store the remaining packet and wait next encrypted buffer.
                if (input.hasRemaining()) {
                    callback.onPending(ByteBuffer.wrap(input.array(), input.position(),
                            input.remaining()));
                    // Clear all data.
                    input.position(0);
                    input.limit(0);
                }
                break;
            } else if (status == SSLEngineResult.Status.CLOSED) {
                mEngineClosed = true;
                break;
            } else {
                if (!input.hasRemaining()) {
                    break;
                }
            }
        }
    }

    private void wrap(SSLEngine engine, ByteBuffer input, CodecCallback callback)
            throws IOException {
        ByteBuffer output = allocate();
        while (true) {
            SSLEngineResult result = engineWrap(engine, input, output);
            SSLEngineResult.Status status = result.getStatus();
            output.flip();
            if (output.hasRemaining()) {
                callback.onEncrypt(output);
            }
            if (status == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                output = allocate(engine.getSession().getApplicationBufferSize());
            } else {
                if (status == SSLEngineResult.Status.CLOSED) {
                    mEngineClosed = true;
                }
                break;
            }
        }
        if (!mEngineClosed && input.hasRemaining()) {
            wrap(engine, input, callback);
        }
    }

    private SSLEngineResult engineWrap(SSLEngine engine, ByteBuffer input, ByteBuffer output)
            throws SSLException {
        return engine.wrap(input, output);
    }

    private SSLEngineResult engineUnwrap(SSLEngine engine, ByteBuffer input, ByteBuffer output)
            throws SSLException {
        int position = input.position();
        SSLEngineResult result;
        // Fixed issue #4
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) {
            // In Android 8.1, the BUFFER_OVERFLOW in the unwrap method will throw an
            // SSLException-EOFException. We catch this error and increase the output buffer
            // capacity.
            while (true) {
                int inputRemaining = input.remaining();
                try {
                    result = engine.unwrap(input, output);
                    break;
                } catch (SSLException e) {
                    if (!output.hasRemaining()) {
                        // Copy
                        ByteBuffer outputTemp = ByteBuffer.allocate(output.capacity() * 2);
                        output.flip();
                        outputTemp.put(output);
                        output = outputTemp;
                    } else {
                        // java.io.EOFException: Read error is an Android 8.1 system bug,
                        // it will cause #4 and #11. We swallowed this exception and not throw.
                        if ((e.getCause() instanceof EOFException && inputRemaining == 31 &&
                                input.remaining() == 0 && output.remaining() == output.capacity())) {
                            // Create a new SSLEngineResult.
                            result = new SSLEngineResult(SSLEngineResult.Status.OK,
                                    SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING,
                                    inputRemaining, 0);
                            break;
                        } else {
                            throw e;
                        }
                    }
                }
            }
        } else {
            result = engine.unwrap(input, output);
        }

        // This is a workaround for a bug in Android 5.0. Android 5.0 does not correctly update
        // the SSLEngineResult.bytesConsumed() in some cases and just return 0.
        //
        // See:
        //     - https://android-review.googlesource.com/c/platform/external/conscrypt/+/122080
        //     - https://github.com/netty/netty/issues/7758
        if (result.bytesConsumed() == 0) {
            int consumed = input.position() - position;
            if (consumed != result.bytesConsumed()) {
                // Create a new SSLEngineResult with the correct bytesConsumed().
                result = new SSLEngineResult(result.getStatus(), result.getHandshakeStatus(),
                        consumed, result.bytesProduced());
            }
        }
        return result;
    }

    private void runDelegatedTasks(SSLEngine engine) {
        while (true) {
            final Runnable task = engine.getDelegatedTask();
            if (task == null) {
                break;
            }
            task.run();
        }
    }

    private ByteBuffer allocate(int size) {
        return ByteBuffer.allocate(size);
    }

    private ByteBuffer allocate() {
        return ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
    }

    /**
     * A callback to receive {@link SSLCodec} results.
     */
    public interface CodecCallback {

        /**
         * The packet is not a perfect SSL encrypted packet, should wait next packet and decode
         * together.
         *
         * @param buffer The buffer should be pended in a queue.
         */
        void onPending(ByteBuffer buffer);

        /**
         * The packet is unable to decrypt or encrypt, should send them to tunnel immediately.
         *
         * @param buffer Packets should send to tunnel.
         * @throws IOException If an I/O error has occurred.
         */
        void onProcess(ByteBuffer buffer) throws IOException;

        /**
         * Output an encrypted packet.
         *
         * @param buffer The encrypted packet.
         * @throws IOException If an I/O error has occurred.
         */
        void onEncrypt(ByteBuffer buffer) throws IOException;

        /**
         * Output an decrypted packet, it is a plaintext.
         *
         * @param buffer The decrypted packet.
         * @throws IOException If an I/O error has occurred.
         */
        void onDecrypt(ByteBuffer buffer) throws IOException;

    }

}
