package com.wrbug.developerhelper.model.entry

import android.os.Parcel
import android.os.Parcelable

class TopActivityInfo() : Parcelable {
    var activity = ""
    var viewIdHex = HashMap<String, String>()
    var fragments: Array<FragmentInfo>? = null

    constructor(parcel: Parcel) : this() {
        activity = parcel.readString()
        fragments = parcel.createTypedArray(FragmentInfo)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(activity)
        parcel.writeTypedArray(fragments, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TopActivityInfo> {
        override fun createFromParcel(parcel: Parcel): TopActivityInfo {
            return TopActivityInfo(parcel)
        }

        override fun newArray(size: Int): Array<TopActivityInfo?> {
            return arrayOfNulls(size)
        }
    }


}