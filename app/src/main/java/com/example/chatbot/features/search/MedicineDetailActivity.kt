package com.example.chatbot.features.search

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.chatbot.databinding.ActivityMedicineDetailBinding
import com.example.chatbot.features.search.Medicine
import com.bumptech.glide.Glide
import com.example.chatbot.R
import android.view.View


class MedicineDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicineDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMedicineDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "약품 상세 정보"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val medicine: Medicine? = intent.getParcelableExtra("MEDICINE_DETAIL")

        if (medicine != null) {

            binding.tvDetailName.text = medicine.name
            binding.tvDetailIdentifiers.text = "식별 기호: ${medicine.id1} / ${medicine.id2}"


            binding.tvDetailForm.text = "제형: ${medicine.form}"
            binding.tvDetailShape.text = "모양: ${medicine.shape}"
            binding.tvDetailColor.text = "색상: ${medicine.color}"
            binding.tvDetailLine.text = "분할선: ${medicine.line}"


            val imageUrl = medicine.imageUrl

            if (imageUrl.isNullOrEmpty()) {
                binding.imgDetailImage.visibility = View.GONE
                binding.tvNoImage.visibility = View.VISIBLE
            } else {
                binding.tvNoImage.visibility = View.GONE
                binding.imgDetailImage.visibility = View.VISIBLE

                Glide.with(this)
                    .load(imageUrl)
                    .into(binding.imgDetailImage)
            }

            android.util.Log.d("DETAIL_VIEW", "상세 정보 로드: ${medicine.name}")

        } else {
            // 데이터가 없을 경우 처리
        }
    }
}