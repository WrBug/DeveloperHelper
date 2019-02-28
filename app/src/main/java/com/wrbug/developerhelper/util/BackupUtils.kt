package com.wrbug.developerhelper.util

import android.net.Uri
import android.os.Environment
import com.wrbug.developerhelper.basecommon.BaseApp
import com.wrbug.developerhelper.commonutil.shell.ShellManager
import com.wrbug.developerhelper.commonutil.toUri
import com.wrbug.developerhelper.xposed.developerhelper.DeveloperHelper
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
            return apkDir.toUri(BaseApp.instance)
        }
        return null
    }

    fun backupAppData(packageName: String, dataDir: String): File? {
        val backupDataDir =
            File(backupDir, "datas/$packageName/${System.currentTimeMillis().format("yyyy-MM-dd-HH_mm_ss")}")
        if (ShellManager.cpFile(dataDir, backupDataDir.absolutePath)) {
            return backupDataDir
        }
        return null
    }
}