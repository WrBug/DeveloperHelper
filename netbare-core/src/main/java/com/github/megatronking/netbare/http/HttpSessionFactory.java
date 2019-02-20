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

import java.util.HashMap;
import java.util.Map;

/**
 * A factory creates {@link HttpSession} instance by id.
 *
 * @author Megatron King
 * @since 2019/1/6 19:42
 */
/* package */ class HttpSessionFactory {

    private final Map<String, HttpSession> mHttpSession;

    /* package */ HttpSessionFactory() {
        mHttpSession = new HashMap<>(1);
    }

    HttpSession create(String id) {
        HttpSession httpSession;
        if (mHttpSession.containsKey(id)) {
            httpSession = mHttpSession.get(id);
        } else {
            httpSession = new HttpSession();
            mHttpSession.put(id, httpSession);
        }
        return httpSession;
    }

}
