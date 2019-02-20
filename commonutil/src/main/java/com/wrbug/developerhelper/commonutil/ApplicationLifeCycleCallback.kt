package com.wrbug.developerhelper.commonutil

import android.app.Application
import android.content.Context

interface ApplicationLifeCycleCallback {
    fun attachBaseContext(context: Context)

    fun onCreate(application: Application)
}