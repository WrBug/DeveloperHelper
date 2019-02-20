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
package com.github.megatronking.netbare.tunnel;

import android.net.VpnService;

import com.github.megatronking.netbare.NetBareXLog;
import com.github.megatronking.netbare.ip.Protocol;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * A TCP tunnel communicates with the remote server.
 *
 * @author Megatron King
 * @since 2018-11-21 01:41
 */
public class TcpRemoteTunnel extends TcpTunnel {

    private final VpnService mVpnService;

    private NetBareXLog mLog;

    public TcpRemoteTunnel(VpnService vpnService, SocketChannel channel, Selector selector,
                           String remoteIp, int remotePort) {
        super(channel, selector);
        this.mVpnService = vpnService;
        this.mLog = new NetBareXLog(Protocol.TCP, remoteIp, remotePort);
    }

    @Override
    public void connect(InetSocketAddress address) throws IOException {
        if (mVpnService.protect(socket())) {
            super.connect(address);
            mLog.i("Connect to remote server %s", address);
        } else {
            throw new IOException("[TCP]Can not protect remote tunnel socket.");
        }
    }

    @Override
    public void onConnected() throws IOException {
        mLog.i("Remote tunnel is connected.");
        super.onConnected();
    }

    @Override
    public int read(ByteBuffer buffer) throws IOException {
        int len = super.read(buffer);
        mLog.i("Read from remote: " + len);
        return len;
    }

    @Override
    public void write(ByteBuffer buffer) throws IOException {
        mLog.i("Write to remote: " + buffer.remaining());
        super.write(buffer);
    }

    @Override
    public void close() {
        mLog.i("Remote tunnel is closed.");
        super.close();
    }


}
