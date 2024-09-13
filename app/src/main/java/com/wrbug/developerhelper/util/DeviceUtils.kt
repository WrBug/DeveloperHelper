package com.wrbug.developerhelper.util

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.wrbug.developerhelper.base.BaseApp
import com.wrbug.developerhelper.commonutil.ShellUtils


object DeviceUtils {
    fun isRoot(): Boolean {
        return ShellUtils.isRoot()
    }

    fun isFloatWindowOpened(): Boolean {
        return isFloatWindowOpened(BaseApp.instance!!)
    }

    fun isFloatWindowOpened(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)
    }
}