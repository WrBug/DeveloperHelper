package com.wrbug.developerhelper.ipc.processshare.data

import com.wrbug.developerhelper.commonutil.Base64
import com.wrbug.developerhelper.commonutil.fromJson
import com.wrbug.developerhelper.commonutil.shell.Shell
import com.wrbug.developerhelper.commonutil.toJson
import java.io.File

class IpcFileInfo(private var file: File) {

    fun save(data: Map<String, *>) {
        save((data.toJson() ?: "{}").toByteArray())
    }

    fun getData(): Map<String, Any> {
        try {
            if (!file.exists()) {
                return emptyMap()
            }
            val text = file.readText().replace("\n", "")
            val data = Base64.decode(text)
            return data.fromJson<Map<String, Any>>() ?: emptyMap()
        } catch (t: Throwable) {
            return emptyMap()
        }
    }

    private fun save(data: ByteArray) {
        val encode = Base64.encodeAsString(data)
        if (file.exists() && file.canWrite()) {
            saveFromJava(encode)
            return
        }
        val commandResult =
            Shell.SU.run("echo $encode > ${file.absolutePath}  && chmod 777 ${file.absolutePath}")
    }

    private fun saveFromJava(data: String) {
        file.writeText(data)
    }
}