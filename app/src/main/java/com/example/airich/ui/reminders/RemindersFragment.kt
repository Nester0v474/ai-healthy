package com.example.airich.ui.reminders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.airich.data.FoodDatabase
import com.example.airich.reminders.ReminderScheduler
import com.example.airich.databinding.FragmentRemindersBinding
import com.example.airich.repository.ReminderRepository
import com.example.airich.viewmodel.ReminderViewModel
import com.example.airich.viewmodel.ReminderViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RemindersFragment : Fragment() {

    private var _binding: FragmentRemindersBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ReminderViewModel
    private lateinit var adapter: ReminderAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRemindersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = FoodDatabase.getDatabase(requireContext().applicationContext)
        val repository = ReminderRepository(database)
        viewModel = ViewModelProvider(this, ReminderViewModelFactory(repository))[ReminderViewModel::class.java]

        adapter = ReminderAdapter(onDelete = { id ->
            ReminderScheduler.cancelReminder(requireContext(), id)
            viewModel.deleteReminder(id)
        })
        binding.rvReminders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReminders.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.reminders.collectLatest { list ->
                adapter.submitList(list)
                binding.tvEmptyList.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                binding.rvReminders.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
            }
        }

        binding.btnAddReminder.setOnClickListener { showAddReminderDialog() }
    }

    private fun showAddReminderDialog() {
        AddReminderDialogFragment.newInstance(
            onSave = { reminder ->
                viewModel.addReminder(reminder) { id ->
                    ReminderScheduler.scheduleReminder(requireContext(), reminder.copy(id = id))
                }
            }
        ).show(parentFragmentManager, "AddReminderDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
