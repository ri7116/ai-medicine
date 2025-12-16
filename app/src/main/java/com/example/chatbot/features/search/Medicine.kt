package com.example.chatbot.features.search

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Medicine(
    val name: String,
    val id1: String,
    val id2: String,
    val form: String,
    val shape: String,
    val color: String,
    val line: String,
    val imageUrl: String?
) : Parcelable