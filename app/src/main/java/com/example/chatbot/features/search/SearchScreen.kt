package com.example.chatbot.features.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.chatbot.R
import com.example.chatbot.ui.theme.ChatBotTheme

val ColorRed = Color(0xFFF44336)
val ColorYellow = Color(0xFFFFEB3B)
val ColorGreen = Color(0xFF4CAF50)
val ColorBlue = Color(0xFF2196F3)
val ColorPurple = Color(0xFF9C27B0)
val ColorOrange = Color(0xFFFF9800)
val ColorBlack = Color(0xFF000000)
val ColorWhite = Color(0xFFFFFFFF)

@Composable
fun SearchScreen(
    viewModel: MedicineSearchViewModel = viewModel(),
    onSearchClicked: (SearchFilter) -> Unit
) {
    val currentFilter by viewModel.currentFilter.observeAsState(SearchFilter())

    var identifier1 by remember { mutableStateOf("") }
    var identifier2 by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        SearchInputField(
            label = "식별문자1",
            hint = "첫번째 식별문자를 입력하세요.",
            value = identifier1,
            onValueChange = { identifier1 = it }
        )

        SearchInputField(
            label = "식별문자2",
            hint = "두번째 식별문자를 입력하세요.",
            value = identifier2,
            onValueChange = { identifier2 = it }
        )

        FilterHeader("제형", topMargin = 10.dp)
        FormSelection(currentFilter.form) { viewModel.selectForm(it) }

        FilterHeader("모양", topMargin = 8.dp)
        ShapeSelection(currentFilter.shape) { viewModel.selectShape(it) }

        FilterHeader("색상", topMargin = 12.dp)
        ColorSelection(currentFilter.color) { viewModel.selectColor(it) }

        FilterHeader("분할선", topMargin = 12.dp)
        SplitLineSelection(currentFilter.splitLine) { viewModel.selectSplitLine(it) }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                viewModel.searchMedicine(identifier1, identifier2)

                val finalFilter = viewModel.currentFilter.value ?: SearchFilter()
                onSearchClicked(finalFilter)
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

@Composable
fun FilterHeader(label: String, topMargin: Dp) {
    Text(
        text = label,
        modifier = Modifier.padding(top = topMargin),
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
}

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

@Composable
fun FormSelection(selectedForm: String?, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        FormItem(label = "정제", imageResId = R.drawable.tablet, selectedForm, onSelect, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(16.dp))
        FormItem(label = "경질캡슐", imageResId = R.drawable.hard_capsule, selectedForm, onSelect, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(16.dp))
        FormItem(label = "연질캡슐", imageResId = R.drawable.soft_capsule, selectedForm, onSelect, modifier = Modifier.weight(1f))
    }
}

@Composable
fun FormItem(
    label: String,
    imageResId: Int,
    selectedForm: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier
) {
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

@Composable
fun ShapeSelection(selectedShape: String?, onSelect: (String) -> Unit) {
    val shapes = listOf("원형", "타원", "삼각형", "사각형", "마름모", "오각형", "육각형", "팔각형")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        shapes.take(4).forEach { shape ->
            ShapeButton(shape, selectedShape, onSelect, modifier = Modifier.weight(1f))
        }
    }
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


@Composable
fun ShapeButton(label: String, selectedShape: String?, onSelect: (String) -> Unit, modifier: Modifier) {
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

@Composable
fun ColorSelection(selectedColor: String?, onSelect: (String) -> Unit) {
    val colors = mapOf(
        "빨강" to ColorRed, "노랑" to ColorYellow, "초록" to ColorGreen, "파랑" to ColorBlue,
        "자주" to ColorPurple, "주황" to ColorOrange, "검정" to ColorBlack, "하양" to ColorWhite
    )

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        colors.entries.take(4).forEach { (name, color) ->
            ColorItem(name, color, selectedColor, onSelect, modifier = Modifier.weight(1f))
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        colors.entries.drop(4).forEach { (name, color) ->
            ColorItem(name, color, selectedColor, onSelect, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ColorItem(name: String, color: Color, selectedColor: String?, onSelect: (String) -> Unit, modifier: Modifier) {
    val isSelected = selectedColor == name

    val colorBackgroundModifier = if (name == "하양") {
        Modifier.border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
    } else {
        Modifier.background(color)
    }

    val selectionModifier = if (isSelected) {
        Modifier
            .border(2.dp, Color(0xFF4CAF50), RoundedCornerShape(4.dp))
            .background(Color(0xFFE8F5E9))
            .padding(1.dp)
    } else {
        Modifier.background(Color.Transparent)
    }

    Column(
        modifier = modifier.clickable { onSelect(name) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .clip(RoundedCornerShape(4.dp))
                .then(colorBackgroundModifier)
                .then(selectionModifier)
        )
        Text(
            text = name,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 2.dp),
            color = if (name == "검정" || name == "하양") Color.Black else Color.Black
        )
    }
}

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

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    ChatBotTheme {
        SearchScreen(
            onSearchClicked = {}
        )
    }
}