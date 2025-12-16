package com.example.chatbot.features.pharmacy

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class PharmacyViewModel : ViewModel() {
    var pharmacies by mutableStateOf<List<Pharmacy>>(emptyList())
    var isLoading by mutableStateOf(false)
    var currentLocationText by mutableStateOf("")

    private val pharmacyApiKey = "9e2e9b714327f8471bd319009a3b20382bead55f9d4ef200358e8cc8e221fc8f"
    private val TAG = "PharmacyViewModel"

    // 현재 위치 저장
    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null

    init {
        Log.d("ViewModelLifecycle", "--- PharmacyViewModel CREATED (instance: ${this.hashCode()}) ---")
    }

    // ##### 외부에서 위치를 업데이트할 수 있는 함수 #####
    fun updateLocation(latitude: Double, longitude: Double) {
        Log.d(TAG, "updateLocation called: lat=$latitude, lon=$longitude")

        // 위치가 유의미하게 변경되었을 때만 새로 로드 (0.001도 = 약 111m)
        val latDiff = kotlin.math.abs((currentLatitude ?: 0.0) - latitude)
        val lonDiff = kotlin.math.abs((currentLongitude ?: 0.0) - longitude)

        if (currentLatitude == null || latDiff > 0.001 || lonDiff > 0.001) {
            currentLatitude = latitude
            currentLongitude = longitude
            currentLocationText = "위치: ${String.format("%.4f", latitude)}, ${String.format("%.4f", longitude)}"

            pharmacies = emptyList() // 기존 데이터 초기화
            fetchPharmacies(latitude, longitude)
            Log.d(TAG, "Location updated and fetching new data")
        } else {
            Log.d(TAG, "Location change too small, skipping fetch")
        }
    }

    // 강제 새로고침
    fun refreshPharmacies() {
        currentLatitude?.let { lat ->
            currentLongitude?.let { lon ->
                pharmacies = emptyList()
                fetchPharmacies(lat, lon)
                Log.d(TAG, "Manual refresh triggered")
            }
        }
    }

    private fun fetchPharmacies(latitude: Double, longitude: Double) {
        Log.d(TAG, "fetchPharmacies started for lat: $latitude, lon: $longitude")
        isLoading = true

        viewModelScope.launch {
            val fetchedPharmacies = withContext(Dispatchers.IO) {
                fetchPharmaciesFromApi(latitude, longitude)
            }
            pharmacies = fetchedPharmacies.sortedBy { it.distance }.take(4)
            isLoading = false
            Log.d(TAG, "Loaded ${pharmacies.size} pharmacies")
        }
    }

    private fun fetchPharmaciesFromApi(latitude: Double, longitude: Double): List<Pharmacy> {
        val pharmacyList = mutableListOf<Pharmacy>()
        try {
            val urlBuilder = StringBuilder("http://apis.data.go.kr/B552657/ErmctInsttInfoInqireService/getParmacyListInfoInqire")
            urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=$pharmacyApiKey")
            urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8"))
            urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("100", "UTF-8"))

            val finalUrl = urlBuilder.toString()
            Log.d(TAG, "Request URL: $finalUrl")

            val url = URL(finalUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Content-type", "application/xml")

            val responseCode = conn.responseCode
            Log.d(TAG, "Response Code: $responseCode")

            if (conn.responseCode in 200..300) {
                val factory = XmlPullParserFactory.newInstance()
                val parser = factory.newPullParser()
                parser.setInput(InputStreamReader(conn.inputStream, "UTF-8"))

                var eventType = parser.eventType
                var currentTag: String? = null
                var dutyName: String? = null
                var dutyAddr: String? = null
                var dutyTel1: String? = null
                var wgs84Lat: Double? = null
                var wgs84Lon: Double? = null
                val openDaysMap = mutableMapOf<String, Boolean>()

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            currentTag = parser.name
                            if (currentTag == "item") {
                                dutyName = null
                                dutyAddr = null
                                dutyTel1 = null
                                wgs84Lat = null
                                wgs84Lon = null
                                openDaysMap.clear()
                            }
                        }
                        XmlPullParser.TEXT -> {
                            val text = parser.text
                            when (currentTag) {
                                "dutyName" -> dutyName = text
                                "dutyAddr" -> dutyAddr = text
                                "dutyTel1" -> dutyTel1 = text
                                "wgs84Lat" -> wgs84Lat = text.toDoubleOrNull()
                                "wgs84Lon" -> wgs84Lon = text.toDoubleOrNull()
                                "dutyTime1c" -> if (text != null) openDaysMap["월"] = true
                                "dutyTime2c" -> if (text != null) openDaysMap["화"] = true
                                "dutyTime3c" -> if (text != null) openDaysMap["수"] = true
                                "dutyTime4c" -> if (text != null) openDaysMap["목"] = true
                                "dutyTime5c" -> if (text != null) openDaysMap["금"] = true
                                "dutyTime6c" -> if (text != null) openDaysMap["토"] = true
                                "dutyTime7c" -> if (text != null) openDaysMap["일"] = true
                            }
                        }
                        XmlPullParser.END_TAG -> {
                            if (parser.name == "item") {
                                if (dutyName != null && dutyAddr != null && dutyTel1 != null && wgs84Lat != null && wgs84Lon != null) {
                                    val distance = calculateDistance(latitude, longitude, wgs84Lat, wgs84Lon)
                                    pharmacyList.add(
                                        Pharmacy(
                                            id = dutyTel1,
                                            name = dutyName,
                                            address = dutyAddr,
                                            phone = dutyTel1,
                                            distance = distance,
                                            openDays = openDaysMap.keys.toList(),
                                            latitude = wgs84Lat,
                                            longitude = wgs84Lon
                                        )
                                    )
                                }
                            }
                            currentTag = null
                        }
                    }
                    eventType = parser.next()
                }
                Log.d(TAG, "Successfully parsed ${pharmacyList.size} pharmacies from XML.")
            } else {
                val errorReader = BufferedReader(InputStreamReader(conn.errorStream))
                val errorResult = errorReader.readText()
                Log.e(TAG, "API Error Response: $errorResult")
                errorReader.close()
            }
            conn.disconnect()

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching or parsing pharmacies", e)
            e.printStackTrace()
        }
        return pharmacyList
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371
        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = sin(latDistance / 2) * sin(latDistance / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(lonDistance / 2) * sin(lonDistance / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }
}