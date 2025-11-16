package com.example.healthyVibe.ui

import android.app.AlertDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthyVibe.R
import com.example.healthyVibe.data.Habit
import com.example.healthyVibe.data.Storage
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat

class HabitsFragment : Fragment() {

    private lateinit var storage: Storage
    private lateinit var adapter: HabitAdapter
    private lateinit var tvProgress: TextView
    private lateinit var tvEmpty: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_habits, container, false)
        storage = Storage(requireContext())
        tvProgress = v.findViewById(R.id.tvProgress)
        tvEmpty = v.findViewById(R.id.tvEmpty)

        val rv = v.findViewById<RecyclerView>(R.id.rvHabits)
        adapter = HabitAdapter(storage.loadHabits(),
            onChanged = {
                storage.saveHabits(adapter.items)
                updateProgressAndEmpty()
            }
        )
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        v.findViewById<View>(R.id.fabAddHabit).setOnClickListener { showAddDialog() }

        updateProgressAndEmpty()
        return v
    }

    private fun showAddDialog() {
        val dialogV = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_habit, null)
        val nameEt = dialogV.findViewById<EditText>(R.id.etName)
        val timesEt = dialogV.findViewById<EditText>(R.id.etTimes)
        val descEt = dialogV.findViewById<EditText>(R.id.etDescription)
        val tvPicked = dialogV.findViewById<TextView>(R.id.tvPickedTime)
        var pickedHour: Int? = null
        var pickedMinute: Int? = null

        dialogV.findViewById<View>(R.id.btnPickTime).setOnClickListener {
            val is24 = DateFormat.is24HourFormat(requireContext())
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(if (is24) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
                .setHour(8)
                .setMinute(0)
                .setTitleText("Select time")
                .build()
            picker.addOnPositiveButtonClickListener {
                pickedHour = picker.hour
                pickedMinute = picker.minute
                tvPicked.text = String.format("%02d:%02d", pickedHour, pickedMinute)
            }
            picker.show(parentFragmentManager, "time_picker_add")
        }

        dialogV.findViewById<View>(R.id.btnClearTime).setOnClickListener {
            pickedHour = null
            pickedMinute = null
            tvPicked.text = "No time"
        }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.add_habit))
            .setView(dialogV)
            .setPositiveButton(R.string.save) { _, _ ->
                val name = nameEt.text.toString().trim()
                val times = timesEt.text.toString().toIntOrNull() ?: 1
                val desc = descEt.text.toString().trim()
                if (name.isEmpty() || times <= 0) {
                    Toast.makeText(requireContext(), "Enter a valid name and times/day", Toast.LENGTH_SHORT).show()
                } else {
                    val timeHHmm = if (pickedHour != null) String.format("%02d:%02d", pickedHour, pickedMinute ?: 0) else null
                    adapter.items.add(Habit(name, times, 0, desc, timeHHmm))
                    adapter.notifyItemInserted(adapter.items.size - 1)
                    storage.saveHabits(adapter.items)
                    updateProgressAndEmpty()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun updateProgressAndEmpty() {
        val items = adapter.items
        var total = 0
        var done = 0
        items.forEach {
            total += it.timesPerDay
            done += it.done
        }
        val percent = if (total == 0) 0 else (done * 100 / total)
        tvProgress.text = "Today: $percent%"
        tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
    }
}
