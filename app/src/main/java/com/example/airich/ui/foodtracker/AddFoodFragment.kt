package com.example.airich.ui.foodtracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.airich.data.FoodDatabase
import com.example.airich.data.FoodItem
import com.example.airich.data.MealType
import com.example.airich.data.FoodCategory
import com.example.airich.databinding.FragmentAddFoodBinding
import com.example.airich.repository.FoodRepository
import com.example.airich.viewmodel.FoodViewModel
import com.example.airich.viewmodel.FoodViewModelFactory
import kotlinx.coroutines.launch

class AddFoodFragment : Fragment() {

    private var _binding: FragmentAddFoodBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FoodViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddFoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            val context = requireContext().applicationContext

            val database = FoodDatabase.getDatabase(context)
            val repository = FoodRepository(database)
            val factory = FoodViewModelFactory(repository)
            viewModel = ViewModelProvider(this, factory)[FoodViewModel::class.java]
        } catch (e: Exception) {

            android.util.Log.e("AddFoodFragment", "Ошибка инициализации", e)
            android.util.Log.e("AddFoodFragment", "Stack trace", e)
            return
        }

        val mealTypes = MealType.values().map { getMealTypeName(it) }
        val mealTypeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            mealTypes
        )
        mealTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMealType.adapter = mealTypeAdapter

        val categories = FoodCategory.values().map { getFoodCategoryName(it) }
        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFoodCategory.adapter = categoryAdapter

        binding.btnAddMeal.setOnClickListener {

            val foodName = binding.etFoodName.text?.toString()?.trim() ?: ""
            if (foodName.isBlank()) {
                binding.etFoodName.error = "Введите название продукта"
                return@setOnClickListener
            }

            val caloriesText = binding.etCalories.text?.toString() ?: ""
            if (caloriesText.isBlank()) {
                binding.etCalories.error = "Введите калории"
                return@setOnClickListener
            }
            val calories = try {
                caloriesText.toDouble()
            } catch (e: NumberFormatException) {
                binding.etCalories.error = "Некорректное число"
                return@setOnClickListener
            }

            val proteinText = binding.etProtein.text?.toString() ?: "0"
            val protein = try {
                proteinText.toDouble()
            } catch (e: NumberFormatException) {
                binding.etProtein.error = "Некорректное число"
                return@setOnClickListener
            }

            val carbsText = binding.etCarbs.text?.toString() ?: "0"
            val carbs = try {
                carbsText.toDouble()
            } catch (e: NumberFormatException) {
                binding.etCarbs.error = "Некорректное число"
                return@setOnClickListener
            }

            val fatText = binding.etFat.text?.toString() ?: "0"
            val fat = try {
                fatText.toDouble()
            } catch (e: NumberFormatException) {
                binding.etFat.error = "Некорректное число"
                return@setOnClickListener
            }

            val amountText = binding.etAmount.text?.toString() ?: ""
            if (amountText.isBlank()) {
                binding.etAmount.error = "Введите количество"
                return@setOnClickListener
            }
            val amount = try {
                amountText.toDouble()
            } catch (e: NumberFormatException) {
                binding.etAmount.error = "Некорректное число"
                return@setOnClickListener
            }

            if (amount <= 0) {
                binding.etAmount.error = "Количество должно быть больше 0"
                return@setOnClickListener
            }

            val selectedMealTypeIndex = binding.spinnerMealType.selectedItemPosition
            val mealType = MealType.values()[selectedMealTypeIndex]

            viewLifecycleOwner.lifecycleScope.launch {
                try {

                    val foodItem = FoodItem(
                        name = foodName,
                        caloriesPer100g = calories,
                        protein = protein,
                        carbs = carbs,
                        fat = fat
                    )

                    val foodId = viewModel.insertFoodItem(foodItem)

                    viewModel.addMealEntry(foodId, mealType, amount)

                    (activity as? FoodTrackerActivity)?.showFoodLogFragment()
                } catch (e: Exception) {
                    android.util.Log.e("AddFoodFragment", "Ошибка при добавлении еды", e)
                }
            }
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

    private fun getFoodCategoryName(category: FoodCategory): String {
        return when (category) {
            FoodCategory.FRUITS -> "Фрукты"
            FoodCategory.VEGETABLES -> "Овощи"
            FoodCategory.NUTS -> "Орехи"
            FoodCategory.DISHES -> "Блюда"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
