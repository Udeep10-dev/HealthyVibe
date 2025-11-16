package com.example.healthyVibe.ui

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.healthyVibe.R
import com.example.healthyVibe.data.Habit
import com.google.android.material.button.MaterialButton

class HabitAdapter(val items: MutableList<Habit>, private val onChanged: () -> Unit) :
    RecyclerView.Adapter<HabitAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.tvHabitName)
        val progress: TextView = v.findViewById(R.id.tvProgress)
        val time: TextView = v.findViewById(R.id.tvTime)
        val desc: TextView = v.findViewById(R.id.tvDesc)
        val btnDone: Button = v.findViewById(R.id.btnDone)
        val btnEdit: MaterialButton = v.findViewById(R.id.btnEdit)
        val btnDelete: MaterialButton  = v.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(LayoutInflater.from(parent.context).inflate(R.layout.item_habit, parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val h = items[position]
        holder.name.text = h.name
        holder.progress.text = "${h.done}/${h.timesPerDay}"
        holder.desc.text = h.description
        if (h.timeHHmm.isNullOrEmpty()) {
            holder.time.visibility = View.GONE
        } else {
            holder.time.visibility = View.VISIBLE
            holder.time.text = h.timeHHmm
        }

        holder.btnDone.setOnClickListener {
            if (h.done < h.timesPerDay) {
                h.done++
                notifyItemChanged(position)
                onChanged()
            }
        }

        holder.btnEdit.setOnClickListener { showEditDialog(holder, position) }
        holder.btnDelete.setOnClickListener {
            items.removeAt(position)
            notifyItemRemoved(position)
            onChanged()
        }

        // Keep long-press delete for convenience
        holder.itemView.setOnLongClickListener {
            items.removeAt(position)
            notifyItemRemoved(position)
            onChanged()
            true
        }
    }

    private fun showEditDialog(holder: VH, position: Int) {
        val ctx = holder.itemView.context
        val h = items[position]

        val dialogV = LayoutInflater.from(ctx).inflate(R.layout.dialog_add_habit, null)
        val nameEt = dialogV.findViewById<EditText>(R.id.etName)
        val timesEt = dialogV.findViewById<EditText>(R.id.etTimes)
        val descEt = dialogV.findViewById<EditText>(R.id.etDescription)
        val tvPicked = dialogV.findViewById<TextView>(R.id.tvPickedTime)
        val btnPick = dialogV.findViewById<View>(R.id.btnPickTime)
        val btnClear = dialogV.findViewById<View>(R.id.btnClearTime)

        nameEt.setText(h.name)
        timesEt.setText(h.timesPerDay.toString())
        descEt.setText(h.description)
        tvPicked.text = h.timeHHmm ?: "No time"

        // Use TimePickerDialog here (shows clock UI on modern devices)
        btnPick.setOnClickListener {
            val hour = h.timeHHmm?.split(":")?.getOrNull(0)?.toIntOrNull() ?: 8
            val minute = h.timeHHmm?.split(":")?.getOrNull(1)?.toIntOrNull() ?: 0
            val tpd = TimePickerDialog(ctx, { _, hh, mm ->
                tvPicked.text = String.format("%02d:%02d", hh, mm)
            }, hour, minute, android.text.format.DateFormat.is24HourFormat(ctx))
            tpd.show()
        }
        btnClear.setOnClickListener { tvPicked.text = "No time" }

        AlertDialog.Builder(ctx)
            .setTitle("Edit Habit")
            .setView(dialogV)
            .setPositiveButton(R.string.save) { _, _ ->
                val newName = nameEt.text.toString().trim()
                val newTimes = timesEt.text.toString().toIntOrNull() ?: h.timesPerDay
                val newDesc = descEt.text.toString().trim()
                if (newName.isEmpty() || newTimes <= 0) {
                    Toast.makeText(ctx, "Invalid values", Toast.LENGTH_SHORT).show()
                } else {
                    h.name = newName
                    h.timesPerDay = newTimes
                    h.description = newDesc
                    h.timeHHmm = tvPicked.text.toString().takeIf { it != "No time" }
                    if (h.done > newTimes) h.done = newTimes
                    notifyItemChanged(position)
                    onChanged()
                }
            }
            .setNeutralButton("Delete") { _, _ ->
                items.removeAt(position)
                notifyItemRemoved(position)
                onChanged()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
