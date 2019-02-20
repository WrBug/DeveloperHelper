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

import java.io.IOException;

/**
 * An abstract base class defined for proxy servers. The local proxy server runs a separated thread
 * and loop to process packets. The sub class needs to impl {@link #process()} to handle the packets.
 *
 * @author Megatron King
 * @since 2018-10-10 00:31
 */
/* package */ abstract class BaseProxyServer extends ProxyServer implements Runnable {

    /**
     * Waiting the specific protocol packets and trying to sent to real remote server.
     *
     * @throws IOException If an I/O error has occurred.
     */
    protected abstract void process() throws IOException;

    private boolean mIsRunning;

    private final Thread mServerThread;

    /* package */ BaseProxyServer(String serverName) {
        this.mServerThread = new Thread(this, serverName);
    }

    @Override
    void startServer() {
        mIsRunning = true;
        mServerThread.start();
    }

    @Override
    void stopServer() {
        mIsRunning = false;
        mServerThread.interrupt();
    }

    @Override
    public void run() {
        while (mIsRunning) {
            try {
                process();
            } catch (IOException e) {
                NetBareLog.e(e.getMessage());
            }
        }
    }

    /* package */ boolean isStopped() {
        return !mIsRunning;
    }

}
