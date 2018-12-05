package com.wrbug.developerhelper.util

import android.content.Context
import android.os.Build
import android.provider.Settings

object DeviceUtils {
    fun isFloatWindowOpened(content: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(content)
    }
}