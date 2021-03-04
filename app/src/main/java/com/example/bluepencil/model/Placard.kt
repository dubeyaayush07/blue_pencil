 package com.example.bluepencil.model

import android.os.Parcel
import android.os.Parcelable


data class Placard (
    var cost: Int = 0,
    var userName: String? = "",
    var userId: String? = "",
    var url: String? = "",
    var type: String? = "",
    var free: Boolean? = true,
    var tags: List<String?>? = null
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.createStringArrayList()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(cost)
        parcel.writeString(userName)
        parcel.writeString(userId)
        parcel.writeString(url)
        parcel.writeString(type)
        parcel.writeValue(free)
        parcel.writeStringList(tags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Placard> {
        override fun createFromParcel(parcel: Parcel): Placard {
            return Placard(parcel)
        }

        override fun newArray(size: Int): Array<Placard?> {
            return arrayOfNulls(size)
        }
    }
}