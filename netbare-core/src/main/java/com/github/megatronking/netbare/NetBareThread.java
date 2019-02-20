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

import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;

import com.github.megatronking.netbare.ip.IpAddress;
import com.github.megatronking.netbare.ip.IpHeader;
import com.github.megatronking.netbare.ip.Protocol;
import com.github.megatronking.netbare.net.UidDumper;
import com.github.megatronking.netbare.proxy.IcmpProxyServerForwarder;
import com.github.megatronking.netbare.proxy.ProxyServerForwarder;
import com.github.megatronking.netbare.proxy.TcpProxyServerForwarder;
import com.github.megatronking.netbare.proxy.UdpProxyServerForwarder;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A work thread running NetBare core logic. NetBase established the VPN connection is this thread
 * and read packets from the VPN file descriptor and transfer them to local proxy servers. Every
 * IP protocol runs an independent local proxy server to receive the packets.
 *
 * @author Megatron King
 * @since 2018-10-08 19:38
 */
/* package */ final class NetBareThread extends Thread {

    private static final int TRANSPORT_WAIT_TIME = 5;

    private final NetBareConfig mConfig;
    private final VpnService mVpnService;

    private boolean mRunning;

    /* package */ NetBareThread(VpnService vpnService, NetBareConfig config) {
        super("NetBare");
        this.mVpnService = vpnService;
        this.mConfig = config;
    }

    @Override
    public void start() {
        mRunning = true;
        super.start();
    }

    @Override
    public void interrupt() {
        mRunning = false;
        super.interrupt();
    }

    @Override
    public void run() {
        super.run();
        if (!mRunning) {
            return;
        }

        // Notify NetBareListener that the service is started now.
        NetBare.get().notifyServiceStarted();

        PacketsTransfer packetsTransfer = null;
        try {
            packetsTransfer = new PacketsTransfer(mVpnService, mConfig);
        } catch (IOException e) {
            NetBareLog.wtf(e);
        }
        if (packetsTransfer != null) {
            // Establish VPN, it runs a while loop unless failed.
            establishVpn(packetsTransfer);
        }

        // Notify NetBareListener that the service is stopped now.
        NetBare.get().notifyServiceStopped();

    }

    private void establishVpn(PacketsTransfer packetsTransfer) {
        VpnService.Builder builder = mVpnService.new Builder();
        builder.setMtu(mConfig.mtu);
        builder.addAddress(mConfig.address.address, mConfig.address.prefixLength);
        if (mConfig.session != null) {
            builder.setSession(mConfig.session);
        }
        if (mConfig.configureIntent != null) {
            builder.setConfigureIntent(mConfig.configureIntent);
        }
        for (IpAddress ip : mConfig.routes) {
            builder.addRoute(ip.address, ip.prefixLength);
        }
        for (String address : mConfig.dnsServers) {
            builder.addDnsServer(address);
        }
        try {
            for (String packageName : mConfig.allowedApplications) {
                builder.addAllowedApplication(packageName);
            }
            for (String packageName : mConfig.disallowedApplications) {
                builder.addDisallowedApplication(packageName);
            }
            // Add self to allowed list.
            if (!mConfig.allowedApplications.isEmpty()) {
                builder.addAllowedApplication(mVpnService.getPackageName());
            }
        } catch (PackageManager.NameNotFoundException e) {
            NetBareLog.wtf(e);
        }
        ParcelFileDescriptor vpnDescriptor = builder.establish();
        if (vpnDescriptor == null) {
            return;
        }

        // Open io with the VPN descriptor.
        FileDescriptor descriptor = vpnDescriptor.getFileDescriptor();
        if (descriptor == null) {
            return;
        }
        InputStream input = new FileInputStream(descriptor);
        OutputStream output = new FileOutputStream(descriptor);
        int mtu = mConfig.mtu;

        packetsTransfer.start();

        try {
            // Read packets from input io and forward them to proxy servers.
            while (mRunning) {
                packetsTransfer.transfer(input, output, mtu);
            }
        } catch (IOException e) {
            NetBareLog.wtf(e);
        }

        packetsTransfer.stop();

        NetBareUtils.closeQuietly(vpnDescriptor);
        NetBareUtils.closeQuietly(input);
        NetBareUtils.closeQuietly(output);
    }

    private static class PacketsTransfer {

        private final UidDumper mUidDumper;
        private final Map<Protocol, ProxyServerForwarder> mForwarderRegistry;

        private PacketsTransfer(VpnService service, NetBareConfig config) throws IOException {
            int mtu = config.mtu;
            String localIp = config.address.address;
            this.mUidDumper = config.dumpUid ? new UidDumper(localIp, config.uidProvider) : null;
            // Register all supported protocols here.
            this.mForwarderRegistry = new LinkedHashMap<>(3);
            // TCP
            this.mForwarderRegistry.put(Protocol.TCP, new TcpProxyServerForwarder(service, localIp, mtu,
                    mUidDumper));
            // UDP
            this.mForwarderRegistry.put(Protocol.UDP, new UdpProxyServerForwarder(service, mtu,
                    mUidDumper));
            // ICMP
            this.mForwarderRegistry.put(Protocol.ICMP, new IcmpProxyServerForwarder());
        }

        private void start()  {
            if (mUidDumper != null) {
                mUidDumper.startDump();
            }
            for (ProxyServerForwarder forwarder : mForwarderRegistry.values()) {
                forwarder.prepare();
            }
        }

        private void stop() {
            for (ProxyServerForwarder forwarder : mForwarderRegistry.values()) {
                forwarder.release();
            }
            mForwarderRegistry.clear();
            if (mUidDumper != null) {
                mUidDumper.stopDump();
            }
        }

        private void transfer(InputStream input, OutputStream output, int mtu) throws IOException {
            // The thread would be blocked if there is no outgoing packets from input stream.
            byte[] packet = new byte[mtu];
            int readLength = input.read(packet);
            if (readLength < 0) {
                throw new IOException("Read -1 from vpn FileDescriptor.");
            }
            if (readLength == 0) {
                SystemClock.sleep(TRANSPORT_WAIT_TIME);
                return;
            }
            transfer(packet, readLength, output);
        }

        private void transfer(byte[] packet, int len, OutputStream output) {
            if (len < IpHeader.MIN_HEADER_LENGTH) {
                NetBareLog.w("Ip header length < " + IpHeader.MIN_HEADER_LENGTH);
                return;
            }
            IpHeader ipHeader = new IpHeader(packet, 0);
            Protocol protocol = Protocol.parse(ipHeader.getProtocol());
            ProxyServerForwarder forwarder = mForwarderRegistry.get(protocol);
            if (forwarder != null) {
                forwarder.forward(packet, len, output);
            } else {
                NetBareLog.w("Unknown ip protocol: " + ipHeader.getProtocol());
            }
        }

    }

}
