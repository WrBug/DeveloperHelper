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

import androidx.annotation.NonNull;

import com.github.megatronking.netbare.NetBareConfig;
import com.github.megatronking.netbare.NetBareUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A dumper analyzes /proc/net/ files to dump uid of the network session. This class may be a
 * battery-killer, but can set {@link NetBareConfig.Builder#dumpUid} to false to close the dumper.
 *
 * @author Megatron King
 * @since 2018-12-03 16:54
 */
public final class UidDumper implements DumpCallback {

    private static final int NET_ALIVE_SECONDS = 60;
    private static final int NET_CONCURRENCY_LEVEL = 6;
    private static final int NET_MAX_SIZE = 100;

    private static final int SESSION_ALIVE_SECONDS = 30;
    private static final int SESSION_CONCURRENCY_LEVEL = 8;
    private static final int SESSION_MAX_SIZE = 100;

    private static final int CORE_POOL_SIZE = 4;
    private static final int MAXIMUM_POOL_SIZE = 4;
    private static final int KEEP_ALIVE_SECONDS = 3 * 60;
    private static final int QUEUE_SIZE = 32;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r, "UidDumper #" + mCount.getAndIncrement());
        }
    };

    /**
     * An {@link Executor} that can be used to execute tasks in parallel.
     */
    /* package */ static final Executor THREAD_POOL_EXECUTOR;

    private final Cache<Integer, Net> mNetCaches;
    private final Cache<Integer, Session> mWaitingSessions;

    private final UidProvider mUidProvider;

    private final NetDumper dumper1;
    private final NetDumper dumper2;
    private final NetDumper dumper3;
    private final NetDumper dumper4;

    static {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
                new LinkedBlockingDeque<Runnable>(QUEUE_SIZE), sThreadFactory);
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        THREAD_POOL_EXECUTOR = threadPoolExecutor;
    }

    public UidDumper(String localIp, UidProvider provider) {
        this.mUidProvider = provider;
        this.mNetCaches = CacheBuilder.newBuilder()
                .expireAfterAccess(NET_ALIVE_SECONDS, TimeUnit.SECONDS)
                .concurrencyLevel(NET_CONCURRENCY_LEVEL)
                .maximumSize(NET_MAX_SIZE)
                .build();
        this.mWaitingSessions = CacheBuilder.newBuilder()
                .expireAfterAccess(SESSION_ALIVE_SECONDS, TimeUnit.SECONDS)
                .concurrencyLevel(SESSION_CONCURRENCY_LEVEL)
                .maximumSize(SESSION_MAX_SIZE)
                .build();

        this.dumper1 = new NetDumper("/proc/net/tcp", localIp, this);
        this.dumper2 = new NetDumper("/proc/net/tcp6", localIp, this);
        this.dumper3 = new NetDumper("/proc/net/udp", localIp, this);
        this.dumper4 = new NetDumper("/proc/net/udp6", localIp, this);
    }

    public void startDump() {
        dumper1.startDump();
        dumper2.startDump();
        dumper3.startDump();
        dumper4.startDump();
    }

    public void stopDump() {
        dumper1.stopDump();
        dumper2.stopDump();
        dumper3.stopDump();
        dumper4.stopDump();
    }

    private void pauseDump() {
        dumper1.pauseDump();
        dumper2.pauseDump();
        dumper3.pauseDump();
        dumper4.pauseDump();
    }

    private void resumeDump() {
        dumper1.resumeDump();
        dumper2.resumeDump();
        dumper3.resumeDump();
        dumper4.resumeDump();
    }

    public void request(Session session) {
        if (mUidProvider != null) {
            int uid = mUidProvider.uid(session);
            if (uid != UidProvider.UID_UNKNOWN) {
                session.uid = uid;
                return;
            }
        }
        int port = NetBareUtils.convertPort(session.localPort);
        Map<Integer, Net> caches = mNetCaches.asMap();
        if (caches.containsKey(port)) {
            Net net = caches.get(port);
            if (net != null) {
                session.uid = net.uid;
            }
        } else {
            // Find net by remote ip from cache
            for (Net net : caches.values()) {
                if (NetBareUtils.convertIp(net.remoteIp) == session.remoteIp) {
                    session.uid = net.uid;
                    return;
                }
            }
            mWaitingSessions.put(port, session);
            // resumeDump();
        }
    }

    @Override
    public void onDump(Net net) {
        mNetCaches.put(net.localPort, net);

        if (mWaitingSessions.size() == 0) {
            // If sleep the threads, some uid would be missed. But if keep the threads running, too
            // much battery would be cost.
            // pauseDump();
            return;
        }
        Map<Integer, Session> map = mWaitingSessions.asMap();
        for (int port : map.keySet()) {
            if (net.localPort == port) {
                Session session = map.get(port);
                if (session != null) {
                    session.uid = net.uid;
                }
                mWaitingSessions.invalidate(port);
                break;
            }
        }
    }

}
