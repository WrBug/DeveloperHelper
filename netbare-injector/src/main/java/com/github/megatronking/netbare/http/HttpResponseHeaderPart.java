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
 * The http response header part, it contains the status line and response headers.
 *
 * @author Megatron King
 * @since 2018-12-11 23:43
 */
public final class HttpResponseHeaderPart extends HttpHeaderPart {

    private final int code;
    private final String message;

    HttpResponseHeaderPart(int code, String message, HttpProtocol protocol, Uri uri,
                           Map<String, List<String>> headers) {
        super(protocol, uri, headers);
        this.code = code;
        this.message = message;
    }

    /**
     * Get the response code from the header part.
     *
     * @return The response code.
     */
    public int code() {
        return this.code;
    }

    /**
     * Get the response message from the header part.
     *
     * @return The response message.
     */
    public String message() {
        return this.message;
    }

    @Override
    public Builder newBuilder() {
        return new Builder(this);
    }

    /**
     * Builder class for {@link HttpResponseHeaderPart}.
     */
    public static class Builder extends HttpHeaderPart.Builder {

        private int code;
        private String message;

        Builder(HttpProtocol protocol, Uri uri, Map<String, List<String>> headers,
                       int code, String message) {
            super(protocol, uri, headers);
            this.code = code;
            this.message = message;
        }

        Builder(HttpResponseHeaderPart responseHeader) {
            super(responseHeader);
            this.code = responseHeader.code;
            this.message = responseHeader.message;
        }

        /**
         * Set a new http response code.
         *
         * @param code The http response code.
         * @return The same builder.
         */
        public Builder code(int code) {
            this.code = code;
            return this;
        }

        /**
         * Set a new http response message, the message should match with the response code.
         *
         * @param message The http response message.
         * @return The same builder.
         */
        public Builder message(@NonNull String message) {
            if (message.contains(NetBareUtils.LINE_END)) {
                throw new IllegalArgumentException("The message contains line end symbol.");
            }
            this.message = message;
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
         * Build a {@link HttpResponseHeaderPart} instance.
         *
         * @return The instance.
         */
        @Override
        public HttpResponseHeaderPart build() {
            return new HttpResponseHeaderPart(code, message, protocol, uri, headers);
        }

    }

    @NonNull
    @Override
    public ByteBuffer toBuffer() {
        StringBuilder builder = new StringBuilder();
        builder.append(protocol().toString());
        builder.append(" ");
        builder.append(code());
        builder.append(" ");
        builder.append(message());
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
