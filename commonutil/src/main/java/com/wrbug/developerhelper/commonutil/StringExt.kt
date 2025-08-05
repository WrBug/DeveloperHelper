package com.wrbug.developerhelper.commonutil

fun String?.ifNotEmpty(call: (String) -> String): String {
    if (isNullOrEmpty()) {
        return ""
    }
    return call(this)
}