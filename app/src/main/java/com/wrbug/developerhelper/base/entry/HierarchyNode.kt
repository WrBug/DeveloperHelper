package com.wrbug.developerhelper.base.entry

import android.graphics.Rect
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@Parcelize
data class HierarchyNode(
    var id: Long = -1L,
    var screenBounds: Rect? = null,
    var parentBounds: Rect? = null,
    var checkable: Boolean = false,
    var checked: Boolean = false,
    var widget: String = "",
    var clickable: Boolean = false,
    var contentDesc: String = "",
    var enabled: Boolean = false,
    var focusable: Boolean = false,
    var focused: Boolean = false,
    var index: String = "",
    var longClickable: Boolean = false,
    var packageName: String = "",
    var password: Boolean = false,
    var scrollable: Boolean = false,
    var selected: Boolean = false,
    var text: String = "",
    var resourceId: String = "",
    var idHex: String? = null,
    var parentId: Long = -1L,
    var childId: ArrayList<HierarchyNode> = arrayListOf()
): Parcelable, Serializable