package com.github.megatronking.netbare.sample

import android.app.Application
import com.wrbug.developerhelper.basewidgetimport.BaseModule

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
        BaseModule.init(this)
    }



}