package com.example.healthyVibe.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthyVibe.R
import com.example.healthyVibe.data.MoodEntry
import com.example.healthyVibe.data.Storage
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class MoodFragment : Fragment() {

    private lateinit var storage: Storage
    private lateinit var adapter: MoodAdapter
    private var chart: LineChart? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_mood, container, false)
        storage = Storage(requireContext())

        adapter = MoodAdapter(storage.loadMoods()) {
            storage.saveMoods(adapter.items)
            updateChart()
        }

        val rv = v.findViewById<RecyclerView>(R.id.rvMoods)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        v.findViewById<View>(R.id.btnAddMood).setOnClickListener { showEmojiPicker() }
        v.findViewById<View>(R.id.btnShare).setOnClickListener { shareSummary() }

        chart = v.findViewById(R.id.moodChart)
        setupChart()
        updateChart()

        return v
    }

    // ---- Add mood with optional description ----

    private fun showEmojiPicker() {

        val emojis = arrayOf(
            "\uD83E\uDD29",
            "\uD83D\uDE00",
            "\uD83D\uDE42",
            "\uD83D\uDE0C",
            "\uD83D\uDE10",
            "\uD83D\uDE15",
            "\uD83D\uDE2C",
            "\uD83D\uDE34",
            "\uD83D\uDE22",
            "\uD83D\uDE21",
            "\uD83E\uDD22",
            "\uD83D\uDCA9"
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Pick mood")
            .setItems(emojis) { _, which ->
                val picked = emojis[which]
                askDescriptionThenAdd(picked)
            }
            .show()
    }

    private fun askDescriptionThenAdd(emoji: String) {
        val input = EditText(requireContext()).apply {
            hint = "Description (optional)"
            setSingleLine(false)
            minLines = 2
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Add description")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                addMood(emoji, input.text?.toString()?.trim().orEmpty())
            }
            .setNegativeButton("Skip") { _, _ ->
                addMood(emoji, "")
            }
            .show()
    }

    private fun addMood(emoji: String, description: String) {
        val entry = MoodEntry(emoji, System.currentTimeMillis(), description.ifEmpty { null })
        adapter.items.add(0, entry)
        adapter.notifyItemInserted(0)
        storage.saveMoods(adapter.items)
        updateChart()
        Toast.makeText(requireContext(), "Mood added", Toast.LENGTH_SHORT).show()
    }

    private fun shareSummary() {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val text = buildString {
            append("My recent moods:\n")
            adapter.items.take(10).forEach {
                append("${it.emoji} ${sdf.format(Date(it.timestamp))}")
                if (!it.description.isNullOrEmpty()) append(" — ${it.description}")
                append("\n")
            }
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share_summary)))
    }

    // ----------------- Chart -----------------

    private fun setupChart() {
        chart?.apply {
            description.isEnabled = false
            setNoDataText("No mood data yet")
            legend.isEnabled = false

            axisRight.isEnabled = false
            axisLeft.axisMinimum = 0f
            axisLeft.axisMaximum = 5f
            axisLeft.granularity = 1f

            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)
            xAxis.valueFormatter = object : ValueFormatter() {
                private val cal = Calendar.getInstance()
                private val fmt = SimpleDateFormat("EEE", Locale.getDefault())
                override fun getFormattedValue(value: Float): String {
                    val daysAgo = (6 - value).roundToInt().coerceIn(0, 6)
                    cal.timeInMillis = System.currentTimeMillis()
                    cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
                    return fmt.format(cal.time)
                }
            }
        }
    }

    private fun updateChart() {
        val entries = computeWeeklyAverages()
        if (entries.isEmpty()) {
            chart?.clear()
            return
        }
        val dataSet = LineDataSet(entries, "Mood").apply {
            setDrawValues(false)
            setDrawCircles(true)
            lineWidth = 2f
            circleRadius = 3.5f
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
        chart?.data = LineData(dataSet)
        chart?.invalidate()
    }

    /** Build 7 points (oldest..today), y = average score (1..5), 0 = no data */
    private fun computeWeeklyAverages(): List<Entry> {
        if (adapter.items.isEmpty()) return emptyList()


        val score = mapOf(
            "\uD83E\uDD29" to 5f,
            "\uD83D\uDE00" to 4f,
            "\uD83D\uDE42" to 3f,
            "\uD83D\uDE0C" to 3f,
            "\uD83D\uDE10" to 2f,
            "\uD83D\uDE15" to 2f,
            "\uD83D\uDE2C" to 2f,
            "\uD83D\uDE34" to 2f,
            "\uD83D\uDE22" to 1f,
            "\uD83D\uDE21" to 1f,
            "\uD83E\uDD22" to 1f,
            "\uD83D\uDCA9" to 1f
        )

        val byDay = FloatArray(7) { 0f }
        val counts = IntArray(7) { 0 }

        val calStart = Calendar.getInstance()
        val calEnd = Calendar.getInstance()

        for (i in 6 downTo 0) {
            val x = (6 - i)

            calStart.timeInMillis = System.currentTimeMillis()
            calStart.add(Calendar.DAY_OF_YEAR, -i)
            calStart.set(Calendar.HOUR_OF_DAY, 0)
            calStart.set(Calendar.MINUTE, 0)
            calStart.set(Calendar.SECOND, 0)
            calStart.set(Calendar.MILLISECOND, 0)

            calEnd.timeInMillis = calStart.timeInMillis
            calEnd.set(Calendar.HOUR_OF_DAY, 23)
            calEnd.set(Calendar.MINUTE, 59)
            calEnd.set(Calendar.SECOND, 59)
            calEnd.set(Calendar.MILLISECOND, 999)

            val startMs = calStart.timeInMillis
            val endMs = calEnd.timeInMillis

            adapter.items.forEach { m ->
                if (m.timestamp in startMs..endMs) {
                    val s = score[m.emoji] ?: 0f
                    if (s > 0f) {
                        byDay[x] += s
                        counts[x] += 1
                    }
                }
            }
        }

        val result = mutableListOf<Entry>()
        for (x in 0..6) {
            val y = if (counts[x] == 0) 0f else byDay[x] / counts[x].toFloat()
            result.add(Entry(x.toFloat(), y))
        }
        return result
    }
}
