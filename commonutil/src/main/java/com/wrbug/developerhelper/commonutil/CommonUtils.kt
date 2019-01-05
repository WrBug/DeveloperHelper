package com.wrbug.developerhelper.commonutil

import android.app.Application
import android.content.Context

object CommonUtils {
    lateinit var application: Application
    fun register(ctx: Context) {
        application = ctx.applicationContext as Application
    }
}