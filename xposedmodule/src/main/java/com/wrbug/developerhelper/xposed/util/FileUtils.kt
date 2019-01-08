package com.wrbug.developerhelper.xposed.util

import de.robv.android.xposed.XposedBridge
import java.io.*

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

    fun readFile(file: File): String {
        val builder = StringBuilder()
        try {
            val fr = FileReader(file)
            var ch = fr.read()
            while (ch != -1) {
                builder.append(ch.toChar())
                ch = fr.read()
            }
        } catch (e: IOException) {
        }

        return builder.toString()
    }

    fun whiteFile(file: File, data: String) {
        try {
            if (!file.exists()) {
                file.createNewFile()
            }
            val fw = FileWriter(file)
            fw.write(data)
            fw.flush()
        } catch (e: IOException) {
        }

    }
}
