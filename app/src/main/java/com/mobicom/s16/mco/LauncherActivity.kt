package com.mobicom.s16.mco

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.mobicom.s16.mco.util.CardCacheManager

class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        Log.d("LauncherActivity", "User is: $user")

        if (user != null) {
            val isAvailable = CardCacheManager.isCacheAvailable(this)
            val isComplete = CardCacheManager.isCacheComplete(this)
            Log.d("LauncherActivity", "Card cache available: $isAvailable, complete: $isComplete")

            val intent = if (!isAvailable || !isComplete) {
                Log.d("LauncherActivity", "Launching DownloadActivity (missing or incomplete cache)")
                Intent(this, DownloadActivity::class.java).apply {
                    putExtra("missingOnly", isAvailable) // only fetch missing if file exists
                }
            } else {
                Log.d("LauncherActivity", "Launching MainActivity")
                Intent(this, MainActivity::class.java)
            }

            startActivity(intent)
        } else {
            Log.d("LauncherActivity", "User not signed in, going to LoginActivity")
            startActivity(Intent(this, LoginActivity::class.java))
        }

        finish()
    }
}
