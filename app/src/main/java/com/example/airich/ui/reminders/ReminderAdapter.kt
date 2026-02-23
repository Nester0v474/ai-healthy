package com.example.airich.ui.reminders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.airich.data.Reminder
import com.example.airich.databinding.ItemReminderBinding

class ReminderAdapter(private val onDelete: (Long) -> Unit) :
    ListAdapter<Reminder, ReminderAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReminderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemReminderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(reminder: Reminder) {
            binding.tvReminderTitle.text = reminder.title
            binding.tvReminderTime.text = String.format("%02d:%02d", reminder.hour, reminder.minute)
            binding.btnDelete.setOnClickListener { onDelete(reminder.id) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Reminder>() {
        override fun areItemsTheSame(old: Reminder, new: Reminder) = old.id == new.id
        override fun areContentsTheSame(old: Reminder, new: Reminder) = old == new
    }
}
