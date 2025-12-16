package com.example.chatbot.features.pharmacy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacyDetailScreen(
    navController: NavController,
    pharmacy: Pharmacy,
    viewModel: PharmacyDetailViewModel = viewModel()
) {
    var showReviewDialog by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    // 화면 진입 시 리뷰 로드
    LaunchedEffect(pharmacy.id) {
        viewModel.fetchReviews(pharmacy.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(pharmacy.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로 가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showReviewDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("리뷰 작성", modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            // 지도 추가
            item {
                PharmacyMapView(pharmacy)
            }

            // 약국 기본 정보
            item {
                PharmacyInfoCard(pharmacy)
            }

            // 리뷰 섹션 헤더
            item {
                Text(
                    text = "리뷰 (${viewModel.reviews.size})",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // 리뷰 목록
            if (viewModel.reviews.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "아직 리뷰가 없습니다.\n첫 번째 리뷰를 작성해보세요!",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(viewModel.reviews) { review ->
                    ReviewItem(review)
                }
            }
        }
    }

    // 리뷰 작성 다이얼로그
    if (showReviewDialog) {
        ReviewDialog(
            isSubmitting = isSubmitting,
            onDismiss = {
                if (!isSubmitting) {
                    showReviewDialog = false
                }
            },
            onSubmit = { rating, comment ->
                isSubmitting = true
                viewModel.submitReview(
                    pharmacyId = pharmacy.id,
                    rating = rating,
                    comment = comment,
                    onSuccess = {
                        isSubmitting = false
                        showReviewDialog = false
                    },
                    onFailure = {
                        isSubmitting = false
                    }
                )
            }
        )
    }
}

@Composable
fun PharmacyMapView(pharmacy: Pharmacy) {
    val pharmacyLocation = LatLng(pharmacy.latitude, pharmacy.longitude)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(pharmacyLocation, 16f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                myLocationButtonEnabled = false
            )
        ) {
            // 약국 위치에 마커 표시
            Marker(
                state = MarkerState(position = pharmacyLocation),
                title = pharmacy.name,
                snippet = pharmacy.address
            )
        }
    }
}

@Composable
fun PharmacyInfoCard(pharmacy: Pharmacy) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = pharmacy.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            InfoRow(label = "주소", value = pharmacy.address)
            Spacer(modifier = Modifier.height(8.dp))

            InfoRow(label = "전화", value = pharmacy.phone)
            Spacer(modifier = Modifier.height(8.dp))

            InfoRow(label = "거리", value = "${String.format("%.1f", pharmacy.distance)}km")
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "운영 요일",
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val allDays = listOf("월", "화", "수", "목", "금", "토", "일")
                allDays.forEach { day ->
                    val isOpen = pharmacy.openDays.contains(day)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = if (isOpen) Color(0xFF4CAF50) else Color.LightGray,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day,
                            color = if (isOpen) Color.White else Color.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row {
        Text(
            text = "$label: ",
            fontSize = 14.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}

@Composable
fun ReviewItem(review: Review) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 별점과 날짜
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StarRating(rating = review.rating, size = 20.dp)

                review.timestamp?.let { timestamp ->
                    val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)
                    Text(
                        text = dateFormat.format(timestamp),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 리뷰 내용
            Text(
                text = review.comment,
                fontSize = 14.sp,
                color = Color.Black,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun StarRating(rating: Float, size: Dp = 24.dp) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(5) { index ->
            Icon(
                imageVector = if (index < rating.toInt()) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = if (index < rating.toInt()) Color(0xFFFFD700) else Color.LightGray,
                modifier = Modifier.size(size)
            )
        }
    }
}

@Composable
fun ReviewDialog(
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (Float, String) -> Unit
) {
    var rating by remember { mutableStateOf(5f) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("리뷰 작성", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("별점을 선택하세요", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))

                // 별점 선택
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(5) { index ->
                        IconButton(
                            onClick = { rating = (index + 1).toFloat() },
                            enabled = !isSubmitting
                        ) {
                            Icon(
                                imageVector = if (index < rating.toInt()) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = null,
                                tint = if (index < rating.toInt()) Color(0xFFFFD700) else Color.LightGray,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("리뷰 내용", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    placeholder = { Text("이 약국에 대한 경험을 공유해주세요") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 4,
                    enabled = !isSubmitting
                )

                if (isSubmitting) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (comment.isNotBlank()) {
                        onSubmit(rating, comment)
                    }
                },
                enabled = comment.isNotBlank() && !isSubmitting
            ) {
                Text(if (isSubmitting) "작성 중..." else "작성")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting
            ) {
                Text("취소")
            }
        }
    )
}