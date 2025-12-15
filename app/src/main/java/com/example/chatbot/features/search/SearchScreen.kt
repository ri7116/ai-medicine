package com.example.chatbot.features.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.* // Material 3 구성 요소 사용
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.BorderStroke
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.rememberScrollState // 화면 스크롤
import androidx.compose.foundation.verticalScroll // 화면 스크롤
import com.example.chatbot.R // R 클래스를 사용하여 드로어블 리소스 참조
import com.example.chatbot.ui.theme.ChatBotTheme // 프로젝트 테마

// XML의 헥사 코드를 Compose Color로 변환
val ColorRed = Color(0xFFF44336)
val ColorYellow = Color(0xFFFFEB3B)
val ColorGreen = Color(0xFF4CAF50)
val ColorBlue = Color(0xFF2196F3)
val ColorPurple = Color(0xFF9C27B0)
val ColorOrange = Color(0xFFFF9800)
val ColorBlack = Color(0xFF000000)
val ColorWhite = Color(0xFFFFFFFF)

// 메인 검색 화면 Compose 함수
@Composable
fun SearchScreen(
    viewModel: MedicineSearchViewModel = viewModel()
) {
    // ViewModel의 필터 상태 관찰
    val currentFilter by viewModel.currentFilter.observeAsState(SearchFilter())

    // EditText 상태 (ViewModel에 직접 연결되지 않으므로 Compose State 사용)
    var identifier1 by remember { mutableStateOf("") }
    var identifier2 by remember { mutableStateOf("") }

    // 스크롤 상태 정의
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // 1. 식별문자 1
        SearchInputField(
            label = "식별문자1",
            hint = "첫번째 식별문자를 입력하세요.",
            value = identifier1,
            onValueChange = { identifier1 = it }
        )

        // 2. 식별문자 2
        SearchInputField(
            label = "식별문자2",
            hint = "두번째 식별문자를 입력하세요.",
            value = identifier2,
            onValueChange = { identifier2 = it }
        )

        // 3. 제형 선택
        FilterHeader("제형", topMargin = 10.dp)
        FormSelection(currentFilter.form) { viewModel.selectForm(it) }

        // 4. 모양 선택
        FilterHeader("모양", topMargin = 8.dp)
        ShapeSelection(currentFilter.shape) { viewModel.selectShape(it) }

        // 5. 색상 선택
        FilterHeader("색상", topMargin = 12.dp)
        ColorSelection(currentFilter.color) { viewModel.selectColor(it) }

        // 6. 분할선 선택
        FilterHeader("분할선", topMargin = 12.dp)
        SplitLineSelection(currentFilter.splitLine) { viewModel.selectSplitLine(it) }

        Spacer(Modifier.height(12.dp))

        // 7. 검색 버튼
        Button(
            onClick = {
                viewModel.searchMedicine(identifier1, identifier2)
            },
            modifier = Modifier
                .width(120.dp)
                .align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(4.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("검색", fontSize = 18.sp)
        }
    }
}

// =========================================================================
// ************************** 헬퍼 컴포저블 *********************************
// =========================================================================

// TextView 대체 헤더
@Composable
fun FilterHeader(label: String, topMargin: Dp) {
    Text(
        text = label,
        modifier = Modifier.padding(top = topMargin),
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
}

// 식별문자 EditText 대체
@Composable
fun SearchInputField(label: String, hint: String, value: String, onValueChange: (String) -> Unit) {
    FilterHeader(label = label, topMargin = 8.dp)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(hint) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Gray
        ),
        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
    )
}

// 3. 제형 선택 (LinearLayout Group 대체)
@Composable
fun FormSelection(selectedForm: String?, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween // XML의 marginEnd와 유사한 효과
    ) {
        FormItem(label = "정제", imageResId = R.drawable.tablet, selectedForm, onSelect, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(16.dp))
        FormItem(label = "경질캡슐", imageResId = R.drawable.hard_capsule, selectedForm, onSelect, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(16.dp))
        FormItem(label = "연질캡슐", imageResId = R.drawable.soft_capsule, selectedForm, onSelect, modifier = Modifier.weight(1f))
    }
}

// 제형 개별 아이템 (LinearLayout 대체)
@Composable
fun FormItem(
    label: String,
    imageResId: Int,
    selectedForm: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier
) {
    // 선택 여부에 따른 스타일 결정 (XML의 search_item_selected/unselected 대체)
    val isSelected = selectedForm == label
    val borderColor = if (isSelected) Color(0xFF4CAF50) else Color.Gray
    val backgroundColor = if (isSelected) Color(0xFFE8F5E9) else Color.White
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { onSelect(label) }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = "$label 이미지",
            modifier = Modifier
                .width(60.dp)
                .height(36.dp)
        )
        Text(
            text = label,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

// 4. 모양 선택 (Button Group 대체)
@Composable
fun ShapeSelection(selectedShape: String?, onSelect: (String) -> Unit) {
    val shapes = listOf("원형", "타원", "삼각형", "사각형", "마름모", "오각형", "육각형", "팔각형")

    // 첫 번째 줄
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp) // XML marginEnd 4dp 대체
    ) {
        shapes.take(4).forEach { shape ->
            ShapeButton(shape, selectedShape, onSelect, modifier = Modifier.weight(1f))
        }
    }
    // 두 번째 줄
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        shapes.takeLast(4).forEach { shape ->
            ShapeButton(shape, selectedShape, onSelect, modifier = Modifier.weight(1f))
        }
    }
}


// 모양 개별 버튼
@Composable
fun ShapeButton(label: String, selectedShape: String?, onSelect: (String) -> Unit, modifier: Modifier) {
    // XML의 search_item_selected/unselected와 유사한 스타일 재현
    val isSelected = selectedShape == label
    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = if (isSelected) Color(0xFFE8F5E9) else Color.White,
        contentColor = Color.Black
    )

    Button(
        onClick = { onSelect(label) },
        modifier = modifier.height(IntrinsicSize.Min),
        colors = buttonColors,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Color(0xFF4CAF50) else Color.Gray
        ),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
    ) {
        Text(text = label, fontSize = 14.sp)
    }
}

// 5. 색상 선택 (ImageButton Group 대체)
@Composable
fun ColorSelection(selectedColor: String?, onSelect: (String) -> Unit) {
    val colors = mapOf(
        "빨강" to ColorRed, "노랑" to ColorYellow, "초록" to ColorGreen, "파랑" to ColorBlue,
        "자주" to ColorPurple, "주황" to ColorOrange, "검정" to ColorBlack, "하양" to ColorWhite
    )

    // 첫 번째 줄
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        colors.entries.take(4).forEach { (name, color) ->
            ColorItem(name, color, selectedColor, onSelect, modifier = Modifier.weight(1f))
        }
    }

    // 두 번째 줄 (수정된 부분)
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        colors.entries.drop(4).forEach { (name, color) ->
            ColorItem(name, color, selectedColor, onSelect, modifier = Modifier.weight(1f))
        }
    }
}

// 색상 개별 아이템 (ImageButton 대체)
@Composable
fun ColorItem(name: String, color: Color, selectedColor: String?, onSelect: (String) -> Unit, modifier: Modifier) {
    val isSelected = selectedColor == name

    // 흰색은 특별히 테두리 스타일을 적용 (XML의 color_border_white 대체)
    val colorBackgroundModifier = if (name == "하양") {
        Modifier.border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
    } else {
        Modifier.background(color)
    }

    // 선택된 경우 테두리 스타일 적용
    val selectionModifier = if (isSelected) {
        Modifier
            .border(2.dp, Color(0xFF4CAF50), RoundedCornerShape(4.dp))
            .background(Color(0xFFE8F5E9)) // 선택된 경우 배경색도 추가
            .padding(1.dp) // 테두리 때문에 내부 패딩 추가
    } else {
        Modifier.background(Color.Transparent)
    }

    Column(
        modifier = modifier.clickable { onSelect(name) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(4.dp)) // 상단 마진 대체
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .clip(RoundedCornerShape(4.dp))
                .then(colorBackgroundModifier) // 색상/테두리 적용
                .then(selectionModifier) // 선택 테두리 적용
        )
        Text(
            text = name,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 2.dp),
            color = if (name == "검정" || name == "하양") Color.Black else Color.Black
        )
    }
}

// 6. 분할선 선택 (Button Group 대체)
@Composable
fun SplitLineSelection(selectedLine: String?, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ShapeButton("없음", selectedLine, onSelect, Modifier.weight(1f).padding(start = 2.dp))
        ShapeButton("(ㅡ)형", selectedLine, onSelect, Modifier.weight(1f))
        ShapeButton("(十)형", selectedLine, onSelect, Modifier.weight(1f).padding(end = 2.dp))
    }
}

// =========================================================================
// ************************** 미리보기 *************************************
// =========================================================================

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    ChatBotTheme {
        SearchScreen()
    }
}