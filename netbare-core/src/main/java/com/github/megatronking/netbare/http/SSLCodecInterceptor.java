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

import com.github.megatronking.netbare.NetBareLog;
import com.github.megatronking.netbare.NetBareXLog;
import com.github.megatronking.netbare.gateway.Request;
import com.github.megatronking.netbare.gateway.Response;
import com.github.megatronking.netbare.ip.Protocol;
import com.github.megatronking.netbare.ssl.JKS;
import com.github.megatronking.netbare.ssl.SSLCodec;
import com.github.megatronking.netbare.ssl.SSLEngineFactory;
import com.github.megatronking.netbare.ssl.SSLUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;

/**
 * An interceptor decodes SSL encrypt packets to plaintext packets.
 *
 * @author Megatron King
 * @since 2018-11-15 15:39
 */
/* package */ class SSLCodecInterceptor extends HttpPendingInterceptor implements SSLRefluxCallback {

    private static SSLEngineFactory sEngineFactory;

    private Request mRequest;
    private Response mResponse;

    private JKS mJKS;

    private SSLHttpRequestCodec mRequestCodec;
    private SSLHttpResponseCodec mResponseCodec;

    private NetBareXLog mLog;

    private boolean mClientAlpnResolved;

    /* package */ SSLCodecInterceptor(JKS jks, Request request, Response response) {
        this.mJKS = jks;
        this.mRequest = request;
        this.mResponse = response;

        if (sEngineFactory == null) {
            try {
                sEngineFactory = new SSLEngineFactory(jks);
            } catch (GeneralSecurityException | IOException e) {
                NetBareLog.e("Create SSLEngineFactory failed: " + e.getMessage());
            }
        }

        mRequestCodec = new SSLHttpRequestCodec(sEngineFactory);
        mResponseCodec = new SSLHttpResponseCodec(sEngineFactory);

        mLog = new NetBareXLog(Protocol.TCP, request.ip(), request.port());
    }

    @Override
    protected void intercept(@NonNull final HttpRequestChain chain, @NonNull ByteBuffer buffer,
                             int index) throws IOException {
        if (!chain.request().isHttps()) {
            chain.process(buffer);
        } else if (!mJKS.isInstalled()) {
            // Skip all interceptors
            chain.processFinal(buffer);
            mLog.w("JSK not installed, skip all interceptors!");
        } else {
            if (!mClientAlpnResolved) {
                buffer = mergeRequestBuffer(buffer);
                int verifyResult = SSLUtils.verifyPacket(buffer);
                if (verifyResult == SSLUtils.PACKET_NOT_ENCRYPTED) {
                    throw new IOException("SSL packet is not encrypt.");
                }
                if (verifyResult == SSLUtils.PACKET_NOT_ENOUGH) {
                    pendRequestBuffer(buffer);
                    return;
                }

                mRequestCodec.setRequest(chain.request());
                // Start handshake with remote server
                mResponseCodec.setRequest(chain.request());

                // Parse the ALPN protocol of client.
                HttpProtocol[] protocols = SSLUtils.parseClientHelloAlpn(buffer);
                mClientAlpnResolved = true;

                if (protocols == null || protocols.length == 0) {
                    mRequestCodec.setSelectedAlpnResolved();
                    mResponseCodec.setSelectedAlpnResolved();
                    mResponseCodec.prepareHandshake();
                } else {
                    // Detect remote server's ALPN and then continue request.
                    mResponseCodec.prepareHandshake(protocols, new SSLHttpResponseCodec.AlpnResolvedCallback() {
                        @Override
                        public void onResult(String selectedAlpnProtocol) throws IOException {
                            if (selectedAlpnProtocol != null) {
                                HttpProtocol protocol = HttpProtocol.parse(selectedAlpnProtocol);
                                // Only accept Http1.1 and Http2.0
                                if (protocol == HttpProtocol.HTTP_1_1 || protocol == HttpProtocol.HTTP_2) {
                                    mRequestCodec.setSelectedAlpnProtocol(protocol);
                                    chain.request().session().protocol = protocol;
                                    mLog.i("Server selected ALPN protocol: " + protocol.toString());
                                } else {
                                    mLog.w("Unexpected server ALPN protocol: " + protocol.toString());
                                }
                            }
                            mRequestCodec.setSelectedAlpnResolved();
                            // Continue request.
                            decodeRequest(chain, ByteBuffer.allocate(0));
                        }
                    });
                }
            }
            // Hold the request buffer until the server ALPN configuration resolved.
            if (!mRequestCodec.selectedAlpnResolved()) {
                pendRequestBuffer(buffer);
                return;
            }

            decodeRequest(chain, buffer);
        }
    }

    @Override
    protected void intercept(@NonNull final HttpResponseChain chain, @NonNull ByteBuffer buffer,
                             int index) throws IOException {
        if (!chain.response().isHttps()) {
            chain.process(buffer);
        } else if (!mJKS.isInstalled()) {
            // Skip all interceptors
            chain.processFinal(buffer);
            mLog.w("JSK not installed, skip all interceptors!");
        } else {
            // Merge buffers
            decodeResponse(chain, buffer);
        }
    }

    @Override
    public void onRequest(HttpRequest request, ByteBuffer buffer) throws IOException {
        mResponseCodec.encode(buffer, new SSLCodec.CodecCallback() {
            @Override
            public void onPending(ByteBuffer buffer) {
            }

            @Override
            public void onProcess(ByteBuffer buffer) {
            }

            @Override
            public void onEncrypt(ByteBuffer buffer) throws IOException {
                // The encrypt request data is sent to remote server
                mRequest.process(buffer);
            }

            @Override
            public void onDecrypt(ByteBuffer buffer) {
            }
        });
    }

    @Override
    public void onResponse(HttpResponse response, ByteBuffer buffer) throws IOException {
        mRequestCodec.encode(buffer, new SSLCodec.CodecCallback() {
            @Override
            public void onPending(ByteBuffer buffer) {
            }

            @Override
            public void onProcess(ByteBuffer buffer) {
            }

            @Override
            public void onEncrypt(ByteBuffer buffer) throws IOException {
                // The encrypt response data is sent to proxy server
                mResponse.process(buffer);
            }

            @Override
            public void onDecrypt(ByteBuffer buffer) {
            }
        });
    }

    private void decodeRequest(final HttpRequestChain chain, ByteBuffer buffer) throws IOException {
        // Merge buffers
        mRequestCodec.decode(mergeRequestBuffer(buffer),
                new SSLCodec.CodecCallback() {
                    @Override
                    public void onPending(ByteBuffer buffer) {
                        pendRequestBuffer(buffer);
                    }

                    @Override
                    public void onProcess(ByteBuffer buffer) throws IOException {
                        chain.processFinal(buffer);
                    }

                    @Override
                    public void onEncrypt(ByteBuffer buffer) throws IOException {
                        mResponse.process(buffer);
                    }

                    @Override
                    public void onDecrypt(ByteBuffer buffer) throws IOException {
                        chain.process(buffer);
                    }
                });
    }


    private void decodeResponse(final HttpResponseChain chain, ByteBuffer buffer) throws IOException {
        // Merge buffers
        mResponseCodec.decode(mergeResponseBuffer(buffer),
                new SSLCodec.CodecCallback() {
                    @Override
                    public void onPending(ByteBuffer buffer) {
                        pendResponseBuffer(buffer);
                    }

                    @Override
                    public void onProcess(ByteBuffer buffer) throws IOException {
                        chain.processFinal(buffer);
                    }

                    @Override
                    public void onEncrypt(ByteBuffer buffer) throws IOException {
                        mRequest.process(buffer);
                    }

                    @Override
                    public void onDecrypt(ByteBuffer buffer) throws IOException {
                        chain.process(buffer);
                    }

                });
    }

}
