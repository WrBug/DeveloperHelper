package com.github.megatronking.netbare.sample

import android.app.Application
import com.github.megatronking.netbare.NetBare
import com.github.megatronking.netbare.ssl.JKS
import com.wrbug.developerhelper.basewidgetimport.BaseWidget

class App : Application() {

    companion object {

        private lateinit var sInstance: App

        fun getInstance(): App {
            return sInstance
        }
    }

    override fun onCreate() {
        super.onCreate()
        sInstance = this
        // 创建自签证书
        BaseWidget.init(this)
    }



}