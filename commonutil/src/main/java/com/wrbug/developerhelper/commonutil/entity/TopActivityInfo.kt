package com.wrbug.developerhelper.commonutil.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class TopActivityInfo(
    var packageName: String = "",
    var activity: String = "",
    var fragments: Array<FragmentInfo>? = null
): Parcelable {

    fun setFullActivity(ac: String) {
        kotlin.runCatching {
            val arr = ac.split("/")
            packageName = arr[0]
            activity = arr[0] + arr[1]
        }
    }

    var viewIdHex = HashMap<String, String>()
}