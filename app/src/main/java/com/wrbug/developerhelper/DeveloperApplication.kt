package com.wrbug.developerhelper

import android.app.Application
import com.tencent.mmkv.MMKV



class DeveloperApplication : Application() {
    companion object {
        var instance: DeveloperApplication? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        MMKV.initialize(this)
    }
}
