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

/**
 * HTTP defines a set of request methods to indicate the desired action to be performed for a given
 * resource.
 *
 * See https://tools.ietf.org/html/rfc7231#section-4
 *
 * @author Megatron King
 * @since 2018-10-15 19:59
 */
public enum HttpMethod {

    /**
     * It means NetBare does not know the method.
     */
    UNKNOWN,

    /**
     * The GET method requests a representation of the specified resource. Requests using GET
     * should only retrieve data.
     */
    GET,

    /**
     * The HEAD method asks for a response identical to that of a GET request, but without the
     * response body.
     */
    HEAD,

    /**
     * The POST method is used to submit an entity to the specified resource, often causing
     * a change in state or side effects on the server.
     */
    POST,

    /**
     * The PUT method replaces all current representations of the target resource with the request
     * payload.
     */
    PUT,

    /**
     * The DELETE method deletes the specified resource.
     */
    DELETE,

    /**
     * The CONNECT method establishes a tunnel to the server identified by the target resource.
     */
    CONNECT,

    /**
     * The OPTIONS method is used to describe the communication options for the target resource.
     */
    OPTIONS,

    /**
     * The TRACE method performs a message loop-back test along the path to the target resource.
     */
    TRACE,

    /**
     * The PATCH method is used to apply partial modifications to a resource.
     */
    PATCH;

    /**
     * Returns the request method enum.
     *
     * @param methodValue A string method presents in request line.
     * @return A HttpMethod enum.
     */
    @NonNull
    public static HttpMethod parse(@NonNull String methodValue) {
        for (HttpMethod method : values()) {
            if (method.name().equals(methodValue)) {
                return method;
            }
        }
        return UNKNOWN;
    }

}
