package com.example.healthyVibe.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.healthyVibe.R
import com.example.healthyVibe.data.Storage
import com.example.healthyVibe.work.HydrationWorker
import java.util.concurrent.TimeUnit

class SettingsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_settings, container, false)
        val et = v.findViewById<EditText>(R.id.etInterval)
        val btn = v.findViewById<Button>(R.id.btnSetReminder)

        val storage = Storage(requireContext())
        et.setText(storage.getHydrationMinutes().toString())

        btn.setOnClickListener {
            val min = et.text.toString().toIntOrNull()?.coerceAtLeast(15) ?: 60
            storage.setHydrationMinutes(min)
            val req = PeriodicWorkRequestBuilder<HydrationWorker>(min.toLong(), TimeUnit.MINUTES).build()
            WorkManager.getInstance(requireContext())
                .enqueueUniquePeriodicWork("hydration", ExistingPeriodicWorkPolicy.UPDATE, req)
            Toast.makeText(requireContext(), "Reminder set every $min minutes", Toast.LENGTH_SHORT).show()
        }
        return v
    }
}
