package com.mobicom.s16.mco

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mobicom.s16.mco.databinding.CardinfoPageBinding

class CardInfoActivity : AppCompatActivity()   {
    private lateinit var binding: CardinfoPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = CardinfoPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imgBack.setOnClickListener {
            finish()
        }


    }
}