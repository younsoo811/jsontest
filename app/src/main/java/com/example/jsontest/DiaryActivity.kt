package com.example.jsontest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.jsontest.databinding.ActivityDiaryBinding

class DiaryActivity : AppCompatActivity(){

    //바인딩 객체 선언
    lateinit var binding: ActivityDiaryBinding

    var dogList = arrayListOf<Dog>(
        Dog("Chow Chow", "2023-06-05 10:00", "보통", "ic_01d"),
        Dog("Breed Pomeranian", "2023-06-04 08:00", "좋음", "ic_11d"),
        Dog("Golden Retriver", "2023-06-03 10:00", "보통", "ic_10n"),
        Dog("Yorkshire Terrier", "2023-06-02 20:00", "나쁨", "ic_50d"),
        Dog("Pug", "2023-06-02 09:00", "나쁨", "ic_02n"),
        Dog("Alaskan Malamute", "2023-06-01 19:00", "나쁨", "ic_03d"),
        Dog("Shih Tzu", "2023-06-01 10:00", "좋음", "ic_01n")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_diary)

        binding = ActivityDiaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dogAdapter = DiaryListAdapter(this, dogList)
        binding.mainListView.adapter = dogAdapter
    }
}