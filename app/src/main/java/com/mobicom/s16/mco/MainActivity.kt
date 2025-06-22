package com.mobicom.s16.mco

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mobicom.s16.mco.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, ArchiveFragment())
                .commit()
            setFabSelected(false)
            binding.bottomNavigationView.selectedItemId = R.id.archive
        }

        binding.fab.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, ScannerFragment())
                .commit()
            setFabSelected(true)
            binding.bottomNavigationView.menu.findItem(R.id.scanner).isChecked = true
        }

        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            setFabSelected(false)
            when (menuItem.itemId) {
                R.id.archive -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, ArchiveFragment())
                        .commit()
                    true
                }
                R.id.profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, ProfileFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }

    }

    private fun setFabSelected(isSelected: Boolean) {
        if (isSelected) {
//            binding.fab.backgroundTintList = getColorStateList(R.color.fab_selected_color)
            binding.fab.setImageResource(R.drawable.camera_svgrepo_com__4_)
        } else {
//            binding.fab.backgroundTintList = getColorStateList(R.color.fab_normal_color)
            binding.fab.setImageResource(R.drawable.camera)
        }
    }

}
