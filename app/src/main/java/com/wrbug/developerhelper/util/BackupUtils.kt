package com.wrbug.developerhelper.util

import android.os.Environment
import com.wrbug.developerhelper.commonutil.Constant
import com.wrbug.developerhelper.commonutil.shell.ShellManager
import java.io.File

object BackupUtils {
    private val backupRootDir: File by lazy {
        val file =
            File(Environment.getExternalStorageDirectory(), "DeveloperHelper/backup")
        if (file.exists().not()) {
            file.mkdirs()
        }
        file
    }

    fun getCurrentAppBackupDir(packageName: String, dateDir: String): File {
        return File(backupRootDir, "$packageName/$dateDir")
    }

    fun backupApk(
        packageName: String,
        dateDir: String,
        apkPath: String,
        fileName: String
    ): String? {
        val apkDir = File(getCurrentAppBackupDir(packageName, dateDir), fileName)
        if (ShellManager.cpFile(apkPath, apkDir.absolutePath)) {
            return apkDir.absolutePath
        }
        return null
    }

    fun backupAppData(dateDir: String, packageName: String): File? {
        val backupDataDir = File(getCurrentAppBackupDir(packageName, dateDir), "data.tar")
        val dataDir = Constant.getDataDir(packageName)
        if (ShellManager.tarCF(backupDataDir.absolutePath, dataDir)) {
            return backupDataDir
        }
        return null
    }

    fun backupAppAndroidData(dateDir: String, packageName: String): File? {
        val backupDataDir = File(
            getCurrentAppBackupDir(packageName, dateDir), "android_data.tar"
        )
        val dataDir =
            Environment.getExternalStorageDirectory().absolutePath + "/Android/data/" + packageName
        if (ShellManager.tarCF(backupDataDir.absolutePath, dataDir)) {
            return backupDataDir
        }
        return null
    }
}