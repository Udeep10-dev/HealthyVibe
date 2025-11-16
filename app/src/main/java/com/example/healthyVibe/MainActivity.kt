package com.example.healthyVibe

import com.example.healthyVibe.ui.ProfileFragment
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.healthyVibe.databinding.ActivityMainBinding
import com.example.healthyVibe.ui.HabitsFragment
import com.example.healthyVibe.ui.MoodFragment
import com.example.healthyVibe.ui.SettingsFragment
import com.example.healthyVibe.data.Storage
import com.example.healthyVibe.data.Keys
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestNotificationPermissionIfNeeded()
        resetHabitsIfNewDay()

        // default fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, HabitsFragment())
            .commit()

        binding.bottomNav.setOnItemSelectedListener { item ->
            val frag = when (item.itemId) {
                R.id.menu_habits -> HabitsFragment()
                R.id.menu_mood -> MoodFragment()
                R.id.menu_settings -> SettingsFragment()
                R.id.menu_profile -> ProfileFragment()
                else -> SettingsFragment()
            }
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    android.R.anim.fade_in, android.R.anim.fade_out,
                    android.R.anim.fade_in, android.R.anim.fade_out
                )
                .replace(R.id.fragmentContainer, frag)
                .commit()
            true
        }
    }

    private fun resetHabitsIfNewDay() {
        val prefs = getSharedPreferences(Keys.PREFS, MODE_PRIVATE)
        val last = prefs.getString("last_reset_ymd", null)
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

        if (last != today) {
            val storage = Storage(this)
            val habits = storage.loadHabits()
            if (habits.isNotEmpty()) {
                habits.forEach { it.done = 0 }
                storage.saveHabits(habits)
            }
            prefs.edit().putString("last_reset_ymd", today).apply()
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }
    }
}
