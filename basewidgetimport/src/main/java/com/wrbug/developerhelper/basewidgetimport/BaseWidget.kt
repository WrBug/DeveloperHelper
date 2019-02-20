package com.wrbug.developerhelper.basewidgetimport

import android.app.Application
import com.github.megatronking.netbare.NetBare
import com.wrbug.developerhelper.commonutil.CommonUtils
import com.wrbug.developerhelper.mmkv.manager.MMKVManager

object BaseWidget {
    fun init(application: Application) {
        MMKVManager.register(application)
        CommonUtils.register(application)
        NetBare.get().attachApplication(application, BuildConfig.DEBUG)
    }
}