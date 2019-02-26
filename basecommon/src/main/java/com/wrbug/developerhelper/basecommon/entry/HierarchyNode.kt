package com.wrbug.developerhelper.basecommon.entry

import android.graphics.Rect
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.reflect.TypeToken
import com.wrbug.developerhelper.commonutil.JsonHelper
import java.io.Serializable

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class HierarchyNode() : Parcelable, Serializable {
    var id: Long = -1L
    var screenBounds: Rect? = null
    var parentBounds: Rect? = null
    var checkable: Boolean = false
    var checked: Boolean = false
    var widget: String = ""
    var clickable: Boolean = false
    var contentDesc: String = ""
    var enabled: Boolean = false
    var focusable: Boolean = false
    var focused: Boolean = false
    var index: String = ""
    var longClickable: Boolean = false
    var packageName: String = ""
    var password: Boolean = false
    var scrollable: Boolean = false
    var selected: Boolean = false
    var text: String = ""
    var resourceId: String = ""
    var idHex: String? = null
    var parentId: Long = -1L
    var childId: ArrayList<HierarchyNode> = arrayListOf()

    constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        screenBounds = parcel.readParcelable(Rect::class.java.classLoader)
        parentBounds = parcel.readParcelable(Rect::class.java.classLoader)
        checkable = parcel.readByte() != 0.toByte()
        checked = parcel.readByte() != 0.toByte()
        widget = parcel.readString()
        clickable = parcel.readByte() != 0.toByte()
        contentDesc = parcel.readString()
        enabled = parcel.readByte() != 0.toByte()
        focusable = parcel.readByte() != 0.toByte()
        focused = parcel.readByte() != 0.toByte()
        index = parcel.readString()
        longClickable = parcel.readByte() != 0.toByte()
        packageName = parcel.readString()
        password = parcel.readByte() != 0.toByte()
        scrollable = parcel.readByte() != 0.toByte()
        selected = parcel.readByte() != 0.toByte()
        text = parcel.readString()
        resourceId = parcel.readString()
        idHex = parcel.readString()
        parentId = parcel.readLong()
        childId = JsonHelper.fromJson(parcel.readString(), object : TypeToken<ArrayList<HierarchyNode>>() {}.type) ?:
                arrayListOf()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeParcelable(screenBounds, flags)
        parcel.writeParcelable(parentBounds, flags)
        parcel.writeByte(if (checkable) 1 else 0)
        parcel.writeByte(if (checked) 1 else 0)
        parcel.writeString(widget)
        parcel.writeByte(if (clickable) 1 else 0)
        parcel.writeString(contentDesc)
        parcel.writeByte(if (enabled) 1 else 0)
        parcel.writeByte(if (focusable) 1 else 0)
        parcel.writeByte(if (focused) 1 else 0)
        parcel.writeString(index)
        parcel.writeByte(if (longClickable) 1 else 0)
        parcel.writeString(packageName)
        parcel.writeByte(if (password) 1 else 0)
        parcel.writeByte(if (scrollable) 1 else 0)
        parcel.writeByte(if (selected) 1 else 0)
        parcel.writeString(text)
        parcel.writeString(resourceId)
        parcel.writeString(idHex)
        parcel.writeLong(parentId)
        parcel.writeString(JsonHelper.toJson(childId))
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HierarchyNode> {
        override fun createFromParcel(parcel: Parcel): HierarchyNode {
            return HierarchyNode(parcel)
        }

        override fun newArray(size: Int): Array<HierarchyNode?> {
            return arrayOfNulls(size)
        }
    }

}