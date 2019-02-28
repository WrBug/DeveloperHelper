package com.wrbug.developerhelper.xposed.dumpdex

import android.content.Context
import com.wrbug.developerhelper.commonutil.FileUtils

import com.wrbug.developerhelper.xposed.util.ApplicationHelper
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
        ApplicationHelper.hook(lpparam) {
            dump(lpparam.packageName, this.javaClass)
            attachBaseContextHook(lpparam, this.classLoader)
        }

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
                                        attachBaseContextHook(lpparam, arg.classLoader)
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


    private fun attachBaseContextHook(lpparam: XC_LoadPackage.LoadPackageParam, classLoader: ClassLoader) {
        XposedBridge.hookAllMethods(
            ClassLoader::class.java,
            "loadClass",
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
