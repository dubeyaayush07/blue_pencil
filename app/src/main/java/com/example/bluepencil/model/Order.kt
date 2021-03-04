package com.example.bluepencil.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import java.util.*

data class Order (
    @DocumentId
    var id: String? = "",
    var userId: String? = "",
    var editorId: String? = "",
    var photoUrls: List<String?>?= null,
    var jobUrls: List<String?>?= null,
    var complete: Boolean? = false,
    var remark: String? = "Beautify",
    var type: String? = "",
    var editorName: String? = "",
    var date: Date = Date()
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.createStringArrayList(),
        parcel.createStringArrayList(),
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        TODO("date")
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(userId)
        parcel.writeString(editorId)
        parcel.writeStringList(photoUrls)
        parcel.writeStringList(jobUrls)
        parcel.writeValue(complete)
        parcel.writeString(remark)
        parcel.writeString(type)
        parcel.writeString(editorName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Order> {
        override fun createFromParcel(parcel: Parcel): Order {
            return Order(parcel)
        }

        override fun newArray(size: Int): Array<Order?> {
            return arrayOfNulls(size)
        }
    }
}