package com.mobicom.s16.mco

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        Log.d("LauncherActivity", "User is: ${user?.uid ?: "null"}")

        // 🔐 Redirect to login if no user is signed in
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // 🚀 Skip cache checks, go directly to MainActivity
        Log.d("LauncherActivity", "Launching MainActivity")
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
