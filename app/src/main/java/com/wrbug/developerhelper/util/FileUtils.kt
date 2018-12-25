package com.wrbug.developerhelper.util

import android.media.MediaMetadataRetriever
import android.os.Environment
import org.dom4j.Document
import org.dom4j.io.OutputFormat
import org.dom4j.io.XMLWriter

import java.io.*
import org.dom4j.io.OutputFormat.createPrettyPrint


/**
 * Created by wrbug on 2017/8/23.
 */
object FileUtils {

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

    fun whiteXml(file: File, document: Document) {
        try {
            if (!file.exists()) {
                file.createNewFile()
            }
            val format = OutputFormat.createPrettyPrint()
            format.encoding = "utf-8"
            val writer = XMLWriter(OutputStreamWriter(FileOutputStream(file), "utf-8"), format)
            writer.write(document)
            writer.close()
        } catch (e: IOException) {
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


    // 根据文件后缀名获得对应的MIME类型。
    private fun getMimeType(filePath: String?): String {
        val mmr = MediaMetadataRetriever()
        var mime = "*/*"
        if (filePath != null) {
            try {
                mmr.setDataSource(filePath)
                mime = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
            } catch (e: IllegalStateException) {
                return mime
            } catch (e: IllegalArgumentException) {
                return mime
            } catch (e: RuntimeException) {
                return mime
            }

        }
        return mime
    }

}
