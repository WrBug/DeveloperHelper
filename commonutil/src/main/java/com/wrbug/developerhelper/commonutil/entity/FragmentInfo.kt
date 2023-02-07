package com.wrbug.developerhelper.commonutil.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FragmentInfo(
    var name: String = "",
    var containerId: String = "",
    var tag: String = "",
    var state: Int = 0,
    var index: Int = 0,
    var who: String = "",
    var backStackNesting: Int = 0,
    var added: Boolean = false,
    var removing: Boolean = false,
    var fromLayout: Boolean = false,
    var inLayout: Boolean = false,
    var hidden: Boolean = true,
    var detached: Boolean = false
): Parcelable {

}