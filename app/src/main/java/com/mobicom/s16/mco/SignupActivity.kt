package com.mobicom.s16.mco

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mobicom.s16.mco.databinding.SignupPageBinding

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: SignupPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = SignupPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignup.setOnClickListener {
            finish()
        }
    }
}