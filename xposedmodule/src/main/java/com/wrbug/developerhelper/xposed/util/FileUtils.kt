package com.wrbug.developerhelper.xposed.util

import java.io.FileOutputStream

import de.robv.android.xposed.XposedBridge

/**
 * Created by wrbug on 2017/8/23.
 */
object FileUtils {

    fun writeByteToFile(data: ByteArray, path: String) {
        try {
            val localFileOutputStream = FileOutputStream(path)
            localFileOutputStream.write(data)
            localFileOutputStream.close()
        } catch (e: Exception) {
            XposedBridge.log(e)
        }
    }
}
