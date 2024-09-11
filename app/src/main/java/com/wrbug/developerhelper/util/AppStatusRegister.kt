package com.wrbug.developerhelper.util

import android.app.Activity
import android.app.Application
import android.os.Bundle

object AppStatusRegister {
    private var count = 0
    private val backgroundMap = hashMapOf<String, () -> Unit>()
    fun init(application: Application) {
        application.registerActivityLifecycleCallbacks(object :
            Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

            }

            override fun onActivityStarted(activity: Activity) {
                count++
            }

            override fun onActivityResumed(activity: Activity) {
            }

            override fun onActivityPaused(activity: Activity) {
            }

            override fun onActivityStopped(activity: Activity) {
                count--
                backgroundCheck()
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityDestroyed(activity: Activity) {
            }

        })
    }


    fun registerBackgroundListener(key: String, listener: () -> Unit) {
        backgroundMap[key] = listener
    }

    fun removeBackgroundListener(key: String) {
        backgroundMap.remove(key)
    }

    private fun backgroundCheck() {
        if (count == 0) {
            backgroundMap.values.forEach { it() }
        }
    }

}