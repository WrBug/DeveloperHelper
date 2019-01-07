package com.wrbug.developerhelper.xposed.dumpdex

import android.os.Build
import com.wrbug.developerhelper.xposed.dumpdex.DeviceUtils
import com.wrbug.developerhelper.xposed.dumpdex.PackerInfo
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

import java.io.File

object Dump {
    fun log(txt: String) {

        XposedBridge.log("developerhelper.native--> $txt")
    }

    fun start(lpparam: XC_LoadPackage.LoadPackageParam) {
        val type = PackerInfo.find(lpparam) ?: return
        val packageName = lpparam.packageName
        if (lpparam.packageName == packageName) {
            val path = "/data/data/$packageName/dump"
            val parent = File(path)
            if (!parent.exists() || !parent.isDirectory) {
                parent.mkdirs()
            }
            log("sdk version:" + Build.VERSION.SDK_INT)
            if (DeviceUtils.isOreo) {
                OreoDump.init(lpparam)
            } else {
                LowSdkDump.init(lpparam, type)
            }

        }
    }
}
