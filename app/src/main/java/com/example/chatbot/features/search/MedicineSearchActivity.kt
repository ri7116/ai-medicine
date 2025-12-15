package com.example.chatbot.features.search

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.chatbot.R
import com.example.chatbot.databinding.ActivityMedicineSearchBinding // View Binding 사용을 위한 import

class MedicineSearchActivity : AppCompatActivity() {

    // 1. View Binding 객체 선언
    private lateinit var binding: ActivityMedicineSearchBinding

    // 2. ViewModel 객체 선언 (검색 상태 관리 담당)
    private lateinit var viewModel: MedicineSearchViewModel

    // **********************************************
    // 3. onCreate() 함수 (단 하나의 복사본만 있어야 함)
    // **********************************************
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding 초기화 및 레이아웃 연결
        binding = ActivityMedicineSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ViewModel 초기화
        viewModel = ViewModelProvider(this).get(MedicineSearchViewModel::class.java)

        // ViewModel Observer 추가: 필터 변경 시 UI 자동 갱신
        viewModel.currentFilter.observe(this) { filter ->
            updateSelectionUI(filter)
        }

        // 리스너 설정 함수 호출 (정의된 함수를 호출만 함)
        setupListeners()

        // 초기 로딩 시 UI 업데이트
        updateSelectionUI(viewModel.currentFilter.value ?: SearchFilter())
    }

    // **********************************************
    // 4. setupListeners() 함수 (클래스 레벨 멤버 함수)
    // **********************************************
    private fun setupListeners() {

        // --- 1. 제형 (Form) 선택 리스너 설정 ---
        binding.llTypeTablet.setOnClickListener {
            viewModel.selectForm("정제")
        }
        binding.llTypeHardCapsule.setOnClickListener {
            viewModel.selectForm("경질캡슐")
        }
        binding.llTypeSoftCapsule.setOnClickListener {
            viewModel.selectForm("연질캡슐")
        }

        // --- 2. 모양 (Shape) 선택 리스너 설정 ---
        binding.btnShapeRound.setOnClickListener {
            viewModel.selectShape("원형")
        }
        binding.btnShapeOval.setOnClickListener {
            viewModel.selectShape("타원")
        }
        binding.btnShapeTriangle.setOnClickListener {
            viewModel.selectShape("삼각형")
        }
        // ... (나머지 모양 버튼들도 동일하게 처리)

        // --- 3. 색상 (Color) 선택 리스너 설정 ---
        binding.btnColorRed.setOnClickListener {
            viewModel.selectColor("빨강")
        }
        binding.btnColorYellow.setOnClickListener {
            viewModel.selectColor("노랑")
        }
        // ... (나머지 색상 버튼들도 동일하게 처리)

        // --- 4. 분할선 (SplitLine) 선택 리스너 설정 ---
        binding.btnLineNone.setOnClickListener {
            viewModel.selectSplitLine("없음")
        }
        binding.btnLineSingle.setOnClickListener {
            viewModel.selectSplitLine("ㅡ형")
        }
        binding.btnLineCross.setOnClickListener {
            viewModel.selectSplitLine("十형")
        }

        // --- 5. 검색 버튼 클릭 리스너 설정 ---
        binding.btnSearch.setOnClickListener {
            val identifier1 = binding.etIdentifier1.text.toString()
            val identifier2 = binding.etIdentifier2.text.toString()
            viewModel.searchMedicine(identifier1, identifier2)
        }
    }

    // **********************************************
    // 5. UI 업데이트 함수들 (클래스 레벨 멤버 함수)
    // **********************************************
    private fun updateSelectionUI(currentFilter: SearchFilter) {

        // --- 1. 제형 (LinearLayout) ---
        updateLinearLayoutBackground(binding.llTypeTablet, currentFilter.form, "정제")
        updateLinearLayoutBackground(binding.llTypeHardCapsule, currentFilter.form, "경질캡슐")
        updateLinearLayoutBackground(binding.llTypeSoftCapsule, currentFilter.form, "연질캡슐")

        // --- 2. 모양 및 분할선 (Button) ---
        // --- 2. 모양 (Button) (전체 완성) ---
        updateButtonBackground(binding.btnShapeRound, currentFilter.shape, "원형")
        updateButtonBackground(binding.btnShapeOval, currentFilter.shape, "타원")
        updateButtonBackground(binding.btnShapeTriangle, currentFilter.shape, "삼각형")
        updateButtonBackground(binding.btnShapeSquare, currentFilter.shape, "사각형")
        updateButtonBackground(binding.btnShapeDiamond, currentFilter.shape, "마름모")
        updateButtonBackground(binding.btnShapePentagon, currentFilter.shape, "오각형")
        updateButtonBackground(binding.btnShapeHexagon, currentFilter.shape, "육각형")
        updateButtonBackground(binding.btnShapeOctagon, currentFilter.shape, "팔각형")

        updateButtonBackground(binding.btnLineNone, currentFilter.splitLine, "없음")
        updateButtonBackground(binding.btnLineSingle, currentFilter.splitLine, "ㅡ형")
        updateButtonBackground(binding.btnLineCross, currentFilter.splitLine, "十형")

        // --- 3. 색상 (ImageButton) ---
        updateImageButtonBackground(binding.btnColorRed, currentFilter.color, "빨강")
        updateImageButtonBackground(binding.btnColorYellow, currentFilter.color, "노랑")
        updateImageButtonBackground(binding.btnColorGreen, currentFilter.color, "초록")
        updateImageButtonBackground(binding.btnColorBlue, currentFilter.color, "파랑")
        updateImageButtonBackground(binding.btnColorPurple, currentFilter.color, "자주")
        updateImageButtonBackground(binding.btnColorOrange, currentFilter.color, "주황")
        updateImageButtonBackground(binding.btnColorBlack, currentFilter.color, "검정")
        updateImageButtonBackground(binding.btnColorWhite, currentFilter.color, "하양")
    }

    private fun updateButtonBackground(button: Button, selectedValue: String?, comparisonValue: String) {
        if (selectedValue == comparisonValue) {
            button.setBackgroundResource(R.drawable.search_item_selected)
        } else {
            button.setBackgroundResource(R.drawable.search_item_unselected)
        }
    }

    private fun updateLinearLayoutBackground(layout: LinearLayout, selectedValue: String?, comparisonValue: String) {
        if (selectedValue == comparisonValue) {
            layout.setBackgroundResource(R.drawable.search_item_selected)
        } else {
            layout.setBackgroundResource(R.drawable.search_item_unselected)
        }
    }

    // 색상 이름에 해당하는 실제 색상 코드를 반환하는 헬퍼 함수 (추가 필요)
    private fun getColorForColorName(colorName: String): Int {
        // XML에 정의된 헥사 코드를 Kotlin Integer 형태로 반환
        return when (colorName) {
            "빨강" -> 0xFFF44336.toInt()
            "노랑" -> 0xFFFFEB3B.toInt()
            "초록" -> 0xFF4CAF50.toInt()
            "파랑" -> 0xFF2196F3.toInt()
            "자주" -> 0xFF9C27B0.toInt()
            "주황" -> 0xFFFF9800.toInt()
            "검정" -> 0xFF000000.toInt()
            "하양" -> 0xFFFFFFFF.toInt() // 흰색도 코드를 반환하되, updateImageButtonBackground에서 별도 처리
            else -> android.R.color.transparent // 기본값
        }
    }

    // 최종 ImageButton 업데이트 로직 (getColorForColorName 함수 사용)
    private fun updateImageButtonBackground(button: ImageButton, selectedValue: String?, comparisonValue: String) {
        if (selectedValue == comparisonValue) {
            // 선택된 경우: 선택 스타일 적용
            button.setBackgroundResource(R.drawable.search_item_selected)
        } else {
            // 선택 해제된 경우: 원래의 색상/테두리 스타일로 복구
            if (comparisonValue == "하양") {
                // 흰색은 Drawable을 사용하여 테두리 스타일로 복구
                button.setBackgroundResource(R.drawable.color_border_white)
            } else {
                // 다른 색상은 원래 배경색(단색)으로 복구
                button.setBackgroundColor(getColorForColorName(comparisonValue))
                // (선택 해제 시 search_item_unselected의 얇은 테두리가 사라지므로, 단색으로만 복구됩니다.)
            }
        }
    }
}