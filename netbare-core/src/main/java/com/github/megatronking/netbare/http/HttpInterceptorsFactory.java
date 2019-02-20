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

import java.util.List;

/**
 * Factory creates a collection {@link HttpInterceptor}s.
 *
 * @author Megatron King
 * @since 2018-11-15 21:58
 */
/* package */ interface HttpInterceptorsFactory {

    /**
     * Creates a collection of http interceptor instances and immediately returns it,
     * it must not be null.
     *
     * @return A http interceptor list.
     */
    @NonNull
    List<HttpInterceptor> create();

}
