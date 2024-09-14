package com.wrbug.developerhelper.model.entity

import com.google.gson.annotations.SerializedName

data class BackupAppInfo(
    @SerializedName("appName")
    var appName: String = "",
    @SerializedName("packageName")
    var packageName: String = "",
    @SerializedName("backupMap")
    val backupMap: HashMap<String, BackupAppItemInfo> = hashMapOf()
)
