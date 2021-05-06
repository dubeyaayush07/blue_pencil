package xyz.bluepencil.bluepencil.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.android.parcel.Parcelize
import java.util.*


@Parcelize
data class Order (
    @DocumentId
    var id: String? = "",
    var userId: String? = "",
    var editorId: String? = "",
    var photoUrls: List<String?>?= null,
    var jobUrls: List<String?>?= null,
    var complete: Boolean? = false,
    var count: Int? = 1,
    var remark: String? = "Beautify",
    var link: String? = "",
    var type: String? = "",
    var editorName: String? = "",
    var date: Date = Date()
): Parcelable