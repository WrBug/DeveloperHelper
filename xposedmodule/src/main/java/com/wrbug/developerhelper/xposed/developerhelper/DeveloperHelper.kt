package com.wrbug.developerhelper.xposed.developerhelper

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import com.jaredrummler.android.shell.Shell
import com.wrbug.developerhelper.xposed.dumpdex.Native
import com.wrbug.developerhelper.xposed.processshare.GlobalConfigProcessData
import com.wrbug.developerhelper.xposed.processshare.ProcessDataManager
import com.wrbug.developerhelper.xposed.saveToFile
import com.wrbug.developerhelper.xposed.xposedLog
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import org.jetbrains.anko.doAsync
import java.io.File

object DeveloperHelper {
    fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        XposedHelpers.findAndHookMethod(
            "com.wrbug.developerhelper.ui.activity.main.MainActivity",
            lpparam.classLoader,
            "onCreate",
            Bundle::class.java,
            object :
                XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam?) {
                    tryReleaseSo(lpparam, param?.thisObject as Activity)
                }

                override fun afterHookedMethod(param: MethodHookParam?) {
                    "Main onCreate".xposedLog()
                    val activity = param?.thisObject as Activity
                    val xposedSettingView = XposedHelpers.getObjectField(activity, "xposedSettingView") as View?
                    xposedSettingView?.apply {
                        visibility = View.VISIBLE
                        val configData = ProcessDataManager.get(GlobalConfigProcessData::class.java)
                        XposedHelpers.callMethod(this, "setChecked", configData.isXposedOpen())
                        XposedHelpers.callMethod(
                            this,
                            "setOnCheckedChangeListener",
                            CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                                configData.setXposedOpen(isChecked)
                            })
                    }

                }
            })
        EnforceMod.start(lpparam)
    }

    private fun tryReleaseSo(lpparam: XC_LoadPackage.LoadPackageParam, activity: Activity) {
        XposedHelpers.findAndHookMethod(
            "com.wrbug.developerhelper.util.DeviceUtils",
            lpparam.classLoader,
            "isRoot",
            object :
                XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    val result = param?.result as Boolean
                    "isRoot->$result".xposedLog()
                    if (result) {
                        "设备已root,开始释放so文件".xposedLog()
                        doAsync {
                            initProcessDataDir()
                            saveSo(activity, Native.SO_FILE)
                            saveSo(activity, Native.SO_FILE_V7a)
                            saveSo(activity, Native.SO_FILE_V8a)
                        }

                    }
                }
            })
    }

    private fun initProcessDataDir() {
        "创建processdata目录".xposedLog()
        val dir = "/data/local/tmp/developerHelper"
        val commandResult = Shell.SU.run("mkdir -p $dir && chmod -R 777 $dir")
        if (commandResult.isSuccessful) {
            "processdata目录创建成功".xposedLog()
        } else {
            "processdata目录创建失败：${commandResult.getStderr()}".xposedLog()
        }
    }


    private fun saveSo(activity: Activity, fileName: String) {
        "正在释放$fileName".xposedLog()
        val tmpDir = File("/data/local/tmp")
        val soFile = File(tmpDir, fileName)
        val inputStream = activity.assets.open(fileName)
        "已获取asset".xposedLog()
        val tmpFile = File(activity.cacheDir, fileName)
        inputStream.saveToFile(tmpFile)
        val commandResult =
            Shell.SU.run("mv ${tmpFile.absolutePath} ${soFile.absolutePath}", "chmod 777 ${soFile.absolutePath}")
        if (commandResult.isSuccessful) {
            "$fileName 释放成功".xposedLog()
        } else {
            "$fileName 释放失败：${commandResult.getStderr()}".xposedLog()

        }

    }
}