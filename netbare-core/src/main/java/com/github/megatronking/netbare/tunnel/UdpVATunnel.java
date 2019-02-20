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
package com.github.megatronking.netbare.tunnel;

import com.github.megatronking.netbare.NetBareLog;
import com.github.megatronking.netbare.NetBareUtils;
import com.github.megatronking.netbare.NetBareVirtualGateway;
import com.github.megatronking.netbare.gateway.Request;
import com.github.megatronking.netbare.gateway.Response;
import com.github.megatronking.netbare.gateway.VirtualGateway;
import com.github.megatronking.netbare.ip.IpHeader;
import com.github.megatronking.netbare.ip.UdpHeader;
import com.github.megatronking.netbare.net.Session;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * UDP protocol virtual gateway tunnel wraps {@link UdpRemoteTunnel} and itself as client and
 * server.
 *
 * @author Megatron King
 * @since 2018-11-25 20:16
 */
public class UdpVATunnel extends VirtualGatewayTunnel implements NioCallback,
        Tunnel {

    private final NioTunnel mRemoteTunnel;
    private final OutputStream mOutput;

    private final int mMtu;

    private Session mSession;
    private VirtualGateway mGateway;

    private UdpHeader mTemplateHeader;

    public UdpVATunnel(Session session, NioTunnel tunnel, OutputStream output, int mtu) {
        this.mRemoteTunnel = tunnel;
        this.mOutput = output;
        this.mMtu = mtu;

        this.mSession = session;
        this.mGateway = new NetBareVirtualGateway(session,
                new Request(mRemoteTunnel), new Response(this));

        this.mRemoteTunnel.setNioCallback(this);
    }

    @Override
    public void connect(InetSocketAddress address) throws IOException {
        mRemoteTunnel.connect(address);
    }

    @Override
    public VirtualGateway getGateway() {
        return mGateway;
    }

    @Override
    public void onConnected() {
    }

    @Override
    public void onRead() throws IOException {
        if (mRemoteTunnel.isClosed()) {
            mGateway.sendRequestFinished();
            mGateway.sendResponseFinished();
            return;
        }
        ByteBuffer buffer = ByteBuffer.allocate(mMtu);
        int len;
        try {
            len = mRemoteTunnel.read(buffer);
        } catch (IOException e) {
            throw new ConnectionShutdownException(e.getMessage());
        }
        if (len < 0) {
            close();
            return;
        }
        mGateway.sendResponse(buffer);
    }

    @Override
    public void onWrite() {
    }

    @Override
    public void onClosed() {
        close();
    }

    @Override
    public NioTunnel getTunnel() {
        return null;
    }

    @Override
    public void close() {
        NetBareUtils.closeQuietly(mRemoteTunnel);
        mGateway.sendRequestFinished();
        mGateway.sendResponseFinished();
    }

    public void send(UdpHeader header) {
        if (mRemoteTunnel.isClosed()) {
            return;
        }
        // Clone a template by the send data.
        if (mTemplateHeader == null) {
            mTemplateHeader = createTemplate(header);
        }

        try {
            mGateway.sendRequest(header.data());
        } catch (IOException e) {
            NetBareLog.e(e.getMessage());
            close();
        }
    }

    @Override
    public void write(ByteBuffer buffer) throws IOException {
        // Write to vpn.
        UdpHeader header = mTemplateHeader.copy();
        ByteBuffer headerBuffer = header.buffer();
        int headLength = header.getIpHeader().getHeaderLength() + header.getHeaderLength();
        byte[] packet = new byte[headLength + buffer.remaining()];
        headerBuffer.get(packet, 0, headLength);
        buffer.get(packet, headLength, packet.length - headLength);

        IpHeader ipHeader = new IpHeader(packet, 0);
        ipHeader.setTotalLength((short) packet.length);

        UdpHeader udpHeader = new UdpHeader(ipHeader, packet, ipHeader.getHeaderLength());
        udpHeader.setTotalLength((short) (packet.length - ipHeader.getHeaderLength()));

        ipHeader.updateChecksum();
        udpHeader.updateChecksum();

        mOutput.write(packet, 0, packet.length);

        mSession.receiveDataSize += packet.length;
    }

    public NioTunnel getRemoteChannel() {
        return mRemoteTunnel;
    }

    private UdpHeader createTemplate(UdpHeader header) {
        UdpHeader templateUdp = header.copy();
        IpHeader templateIp = templateUdp.getIpHeader();
        // Swap ip
        int sourceIp = templateIp.getSourceIp();
        int destinationIp = templateIp.getDestinationIp();
        templateIp.setSourceIp(destinationIp);
        templateIp.setDestinationIp(sourceIp);
        // Swap port
        short sourcePort = templateUdp.getSourcePort();
        short destinationPort = templateUdp.getDestinationPort();
        templateUdp.setDestinationPort(sourcePort);
        templateUdp.setSourcePort(destinationPort);
        return templateUdp;
    }

}
