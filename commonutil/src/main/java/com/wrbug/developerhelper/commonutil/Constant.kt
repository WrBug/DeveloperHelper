package com.wrbug.developerhelper.commonutil

import android.os.Build

object Constant {
    private const val DATA_MIRROR_DIR = "/data_mirror/data_ce/null/0"
    private const val DATA_DIR = "/data/data"

    fun getDataDir(packageName: String): String {
        return "$dataDir/$packageName"
    }

    val dataDir by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            DATA_MIRROR_DIR
        } else {
            DATA_DIR
        }
    }
}