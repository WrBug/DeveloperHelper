package com.wrbug.developerhelper.model.entity

import kotlinx.parcelize.Parcelize
import java.io.File
import java.io.Serializable

data class BackupAppData(
    val appName: String,
    val packageName: String,
    val rootDir: File,
    val backupMap: HashMap<String, BackupAppItemInfo>,
    val iconPath: File?
) : Serializable
