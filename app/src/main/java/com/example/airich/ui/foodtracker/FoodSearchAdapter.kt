package com.example.airich.ui.foodtracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.airich.data.FoodItem
import com.example.airich.databinding.ItemFoodSearchBinding
import java.text.DecimalFormat

class FoodSearchAdapter(
    private val onItemClick: (FoodItem) -> Unit
) : ListAdapter<FoodItem, FoodSearchAdapter.FoodViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val binding = ItemFoodSearchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FoodViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class FoodViewHolder(
        private val binding: ItemFoodSearchBinding,
        private val onItemClick: (FoodItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val df = DecimalFormat("#.#")

        fun bind(foodItem: FoodItem) {
            binding.tvFoodName.text = foodItem.name
            binding.tvFoodInfo.text =
                "${df.format(foodItem.caloriesPer100g)} ккал/100г | " +
                "Б: ${df.format(foodItem.protein)}г, " +
                "У: ${df.format(foodItem.carbs)}г, " +
                "Ж: ${df.format(foodItem.fat)}г"

            binding.root.setOnClickListener {
                onItemClick(foodItem)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<FoodItem>() {
        override fun areItemsTheSame(oldItem: FoodItem, newItem: FoodItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FoodItem, newItem: FoodItem): Boolean {
            return oldItem == newItem
        }
    }
}
