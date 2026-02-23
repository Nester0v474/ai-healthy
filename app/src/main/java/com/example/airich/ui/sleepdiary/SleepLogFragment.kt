package com.example.airich.ui.sleepdiary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.airich.data.FoodDatabase
import com.example.airich.databinding.FragmentSleepLogBinding
import com.example.airich.repository.SleepRepository
import com.example.airich.viewmodel.SleepViewModel
import com.example.airich.viewmodel.SleepViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SleepLogFragment : Fragment() {

    private var _binding: FragmentSleepLogBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SleepViewModel
    private lateinit var adapter: SleepEntryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSleepLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = FoodDatabase.getDatabase(requireContext().applicationContext)
        val repository = SleepRepository(database)
        viewModel = ViewModelProvider(this, SleepViewModelFactory(repository))[SleepViewModel::class.java]

        adapter = SleepEntryAdapter(onDelete = { viewModel.deleteEntry(it) })
        binding.rvSleepEntries.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSleepEntries.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.recentEntries.collectLatest { list ->
                adapter.submitList(list)
                binding.tvEmptyList.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                binding.rvSleepEntries.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
            }
        }

        binding.btnAddSleep.setOnClickListener { showAddSleepDialog() }
    }

    private fun showAddSleepDialog() {
        AddSleepDialogFragment.newInstance { entry ->
            viewModel.addOrUpdateEntry(entry)
        }.show(parentFragmentManager, "AddSleepDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
