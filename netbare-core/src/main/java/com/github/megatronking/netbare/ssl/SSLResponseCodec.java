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

import com.github.megatronking.netbare.NetBareLog;
import com.github.megatronking.netbare.gateway.Request;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

/**
 * An implementation of {@link SSLCodec} to codec response SSL packets. This codec creates a SSL
 * client engine using {@link SSLEngineFactory}, it requires the remote server ip and host.
 * Before encrypt, should call {@link #prepareHandshake()} to start SSL handshake.
 *
 * @author Megatron King
 * @since 2018-11-16 01:30
 */
public class SSLResponseCodec extends SSLCodec {

    private final SSLEngineFactory mSSLEngineFactory;

    private Request mRequest;
    private SSLEngine mEngine;

    /**
     * Constructs an instance of {@link SSLCodec} by a factory.
     *
     * @param factory A factory produces {@link SSLEngine}.
     */
    public SSLResponseCodec(SSLEngineFactory factory) {
        super(factory);
        this.mSSLEngineFactory = factory;
    }

    /**
     * Bind a {@link Request} to this codec.
     *
     * @param request A request has terminated remote server ip and port.
     */
    public void setRequest(Request request) {
        this.mRequest = request;
    }

    @Override
    protected SSLEngine createEngine(SSLEngineFactory factory) {
        if (mEngine == null) {
            try {
                String host = mRequest.host() != null ? mRequest.host() : mRequest.ip();
                mEngine = factory.createClientEngine(host, mRequest.port());
                mEngine.setUseClientMode(true);
            } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
                NetBareLog.e("Failed to create client SSLEngine: " + e.getMessage());
            }
        }
        return mEngine;
    }

    /**
     * Prepare and start SSL handshake with the remote server.
     *
     * @throws IOException If an I/O error has occurred.
     */
    public void prepareHandshake() throws IOException {
        if (mEngine != null) {
            // The handshake was started.
            return;
        }
        SSLEngine engine = createEngine(mSSLEngineFactory);
        if (engine == null) {
            throw new SSLException("Failed to create client SSLEngine.");
        }
        ByteBuffer input = ByteBuffer.allocate(0);
        handshake(engine, input, new CodecCallback() {
            @Override
            public void onPending(ByteBuffer buffer) {
            }

            @Override
            public void onProcess(ByteBuffer buffer) {
            }

            @Override
            public void onEncrypt(ByteBuffer buffer) throws IOException {
                // Send to remote server
                mRequest.process(buffer);
            }

            @Override
            public void onDecrypt(ByteBuffer buffer) {
            }
        });
    }

}
