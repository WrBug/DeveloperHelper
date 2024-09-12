package com.wrbug.developerhelper.util

import android.os.Environment
import com.wrbug.developerhelper.commonutil.Constant
import com.wrbug.developerhelper.commonutil.shell.ShellManager
import java.io.File

object BackupUtils {
    val backupDir: File by lazy {
        val file =
            File(Environment.getExternalStorageDirectory(), "com.wrbug.developerHelper/backup")
        if (file.exists().not()) {
            file.mkdirs()
        }
        file
    }

    fun backupApk(
        packageName: String,
        dateDir: String,
        apkPath: String,
        fileName: String
    ): String? {
        val apkDir = File(backupDir, "$packageName/$dateDir/$fileName")
        if (ShellManager.cpFile(apkPath, apkDir.absolutePath)) {
            return apkDir.absolutePath
        }
        return null
    }

    fun backupAppData(dateDir: String, packageName: String): File? {
        val backupDataDir = File(
            backupDir,
            "$packageName/$dateDir/data"
        )
        val dataDir = Constant.getDataDir(packageName)
        if (ShellManager.cpFile("$dataDir/", backupDataDir.absolutePath)) {
            return backupDataDir
        }
        return null
    }
}