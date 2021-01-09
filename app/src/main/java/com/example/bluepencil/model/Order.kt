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
    var photoUrl: String? = "",
    var jobUrl: String? = "",
    var complete: Boolean? = false,
    var date: Date = Date()
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        TODO("date")
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(userId)
        parcel.writeString(editorId)
        parcel.writeString(photoUrl)
        parcel.writeString(jobUrl)
        parcel.writeValue(complete)
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