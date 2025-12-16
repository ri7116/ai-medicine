package com.example.chatbot.features.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.InputStreamReader
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken





class MedicineSearchViewModel(application: Application) : AndroidViewModel(application) {

    private val _currentFilter = MutableLiveData(SearchFilter())
    val currentFilter: MutableLiveData<SearchFilter> = _currentFilter

    private val _searchResults = MutableLiveData<List<Medicine>>(emptyList())
    val searchResults: LiveData<List<Medicine>> = _searchResults


    private val allMedicines: List<Medicine> by lazy {
        loadMedicinesFromAssets()
    }

    private fun loadMedicinesFromAssets(): List<Medicine> {
        // ... (기존 로드 로직 유지) ...
        val application = getApplication<Application>()
        android.util.Log.i("DATA_LOAD", "JSON 로드 시도 중...")
        return try {
            val inputStream = application.assets.open("medicine_data.json")
            val reader = InputStreamReader(inputStream)
            val listType = object : TypeToken<List<Medicine>>() {}.type
            val loadedList = Gson().fromJson<List<Medicine>>(reader, listType)
            android.util.Log.i("DATA_LOAD", "JSON 로드 성공. 총 ${loadedList.size}개 로드됨.")
            loadedList
        } catch (e: Exception) {
            android.util.Log.e("DATA_LOAD", "JSON 로드 실패: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    fun searchMedicine(id1: String, id2: String) {
        val current = _currentFilter.value ?: SearchFilter()
        val newFilter = current.copy(identifier1 = id1, identifier2 = id2)
        _currentFilter.value = newFilter
    }

    fun executeSearchWithFilter(filter: SearchFilter) {
        performFilterAndSetResults(filter)
    }

    private fun performFilterAndSetResults(filter: SearchFilter) {
        android.util.Log.d("SEARCH_DEBUG", "명시적 검색 실행. 필터: ${filter}")

        val filteredList = allMedicines.filter { medicine ->
            val id1Match = filter.identifier1.isEmpty() || medicine.id1.contains(filter.identifier1, ignoreCase = true)
            val id2Match = filter.identifier2.isEmpty() || medicine.id2.contains(filter.identifier2, ignoreCase = true)
            val formMatch = filter.form == null || medicine.form == filter.form
            val shapeMatch = filter.shape == null || medicine.shape == filter.shape
            val colorMatch = filter.color == null || medicine.color == filter.color
            val lineMatch = filter.splitLine == null || medicine.line == filter.splitLine

            id1Match && id2Match && formMatch && shapeMatch && colorMatch && lineMatch
        }
        _searchResults.value = filteredList

        android.util.Log.d("SEARCH_DEBUG", "필터링 완료. 결과: ${filteredList.size}개")
    }

    fun selectForm(form: String) {
        val current = _currentFilter.value ?: SearchFilter()
        val newForm = if (current.form == form) null else form
        _currentFilter.value = current.copy(form = newForm)
    }

    fun selectShape(shape: String) {
        val current = _currentFilter.value ?: SearchFilter()
        val newShape = if (current.shape == shape) null else shape
        _currentFilter.value = current.copy(shape = newShape)
    }

    fun selectColor(color: String) {
        val current = _currentFilter.value ?: SearchFilter()
        val newColor = if (current.color == color) null else color
        _currentFilter.value = current.copy(color = newColor)
    }

    fun selectSplitLine(line: String) {
        val current = _currentFilter.value ?: SearchFilter()
        val newLine = if (current.splitLine == line) null else line
        _currentFilter.value = current.copy(splitLine = newLine)
    }
}