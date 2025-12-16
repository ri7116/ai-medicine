package com.example.chatbot.features.search

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import android.content.Intent
import com.example.chatbot.databinding.ActivitySearchResultBinding
import com.example.chatbot.features.search.MedicineSearchViewModel
import com.example.chatbot.features.search.SearchFilter


class SearchResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchResultBinding
    private lateinit var viewModel: MedicineSearchViewModel
    private lateinit var medicineAdapter: MedicineAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySearchResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(MedicineSearchViewModel::class.java)

        medicineAdapter = MedicineAdapter { clickedMedicine ->
            val intent = Intent(this, MedicineDetailActivity::class.java).apply {
                putExtra("MEDICINE_DETAIL", clickedMedicine)
            }
            startActivity(intent)
        }
        binding.recyclerViewResults.adapter = medicineAdapter
        binding.recyclerViewResults.layoutManager = LinearLayoutManager(this)


        viewModel.searchResults.observe(this) { results ->
            android.util.Log.d("SEARCH_RESULT", "결과 화면에 ${results.size}개 표시")
            medicineAdapter.submitList(results)
        }

        val filter = intent.getParcelableExtra<SearchFilter>("SEARCH_FILTER")

        if (filter != null) {
            viewModel.executeSearchWithFilter(filter)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "검색 결과"
    }
}