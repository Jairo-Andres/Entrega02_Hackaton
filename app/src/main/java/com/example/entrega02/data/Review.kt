package com.example.entrega02.data

import java.io.Serializable
data class Review(
    var userName: String = "",
    var score: Float = 0.0f,
    var comment: String = "",
    var userIcon: String = ""
) : Serializable