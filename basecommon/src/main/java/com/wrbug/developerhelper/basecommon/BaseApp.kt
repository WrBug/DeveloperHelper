package com.wrbug.developerhelper.basecommon

import android.app.Application

open class BaseApp : Application() {
    companion object {
        lateinit var instance: BaseApp
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}