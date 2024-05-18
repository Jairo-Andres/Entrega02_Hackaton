package com.example.entrega02.data

import java.io.Serializable

data class TouristicPlace(
    val ID: String,
    val name: String,
    val picture: String,
    val scores: List<Float>,
    val coordinates:ArrayList<String>,
    val reviews: ArrayList<Review>,
    val placeDescription: String
): Serializable