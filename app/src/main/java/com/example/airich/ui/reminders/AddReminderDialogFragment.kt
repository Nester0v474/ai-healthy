package com.example.airich.ui.reminders

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.example.airich.data.Reminder
import com.example.airich.databinding.DialogAddReminderBinding
import java.util.Calendar

class AddReminderDialogFragment : DialogFragment() {

    private var _binding: DialogAddReminderBinding? = null
    private val binding get() = _binding!!

    private var onSave: ((Reminder) -> Unit)? = null
    private var hour = 9
    private var minute = 0

    companion object {
        fun newInstance(onSave: (Reminder) -> Unit): AddReminderDialogFragment {
            return AddReminderDialogFragment().apply { this.onSave = onSave }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): android.app.Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogAddReminderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateTimeButton()

        binding.btnPickTime.setOnClickListener {
            TimePickerDialog(
                requireContext(),
                { _, h, m ->
                    hour = h
                    minute = m
                    updateTimeButton()
                },
                hour,
                minute,
                true
            ).show()
        }

        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text?.toString()?.trim()
            if (title.isNullOrBlank()) {
                binding.etTitle.error = "Введите текст"
                return@setOnClickListener
            }
            onSave?.invoke(
                Reminder(
                    title = title,
                    hour = hour,
                    minute = minute,
                    enabled = true,
                    repeatDaily = true
                )
            )
            dismiss()
        }
    }

    private fun updateTimeButton() {
        binding.btnPickTime.text = String.format("%02d:%02d", hour, minute)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
