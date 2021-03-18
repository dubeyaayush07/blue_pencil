package xyz.bluepencil.bluepencil.model

import com.google.firebase.firestore.DocumentId


data class User(
    @DocumentId
    var id: String? = "",
    var uid: String? = "",
    var name: String? = "",
    var token: String? = "",
    var freeCount: Int? = 1
)
