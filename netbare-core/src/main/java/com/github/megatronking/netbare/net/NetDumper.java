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
package com.github.megatronking.netbare.net;

import com.github.megatronking.netbare.NetBareLog;
import com.github.megatronking.netbare.NetBareUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A Dumper uses for dumping net info from /proc/net/.
 *
 * @author Megatron King
 * @since 2018-12-01 22:36
 */
/* package */ class NetDumper implements Runnable {

    private final String mArg;
    private final String mLocalIp;
    private final DumpCallback mCallback;

    private boolean mIsRunning;

    NetDumper(String arg, String localIp, DumpCallback callback) {
        this.mArg = arg;
        this.mLocalIp = localIp;
        this.mCallback = callback;
    }

    void startDump() {
        mIsRunning = true;
        UidDumper.THREAD_POOL_EXECUTOR.execute(this);
    }

    void stopDump() {
        mIsRunning = false;
    }

    void pauseDump() {
        synchronized (mArg) {
            try {
                mArg.wait();
            } catch (InterruptedException e) {
                // do nothing
            }
        }
    }

    void resumeDump() {
        synchronized (mArg) {
            mArg.notify();
        }
    }

    @Override
    public void run() {
        while (mIsRunning) {
            ProcessBuilder builder = new ProcessBuilder("cat", mArg);
            InputStream is = null;
            BufferedReader reader = null;
            try {
                Process process = builder.start();
                is = process.getInputStream();
                reader = new BufferedReader(new InputStreamReader(is));
                String line;
                int index = 0;
                while ((line = reader.readLine()) != null) {
                    index++;
                    if (index == 1) {
                        // Skip the table title
                        continue;
                    }
                    String[] columns = line.trim().split(" ");
                    if (columns.length < 8) {
                        // Uid is in the 8th of columns.
                        continue;
                    }
                    int uid = NetBareUtils.parseInt(columns[7], -1);
                    if (uid == -1 || uid == 0) {
                        continue;
                    }
                    String[] local = columns[1].split(":");
                    if (local.length != 2 || local[0].length() < 8) {
                        continue;
                    }
                    String[] remote = columns[2].split(":");
                    if (remote.length != 2 || remote[0].length() < 8) {
                        continue;
                    }
                    String localIp = parseIp(local[0]);
                    if (localIp == null || !localIp.equals(mLocalIp)) {
                        continue;
                    }
                    String remoteIp = parseIp(remote[0]);
                    if (remoteIp == null || remoteIp.equals("0.0.0.0")
                            || remoteIp.equals("255.255.255.255")) {
                        continue;
                    }
                    int localPort = parsePort(local[1]);
                    if (localPort == -1) {
                        continue;
                    }
                    int remotePort = parsePort(remote[1]);
                    if (remotePort == -1) {
                        continue;
                    }
                    mCallback.onDump(new Net(uid, localIp, localPort, remoteIp, remotePort));
                }
            } catch (IOException e) {
                NetBareLog.wtf(e);
            }
            NetBareUtils.closeQuietly(is);
            NetBareUtils.closeQuietly(reader);
        }
    }

    private String parseIp(String ip) {
        ip = ip.substring(ip.length() - 8);
        int ip1 = NetBareUtils.parseInt(ip.substring(6, 8), 16, -1);
        int ip2 = NetBareUtils.parseInt(ip.substring(4, 6), 16, -1);
        int ip3 = NetBareUtils.parseInt(ip.substring(2, 4), 16, -1);
        int ip4 = NetBareUtils.parseInt(ip.substring(0, 2), 16, -1);
        if (ip1 < 0 || ip2 < 0 || ip3 < 0 || ip4 < 0) {
            return null;
        }
        return ip1 + "." + ip2 + "." + ip3 + "." + ip4;
    }

    private int parsePort(String port) {
        return NetBareUtils.parseInt(port, 16, -1);
    }

}
