package com.wrbug.developerhelper.basecommon

import android.app.Application

abstract class BaseApp : Application() {
    companion object {
        lateinit var instance: BaseApp
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }


    abstract fun showToast(msg: String)

    fun showToast(id: Int){
        showToast(getString(id))
    }

}