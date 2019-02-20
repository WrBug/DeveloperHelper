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

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.net.VpnService;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;

import androidx.core.content.ContextCompat;
import com.github.megatronking.netbare.gateway.DefaultVirtualGatewayFactory;
import com.github.megatronking.netbare.gateway.VirtualGatewayFactory;
import com.github.megatronking.netbare.ssl.JKS;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * NetBare is a single instance, we can use this class to config and manage the NetBare service.
 * The NetBare service is an implement class of {@link VpnService}, before starting this service,
 * should call {@link #prepare()} to check the vpn state.
 *
 * Start and stop the NetBare service:
 * <pre>
 * <code>
 *     NetBare.get().start(config);
 *     NetBare.get().stop();
 * </code>
 * </pre>
 *
 * @author Megatron King
 * @since 2018-10-07 09:28
 */
public final class NetBare {

    private static class Holder {

        private static final NetBare INSTANCE = new NetBare();

    }

    private final Set<NetBareListener> mListeners;
    private final Handler mMainThreadHandler;

    private Application mApp;
    private NetBareConfig mNetBareConfig;

    private boolean mAlive;

    public static NetBare get() {
        return Holder.INSTANCE;
    }

    private NetBare() {
        mListeners = new LinkedHashSet<>();
        mMainThreadHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Attach an application instance to NetBare. We recommend you to call this method in your
     * {@link Application} class.
     *
     * @param application The application instance.
     * @param debug Should print logs in console.
     * @return The single instance of NetBare.
     */
    public NetBare attachApplication(@NonNull Application application, boolean debug) {
        JKS.init(application);
        mApp = application;
        NetBareLog.setDebug(debug);
        return this;
    }

    /**
     * Prepare to establish a VPN connection. This method returns {@code null} if the VPN
     * application is already prepared or if the user has previously consented to the VPN
     * application. Otherwise, it returns an {@link Intent} to a system activity. The application
     * should launch the activity using {@link Activity#startActivityForResult} to get itself
     * prepared.
     *
     * @return The intent to call using {@link Activity#startActivityForResult}.
     */
    public Intent prepare() {
        return VpnService.prepare(mApp);
    }

    /**
     * Start the NetBare service with your specific configuration. If the service is started,
     * {@link NetBareListener#onServiceStarted()} will be invoked.
     *
     * @param config The configuration for NetBare service.
     */
    public void start(@NonNull NetBareConfig config) {
        if (config.mtu <= 0) {
            throw new RuntimeException("Must set mtu in NetBareConfig");
        }
        if (config.address == null) {
            throw new RuntimeException("Must set address in NetBareConfig");
        }
        mNetBareConfig = config;
        Intent intent = new Intent(NetBareService.ACTION_START);
        intent.setPackage(mApp.getPackageName());
        ContextCompat.startForegroundService(mApp, intent);
    }

    /**
     * Stop the NetBare service. If the service is started,
     * {@link NetBareListener#onServiceStopped()} will be invoked.
     */
    public void stop() {
        Intent intent = new Intent(NetBareService.ACTION_STOP);
        intent.setPackage(mApp.getPackageName());
        mApp.startService(intent);
    }

    /**
     * Whether the NetBare service is alive or not.
     *
     * @return True if the service is alive, false otherwise.
     */
    public boolean isActive() {
        return mAlive;
    }

    /**
     * Register a callback to be invoked when the service state changes.
     *
     * @param listener The callback to register.
     */
    public void registerNetBareListener(NetBareListener listener) {
        mListeners.add(listener);
    }

    /**
     * Remove a previously installed service state callback.
     *
     * @param listener The callback to remove.
     */
    public void unregisterNetBareListener(NetBareListener listener) {
        mListeners.remove(listener);
    }

    /* package */ NetBareConfig getConfig() {
        return mNetBareConfig;
    }

    /* package */ VirtualGatewayFactory getGatewayFactory() {
        // Make sure the virtual gateway not be null.
        return mNetBareConfig.gatewayFactory == null ? DefaultVirtualGatewayFactory.create() :
                mNetBareConfig.gatewayFactory;
    }

    /* package */ void notifyServiceStarted() {
        mAlive = true;
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                for (NetBareListener listener : mListeners) {
                    listener.onServiceStarted();
                }
            }
        });
    }

    /* package */ void notifyServiceStopped() {
        mAlive = false;
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                for (NetBareListener listener : mListeners) {
                    listener.onServiceStopped();
                }
            }
        });
    }

}
