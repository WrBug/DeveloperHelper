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

import android.net.Uri;
import androidx.annotation.NonNull;

import com.github.megatronking.netbare.NetBareUtils;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

/**
 * The http request header part, it contains the request line and request headers.
 *
 * @author Megatron King
 * @since 2018-12-11 23:59
 */
public final class HttpRequestHeaderPart extends HttpHeaderPart {

    private final HttpMethod method;

    HttpRequestHeaderPart(HttpProtocol protocol, HttpMethod method, Uri uri,
                          Map<String, List<String>> headers) {
        super(protocol, uri, headers);
        this.method = method;
    }

    /**
     * Get the request method from the header part.
     *
     * @return The request method.
     */
    public HttpMethod method() {
        return this.method;
    }

    /**
     * Get the request path from the header part.
     *
     * @return The request path.
     */
    public String path() {
        String path = uri().getPath();
        int pathIndex = uri().toString().indexOf(path);
        return uri().toString().substring(pathIndex);
    }

    @Override
    public Builder newBuilder() {
        return new Builder(this);
    }

    /**
     * Builder class for {@link HttpRequestHeaderPart}.
     */
    public static class Builder extends HttpHeaderPart.Builder {

        private HttpMethod method;

        Builder(HttpProtocol protocol, Uri uri, Map<String, List<String>> headers,
                       HttpMethod method) {
            super(protocol, uri, headers);
            this.method = method;
        }

        Builder(HttpRequestHeaderPart requestHeader) {
            super(requestHeader);
            this.method = requestHeader.method;
        }

        /**
         * Set a new http request method.
         *
         * @param method The http request method.
         * @return The same builder.
         */
        public Builder method(@NonNull HttpMethod method) {
            this.method = method;
            return this;
        }

        /**
         * Set a new http request uri.
         *
         * @param uri The http request uri.
         * @return The same builder.
         */
        public Builder uri(@NonNull Uri uri) {
            this.uri = uri;
            return this;
        }

        /**
         * Add a http header.
         *
         * @param name The name of the header.
         * @param value The value of the header.
         * @return The same builder.
         */
        @Override
        public Builder addHeader(@NonNull String name, @NonNull String value) {
            return (Builder) super.addHeader(name, value);
        }

        /**
         * Replace the http header by name. If the name has multiple values, the values will be all
         * removed.
         *
         * @param name The name of the header.
         * @param value The value of the header.
         * @return The same builder.
         */
        @Override
        public Builder replaceHeader(@NonNull String name, @NonNull String value) {
            return (Builder) super.replaceHeader(name, value);
        }

        /**
         * Update the http header by name and index. If the index of values not exists, then do
         * nothing.
         *
         * @param name The name of the header.
         * @param value The value of the header.
         * @param index The value index in list.
         * @return The same builder.
         *
         * @see #updateOrAddHeader(String, String, int)
         */
        @Override
        public Builder updateHeader(@NonNull String name, @NonNull String value, int index) {
            return (Builder) super.updateHeader(name, value, index);
        }

        /**
         * Update the http header by name and index. If the index of values not exists, then append
         * a new header.
         *
         * @param name The name of the header.
         * @param value The value of the header.
         * @param index The value index in list.
         * @return The same builder.
         *
         * @see #updateHeader(String, String, int)
         */
        @Override
        public Builder updateOrAddHeader(@NonNull String name, @NonNull String value, int index) {
            return (Builder) super.updateOrAddHeader(name, value, index);
        }

        /**
         * Remove the http header by name. If the name has multiple values, the values will be all
         * removed.
         *
         * @param name The name of the header.
         * @return The same builder.
         */
        @Override
        public Builder removeHeader(@NonNull String name) {
            return (Builder) super.removeHeader(name);
        }

        /**
         * Remove the http header by name and index. If the index of values not exists, then do
         * nothing.
         *
         * @param name The name of the header.
         * @return The same builder.
         */
        @Override
        public Builder removeHeader(@NonNull String name, int index) {
            return (Builder) super.removeHeader(name, index);
        }

        /**
         * Remove all http headers.
         *
         * @return The same builder.
         */
        @Override
        public Builder removeHeaders() {
            return (Builder) super.removeHeaders();
        }

        /**
         * Build a {@link HttpRequestHeaderPart} instance.
         *
         * @return The instance.
         */
        @Override
        public HttpRequestHeaderPart build() {
            return new HttpRequestHeaderPart(protocol, method, uri, headers);
        }

    }

    @NonNull
    @Override
    public ByteBuffer toBuffer() {
        StringBuilder builder = new StringBuilder();
        builder.append(method.name());
        builder.append(" ");
        builder.append(path());
        builder.append(" ");
        builder.append(protocol().toString());
        builder.append(NetBareUtils.LINE_END);
        for (Map.Entry<String, List<String>> entry : headers().entrySet()) {
            for (String value : entry.getValue()) {
                builder.append(entry.getKey());
                builder.append(": ");
                builder.append(value);
                builder.append(NetBareUtils.LINE_END);
            }
        }
        builder.append(NetBareUtils.LINE_END);
        return ByteBuffer.wrap(builder.toString().getBytes());
    }

}
