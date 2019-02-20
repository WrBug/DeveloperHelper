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

import com.github.megatronking.netbare.gateway.Response;
import com.github.megatronking.netbare.http2.Http2Settings;
import com.github.megatronking.netbare.http2.Http2Updater;

import java.util.HashMap;
import java.util.Map;

/**
 * A zygote http response class, it creates the real http response instance.
 *
 * @author Megatron King
 * @since 2019/1/6 17:09
 */
public class HttpZygoteResponse extends HttpResponse implements Http2Updater {

    private final Response mResponse;
    private final HttpSessionFactory mSessionFactory;
    private final Map<String, HttpResponse> mCachedResponses;

    private HttpResponse mActiveResponse;

    /* package */ HttpZygoteResponse(Response response, HttpSessionFactory factory) {
        super(response, factory.create(response.id()));
        this.mResponse = response;
        this.mSessionFactory = factory;
        this.mCachedResponses = new HashMap<>();
    }

    public void zygote(HttpId id) {
        if (mCachedResponses.containsKey(id.id)) {
            mActiveResponse = mCachedResponses.get(id.id);
        } else {
            HttpSession originSession = super.session();
            HttpSession session = mSessionFactory.create(id.id);
            session.isHttps = originSession.isHttps;
            session.protocol = originSession.protocol;
            session.clientHttp2Settings = originSession.clientHttp2Settings;
            session.peerHttp2Settings = originSession.peerHttp2Settings;
            HttpResponse response = new HttpResponse(mResponse, id, session);
            mCachedResponses.put(id.id, response);
            mActiveResponse = response;
        }
    }

    @Override
    public void onSettingsUpdate(Http2Settings http2Settings) {
        session().peerHttp2Settings = http2Settings;
    }

    @Override
    public void onStreamFinished() {
        HttpResponse response = getActive();
        if (response != null) {
            response.session().responseStreamEnd = true;
        }
    }

    HttpResponse getActive() {
        return mActiveResponse;
    }

}

