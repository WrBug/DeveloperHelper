package com.wrbug.developerhelper.xposed.dumpdex

import android.app.Application
import android.app.Instrumentation
import android.content.Context
import com.wrbug.developerhelper.basecommon.showToast

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
import java.util.ArrayList

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
                    param?.apply {
                        log("Application=$result")
                        dump(lpparam.packageName, result.javaClass)
                        attachBaseContextHook(lpparam, result as Application)
                    }
                }
            })

        XposedHelpers.findClassIfExists(type.application, lpparam.classLoader)?.apply {
            var list = ArrayList<String>()
            for (method in methods) {
                list.add(method.name)
            }
            for (method in declaredMethods) {
                list.add(method.name)
            }
            "开始hook application ${list.size} 方法".xposedLog()
            list.forEach {
                XposedBridge.hookAllMethods(this, it, object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam?) {
                        param?.args?.apply {
                            if (this.isEmpty().not()) {
                                this.forEach { arg ->
                                    if (arg is Context) {
                                        "hook $arg".xposedLog()
                                        dump(lpparam.packageName, arg.javaClass)
                                        attachBaseContextHook(lpparam, arg)
                                    }
                                }
                            }
                        }
                    }
                })
            }
        }
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


    private fun attachBaseContextHook(lpparam: XC_LoadPackage.LoadPackageParam, context: Context) {
        val classLoader = context.classLoader
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
                    dump(lpparam.packageName, result)
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
                    dump(lpparam.packageName, result)
                }
            })
    }
}
