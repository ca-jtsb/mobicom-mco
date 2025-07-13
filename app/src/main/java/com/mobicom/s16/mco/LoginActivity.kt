package com.mobicom.s16.mco

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.mobicom.s16.mco.databinding.LoginPageBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: LoginPageBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = LoginPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmailLogin.text.toString().trim()
            val password = binding.etPasswordLogin.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, task.exception?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        binding.tvCreateAccount.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            // Skip login if already logged in
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
