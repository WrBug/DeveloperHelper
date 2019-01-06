package com.wrbug.developerhelper.util

import android.net.Uri
import android.os.Environment
import com.wrbug.developerhelper.commonutil.shell.ShellManager
import java.io.File

object BackupUtils {
    private val backupDir: File by lazy {
        val file = File(Environment.getExternalStorageDirectory(), "com.wrbug.developerHelper/backup")
        if (file.exists().not()) {
            file.mkdirs()
        }
        file
    }

    fun backupApk(packageName: String, apkPath: String, fileName: String): Uri? {
        val apkDir = File(backupDir, "apks/$packageName/$fileName")
        if (ShellManager.cpFile(apkPath, apkDir.absolutePath)) {
            return apkDir.toUri()
        }
        return null
    }
}