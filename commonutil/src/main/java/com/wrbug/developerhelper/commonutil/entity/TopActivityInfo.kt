package com.wrbug.developerhelper.commonutil.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class TopActivityInfo(
    var activity: String = "", var fragments: Array<FragmentInfo>? = null
): Parcelable {

    var viewIdHex = HashMap<String, String>()
}