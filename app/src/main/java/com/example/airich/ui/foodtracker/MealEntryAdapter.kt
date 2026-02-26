package com.example.airich.ui.foodtracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.airich.data.MealEntryWithFood
import com.example.airich.data.MealType
import com.example.airich.databinding.ItemMealEntryBinding
import java.text.DecimalFormat

class MealEntryAdapter(
    private val onDeleteClick: (Long) -> Unit
) : ListAdapter<MealEntryWithFood, MealEntryAdapter.MealEntryViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealEntryViewHolder {
        val binding = ItemMealEntryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MealEntryViewHolder(binding, onDeleteClick)
    }

    override fun onBindViewHolder(holder: MealEntryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MealEntryViewHolder(
        private val binding: ItemMealEntryBinding,
        private val onDeleteClick: (Long) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val df = DecimalFormat("#.#")

        fun bind(mealEntryWithFood: MealEntryWithFood) {
            val mealEntry = mealEntryWithFood.mealEntry
            val foodItem = mealEntryWithFood.foodItem

            binding.tvFoodName.text = foodItem.name
            binding.tvMealType.text = getMealTypeName(mealEntry.mealType)
            binding.tvAmount.text = "${df.format(mealEntry.amountInGrams)} г"

            binding.tvCalories.text = "${df.format(mealEntryWithFood.totalCalories)} ккал"
            binding.tvProtein.text = "Б: ${df.format(mealEntryWithFood.totalProtein)}г"
            binding.tvCarbs.text = "У: ${df.format(mealEntryWithFood.totalCarbs)}г"
            binding.tvFat.text = "Ж: ${df.format(mealEntryWithFood.totalFat)}г"

            binding.root.setOnLongClickListener {
                onDeleteClick(mealEntry.id)
                true
            }
        }

        private fun getMealTypeName(mealType: MealType): String {
            return when (mealType) {
                MealType.BREAKFAST -> "Завтрак"
                MealType.LUNCH -> "Обед"
                MealType.DINNER -> "Ужин"
                MealType.SNACK -> "Перекус"
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MealEntryWithFood>() {
        override fun areItemsTheSame(oldItem: MealEntryWithFood, newItem: MealEntryWithFood): Boolean {
            return oldItem.mealEntry.id == newItem.mealEntry.id
        }

        override fun areContentsTheSame(oldItem: MealEntryWithFood, newItem: MealEntryWithFood): Boolean {
            return oldItem == newItem
        }
    }
}
