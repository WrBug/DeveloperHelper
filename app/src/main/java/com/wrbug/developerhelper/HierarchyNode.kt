package com.wrbug.developerhelper

import android.graphics.Rect
import android.os.Parcel
import android.os.Parcelable

class HierarchyNode() : Parcelable {
    var id: Long = (2L shl 32)-1 and System.currentTimeMillis()
    var bounds: Rect? = null
    var checkable: Boolean = false
    var checked: Boolean = false
    var classPath: String = ""
    var clickable: Boolean = false
    var contentdesc: String = ""
    var enabled: Boolean = false
    var focusable: Boolean = false
    var focused: Boolean = false
    var index: String = ""
    var longclickable: Boolean = false
    var packagePath: String = ""
    var password: Boolean = false
    var scrollable: Boolean = false
    var selected: Boolean = false
    var text: String = ""
    var resourceId: String = ""
    var parentId: Long = 0
    val childId: ArrayList<Long> = arrayListOf()

    constructor(parcel: Parcel) : this() {
        bounds = parcel.readParcelable(Rect::class.java.classLoader)
        checkable = parcel.readByte() != 0.toByte()
        checked = parcel.readByte() != 0.toByte()
        classPath = parcel.readString()
        clickable = parcel.readByte() != 0.toByte()
        contentdesc = parcel.readString()
        enabled = parcel.readByte() != 0.toByte()
        focusable = parcel.readByte() != 0.toByte()
        focused = parcel.readByte() != 0.toByte()
        index = parcel.readString()
        longclickable = parcel.readByte() != 0.toByte()
        packagePath = parcel.readString()
        password = parcel.readByte() != 0.toByte()
        scrollable = parcel.readByte() != 0.toByte()
        selected = parcel.readByte() != 0.toByte()
        text = parcel.readString()
        resourceId = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(bounds, flags)
        parcel.writeByte(if (checkable) 1 else 0)
        parcel.writeByte(if (checked) 1 else 0)
        parcel.writeString(classPath)
        parcel.writeByte(if (clickable) 1 else 0)
        parcel.writeString(contentdesc)
        parcel.writeByte(if (enabled) 1 else 0)
        parcel.writeByte(if (focusable) 1 else 0)
        parcel.writeByte(if (focused) 1 else 0)
        parcel.writeString(index)
        parcel.writeByte(if (longclickable) 1 else 0)
        parcel.writeString(packagePath)
        parcel.writeByte(if (password) 1 else 0)
        parcel.writeByte(if (scrollable) 1 else 0)
        parcel.writeByte(if (selected) 1 else 0)
        parcel.writeString(text)
        parcel.writeString(resourceId)
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