package com.wrbug.developerhelper.xposed.developerhelper

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import com.jaredrummler.android.shell.Shell
import com.wrbug.developerhelper.xposed.dumpdex.Native
import com.wrbug.developerhelper.ipc.processshare.GlobalConfigProcessData
import com.wrbug.developerhelper.ipc.processshare.ProcessDataCreator
import com.wrbug.developerhelper.ipc.processshare.manager.GlobalConfigProcessDataManager
import com.wrbug.developerhelper.xposed.saveToFile
import com.wrbug.developerhelper.xposed.xposedLog
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import org.jetbrains.anko.doAsync
import java.io.File
import java.lang.Exception

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
                    val xposedSettingView =
                        XposedHelpers.getObjectField(activity, "xposedSettingView") as View?
                    xposedSettingView?.visibility = View.VISIBLE
                    XposedHelpers.callMethod(
                        xposedSettingView,
                        "setOnCheckedChangeListener",
                        CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                            GlobalConfigProcessDataManager.instance.setXposedOpen(isChecked)
                        })
                    val opened = GlobalConfigProcessDataManager.instance.isXposedOpen()
                    if (!opened) {
                        //首次启动，存在tcp服务没启动情况，延时1秒
                        xposedSettingView?.isEnabled = false
                        xposedSettingView?.postDelayed({
                            XposedHelpers.callMethod(
                                xposedSettingView,
                                "setChecked",
                                GlobalConfigProcessDataManager.instance.isXposedOpen()
                            )
                            xposedSettingView.isEnabled = true
                        }, 1000)
                    } else {
                        XposedHelpers.callMethod(
                            xposedSettingView,
                            "setChecked",
                            GlobalConfigProcessDataManager.instance.isXposedOpen()
                        )
                    }
                }
            })
        AppInfoPagerAdapterHook.start(lpparam)
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
                            try {
                                initProcessDataDir()
                                saveSo(activity, "armeabi", Native.SO_FILE)
                                saveSo(activity, "armeabi-v7a", Native.SO_FILE_V7a)
                                saveSo(activity, "arm64-v8a", Native.SO_FILE_V8a)
                            } catch (e: Exception) {
                                XposedBridge.log(e)
                            }
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


    private fun saveSo(activity: Activity, libPath: String, fileName: String) {
        "正在释放$fileName".xposedLog()
        val tmpDir = File("/data/local/tmp")
        val soFile = File(tmpDir, fileName)
        val inputStream =
            activity.classLoader.getResource("lib/$libPath/libnativeDump.so").openStream()
        if (inputStream == null) {
            "$libPath/libnativeDump.so 不存在".xposedLog()
            return
        }
        "已获取asset".xposedLog()
        val tmpFile = File(activity.cacheDir, fileName)
        inputStream.saveToFile(tmpFile)
        val commandResult =
            Shell.SU.run(
                "cp ${tmpFile.absolutePath} ${soFile.absolutePath}",
                "chmod 777 ${soFile.absolutePath}"
            )
        if (commandResult.isSuccessful) {
            "$fileName 释放成功".xposedLog()
        } else {
            "$fileName 释放失败：${commandResult.getStderr()}".xposedLog()
        }
        tmpFile.delete()
    }
}