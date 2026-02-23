package com.example.airich.ui.healthytasktracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.airich.data.HealthyTaskEntry
import com.example.airich.databinding.ItemHealthyTaskEntryBinding
import java.time.format.DateTimeFormatter
import java.util.Locale

class HealthyTaskEntryAdapter(
    private val onToggleComplete: (Long, Boolean) -> Unit,
    private val onDeleteClick: (Long) -> Unit
) : ListAdapter<HealthyTaskEntry, HealthyTaskEntryAdapter.HealthyTaskEntryViewHolder>(DiffCallback()) {
    
    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault())
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HealthyTaskEntryViewHolder {
        val binding = ItemHealthyTaskEntryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HealthyTaskEntryViewHolder(binding, onToggleComplete, onDeleteClick, dateFormatter)
    }
    
    override fun onBindViewHolder(holder: HealthyTaskEntryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class HealthyTaskEntryViewHolder(
        private val binding: ItemHealthyTaskEntryBinding,
        private val onToggleComplete: (Long, Boolean) -> Unit,
        private val onDeleteClick: (Long) -> Unit,
        private val dateFormatter: DateTimeFormatter
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(healthyTaskEntry: HealthyTaskEntry) {
            binding.tvTitle.text = healthyTaskEntry.title
            binding.tvDate.text = healthyTaskEntry.date.format(dateFormatter)
            binding.checkboxCompleted.isChecked = healthyTaskEntry.isCompleted
            
            // Отображаем описание, если оно есть
            if (healthyTaskEntry.description.isNullOrBlank()) {
                binding.tvDescription.visibility = View.GONE
            } else {
                binding.tvDescription.visibility = View.VISIBLE
                binding.tvDescription.text = healthyTaskEntry.description
            }
            
            // Визуальное оформление в зависимости от статуса
            if (healthyTaskEntry.isCompleted) {
                binding.tvTitle.alpha = 0.6f
                binding.tvDescription.alpha = 0.6f
                binding.tvDate.alpha = 0.6f
            } else {
                binding.tvTitle.alpha = 1.0f
                binding.tvDescription.alpha = 1.0f
                binding.tvDate.alpha = 1.0f
            }
            
            // Обработчик чекбокса
            binding.checkboxCompleted.setOnCheckedChangeListener { _, isChecked ->
                onToggleComplete(healthyTaskEntry.id, isChecked)
            }
            
            // Долгое нажатие для удаления
            binding.root.setOnLongClickListener {
                onDeleteClick(healthyTaskEntry.id)
                true
            }
        }
    }
    
    class DiffCallback : DiffUtil.ItemCallback<HealthyTaskEntry>() {
        override fun areItemsTheSame(oldItem: HealthyTaskEntry, newItem: HealthyTaskEntry): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: HealthyTaskEntry, newItem: HealthyTaskEntry): Boolean {
            return oldItem == newItem
        }
    }
}

