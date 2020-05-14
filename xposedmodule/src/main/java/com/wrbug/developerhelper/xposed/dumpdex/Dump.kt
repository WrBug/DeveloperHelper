package com.wrbug.developerhelper.xposed.dumpdex

import android.os.Build
import android.os.Process
import com.wrbug.developerhelper.ipc.processshare.DumpDexListProcessData
import com.wrbug.developerhelper.ipc.processshare.ProcessDataCreator
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
        val data = ProcessDataCreator.get(DumpDexListProcessData::class.java)
        val packageNames = data.getData() ?: return
        val packageName = lpparam.packageName
        if (packageNames.contains(packageName).not()) {
            "未包含 $packageName ,忽略".xposedLog()
            return
        }
        copySoToCacheDir(packageName)
        "pid:${Process.myPid()},tid:${Process.myTid()},uid:${Process.myUid()},".xposedLog()
        "准备脱壳：$packageName".xposedLog()
        if (lpparam.packageName == packageName) {
            val path = "/data/data/$packageName/dump"
            val parent = File(path)
            if (!parent.exists() || !parent.isDirectory) {
                parent.mkdirs()
            }
            log("sdk version:" + Build.VERSION.SDK_INT)
            if (DeviceUtils.isOreo || DeviceUtils.isPie) {
                OreoDump.init(lpparam, type)
            } else {
                LowSdkDump.init(lpparam, type)
            }

        }
    }

    private fun copySoToCacheDir(packageName: String) {
        copySoFile(packageName, Native.SO_FILE)
        copySoFile(packageName, Native.SO_FILE_V7a)
        copySoFile(packageName, Native.SO_FILE_V8a)
    }

    private fun copySoFile(packageName: String, filename: String) {
        val file = File("/data/local/tmp/$filename")
        val mvFile = File("/data/data/$packageName/cache", filename)
        if (mvFile.exists()) {
            mvFile.delete()
        }
        file.copyTo(mvFile)
    }
}
