package com.wrbug.developerhelper.xposed.util

import android.app.Application
import android.content.Context
import com.wrbug.developerhelper.xposed.xposedLog
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

object ApplicationHelper {
    private val applicationList = ArrayList<Context>()
    private val hookedApplication = ArrayList<Context>()
    fun hook(lpparam: XC_LoadPackage.LoadPackageParam, action: Context.() -> Unit) {
        XposedHelpers.findAndHookMethod(
            "android.content.ContextWrapper",
            lpparam.classLoader,
            "attachBaseContext",
            Context::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam?) {
                    param?.args?.let {
                        val context = it[0] as Context
                        checkContext(context, action)
                    }
                }
            })
        hookNewApplication(lpparam.classLoader, action)
        hookApplicationConstructors(lpparam.classLoader, action)
    }

    private fun hookApplicationConstructors(classLoader: ClassLoader, action: Context.() -> Unit) {
        XposedHelpers.findClassIfExists(Application::class.java.name, classLoader)?.apply {
            XposedBridge.hookAllConstructors(this, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    "Application: ${param?.thisObject} newInstance".xposedLog()
                    hookApplication(param?.thisObject as Application, action)
                }
            })
        }
    }

    private fun hookNewApplication(classLoader: ClassLoader, action: Context.() -> Unit) {
        XposedBridge.hookAllMethods(
            classLoader.loadClass("android.app.Instrumentation"),
            "newApplication", newApplicationHook(action)
        )
    }

    private fun newApplicationHook(action: Context.() -> Unit) = object : XC_MethodHook() {
        @Throws(Throwable::class)
        override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam?) {
            "newApplication=${param?.result}".xposedLog()
            param?.result.apply {
                checkContext(this as Context, action)
                hookNewApplication(this.classLoader, action)
                hookApplication(this, action)
            }
        }
    }

    private fun hookApplication(context: Context, action: Context.() -> Unit) {
        if (hookedApplication.contains(context)) {
            return
        }
        hookedApplication.add(context)
        val methodNames = HashSet<String>()
        val methods = arrayOf(*context.javaClass.declaredMethods, *context.javaClass.methods)
        methods.forEach { method ->
            for (parameterType in method.parameterTypes) {
                if (Context::class.java.isAssignableFrom(parameterType)) {
                    methodNames.add(method.name)
                    break
                }
            }
        }
        for (methodName in methodNames) {
            "hook app method=$methodName".xposedLog()
            XposedBridge.hookAllMethods(context.javaClass, methodName, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    "hook app mathod=${param?.method?.name}".xposedLog()
                    param?.args?.let {
                        for (any in it) {
                            any?.apply {
                                if (any is Context) {
                                    checkContext(any, action)
                                }
                            }
                        }
                    }
                }
            })
        }
    }

    private fun checkContext(context: Context, action: Context.() -> Unit) {
        if (applicationList.contains(context).not()) {
            applicationList.add(context)
            context.action()
        }
    }
}