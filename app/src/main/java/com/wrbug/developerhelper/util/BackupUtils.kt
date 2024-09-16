package com.wrbug.developerhelper.util

import android.content.Context
import android.os.Environment
import com.wrbug.developerhelper.commonutil.Constant
import com.wrbug.developerhelper.commonutil.entity.ApkInfo
import com.wrbug.developerhelper.commonutil.fromJson
import com.wrbug.developerhelper.commonutil.safeCreateSingle
import com.wrbug.developerhelper.commonutil.safeRead
import com.wrbug.developerhelper.commonutil.shell.ShellManager
import com.wrbug.developerhelper.commonutil.toJson
import com.wrbug.developerhelper.model.entity.BackupAppData
import com.wrbug.developerhelper.model.entity.BackupAppInfo
import com.wrbug.developerhelper.model.entity.BackupAppItemInfo
import io.reactivex.rxjava3.core.Single
import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.AdaptiveIcon
import java.io.File

object BackupUtils {
    private const val ANDROID_DATA_TAR = "android_data.tar"
    private const val DATA_TAR = "data.tar"
    private const val CONFIG_JSON = "config.json"
    private const val ICON_PNG = "icon.png"
    private val backupRootDir: File by lazy {
        val file =
            File(Environment.getExternalStorageDirectory(), "DeveloperHelper/backup")
        if (file.exists().not()) {
            file.mkdirs()
        }
        file
    }

    fun getCurrentAppBackupDir(packageName: String, dateDir: String): File {
        return File(getAppBackupDir(packageName), dateDir)
    }

    fun getAppBackupDir(packageName: String): File {
        return File(backupRootDir, packageName).apply {
            if (!exists()) {
                mkdirs()
            }
        }
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
        val backupDataDir = File(getCurrentAppBackupDir(packageName, dateDir), DATA_TAR)
        val dataDir = Constant.getDataDir(packageName)
        if (ShellManager.tarCF(backupDataDir.absolutePath, dataDir)) {
            return backupDataDir
        }
        return null
    }

    fun backupAppAndroidData(dateDir: String, packageName: String): String? {
        val backupDataDir = File(
            getCurrentAppBackupDir(packageName, dateDir), ANDROID_DATA_TAR
        )
        val dataDir =
            Environment.getExternalStorageDirectory().absolutePath + "/Android/data/" + packageName
        if (!File(dataDir).exists()) {
            return ""
        }
        if (ShellManager.tarCF(backupDataDir.absolutePath, dataDir)) {
            return backupDataDir.absolutePath
        }
        return null
    }

    fun saveBackupInfo(
        apkInfo: ApkInfo,
        backupAppItemInfo: BackupAppItemInfo,
        tarFile: String
    ): Boolean {
        val apkFile = apkInfo.applicationInfo.publicSourceDir
        val tmpApkFile = File(getAppBackupDir(apkInfo.applicationInfo.packageName), "tmp.apk")
        ShellManager.cpFile(apkFile, tmpApkFile.absolutePath)
        runCatching {
            ApkFile(tmpApkFile).allIcons.find { it.isFile }?.data?.let {
                File(getAppBackupDir(apkInfo.applicationInfo.packageName), ICON_PNG).writeBytes(it)
            }
        }
        tmpApkFile.delete()
        backupAppItemInfo.androidDataFile = ANDROID_DATA_TAR
        backupAppItemInfo.dataFile = DATA_TAR
        val configFile = File(getAppBackupDir(apkInfo.applicationInfo.packageName), CONFIG_JSON)
        val info = configFile.safeRead().fromJson<BackupAppInfo>() ?: BackupAppInfo()
        info.appName = apkInfo.getAppName()
        info.packageName = apkInfo.applicationInfo.packageName
        info.backupMap[tarFile] = backupAppItemInfo
        configFile.writeText(info.toJson().orEmpty())
        return true
    }


    fun zipBackupFile(packageName: String, dateDir: String): File? {
        val target = File(getAppBackupDir(packageName), "$dateDir.tar")
        val src = getCurrentAppBackupDir(packageName, dateDir).absolutePath
        if (ShellManager.tarCF(target.absolutePath, src)) {
            ShellManager.rmFile(src)
            return target
        }
        return null
    }

    fun getAllBackupInfo(): Single<List<BackupAppData>> {
        return safeCreateSingle {
            val list = arrayListOf<BackupAppData>()
            backupRootDir.listFiles()?.forEach { root ->
                val configJson = File(root, CONFIG_JSON)
                if (!configJson.exists()) {
                    return@forEach
                }
                val info = configJson.safeRead().fromJson<BackupAppInfo>() ?: return@forEach
                val map = info.backupMap.filter { File(root, it.key).exists() }
                val icoFile = File(root, ICON_PNG).takeIf { it.exists() }
                list.add(BackupAppData(info.appName, info.packageName, root, HashMap(map), icoFile))
            }
            it.onSuccess(list)
        }
    }


}