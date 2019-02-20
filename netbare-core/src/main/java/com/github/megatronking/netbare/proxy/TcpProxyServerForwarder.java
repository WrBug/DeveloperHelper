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
package com.github.megatronking.netbare.proxy;

import android.net.VpnService;

import com.github.megatronking.netbare.NetBareLog;
import com.github.megatronking.netbare.NetBareUtils;
import com.github.megatronking.netbare.ip.IpHeader;
import com.github.megatronking.netbare.ip.Protocol;
import com.github.megatronking.netbare.ip.TcpHeader;
import com.github.megatronking.netbare.net.Session;
import com.github.megatronking.netbare.net.SessionProvider;
import com.github.megatronking.netbare.net.UidDumper;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Handshake with local TCP proxy server and then forward packets to it.
 *
 * @author Megatron King
 * @since 2018-10-09 01:30
 */
public final class TcpProxyServerForwarder implements ProxyServerForwarder {

    private final SessionProvider mSessionProvider;
    private final TcpProxyServer mProxyServer;

    public TcpProxyServerForwarder(VpnService vpnService, String ip, int mtu,
                                   UidDumper dumper) throws IOException {
        this.mSessionProvider = new SessionProvider(dumper);
        this.mProxyServer = new TcpProxyServer(vpnService, ip, mtu);
        this.mProxyServer.setSessionProvider(mSessionProvider);
    }

    @Override
    public void prepare() {
        this.mProxyServer.start();
    }

    @Override
    public void forward(byte[] packet, int len, OutputStream output) {
        IpHeader ipHeader = new IpHeader(packet, 0);
        TcpHeader tcpHeader = new TcpHeader(ipHeader, packet, ipHeader.getHeaderLength());

        // Src IP & Port
        int localIp = ipHeader.getSourceIp();
        short localPort = tcpHeader.getSourcePort();

        // Dest IP & Port
        int remoteIp = ipHeader.getDestinationIp();
        short remotePort = tcpHeader.getDestinationPort();

        // TCP data size
        int tcpDataSize = ipHeader.getDataLength() - tcpHeader.getHeaderLength();

        NetBareLog.v("ip: %s:%d -> %s:%d", NetBareUtils.convertIp(localIp),
                NetBareUtils.convertPort(localPort), NetBareUtils.convertIp(remoteIp),
                NetBareUtils.convertPort(remotePort));
        NetBareLog.v("tcp: %s, size: %d", tcpHeader.toString(), tcpDataSize);

        // Tcp handshakes and proxy forward flow.

        // Client: 10.1.10.1:40988
        // Server: 182.254.116.117:80
        // Proxy Server: 10.1.10.1:38283

        // 10.1.10.1:40988 -> 182.254.116.117:80 SYN
        // Forward: 182.254.116.117:40988 -> 10.1.10.1:38283 SYN

        // 10.1.10.1:38283 -> 182.254.116.117:40988 SYN+ACK
        // Forward: 182.254.116.117:80 -> 10.1.10.1:40988 SYN+ACK

        // 10.1.10.1:40988 -> 182.254.116.117:80 ACK
        // Forward: 182.254.116.117:80 -> 10.1.10.1:38283 ACK

        if (localPort != mProxyServer.getPort()) {
            // Client requests to server
            Session session = mSessionProvider.ensureQuery(Protocol.TCP, localPort, remotePort, remoteIp);
            session.packetIndex++;

            // Forward client request to proxy server.
            ipHeader.setSourceIp(remoteIp);
            ipHeader.setDestinationIp(mProxyServer.getIp());
            tcpHeader.setDestinationPort(mProxyServer.getPort());

            ipHeader.updateChecksum();
            tcpHeader.updateChecksum();

            session.sendDataSize += tcpDataSize;
        } else {
            // Proxy server responses forward client request.
            Session session = mSessionProvider.query(remotePort);
            if (session == null) {
                NetBareLog.w("No session saved with key: " + remotePort);
                return;
            }
            // Forward proxy server response to client.
            ipHeader.setSourceIp(remoteIp);
            ipHeader.setDestinationIp(mProxyServer.getIp());
            tcpHeader.setSourcePort(session.remotePort);

            ipHeader.updateChecksum();
            tcpHeader.updateChecksum();

            session.receiveDataSize += tcpDataSize;
        }

        try {
            output.write(packet, 0, len);
        } catch (IOException e) {
            NetBareLog.e(e.getMessage());
        }
    }

    @Override
    public void release() {
        this.mProxyServer.stop();
    }

}
