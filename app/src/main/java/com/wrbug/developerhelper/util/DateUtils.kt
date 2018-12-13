package com.wrbug.developerhelper.util

import java.text.SimpleDateFormat
import java.util.*


fun Date.format(format: String): String {
    val simpleDateFormat = SimpleDateFormat(format)
    return simpleDateFormat.format(this)
}


fun Long.format(format: String): String {
    return Date(this).format(format)
}

fun Long.formatyyyyMMddHHmmss(): String {
    return Date(this).format("yyyy-MM-dd HH:mm:ss")
}