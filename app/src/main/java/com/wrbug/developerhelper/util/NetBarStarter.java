package com.wrbug.developerhelper.util;

import android.content.Context;
import android.content.Intent;
import com.github.megatronking.netbare.NetBare;
import com.github.megatronking.netbare.NetBareConfig;
import com.github.megatronking.netbare.http.HttpInjectInterceptor;
import com.github.megatronking.netbare.http.HttpInterceptorFactory;
import com.github.megatronking.netbare.ssl.JKS;

import java.util.List;

public class NetBarStarter {
    private Object mlock = new Object();
    private static NetBarStarter instance;
    private NetBare netBare;

    private NetBarStarter() {
        netBare = NetBare.get();
    }

    private static NetBarStarter getInstance() {
        if (instance == null) {
            synchronized (NetBarStarter.class) {
                if (instance == null) {
                    instance = new NetBarStarter();
                }
            }

        }
        return instance;
    }

    public static boolean isJksInstalled(Context context) {
        return JKS.isInstalled(context, JKS.getJskAlias());
    }

    public static boolean installJks(Context context) {
        if (isJksInstalled(context)) {
            return true;
        }
        try {
            JKS.install(context, JKS.getJskAlias(), JKS.getJskAlias());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            // 安装失败
        }
        return false;
    }

    public static Intent prepareVpn() {
        return getInstance().netBare.prepare();
    }

    public static void toggle(Context context) {
        if (getInstance().netBare.isActive()) {
            getInstance().netBare.stop();
        } else {
            start(context);
        }
    }

    private static void start(Context context) {
        // 安装自签证书
        if (!installJks(context)) {
            return;
        }
        // 配置VPN
        Intent intent = prepareVpn();
        if (intent != null) {
            return;
        }
        // 启动NetBare服务
        getInstance().netBare.start(
                NetBareConfig.defaultHttpConfig(
                        JKS.getJks(),
                        interceptorFactories()
                ).newBuilder().dumpUid(true).build()
        );
    }

    private static List<HttpInterceptorFactory> interceptorFactories() {
        return null;
    }
}
