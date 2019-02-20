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

import com.github.megatronking.netbare.http2.Http2Settings;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides HTTP protocol session information.
 *
 * @author Megatron King
 * @since 2018-11-10 11:56
 */
/* package */ class HttpSession {

    boolean isHttps;
    HttpProtocol protocol;
    HttpMethod method;
    String path;
    Map<String, List<String>> requestHeaders = new LinkedHashMap<>();
    Map<String, List<String>> responseHeaders = new LinkedHashMap<>();
    int code;
    String message;
    int reqBodyOffset;
    int resBodyOffset;
    // Belows is for HTTP2
    Http2Settings clientHttp2Settings;
    Http2Settings peerHttp2Settings;
    boolean requestStreamEnd;
    boolean responseStreamEnd;

}
