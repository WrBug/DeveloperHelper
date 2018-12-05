package com.wrbug.developerhelper

import android.app.Application
import com.tencent.mmkv.MMKV
import com.wrbug.developerhelper.basecommon.BaseApp


class DeveloperApplication : BaseApp() {


    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
    }
}
