package com.wrbug.developerhelper.basecommon

import android.app.Application

open class BaseApp : Application() {
    companion object {
        var instance: BaseApp? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}