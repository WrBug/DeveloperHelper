package com.wrbug.developerhelper.model.entity

import java.io.File

data class BackupAppData(
    val appName: String,
    val packageName: String,
    val rootDir: File,
    val backupMap: Map<String, BackupAppItemInfo>,
    val iconPath: File?
)
