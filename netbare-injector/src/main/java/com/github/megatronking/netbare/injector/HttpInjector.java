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
package com.github.megatronking.netbare.injector;

import androidx.annotation.NonNull;

import com.github.megatronking.netbare.http.HttpBody;
import com.github.megatronking.netbare.http.HttpRequest;
import com.github.megatronking.netbare.http.HttpRequestHeaderPart;
import com.github.megatronking.netbare.http.HttpResponse;
import com.github.megatronking.netbare.http.HttpResponseHeaderPart;
import com.github.megatronking.netbare.stream.Stream;

import java.io.IOException;

/**
 * An injector for HTTP protocol packets, you can block and modify HTTP headers and bodies in this
 * injector.
 * <p>
 * Remember do not hold those methods for a long time.
 * </p>
 *
 * @author Megatron King
 * @since 2018-12-15 21:50
 */
public interface HttpInjector {

    /**
     * Determine should the injector apply to this request.
     *
     * @param request Http request session.
     * @return True if do injection to this request.
     */
    boolean sniffRequest(@NonNull HttpRequest request);

    /**
     * Determine should the injector apply to this response.
     *
     * @param response Http response session.
     * @return True if do injection to this response.
     */
    boolean sniffResponse(@NonNull HttpResponse response);

    /**
     * Inject the http request header part, call {@link InjectorCallback#onFinished(Stream)} after
     * the injection.
     *
     * @param header Http header part.
     * @param callback A injection finish callback.
     * @throws IOException If an I/O error has occurred.
     */
    void onRequestInject(@NonNull HttpRequestHeaderPart header, @NonNull InjectorCallback callback)
            throws IOException;

    /**
     * Inject the http response header part, call {@link InjectorCallback#onFinished(Stream)} after
     * the injection.
     *
     * @param header Http header part.
     * @param callback A injection finish callback.
     * @throws IOException If an I/O error has occurred.
     */
    void onResponseInject(@NonNull HttpResponseHeaderPart header, @NonNull InjectorCallback callback)
            throws IOException;

    /**
     * Inject the http request body part, call {@link InjectorCallback#onFinished(Stream)} after
     * the injection.
     *
     * @param body Http body part.
     * @param callback A injection finish callback.
     * @throws IOException If an I/O error has occurred.
     */
    void onRequestInject(@NonNull HttpRequest request, @NonNull HttpBody body,
                         @NonNull InjectorCallback callback) throws IOException;

    /**
     * Inject the http response body part, call {@link InjectorCallback#onFinished(Stream)} after
     * the injection.
     *
     * @param body Http body part.
     * @param callback A injection finish callback.
     * @throws IOException If an I/O error has occurred.
     */
    void onResponseInject(@NonNull HttpResponse response, @NonNull HttpBody body,
                          @NonNull InjectorCallback callback) throws IOException;

    /**
     * Notify the injector that the request is finished.
     *
     * @param request Http request session.
     */
    void onRequestFinished(@NonNull HttpRequest request);

    /**
     * Notify the injector that the response is finished.
     *
     * @param response Http response session.
     */
    void onResponseFinished(@NonNull HttpResponse response);

}
