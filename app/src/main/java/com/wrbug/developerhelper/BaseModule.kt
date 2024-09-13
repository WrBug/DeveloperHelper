package com.wrbug.developerhelper

import android.app.Application
import com.wrbug.developerhelper.commonutil.CommonUtils
import com.wrbug.developerhelper.mmkv.manager.MMKVManager

object BaseModule {
    fun init(application: Application) {
        MMKVManager.register(application)
        CommonUtils.register(application)
    }
}