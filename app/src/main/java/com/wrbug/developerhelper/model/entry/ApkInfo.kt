package com.wrbug.developerhelper.model.entry

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.graphics.drawable.Drawable
import com.wrbug.developerhelper.basecommon.BaseApp

class ApkInfo(val packageInfo: PackageInfo, val applicationInfo: ApplicationInfo) {

    fun getIco(): Drawable {
        return applicationInfo.loadIcon(BaseApp.instance.packageManager)
    }

}