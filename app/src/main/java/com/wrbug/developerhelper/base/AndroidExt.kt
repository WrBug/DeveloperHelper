package com.wrbug.developerhelper.base

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageInfo
import android.os.Build

inline val PackageInfo.versionCodeLong: Long
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        longVersionCode
    } else {
        versionCode.toLong()
    }


@Suppress("DEPRECATION")
fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    return manager.getRunningServices(Integer.MAX_VALUE)
        .any { it.service.className == serviceClass.name }
}
