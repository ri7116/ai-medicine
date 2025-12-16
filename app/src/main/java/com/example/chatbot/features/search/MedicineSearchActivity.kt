package com.example.chatbot.features.search

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.chatbot.R
import com.example.chatbot.databinding.ActivityMedicineSearchBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import com.example.chatbot.features.search.SearchFilter
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext

class MedicineSearchActivity : AppCompatActivity() {

    private lateinit var viewModel: MedicineSearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get<MedicineSearchViewModel>(MedicineSearchViewModel::class.java)

        setContent {
            val context = LocalContext.current

            SearchScreen(
                viewModel = viewModel,
                onSearchClicked = { filter ->
                    val intent = Intent(context, SearchResultActivity::class.java).apply {
                        putExtra("SEARCH_FILTER", filter)
                    }
                    context.startActivity(intent)
                }
            )
        }
    }
}