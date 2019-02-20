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
/*
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.megatronking.netbare.http2;

/**
 * http://tools.ietf.org/html/draft-ietf-httpbis-http2-17#section-7
 *
 * @author Megatron King
 * @since 2019/1/11 23:10
 */
/* package */ enum ErrorCode {

    /**
     * Not an error!
     */
    NO_ERROR(0),

    PROTOCOL_ERROR(1),

    INTERNAL_ERROR(2),

    FLOW_CONTROL_ERROR(3),

    SETTINGS_TIMEOUT(4),

    STREAM_CLOSED(5),

    FRAME_SIZE_ERROR(6),

    REFUSED_STREAM(7),

    CANCEL(8),

    COMPRESSION_ERROR(9),

    CONNECT_ERROR(0xa),

    ENHANCE_YOUR_CALM(0xb),

    INADEQUATE_SECURITY(0xc),

    HTTP_1_1_REQUIRED(0xd);

    public final int httpCode;

    ErrorCode(int httpCode) {
        this.httpCode = httpCode;
    }

    /* package */ static ErrorCode fromHttp2(int code) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.httpCode == code) {
                return errorCode;
            }
        }
        return null;
    }

}
