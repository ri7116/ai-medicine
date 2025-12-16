package com.example.chatbot.features.pharmacy

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
fun PharmacyScreen(navController: NavController, viewModel: PharmacyViewModel) {
    val context = LocalContext.current
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // 위치 권한 확인 및 요청
    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    // 위치 가져오기
    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted) {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                10000L // 10초마다 업데이트
            ).build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        Log.d("PharmacyScreen", "Location updated: ${location.latitude}, ${location.longitude}")
                        viewModel.updateLocation(location.latitude, location.longitude)
                    }
                }
            }

            try {
                // 실시간 위치 요청
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    null
                )


            } catch (e: SecurityException) {
                Log.e("PharmacyScreen", "Location permission denied", e)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("약국 정보", fontWeight = FontWeight.Bold)
                        if (viewModel.currentLocationText.isNotEmpty()) {
                            Text(
                                text = viewModel.currentLocationText,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                ),
                actions = {
                    // 새로고침 버튼
                    if (locationPermissionState.status.isGranted) {
                        TextButton(
                            onClick = {
                                viewModel.refreshPharmacies()
                            }
                        ) {
                            Text("새로고침")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF0F0F0))
                .padding(16.dp)
        ) {
            // 권한 요청 메시지
            if (!locationPermissionState.status.isGranted) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "위치 권한이 필요합니다",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "주변 약국을 찾기 위해 위치 권한을 허용해주세요.",
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { locationPermissionState.launchPermissionRequest() }
                        ) {
                            Text("권한 허용")
                        }
                    }
                }
            }

            // 약국 목록 표시 영역
            if (viewModel.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("주변 약국을 찾는 중...", color = Color.Gray)
                    }
                }
            } else if (viewModel.pharmacies.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "주변에 약국이 없습니다",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(viewModel.pharmacies) { pharmacy ->
                        PharmacyItem(pharmacy = pharmacy, onClick = {
                            navController.navigate("pharmacy_detail/${pharmacy.id}")
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun PharmacyItem(pharmacy: Pharmacy, onClick: () -> Unit) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = pharmacy.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = pharmacy.address, fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = pharmacy.phone, fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${String.format("%.1f", pharmacy.distance)}km",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Weekdays(openDays = pharmacy.openDays)
        }
    }
}

@Composable
fun Weekdays(openDays: List<String>) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val allDays = listOf("월", "화", "수", "목", "금", "토", "일")
        allDays.forEach { day ->
            val isOpen = openDays.contains(day)
            Box(
                modifier = Modifier
                    .size(32.dp)
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