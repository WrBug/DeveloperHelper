package com.wrbug.developerhelper.xposed.dumpdex

import android.os.Build
import android.os.Process
import com.wrbug.developerhelper.commonutil.ZipUtils
import com.wrbug.developerhelper.ipc.processshare.DumpDexListProcessData
import com.wrbug.developerhelper.ipc.processshare.ProcessDataCreator
import com.wrbug.developerhelper.ipc.processshare.manager.DumpDexListProcessDataManager
import com.wrbug.developerhelper.ipc.processshare.manager.FileProcessDataManager
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
        val packageName = lpparam.packageName
        if (!DumpDexListProcessDataManager.instance.containPackage(packageName)) {
            "未包含 $packageName ,忽略".xposedLog()
            return
        }
        DumpDexListProcessDataManager.instance.setData(packageName to false)
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
        val dir = "/data/data/$packageName/cache"
        val data = FileProcessDataManager.instance.getDumpSoZipFile().blockingFirst() ?: return
        val file = File(dir, "so.zip").apply {
            if (exists()) {
                delete()
            }
            writeBytes(data)
        }
        ZipUtils.unzip(file.absolutePath, dir)
    }
}
