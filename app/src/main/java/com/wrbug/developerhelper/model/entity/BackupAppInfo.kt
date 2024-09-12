package com.wrbug.developerhelper.model.entity

data class BackupAppInfo(
    var backupApk: Boolean = false,
    var backupData: Boolean = false,
    var backupAndroidData: Boolean = false,
    var versionName: String = "",
    var versionCode: Int = 0,
    var packageName: String = ""
)
