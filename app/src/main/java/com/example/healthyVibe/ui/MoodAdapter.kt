package com.example.healthyVibe.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthyVibe.R
import com.example.healthyVibe.data.MoodEntry
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class MoodAdapter(
    val items: MutableList<MoodEntry>,
    private val onChanged: () -> Unit   // called after add/clear to save + refresh chart
) : RecyclerView.Adapter<MoodAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val emoji: TextView = v.findViewById(R.id.tvEmoji)
        val date: TextView = v.findViewById(R.id.tvDate)
        val desc: TextView = v.findViewById(R.id.tvDesc)
        val btnClear: MaterialButton = v.findViewById(R.id.btnClearMood)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(LayoutInflater.from(parent.context).inflate(R.layout.item_mood, parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val m = items[position]
        holder.emoji.text = m.emoji

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        holder.date.text = sdf.format(Date(m.timestamp))

        val d = m.description?.trim().orEmpty()
        if (d.isEmpty()) {
            holder.desc.visibility = View.GONE
        } else {
            holder.desc.visibility = View.VISIBLE
            holder.desc.text = d
        }

        holder.btnClear.setOnClickListener {
            val idx = holder.bindingAdapterPosition
            if (idx != RecyclerView.NO_POSITION) {
                items.removeAt(idx)
                notifyItemRemoved(idx)
                onChanged()
            }
        }
    }
}
