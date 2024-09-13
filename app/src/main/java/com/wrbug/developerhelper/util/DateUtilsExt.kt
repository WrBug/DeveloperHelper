package com.wrbug.developerhelper.util

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*


@SuppressLint("SimpleDateFormat")
fun Date.format(format: String="yyyy-MM-dd HH:mm:ss"): String {
    val simpleDateFormat = SimpleDateFormat(format)
    return simpleDateFormat.format(this)
}


fun Long.format(format: String="yyyy-MM-dd HH:mm:ss"): String {
    return Date(this).format(format)
}
