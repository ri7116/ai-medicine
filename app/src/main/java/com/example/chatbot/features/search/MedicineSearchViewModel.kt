package com.example.chatbot.features.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// 검색 필터 상태를 저장하는 데이터 클래스
data class SearchFilter(
    val identifier1: String = "",
    val identifier2: String = "",
    val form: String? = null,
    val shape: String? = null,
    val color: String? = null,
    val splitLine: String? = null
)

class MedicineSearchViewModel : ViewModel() {

    // 검색 필터의 현재 상태를 저장하고 관찰 가능하게 만듭니다.
    private val _currentFilter = MutableLiveData(SearchFilter())
    val currentFilter: MutableLiveData<SearchFilter> = _currentFilter

    // --- 제형(Form) 선택 로직 ---
    fun selectForm(form: String) {
        val current = _currentFilter.value ?: SearchFilter()
        val newForm = if (current.form == form) null else form // 토글 로직 유지

        // **새로운 객체 생성 (copy)**
        _currentFilter.value = current.copy(form = newForm)
    }

    // --- 모양(Shape) 선택 로직 ---
    fun selectShape(shape: String) {
        val current = _currentFilter.value ?: SearchFilter()
        val newShape = if (current.shape == shape) null else shape

        // **새로운 객체 생성 (copy)**
        _currentFilter.value = current.copy(shape = newShape)
    }

    // --- 색상(Color) 선택 로직 ---
    fun selectColor(color: String) {
        val current = _currentFilter.value ?: SearchFilter()
        val newColor = if (current.color == color) null else color

        // **새로운 객체 생성 (copy)**
        _currentFilter.value = current.copy(color = newColor)
    }

    // --- 분할선(SplitLine) 선택 로직 ---
    fun selectSplitLine(line: String) {
        val current = _currentFilter.value ?: SearchFilter()
        val newLine = if (current.splitLine == line) null else line

        // **새로운 객체 생성 (copy)**
        _currentFilter.value = current.copy(splitLine = newLine)
    }

    // --- 검색 실행 로직도 copy()로 수정 ---
    fun searchMedicine(id1: String, id2: String) {
        val current = _currentFilter.value ?: SearchFilter()

        // **새로운 객체 생성 (copy)**
        _currentFilter.value = current.copy(identifier1 = id1, identifier2 = id2)
    }
}