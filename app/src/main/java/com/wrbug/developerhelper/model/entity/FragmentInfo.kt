package com.wrbug.developerhelper.model.entity

import android.os.Parcel
import android.os.Parcelable

class FragmentInfo() : Parcelable {
    var name = ""
    var fragmentId = ""
    var containerId = ""
    var tag = ""
    var state = 0
    var index = 0
    var who = ""
    var backStackNesting = 0
    var added = false
    var removing = false
    var fromLayout = false
    var inLayout = false
    var hidden = true
    var detached = false

    constructor(parcel: Parcel) : this() {
        name = parcel.readString()
        fragmentId = parcel.readString()
        containerId = parcel.readString()
        tag = parcel.readString()
        state = parcel.readInt()
        index = parcel.readInt()
        who = parcel.readString()
        backStackNesting = parcel.readInt()
        added = parcel.readByte() != 0.toByte()
        removing = parcel.readByte() != 0.toByte()
        fromLayout = parcel.readByte() != 0.toByte()
        inLayout = parcel.readByte() != 0.toByte()
        hidden = parcel.readByte() != 0.toByte()
        detached = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(fragmentId)
        parcel.writeString(containerId)
        parcel.writeString(tag)
        parcel.writeInt(state)
        parcel.writeInt(index)
        parcel.writeString(who)
        parcel.writeInt(backStackNesting)
        parcel.writeByte(if (added) 1 else 0)
        parcel.writeByte(if (removing) 1 else 0)
        parcel.writeByte(if (fromLayout) 1 else 0)
        parcel.writeByte(if (inLayout) 1 else 0)
        parcel.writeByte(if (hidden) 1 else 0)
        parcel.writeByte(if (detached) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FragmentInfo> {
        override fun createFromParcel(parcel: Parcel): FragmentInfo {
            return FragmentInfo(parcel)
        }

        override fun newArray(size: Int): Array<FragmentInfo?> {
            return arrayOfNulls(size)
        }
    }


}