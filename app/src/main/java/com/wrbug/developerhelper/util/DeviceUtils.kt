package com.wrbug.developerhelper.util

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import com.jaredrummler.android.shell.CommandResult
import com.wrbug.developerhelper.basecommon.BaseApp


object DeviceUtils {
    fun isRoot(): Boolean {
        val id = ShellUtils.runWithSu("id")
        return !TextUtils.isEmpty(id.getStdout())
    }

    fun isFloatWindowOpened(): Boolean {
        return isFloatWindowOpened(BaseApp.instance!!)
    }

    fun isFloatWindowOpened(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)
    }
}