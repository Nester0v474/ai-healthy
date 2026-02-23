package com.example.airich.ui.habittracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.airich.data.FoodDatabase
import com.example.airich.databinding.FragmentHabitLogBinding
import com.example.airich.repository.HabitRepository
import com.example.airich.viewmodel.HabitViewModel
import com.example.airich.viewmodel.HabitViewModelFactory
import com.example.airich.viewmodel.HabitWithCompletion
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HabitLogFragment : Fragment() {

    private var _binding: FragmentHabitLogBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HabitViewModel
    private lateinit var adapter: HabitAdapter

    private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", java.util.Locale.forLanguageTag("ru"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHabitLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = FoodDatabase.getDatabase(requireContext().applicationContext)
        val repository = HabitRepository(database)
        viewModel = ViewModelProvider(this, HabitViewModelFactory(repository))[HabitViewModel::class.java]

        adapter = HabitAdapter(
            onToggle = { viewModel.toggleCompletion(it) },
            onDelete = { viewModel.deleteHabit(it) }
        )
        binding.rvHabits.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHabits.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedDate.collect { date ->
                binding.tvSelectedDate.text = if (date == LocalDate.now()) "Сегодня" else date.format(dateFormatter)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.habitItems.collectLatest { items ->
                adapter.submitList(items)
                binding.tvEmptyList.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                binding.rvHabits.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
            }
        }

        binding.btnAddHabit.setOnClickListener { showAddHabitDialog() }
    }

    private fun showAddHabitDialog() {
        AddHabitDialogFragment.newInstance { name ->
            viewModel.addHabit(name)
        }.show(parentFragmentManager, "AddHabitDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
