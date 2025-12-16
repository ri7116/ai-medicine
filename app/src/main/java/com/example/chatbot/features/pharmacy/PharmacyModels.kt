package com.example.chatbot.features.pharmacy

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Pharmacy(
    val id: String,
    val name: String,
    val address: String,
    val phone: String,
    val distance: Double,
    val openDays: List<String>,
    val latitude: Double,
    val longitude: Double
)


data class Review(
    val id: String = "",
    val pharmacyId: String = "",
    val rating: Float = 0f,
    val comment: String = "",

    @ServerTimestamp
    val timestamp: Date? = null
)
