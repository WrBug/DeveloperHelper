package com.wrbug.developerhelper.util

import android.content.pm.PackageInfo
import com.wrbug.developerhelper.basecommon.BaseApp
import com.wrbug.developerhelper.model.entry.ApkInfo

object AppInfoManager {
    private val appMap = HashMap<String, ApkInfo>()

    /**
     * 获取所有应用
     */
    fun getAllApps(): HashMap<String, ApkInfo> {
        val apkMap = HashMap<String, ApkInfo>()
        val packageInfoMap = HashMap<String, PackageInfo>()
        val pManager = BaseApp.instance.packageManager
        // 获取手机内所有应用
        val appList = pManager.getInstalledApplications(0)
        val paklist = pManager.getInstalledPackages(0)
        for (packageInfo in paklist) {
            packageInfoMap[packageInfo.packageName] = packageInfo
        }
        for (appInfo in appList) {
            packageInfoMap[appInfo.packageName]?.let {
                apkMap[appInfo.processName] = ApkInfo(it, appInfo)
            }
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