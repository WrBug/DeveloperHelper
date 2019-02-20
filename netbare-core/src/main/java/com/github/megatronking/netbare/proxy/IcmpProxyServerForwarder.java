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

import com.github.megatronking.netbare.NetBareLog;
import com.github.megatronking.netbare.ip.IcmpHeader;
import com.github.megatronking.netbare.ip.IpHeader;

import java.io.OutputStream;

/**
 * Forward the Internet Control Message Protocol (ICMP) to proxy server.
 *
 * @author Megatron King
 * @since 2018-10-09 01:30
 */
public final class IcmpProxyServerForwarder implements ProxyServerForwarder {

    @Override
    public void prepare() {
        // TODO
    }

    @Override
    public void forward(byte[] packet, int len, OutputStream output) {
        IpHeader ipHeader = new IpHeader(packet, 0);
        IcmpHeader icmpHeader = new IcmpHeader(ipHeader, packet, ipHeader.getHeaderLength());
        NetBareLog.v("ICMP type: " + icmpHeader.getType());
        NetBareLog.v("ICMP code: " + icmpHeader.getCode());
        // TODO transfer to proxy server
    }

    @Override
    public void release() {
        // TODO
    }

}
