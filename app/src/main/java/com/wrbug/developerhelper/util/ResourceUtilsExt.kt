package com.wrbug.developerhelper.util

import android.content.Context
import com.wrbug.developerhelper.basecommon.BaseApp

fun Int.toResString(context: Context=BaseApp.instance): String {
    return context.getString(this)
}
