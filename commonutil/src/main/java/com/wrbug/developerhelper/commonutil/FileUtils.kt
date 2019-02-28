package com.wrbug.developerhelper.commonutil

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
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
        }
    }


    fun inputstreamtofile(ins: InputStream, file: File) {
        try {
            if (file.exists()) {
                file.createNewFile()
            }
            val os = FileOutputStream(file)
            os.write(ins.readBytes())
            os.close()
            ins.close()
        } catch (t: Throwable) {

        }

    }


    fun inputStream2String(ins: InputStream): String {
        val out = StringBuffer()
        val b = ByteArray(4096)
        try {
            var n: Int = ins.read(b)
            while (n != -1) {
                out.append(String(b, 0, n))
                n = ins.read(b)
            }
        } catch (e: IOException) {
            return ""
        }

        return out.toString()
    }


    fun readFile(file: File): String {
        var inputStream: FileInputStream? = null
        try {
            inputStream = FileInputStream(file)
            val bytes = ByteArray(inputStream.available())
            inputStream.read(bytes)
            return String(bytes)
        } catch (e: IOException) {
        } finally {
            inputStream?.close()
        }
        return ""
    }

    fun whiteFile(file: File, data: String) {
        var outputStream: FileOutputStream? = null
        try {
            outputStream = FileOutputStream(file)
            outputStream.write(data.toByteArray())
        } catch (e: IOException) {
        }finally {
            outputStream?.close()
        }

    }
}


fun File.toUri(context: Context): Uri? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        FileProvider.getUriForFile(context, "com.wrbug.developerhelper.fileprovider", this)
    } else {
        Uri.fromFile(this)
    }
}
