package com.wrbug.developerhelper.commonutil

import java.io.File

fun File.safeRead(): String {
    return runCatching {
        readText()
    }.getOrDefault("")
}