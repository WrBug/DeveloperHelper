package com.wrbug.developerhelper.xposed.dumpdex

import android.os.Build
import com.wrbug.developerhelper.xposed.dumpdex.DeviceUtils
import com.wrbug.developerhelper.xposed.dumpdex.PackerInfo
import com.wrbug.developerhelper.xposed.processshare.DumpDexListProcessData
import com.wrbug.developerhelper.xposed.processshare.ProcessDataManager
import com.wrbug.developerhelper.xposed.xposedLog
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

import java.io.File

object Dump {
    fun log(txt: String) {

        XposedBridge.log("developerhelper.xposed.Dump--> $txt")
    }

    fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        val type = PackerInfo.find(lpparam) ?: return
        val data = ProcessDataManager.get(DumpDexListProcessData::class.java)
        val packageNames = data.getData() ?: return
        val packageName = lpparam.packageName
        if (packageNames.contains(packageName).not()) {
            "未包含 $packageName ,忽略".xposedLog()
            return
        }
        "准备脱壳：$packageName".xposedLog()
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
