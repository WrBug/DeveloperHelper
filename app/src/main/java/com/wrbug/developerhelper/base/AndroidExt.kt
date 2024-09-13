package com.wrbug.developerhelper.base

import android.content.pm.PackageInfo
import android.os.Build

inline val PackageInfo.versionCodeLong: Long
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        longVersionCode
    } else {
        versionCode.toLong()
    }