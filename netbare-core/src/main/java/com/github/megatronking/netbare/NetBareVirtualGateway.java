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
package com.github.megatronking.netbare;

import android.os.Process;

import com.github.megatronking.netbare.gateway.Request;
import com.github.megatronking.netbare.gateway.Response;
import com.github.megatronking.netbare.gateway.VirtualGateway;
import com.github.megatronking.netbare.ip.Protocol;
import com.github.megatronking.netbare.net.Session;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

/**
 * The main virtual gateway used in proxy servers, it wraps the actual virtual gateway. We use this
 * class to do some internal verifications.
 *
 * @author Megatron King
 * @since 2018-11-17 23:10
 */
public final class NetBareVirtualGateway extends VirtualGateway {

    /**
     * Policy is indeterminate, we should resolve the policy before process data.
     */
    private static final int POLICY_INDETERMINATE = 0;

    /**
     * This policy allows data flow to configured virtual gateway.
     */
    private static final int POLICY_ALLOWED = 1;

    /**
     * This policy doesn't allow data flow to configured virtual gateway.
     */
    private static final int POLICY_DISALLOWED = 2;

    private final VirtualGateway mGateway;
    private final Session mSession;
    private final NetBareXLog mLog;

    private int mPolicy;

    private boolean mRequestFinished;
    private boolean mResponseFinished;

    public NetBareVirtualGateway(Session session, Request request, Response response) {
        super(session, request, response);
        mGateway = NetBare.get().getGatewayFactory().create(session, request, response);
        mSession = session;
        mLog = new NetBareXLog(session);

        NetBareConfig config = NetBare.get().getConfig();
        if (config == null || (config.excludeSelf && session.uid == Process.myUid())) {
            // Exclude the app itself.
            mLog.w("Exclude an app-self connection!");
            mPolicy = POLICY_DISALLOWED;
        } else {
            mPolicy = POLICY_INDETERMINATE;
        }
    }

    @Override
    public void sendRequest(ByteBuffer buffer) throws IOException {
        if (mRequestFinished) {
            mLog.w("Drop a buffer due to request has finished.");
            return;
        }
        resolvePolicyIfNecessary(buffer);
        if (mPolicy == POLICY_ALLOWED) {
            mGateway.sendRequest(buffer);
        } else if (mPolicy == POLICY_DISALLOWED) {
            super.sendRequest(buffer);
        }
    }

    @Override
    public void sendResponse(ByteBuffer buffer) throws IOException {
        if (mResponseFinished) {
            mLog.w("Drop a buffer due to response has finished.");
            return;
        }
        resolvePolicyIfNecessary(buffer);
        if (mPolicy == POLICY_ALLOWED) {
            mGateway.sendResponse(buffer);
        } else if (mPolicy == POLICY_DISALLOWED) {
            super.sendResponse(buffer);
        }
    }

    @Override
    public void sendRequestFinished() {
        if (mRequestFinished) {
            return;
        }
        mLog.i("Gateway request finished!");
        mRequestFinished = true;
        if (mPolicy == POLICY_ALLOWED) {
            mGateway.sendRequestFinished();
        } else if (mPolicy == POLICY_DISALLOWED) {
            super.sendRequestFinished();
        }
    }

    @Override
    public void sendResponseFinished() {
        if (mResponseFinished) {
            return;
        }
        mLog.i("Gateway response finished!");
        mResponseFinished = true;
        if (mPolicy == POLICY_ALLOWED) {
            mGateway.sendResponseFinished();
        } else if (mPolicy == POLICY_DISALLOWED) {
            super.sendResponseFinished();
        }
    }

    private void resolvePolicyIfNecessary(ByteBuffer buffer) {
        if (mPolicy != POLICY_INDETERMINATE) {
            // Resolved.
            return;
        }
        if (!buffer.hasRemaining()) {
            // Invalid buffer remaining, do nothing.
            return;
        }
        if (mSession.protocol != Protocol.TCP) {
            mPolicy = POLICY_ALLOWED;
            return;
        }

        // Now we verify the TCP protocol host
        String domain;
        if (isHttp(buffer)) {
            domain = parseHttpHost(buffer.array(), buffer.position(), buffer.remaining());
        } else {
            domain = parseHttpsHost(buffer.array(), buffer.position(), buffer.remaining());
        }
        if (domain == null) {
            // Maybe not http protocol.
            mPolicy = POLICY_ALLOWED;
            return;
        } else {
            mSession.host = domain;
        }
        NetBareConfig config = NetBare.get().getConfig();
        Set<String> allowedHost = new HashSet<>(config.allowedHosts);
        Set<String> disallowedHost = new HashSet<>(config.disallowedHosts);

        boolean isAllowedHostEmpty = allowedHost.isEmpty();
        boolean isDisallowedHostEmpty = disallowedHost.isEmpty();

        if (isAllowedHostEmpty && isDisallowedHostEmpty) {
            // No white and black list, it means allow everything.
            mPolicy = POLICY_ALLOWED;
            return;
        }

        if (!isDisallowedHostEmpty) {
            // Check domain hosts.
            for (String host : disallowedHost) {
                if (host.equals(domain)) {
                    // Denied host.
                    mPolicy = POLICY_DISALLOWED;
                    return;
                }
            }
            // Check ip hosts.
            for (String host : disallowedHost) {
                if (host.equals(NetBareUtils.convertIp(mSession.remoteIp))) {
                    // Denied host.
                    mPolicy = POLICY_DISALLOWED;
                    return;
                }
            }
        }

        if (!isAllowedHostEmpty) {
            for (String host : allowedHost) {
                if (host.equals(domain)) {
                    mPolicy = POLICY_ALLOWED;
                    return;
                }
            }
            for (String host : allowedHost) {
                if (host.equals(NetBareUtils.convertIp(mSession.remoteIp))) {
                    mPolicy = POLICY_ALLOWED;
                    return;
                }
            }
            mPolicy = POLICY_DISALLOWED;
        } else {
            mPolicy = POLICY_ALLOWED;
        }
    }

    private boolean isHttp(ByteBuffer buffer) {
        switch (buffer.get(buffer.position())) {
            // HTTP methods.
            case 'G':
            case 'H':
            case 'P':
            case 'D':
            case 'O':
            case 'T':
            case 'C':
                return true;
            default:
                // Unknown first byte data.
                break;
        }
        return false;
    }

    private String parseHttpHost(byte[] buffer, int offset, int size) {
        String header = new String(buffer, offset, size);
        String[] headers = header.split(NetBareUtils.LINE_END_REGEX);
        if (headers.length <= 1) {
            return null;
        }
        for (int i = 1; i < headers.length; i++) {
            String requestHeader = headers[i];
            // Reach the header end
            if (requestHeader.isEmpty()) {
                return null;
            }
            String[] nameValue = requestHeader.split(":");
            if (nameValue.length < 2) {
                return null;
            }
            String name = nameValue[0].trim();
            String value = requestHeader.replaceFirst(nameValue[0] + ": ", "").trim();
            if (name.toLowerCase().equals("host")) {
                return value;
            }
        }
        return null;
    }

    private String parseHttpsHost(byte[] buffer, int offset, int size) {
        int limit = offset + size;
        // Client Hello
        if (size <= 43 || buffer[offset] != 0x16) {
            mLog.w("Failed to get host from SNI: Bad ssl packet.");
            return null;
        }
        // Skip 43 byte header
        offset += 43;

        // Read sessionID
        if (offset + 1 > limit) {
            mLog.w("Failed to get host from SNI: No session id.");
            return null;
        }
        int sessionIDLength = buffer[offset++] & 0xFF;
        offset += sessionIDLength;

        // Read cipher suites
        if (offset + 2 > limit) {
            mLog.w("Failed to get host from SNI: No cipher suites.");
            return null;
        }

        int cipherSuitesLength = readShort(buffer, offset) & 0xFFFF;
        offset += 2;
        offset += cipherSuitesLength;

        // Read Compression method.
        if (offset + 1 > limit) {
            mLog.w("Failed to get host from SNI: No compression method.");
            return null;
        }
        int compressionMethodLength = buffer[offset++] & 0xFF;
        offset += compressionMethodLength;

        // Read Extensions
        if (offset + 2 > limit) {
            mLog.w("Failed to get host from SNI: no extensions.");
            return null;
        }
        int extensionsLength = readShort(buffer, offset) & 0xFFFF;
        offset += 2;

        if (offset + extensionsLength > limit) {
            mLog.w("Failed to get host from SNI: no sni.");
            return null;
        }

        while (offset + 4 <= limit) {
            int type0 = buffer[offset++] & 0xFF;
            int type1 = buffer[offset++] & 0xFF;
            int length = readShort(buffer, offset) & 0xFFFF;
            offset += 2;
            // Got the SNI info
            if (type0 == 0x00 && type1 == 0x00 && length > 5) {
                offset += 5;
                length -= 5;
                if (offset + length > limit) {
                    return null;
                }
                return new String(buffer, offset, length);
            } else {
                offset += length;
            }

        }
        mLog.w("Failed to get host from SNI: no host.");
        return null;
    }

    private short readShort(byte[] data, int offset) {
        int r = ((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF);
        return (short) r;
    }

}
