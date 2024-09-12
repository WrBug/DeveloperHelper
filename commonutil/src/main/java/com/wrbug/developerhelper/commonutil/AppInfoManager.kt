package com.wrbug.developerhelper.commonutil

import android.app.ActivityManager
import android.content.Context
import com.wrbug.developerhelper.commonutil.entity.ApkInfo
import com.wrbug.developerhelper.commonutil.shell.ShellManager
import java.io.File


object AppInfoManager {
    private val appMap = HashMap<String, ApkInfo>()

    /**
     * 获取所有应用
     */
    fun getAllApps(): HashMap<String, ApkInfo> {
        val apkMap = HashMap<String, ApkInfo>()
        val pManager = CommonUtils.application.packageManager
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


    fun getTopActivityClassName(context: Context): String? {
        var topActivityClass: String? = null
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        try {
            val runningTaskInfos = activityManager.getRunningTasks(1)
            if (runningTaskInfos != null && runningTaskInfos.size > 0) {
                val f = runningTaskInfos[0].topActivity
                topActivityClass = f!!.className
            }
        } catch (e: Exception) {
        }
        return topActivityClass
    }


    fun getSharedPreferencesFiles(packageName: String): Array<File> {
        return getSharedPreferencesFiles(Constant.dataDir, packageName)
    }

    private fun getSharedPreferencesFiles(dir: String, packageName: String): Array<File> {
        val path = "$dir/$packageName/shared_prefs"
        val list = ShellManager.lsDir(path)
        val files = ArrayList<File>()
        for (file in list) {
            file?.let {
                if (it.endsWith(".xml")) {
                    files.add(File(path, it))
                }
            }

        }
        return files.toTypedArray()
    }

}