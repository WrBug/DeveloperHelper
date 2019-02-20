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

import com.github.megatronking.netbare.gateway.Request;
import com.github.megatronking.netbare.http2.Http2Settings;
import com.github.megatronking.netbare.http2.Http2Updater;

import java.util.HashMap;
import java.util.Map;

/**
 * A zygote http request class, it creates the real http request instance.
 *
 * @author Megatron King
 * @since 2019/1/6 17:00
 */
public class HttpZygoteRequest extends HttpRequest implements Http2Updater {

    private final Request mRequest;
    private final HttpSessionFactory mSessionFactory;
    private final Map<String, HttpRequest> mCachedRequests;

    private HttpRequest mActiveRequest;

    /* package */ HttpZygoteRequest(Request request, HttpSessionFactory factory) {
        super(request, factory.create(request.id()));
        this.mRequest = request;
        this.mSessionFactory = factory;
        this.mCachedRequests = new HashMap<>();
    }

    public void zygote(HttpId id) {
        if (mCachedRequests.containsKey(id.id)) {
            mActiveRequest = mCachedRequests.get(id.id);
        } else {
            HttpSession originSession = session();
            HttpSession session = mSessionFactory.create(id.id);
            session.isHttps = originSession.isHttps;
            session.protocol = originSession.protocol;
            session.clientHttp2Settings = originSession.clientHttp2Settings;
            session.peerHttp2Settings = originSession.peerHttp2Settings;
            HttpRequest request = new HttpRequest(mRequest, id, session);
            mCachedRequests.put(id.id, request);
            mActiveRequest = request;
        }
    }

    @Override
    public void onSettingsUpdate(Http2Settings http2Settings) {
        session().clientHttp2Settings = http2Settings;
    }

    @Override
    public void onStreamFinished() {
        HttpRequest request = getActive();
        if (request != null) {
            request.session().requestStreamEnd = true;
        }
    }

    HttpRequest getActive() {
        return mActiveRequest;
    }

}
