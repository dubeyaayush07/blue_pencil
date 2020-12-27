package com.example.bluepencil

import java.util.*

data class Order (
    var userId: String? = "",
    var editorId: String? = "",
    var photoUrl: String? = "",
    var jobUrl: String? = "",
    var date: Date = Date()
) { }