package com.example.chatbot.features.pharmacy

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date

class PharmacyDetailViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    var reviews by mutableStateOf<List<Review>>(emptyList())
    private val TAG = "PharmacyDetailViewModel"

    fun fetchReviews(pharmacyId: String) {
        Log.d(TAG, "Fetching reviews for pharmacyId: $pharmacyId")
        db.collection("reviews")
            .whereEqualTo("pharmacyId", pharmacyId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                viewModelScope.launch {
                    val reviewList = withContext(Dispatchers.IO) {
                        snapshots?.map { doc ->
                            doc.toObject(Review::class.java).copy(id = doc.id)
                        } ?: emptyList()
                    }
                    reviews = reviewList
                    Log.d(TAG, "Loaded ${reviewList.size} reviews.")
                }
            }
    }

    fun submitReview(
        pharmacyId: String,
        rating: Float,
        comment: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (pharmacyId.isBlank() || comment.isBlank() || rating == 0f) {
            Log.w(TAG, "Review submission cancelled: missing data.")
            onFailure(IllegalArgumentException("Missing required data"))
            return
        }

        // 즉시 UI에 추가
        val tempReview = Review(
            id = "temp_${System.currentTimeMillis()}",
            pharmacyId = pharmacyId,
            rating = rating,
            comment = comment,
            timestamp = Date()
        )


        reviews = listOf(tempReview) + reviews

        viewModelScope.launch {
            try {

                val review = Review(
                    pharmacyId = pharmacyId,
                    rating = rating,
                    comment = comment
                )

                withContext(Dispatchers.IO) {
                    db.collection("reviews")
                        .add(review)
                        .await()
                }

                Log.d(TAG, "Review added successfully")
                onSuccess()



            } catch (e: Exception) {
                Log.e(TAG, "Error adding review", e)

                // 실패 시 임시 리뷰 제거
                reviews = reviews.filter { it.id != tempReview.id }

                onFailure(e)
            }
        }
    }
}