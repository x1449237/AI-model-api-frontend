package com.aiaggregator.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.aiaggregator.app.databinding.ActivityMainBinding
import com.aiaggregator.app.ui.home.HomeFragment
import com.aiaggregator.app.ui.code.CodeFragment
import com.aiaggregator.app.ui.image.ImageFragment
import com.aiaggregator.app.ui.cluster.ClusterFragment
import com.aiaggregator.app.ui.history.HistoryFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val nightMode = prefs.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(nightMode)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_code -> CodeFragment()
                R.id.nav_image -> ImageFragment()
                R.id.nav_cluster -> ClusterFragment()
                R.id.nav_history -> HistoryFragment()
                else -> HomeFragment()
            }
            loadFragment(fragment)
            true
        }

        binding.bottomNav.setOnItemReselectedListener { }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}