package com.wrbug.developerhelper.model.entity

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class BackupAppItemInfo(
    @SerializedName("backupFile")
    var backupFile: String = "",
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
    @SerializedName("memo")
    var memo: String = ""
) : Parcelable, Serializable {
    companion object {
        val EMPTY = BackupAppItemInfo()
    }
}
