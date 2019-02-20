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

import com.github.megatronking.netbare.gateway.Response;
import com.github.megatronking.netbare.http2.Http2Settings;
import com.github.megatronking.netbare.ip.Protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

/**
 * It is an implementation of {@link Response} represents the HTTP protocol. Instances of this
 * class are not immutable.
 *
 * @author Megatron King
 * @since 2018-11-11 23:37
 */
public class HttpResponse extends Response {

    private Response mResponse;

    private HttpId mHttpId;
    private HttpSession mSession;

    /* package */ HttpResponse(Response response, HttpSession session) {
        this(response, null, session);
    }

    /* package */ HttpResponse(Response response, HttpId httpId, HttpSession session) {
        this.mResponse = response;
        this.mHttpId = httpId;
        this.mSession = session;
    }

    /* package */ HttpSession session() {
        return mSession;
    }

    @Override
    public void process(ByteBuffer buffer) throws IOException {
        mResponse.process(buffer);
    }

    @Override
    public String id() {
        return mHttpId != null ? mHttpId.id : mResponse.id();
    }

    @Override
    public long time() {
        return mHttpId != null ? mHttpId.time : mResponse.time();
    }

    @Override
    public int uid() {
        return mResponse.uid();
    }

    @Override
    public String ip() {
        return mResponse.ip();
    }

    @Override
    public int port() {
        return mResponse.port();
    }

    @Override
    public Protocol protocol() {
        return mResponse.protocol();
    }

    @Override
    public String host() {
        return mResponse.host();
    }

    /**
     * Returns the request method for this request.
     *
     * @return The request method.
     */
    public HttpMethod method() {
        return mSession.method;
    }

    /**
     * Returns this response's http protocol, such as {@link HttpProtocol#HTTP_1_1} or
     * {@link HttpProtocol#HTTP_1_0}.
     *
     * @return The response protocol.
     */
    public HttpProtocol httpProtocol() {
        return mSession.protocol;
    }

    /**
     * Returns this request's path.
     *
     * @return The request path.
     */
    public String path() {
        return mSession.path;
    }

    /**
     * Whether the request is a HTTPS request.
     *
     * @return HTTPS returns true.
     */
    public boolean isHttps() {
        return mSession.isHttps;
    }

    /**
     * Whether the request is a web socket protocol.
     *
     * @return Web socket protocol returns true.
     */
    public boolean isWebSocket() {
        if (mSession.code != 101) {
            return false;
        }
        List<String> upgradeHeaderValues = null;
        for (Map.Entry<String, List<String>> entry : mSession.responseHeaders.entrySet()) {
            if ("upgrade".equalsIgnoreCase(entry.getKey())) {
                upgradeHeaderValues = entry.getValue();
            }
        }
        if (upgradeHeaderValues == null || upgradeHeaderValues.isEmpty()) {
            return false;
        }
        for (String value : upgradeHeaderValues) {
            if ("websocket".equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns this request's URL.
     *
     * @return The request URL.
     */
    public String url() {
        String path = path() == null ? "" : path();
        return (isHttps() ? "https://" : "http://") + host() + path;
    }

    /**
     * Returns this request's headers.
     *
     * @return A map of headers.
     */
    public Map<String, List<String>> requestHeaders() {
        return mSession.requestHeaders;
    }

    /**
     * Returns this response's headers.
     *
     * @return A map of headers.
     */
    public Map<String, List<String>> responseHeaders() {
        return mSession.responseHeaders;
    }

    /**
     * Returns this request's header values by name.
     *
     * @param name A header name.
     * @return A collection of header values.
     */
    public List<String> requestHeader(String name) {
        return requestHeaders().get(name);
    }

    /**
     * Returns this response's header values by name.
     *
     * @param name A header name.
     * @return A collection of header values.
     */
    public List<String> responseHeader(String name) {
        return responseHeaders().get(name);
    }

    /**
     * Returns the HTTP status code.
     *
     * @return Status code.
     */
    public int code() {
        return mSession.code;
    }

    /**
     * Returns the HTTP status message.
     *
     * @return Status message.
     */
    public String message() {
        return mSession.message;
    }

    /**
     * Returns the offset of request body's starting index in request data.
     *
     * @return Offset of request body.
     */
    public int requestBodyOffset() {
        return mSession.reqBodyOffset;
    }

    /**
     * Returns the offset of response body's starting index in request data.
     *
     * @return Offset of request body.
     */
    public int responseBodyOffset() {
        return mSession.resBodyOffset;
    }

    /**
     * Returns the HTTP2 stream id.
     *
     * @return A stream id.
     */
    public int streamId() {
        return mHttpId != null ? mHttpId.streamId : -1;
    }

    /**
     * Returns the HTTP/2 client settings.
     *
     * @return Client settings.
     */
    public Http2Settings clientHttp2Settings() {
        return mSession.clientHttp2Settings;
    }

    /**
     * Returns the HTTP/2 peer settings.
     *
     * @return Client settings.
     */
    public Http2Settings peerHttp2Settings() {
        return mSession.peerHttp2Settings;
    }

    /**
     * Whether the current HTTP2 response stream is end.
     *
     * @return End is true.
     */
    public boolean responseStreamEnd() {
        return mSession.responseStreamEnd;
    }

}
