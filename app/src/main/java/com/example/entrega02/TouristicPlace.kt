package com.example.entrega02

import java.io.Serializable

data class TouristicPlace(val name: String, val picture: String, val scores:ArrayList<Float>, val coordinates:ArrayList<String>): Serializable