package com.wrbug.developerhelper.model.entity

import com.google.gson.annotations.SerializedName

data class BackupAppItemInfo(
    @SerializedName("backupApk")
    var backupApk: Boolean = false,
    @SerializedName("backupData")
    var backupData: Boolean = false,
    @SerializedName("backupAndroidData")
    var backupAndroidData: Boolean = false,
    @SerializedName("apkFile")
    var apkFile: String = "",
    @SerializedName("versionName")
    var versionName: String = "",
    @SerializedName("versionCode")
    var versionCode: Long = 0,
    @SerializedName("packageName")
    var packageName: String = "",
    @SerializedName("time")
    var time: Long = 0,
    @SerializedName("dataFile")
    var dataFile: String = "",
    @SerializedName("androidDataFile")
    var androidDataFile: String = "",
) {
    companion object {
        val EMPTY = BackupAppItemInfo()
    }
}
