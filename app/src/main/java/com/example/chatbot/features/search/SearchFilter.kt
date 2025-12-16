package com.example.chatbot.features.search

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SearchFilter(
    val identifier1: String = "",
    val identifier2: String = "",
    val form: String? = null,
    val shape: String? = null,
    val color: String? = null,
    val splitLine: String? = null
) : Parcelable