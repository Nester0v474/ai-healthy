package com.example.airich.ui.sleepdiary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.airich.data.SleepEntry
import com.example.airich.databinding.ItemSleepEntryBinding
import java.time.format.DateTimeFormatter

class SleepEntryAdapter(private val onDelete: (Long) -> Unit) :
    ListAdapter<SleepEntry, SleepEntryAdapter.ViewHolder>(DiffCallback()) {

    private val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", java.util.Locale.forLanguageTag("ru"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSleepEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemSleepEntryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(entry: SleepEntry) {
            binding.tvDate.text = entry.date.format(dateFormatter)
            binding.tvSleepTime.text = "${entry.bedTime} — ${entry.wakeTime}"
            binding.tvQuality.text = "Качество: " + "★".repeat(entry.quality) + "☆".repeat(5 - entry.quality)
            binding.tvNote.text = entry.note
            binding.tvNote.visibility = if (entry.note.isNullOrBlank()) View.GONE else View.VISIBLE
            binding.btnDelete.setOnClickListener { onDelete(entry.id) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SleepEntry>() {
        override fun areItemsTheSame(old: SleepEntry, new: SleepEntry) = old.id == new.id
        override fun areContentsTheSame(old: SleepEntry, new: SleepEntry) = old == new
    }
}
