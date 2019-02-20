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

import com.github.megatronking.netbare.NetBareUtils;
import com.github.megatronking.netbare.NetBareXLog;
import com.github.megatronking.netbare.ip.Protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Parse HTTP request header part and response header part from HTTP packets. The parse result will
 *  be set to {@link HttpSession}.
 *
 * @author Megatron King
 * @since 2018-12-09 12:19
 */
/* package */ final class HttpHeaderParseInterceptor extends HttpIndexInterceptor {

    private NetBareXLog mLog;

    @Override
    protected void intercept(@NonNull HttpRequestChain chain, @NonNull ByteBuffer buffer,
                             int index) throws IOException {
        if (index > 0) {
            chain.process(buffer);
            return;
        }
        if (mLog == null) {
            mLog = new NetBareXLog(Protocol.TCP, chain.request().ip(), chain.request().port());
        }
        parseRequestHeader(chain.request().session(), buffer);
        chain.process(buffer);
    }

    @Override
    protected void intercept(@NonNull HttpResponseChain chain, @NonNull ByteBuffer buffer,
                             int index) throws IOException {
        if (index > 0) {
            chain.process(buffer);
            return;
        }
        if (mLog == null) {
            mLog = new NetBareXLog(Protocol.TCP, chain.response().ip(), chain.response().port());
        }
        parseResponseHeader(chain.response().session(), buffer);
        chain.process(buffer);
    }

    private void parseRequestHeader(HttpSession session, ByteBuffer buffer) {
        session.reqBodyOffset = buffer.remaining();
        String headerString = new String(buffer.array(), buffer.position(), buffer.remaining());
        String[] headers = headerString.split(NetBareUtils.LINE_END_REGEX);
        String[] requestLine = headers[0].split(" ");
        if (requestLine.length != 3) {
            mLog.w("Unexpected http request line: " + headers[0]);
            return;
        }
        // Method
        HttpMethod method = HttpMethod.parse(requestLine[0]);
        if (method == HttpMethod.UNKNOWN) {
            mLog.w("Unknown http request method: " + requestLine[0]);
            return;
        }
        session.method = method;
        // Path
        session.path = requestLine[1];
        // Http Protocol
        HttpProtocol protocol = HttpProtocol.parse(requestLine[2]);
        if (protocol == HttpProtocol.UNKNOWN) {
            mLog.w("Unknown http request protocol: " + requestLine[0]);
            return;
        }
        session.protocol = protocol;

        // Http request headers
        if (headers.length <= 1) {
            mLog.w("Unexpected http request headers.");
            return;
        }
        for (int i = 1; i < headers.length; i++) {
            String requestHeader = headers[i];
            // Reach the header end
            if (requestHeader.isEmpty()) {
                continue;
            }
            String[] nameValue = requestHeader.split(":");
            if (nameValue.length < 2) {
                mLog.w("Unexpected http request header: " + requestHeader);
                continue;
            }
            String name = nameValue[0].trim();
            String value = requestHeader.replaceFirst(nameValue[0] + ": ", "").trim();
            List<String> header = session.requestHeaders.get(name);
            if (header == null) {
                header = new ArrayList<>(1);
                session.requestHeaders.put(name, header);
            }
            header.add(value);
        }
    }

    private void parseResponseHeader(HttpSession session, ByteBuffer buffer) {
        session.resBodyOffset = buffer.remaining();
        String headerString = new String(buffer.array(), buffer.position(), buffer.remaining());
        // In some condition, no request but has response, we set the method to unknown.
        if (session.method == null) {
            session.method = HttpMethod.UNKNOWN;
        }
        String[] headers = headerString.split(NetBareUtils.LINE_END_REGEX);
        String[] responseLine = headers[0].split(" ");
        if (responseLine.length < 2) {
            mLog.w("Unexpected http response line: " + headers[0]);
            return;
        }
        // Http Protocol
        HttpProtocol protocol = HttpProtocol.parse(responseLine[0]);
        if (protocol == HttpProtocol.UNKNOWN) {
            mLog.w("Unknown http response protocol: " + responseLine[0]);
            return;
        }
        if (session.protocol != protocol) {
            // Protocol downgrade
            if (session.protocol != null) {
                mLog.w("Unmatched http protocol, request is " + session.protocol +
                        " but response is " + responseLine[0]);
            }
            session.protocol = protocol;
        }
        // Code
        int code = NetBareUtils.parseInt(responseLine[1], -1);
        if (code == -1) {
            mLog.w("Unexpected http response code: " + responseLine[1]);
            return;
        }
        session.code = code;
        // Message
        session.message = headers[0].replaceFirst(responseLine[0], "")
                .replaceFirst(responseLine[1], "").trim();

        // Http response headers
        if (headers.length <= 1) {
            mLog.w("Unexpected http response headers.");
            return;
        }
        for (int i = 1; i < headers.length; i++) {
            String responseHeader = headers[i];
            // Reach the header end
            if (responseHeader.isEmpty()) {
                continue;
            }
            String[] nameValue = responseHeader.split(":");
            if (nameValue.length < 2) {
                mLog.w("Unexpected http response header: " + responseHeader);
                continue;
            }
            String name = nameValue[0].trim();
            String value = responseHeader.replaceFirst(nameValue[0] + ": ", "").trim();
            List<String> header = session.responseHeaders.get(name);
            if (header == null) {
                header = new ArrayList<>(1);
                session.responseHeaders.put(name, header);
            }
            header.add(value);
        }
    }

}
