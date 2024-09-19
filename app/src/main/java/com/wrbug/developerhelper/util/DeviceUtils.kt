package com.wrbug.developerhelper.util

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.WindowManager
import com.wrbug.developerhelper.base.BaseApp
import com.wrbug.developerhelper.commonutil.shell.ShellUtils


object DeviceUtils {
    fun isRoot(): Boolean {
        return ShellUtils.isRoot()
    }

    fun isFloatWindowOpened(): Boolean {
        return isFloatWindowOpened(BaseApp.instance)
    }

    fun isFloatWindowOpened(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)
    }


    fun getScreenWidth(): Int {
        val manager: WindowManager =
            BaseApp.instance.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        manager.defaultDisplay.getMetrics(outMetrics)
        return outMetrics.widthPixels
    }

    fun getScreenHeight(): Int {
        val manager: WindowManager =
            BaseApp.instance.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        manager.defaultDisplay.getMetrics(outMetrics)
        return outMetrics.heightPixels
    }
}