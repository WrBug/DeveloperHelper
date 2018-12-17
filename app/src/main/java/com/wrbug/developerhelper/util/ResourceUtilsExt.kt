package com.wrbug.developerhelper.util

import android.content.Context
import com.wrbug.developerhelper.basecommon.BaseApp

fun Int.toResString(context: Context): String {
    return context.getString(this)
}

fun Int.toResString(): String = toResString(BaseApp.instance)