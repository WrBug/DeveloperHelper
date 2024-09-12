package com.wrbug.developerhelper.commonutil.entity

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import com.wrbug.developerhelper.commonutil.CommonUtils
import kotlinx.parcelize.Parcelize

@Parcelize
class ApkInfo(
    val packageInfo: PackageInfo,
    val applicationInfo: ApplicationInfo,
    var topActivity: String = ""
) : Parcelable {

    fun getIco(): Drawable {
        return applicationInfo.loadIcon(CommonUtils.application.packageManager)
    }

    fun getAppName(): String {
        val label = CommonUtils.application.packageManager.getApplicationLabel(applicationInfo)
        return label.toString()
    }

    fun generateBackupApkFileName(): String {
        return packageInfo.versionName + ".apk"
    }

}