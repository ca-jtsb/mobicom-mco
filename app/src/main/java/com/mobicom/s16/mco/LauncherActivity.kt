package com.mobicom.s16.mco

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.mobicom.s16.mco.util.CardCacheManager
import java.io.File

class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        Log.d("LauncherActivity", "User is: $user")

        if (user != null) {
            val file = File(filesDir, "cards_cache.json")
            // uncomment this line to delete the cache file for testing purposes
//            if (file.exists()) {
//                file.delete()
//                Log.d("LauncherActivity", "Deleted existing cache for testing")
//            }
            val cacheAvailable = CardCacheManager.isCacheAvailable(this)
            Log.d("LauncherActivity", "Card cache available: $cacheAvailable")

            if (!cacheAvailable) {
                Log.d("LauncherActivity", "Launching DownloadActivity")
                startActivity(Intent(this, DownloadActivity::class.java))
            } else {
                Log.d("LauncherActivity", "Launching MainActivity")
                startActivity(Intent(this, MainActivity::class.java))
            }
        } else {
            Log.d("LauncherActivity", "User not signed in, going to LoginActivity")
            startActivity(Intent(this, LoginActivity::class.java))
        }

        finish()
    }
}
