package com.example.airich.ui.moodtracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.airich.data.MoodEntry
import com.example.airich.databinding.ItemMoodEntryBinding
import java.time.format.DateTimeFormatter
import java.util.Locale

class MoodEntryAdapter(
    private val onDeleteClick: (Long) -> Unit
) : ListAdapter<MoodEntry, MoodEntryAdapter.MoodEntryViewHolder>(DiffCallback()) {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodEntryViewHolder {
        val binding = ItemMoodEntryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MoodEntryViewHolder(binding, onDeleteClick, dateFormatter)
    }

    override fun onBindViewHolder(holder: MoodEntryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MoodEntryViewHolder(
        private val binding: ItemMoodEntryBinding,
        private val onDeleteClick: (Long) -> Unit,
        private val dateFormatter: DateTimeFormatter
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(moodEntry: MoodEntry) {
            binding.tvDate.text = moodEntry.date.format(dateFormatter)
            binding.ratingMood.rating = moodEntry.moodScore.toFloat()

            if (moodEntry.note.isNullOrBlank()) {
                binding.tvNote.visibility = View.GONE
            } else {
                binding.tvNote.visibility = View.VISIBLE
                binding.tvNote.text = moodEntry.note
            }

            binding.root.setOnLongClickListener {
                onDeleteClick(moodEntry.id)
                true
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MoodEntry>() {
        override fun areItemsTheSame(oldItem: MoodEntry, newItem: MoodEntry): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MoodEntry, newItem: MoodEntry): Boolean {
            return oldItem == newItem
        }
    }
}
