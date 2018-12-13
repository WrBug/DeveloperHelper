package com.wrbug.developerhelper.util

import com.wrbug.developerhelper.basecommon.BaseApp
import com.wrbug.developerhelper.model.entry.ApkInfo

object AppInfoManager {
    private val appMap = HashMap<String, ApkInfo>()

    /**
     * 获取所有应用
     */
    fun getAllApps(): HashMap<String, ApkInfo> {
        val apkMap = HashMap<String, ApkInfo>()
        val pManager = BaseApp.instance.packageManager
        // 获取手机内所有应用
        val paklist = pManager.getInstalledPackages(0)
        for (packageInfo in paklist) {
            apkMap[packageInfo.packageName] = ApkInfo(packageInfo, packageInfo.applicationInfo)
        }
        return apkMap
    }

    fun getAppByPackageName(packageName: String): ApkInfo? {
        if (appMap.containsKey(packageName)) {
            return appMap[packageName]
        }
        appMap.putAll(getAllApps())
        return appMap[packageName]
    }
}