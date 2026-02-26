package com.example.airich.ui.healthytasktracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.airich.data.FoodDatabase
import com.example.airich.databinding.FragmentHealthyTaskLogBinding
import com.example.airich.repository.HealthyTaskRepository
import com.example.airich.viewmodel.HealthyTaskViewModel
import com.example.airich.viewmodel.HealthyTaskViewModelFactory
import kotlinx.coroutines.launch

class HealthyTaskLogFragment : Fragment() {

    private var _binding: FragmentHealthyTaskLogBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HealthyTaskViewModel
    private lateinit var adapter: HealthyTaskEntryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthyTaskLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            val context = requireContext().applicationContext

            val database = FoodDatabase.getDatabase(context)
            val repository = HealthyTaskRepository(database)
            val factory = HealthyTaskViewModelFactory(repository)
            viewModel = ViewModelProvider(this, factory)[HealthyTaskViewModel::class.java]
        } catch (e: Exception) {
            android.util.Log.e("HealthyTaskLogFragment", "Ошибка инициализации", e)
            return
        }

        adapter = HealthyTaskEntryAdapter(
            onToggleComplete = { id, isCompleted ->
                viewModel.toggleCompletionStatus(id, isCompleted)
            },
            onDeleteClick = { id ->
                viewModel.deleteHealthyTaskEntry(id)
            }
        )
        binding.rvHealthyTaskEntries.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHealthyTaskEntries.adapter = adapter

        setupFilters()

        viewModel.allHealthyTaskEntries.observe(viewLifecycleOwner) { entries ->
            updateList()
        }

        viewModel.activeHealthyTaskEntries.observe(viewLifecycleOwner) { entries ->
            updateList()
        }

        viewModel.completedHealthyTaskEntries.observe(viewLifecycleOwner) { entries ->
            updateList()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.filter.collect { filter ->
                updateFilterButtons(filter)
                updateList()
            }
        }

        binding.btnAddHealthyTask.setOnClickListener {
            showAddHealthyTaskDialog()
        }
    }

    private fun setupFilters() {
        binding.btnFilterAll.setOnClickListener {
            viewModel.setFilter("all")
        }

        binding.btnFilterActive.setOnClickListener {
            viewModel.setFilter("active")
        }

        binding.btnFilterCompleted.setOnClickListener {
            viewModel.setFilter("completed")
        }
    }

    private fun updateFilterButtons(selectedFilter: String) {
        binding.btnFilterAll.isSelected = selectedFilter == "all"
        binding.btnFilterActive.isSelected = selectedFilter == "active"
        binding.btnFilterCompleted.isSelected = selectedFilter == "completed"
    }

    private fun updateList() {
        val filter = viewModel.filter.value ?: "all"
        val entries = when (filter) {
            "active" -> viewModel.activeHealthyTaskEntries.value ?: emptyList()
            "completed" -> viewModel.completedHealthyTaskEntries.value ?: emptyList()
            else -> viewModel.allHealthyTaskEntries.value ?: emptyList()
        }
        adapter.submitList(entries)

        if (entries.isEmpty()) {
            binding.tvEmptyList.visibility = View.VISIBLE
            binding.rvHealthyTaskEntries.visibility = View.GONE
        } else {
            binding.tvEmptyList.visibility = View.GONE
            binding.rvHealthyTaskEntries.visibility = View.VISIBLE
        }
    }

    private fun showAddHealthyTaskDialog() {
        try {
            if (!::viewModel.isInitialized) {
                return
            }

            val dialog = AddHealthyTaskDialogFragment.newInstance { title, description ->
                if (::viewModel.isInitialized) {
                    viewModel.addHealthyTaskEntry(title, description)
                }
            }
            dialog.show(parentFragmentManager, "AddHealthyTaskDialog")
        } catch (e: Exception) {
            android.util.Log.e("HealthyTaskLogFragment", "Ошибка показа диалога", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
