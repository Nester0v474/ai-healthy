package com.example.airich.ui.habittracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.airich.databinding.ItemHabitBinding
import com.example.airich.viewmodel.HabitWithCompletion

class HabitAdapter(
    private val onToggle: (Long) -> Unit,
    private val onDelete: (Long) -> Unit
) : ListAdapter<HabitWithCompletion, HabitAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHabitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemHabitBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HabitWithCompletion) {
            binding.tvHabitName.text = item.habit.name
            binding.checkboxCompleted.isChecked = item.isCompleted
            binding.checkboxCompleted.setOnClickListener { onToggle(item.habit.id) }
            binding.btnDelete.setOnClickListener { onDelete(item.habit.id) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<HabitWithCompletion>() {
        override fun areItemsTheSame(old: HabitWithCompletion, new: HabitWithCompletion) =
            old.habit.id == new.habit.id

        override fun areContentsTheSame(old: HabitWithCompletion, new: HabitWithCompletion) =
            old == new
    }
}
