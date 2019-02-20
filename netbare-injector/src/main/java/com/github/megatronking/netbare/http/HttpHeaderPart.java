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
import androidx.annotation.Nullable;

import com.github.megatronking.netbare.utils.CaseInsensitiveLinkedMap;
import com.github.megatronking.netbare.stream.Stream;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A base class of http header part, it contains the request/status line and headers.
 *
 * @author Megatron King
 * @since 2018-12-11 23:30
 */
/* package */ abstract class HttpHeaderPart implements Stream {

    private final HttpProtocol protocol;
    private final Uri uri;
    private final Map<String, List<String>> headers;

    HttpHeaderPart(@NonNull HttpProtocol protocol, @NonNull Uri uri,
                   @NonNull Map<String, List<String>> headers) {
        this.protocol = protocol;
        this.uri = uri;
        this.headers = headers;
    }

    /**
     * Get the http request uri.
     *
     * @return A {@link Uri} object.
     */
    @NonNull
    public Uri uri() {
        return this.uri;
    }

    /**
     * Get the http protocol.
     *
     * @return The http protocol.
     */
    @NonNull
    public HttpProtocol protocol() {
        return this.protocol;
    }

    /**
     * Get a collection of http headers from the header part.
     *
     * @return A map collection.
     */
    @NonNull
    public Map<String, List<String>> headers() {
        return this.headers;
    }

    /**
     * Get a list of header values by name.
     *
     * @param name The name of a header.
     * @return a list of header values.
     */
    @Nullable
    public List<String> headers(@NonNull String name) {
        return this.headers.get(name);
    }

    /**
     * Get the last value corresponding to the specified name, or null.
     *
     * @param name The name of a header.
     * @return The value of the specified header.
     */
    @Nullable
    public String header(@NonNull String name) {
        List<String> headers = headers(name);
        return headers != null ? headers.get(headers.size() - 1) : null;
    }

    /**
     * Create a new builder for this header, the new builder will do a shallow copy of this header
     * part.
     *
     * @return A Builder to generate a header part instance.
     */
    protected abstract Builder newBuilder();

    static abstract class Builder {

        HttpProtocol protocol;
        Uri uri;
        Map<String, List<String>> headers;

        Builder(HttpProtocol protocol, Uri uri, Map<String, List<String>> headers) {
            this.protocol = protocol;
            this.uri = uri;
            this.headers = headers;
        }

        Builder(HttpHeaderPart httpHeader) {
            this.protocol = httpHeader.protocol;
            this.uri = httpHeader.uri;
            this.headers = new CaseInsensitiveLinkedMap<>(httpHeader.headers);
        }

        Builder removeHeader(@NonNull String name) {
            this.headers.remove(name);
            return this;
        }

        Builder removeHeader(@NonNull String name, int index) {
            List<String> header = headers.get(name);
            if (header != null) {
                if (header.size() > index) {
                    header.remove(index);
                }
            }
            if (header == null || header.isEmpty()) {
                headers.remove(name);
            }
            return this;
        }

        Builder replaceHeader(@NonNull String name, @NonNull String value) {
            List<String> header = headers.get(name);
            if (header == null) {
                header = new ArrayList<>(1);
                headers.put(name, header);
            } else {
                header.clear();
            }
            header.add(value);
            return this;
        }

        Builder updateHeader(@NonNull String name, @NonNull String value, int index) {
            List<String> header = headers.get(name);
            if (header == null) {
                header = new ArrayList<>(1);
                headers.put(name, header);
            }
            if (header.size() > index) {
                header.remove(index);
                header.add(index, value);
            }
            return this;
        }

        Builder updateOrAddHeader(@NonNull String name, @NonNull String value, int index) {
            List<String> header = headers.get(name);
            if (header == null) {
                header = new ArrayList<>(1);
                headers.put(name, header);
            }
            if (header.size() > index) {
                header.remove(index);
                header.add(index, value);
            } else {
                header.add(value);
            }
            return this;
        }

        Builder addHeader(@NonNull String name, @NonNull String value) {
            List<String> header = headers.get(name);
            if (header == null) {
                header = new ArrayList<>(1);
                headers.put(name, header);
            }
            header.add(value);
            return this;
        }

        Builder removeHeaders() {
            headers.clear();
            return this;
        }

        abstract HttpHeaderPart build();

    }

}
