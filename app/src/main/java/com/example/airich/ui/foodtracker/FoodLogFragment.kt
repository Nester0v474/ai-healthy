package com.example.airich.ui.foodtracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.airich.R
import com.example.airich.data.FoodDatabase
import com.example.airich.databinding.FragmentFoodLogBinding
import com.example.airich.repository.FoodRepository
import com.example.airich.viewmodel.FoodViewModel
import com.example.airich.viewmodel.FoodViewModelFactory
import java.text.DecimalFormat

class FoodLogFragment : Fragment() {

    private var _binding: FragmentFoodLogBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FoodViewModel
    private lateinit var adapter: MealEntryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFoodLogBinding.inflate(inflater, container, false)
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

            android.util.Log.e("FoodLogFragment", "Ошибка инициализации", e)
            android.util.Log.e("FoodLogFragment", "Stack trace", e)

            binding.tvCalories.text = "Ошибка загрузки данных"
            return
        }

        adapter = MealEntryAdapter { mealEntryId ->
            viewModel.deleteMealEntry(mealEntryId)
        }
        binding.rvMealEntries.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMealEntries.adapter = adapter

        viewModel.mealEntries.observe(viewLifecycleOwner) { entries ->
            adapter.submitList(entries)
        }

        viewModel.dailySummary.observe(viewLifecycleOwner) { summary ->
            summary?.let {
                val df = DecimalFormat("#.#")
                binding.tvCalories.text = "Калории: ${df.format(it.calories)}/2000 ккал"
                binding.tvProtein.text = "Белки: ${df.format(it.protein)} г"
                binding.tvCarbs.text = "Углеводы: ${df.format(it.carbs)} г"
                binding.tvFat.text = "Жиры: ${df.format(it.fat)} г"
            }
        }

        binding.fabAddMeal.setOnClickListener {
            (activity as? FoodTrackerActivity)?.showAddFoodFragment()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
