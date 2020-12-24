package com.example.bluepencil

data class Placard (
    var cost: Int = 0,
    var userName: String = "",
    var userId: String = "",
    var tags: List<String>? = null
)