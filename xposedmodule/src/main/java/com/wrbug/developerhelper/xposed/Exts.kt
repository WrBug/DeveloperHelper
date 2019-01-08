package com.wrbug.developerhelper.xposed

import de.robv.android.xposed.XposedBridge
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

fun Throwable.xposedLog() {
    XposedBridge.log(this)
}


fun String.xposedLog(tag: String = "developerhelper.xposed--> ") {
    XposedBridge.log(tag + this)
}

fun InputStream.saveToFile(file: File) {
    if (file.exists().not()) {
        file.createNewFile()
    }
    val fileOutputStream = FileOutputStream(file)
    fileOutputStream.write(readBytes())
    fileOutputStream.flush()
    fileOutputStream.close()
}