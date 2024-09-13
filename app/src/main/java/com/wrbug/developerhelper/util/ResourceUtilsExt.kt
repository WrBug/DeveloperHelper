package com.wrbug.developerhelper.util

import android.content.Context
import androidx.core.content.ContextCompat
import com.wrbug.developerhelper.base.BaseApp

fun Int.getString(context: Context = BaseApp.instance): String {
    return context.getString(this)
}

fun Int.getColor(context: Context = BaseApp.instance): Int {
    return ContextCompat.getColor(context, this)
}

fun Int.getString(vararg formatArgs: Any): String {
    return BaseApp.instance.getString(this, *formatArgs)
}

fun getString(resId: Int): String {
    return BaseApp.instance.getString(resId)
}