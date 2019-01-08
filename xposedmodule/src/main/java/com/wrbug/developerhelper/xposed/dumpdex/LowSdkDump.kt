package com.wrbug.developerhelper.xposed.dumpdex

import android.app.Application
import android.content.Context

import com.wrbug.developerhelper.xposed.dumpdex.DeviceUtils
import com.wrbug.developerhelper.xposed.util.FileUtils
import com.wrbug.developerhelper.xposed.dumpdex.Native
import com.wrbug.developerhelper.xposed.dumpdex.PackerInfo
import com.wrbug.developerhelper.xposed.xposedLog

import java.io.File

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * LowSdkDump
 *
 * @author WrBug
 * @since 2018/3/23
 */
object LowSdkDump {
    fun log(txt: String) {
        txt.xposedLog("developerhelper.xposed.LowSdkDump-->")
    }

    fun init(lpparam: XC_LoadPackage.LoadPackageParam, type: PackerInfo.Type) {
        log("start hook Instrumentation#newApplication")
        if (DeviceUtils.supportNativeHook()) {
            Native.dump(lpparam.packageName)
        }
        if (type == PackerInfo.Type.BAI_DU) {
            return
        }
        XposedHelpers.findAndHookMethod(
            "android.app.Instrumentation",
            lpparam.classLoader,
            "newApplication",
            ClassLoader::class.java,
            String::class.java,
            Context::class.java,
            object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam?) {
                    log("Application=" + param!!.result)
                    dump(lpparam.packageName, param.result.javaClass)
                    attachBaseContextHook(lpparam, param.result as Application)
                }
            })
    }

    private fun dump(packageName: String, aClass: Class<*>?) {
        val dexCache = XposedHelpers.getObjectField(aClass, "dexCache")
        log("decCache=$dexCache")
        val o = XposedHelpers.callMethod(dexCache, "getDex")
        val bytes = XposedHelpers.callMethod(o, "getBytes") as ByteArray
        val path = "/data/data/$packageName/dump"
        val file = File(path, "source-" + bytes.size + ".dex")
        if (file.exists()) {
            log(file.name + " exists")
            return
        }
        FileUtils.writeByteToFile(bytes, file.absolutePath)
    }


    private fun attachBaseContextHook(lpparam: XC_LoadPackage.LoadPackageParam, application: Application) {
        val classLoader = application.classLoader
        XposedHelpers.findAndHookMethod(
            ClassLoader::class.java,
            "loadClass",
            String::class.java,
            Boolean::class.javaPrimitiveType,
            object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam?) {
                    log("loadClass->" + param!!.args[0])
                    val result = param.result as Class<*>
                    if (result != null) {
                        dump(lpparam.packageName, result)
                    }
                }
            })
        XposedHelpers.findAndHookMethod(
            "java.lang.ClassLoader",
            classLoader,
            "loadClass",
            String::class.java,
            Boolean::class.javaPrimitiveType,
            object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam?) {
                    log("loadClassWithclassLoader->" + param!!.args[0])
                    val result = param.result as Class<*>
                    if (result != null) {
                        dump(lpparam.packageName, result)
                    }
                }
            })
    }
}
