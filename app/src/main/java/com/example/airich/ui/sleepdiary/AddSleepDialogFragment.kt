package com.example.airich.ui.sleepdiary

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.example.airich.data.SleepEntry
import com.example.airich.databinding.DialogAddSleepBinding
import java.time.LocalDate

class AddSleepDialogFragment : DialogFragment() {

    private var _binding: DialogAddSleepBinding? = null
    private val binding get() = _binding!!

    private var onSave: ((SleepEntry) -> Unit)? = null
    private var bedHour = 22
    private var bedMinute = 0
    private var wakeHour = 7
    private var wakeMinute = 0

    companion object {
        fun newInstance(onSave: (SleepEntry) -> Unit): AddSleepDialogFragment {
            return AddSleepDialogFragment().apply { this.onSave = onSave }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): android.app.Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogAddSleepBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateTimeButtons()

        binding.btnBedTime.setOnClickListener {
            TimePickerDialog(
                requireContext(),
                { _, h, m ->
                    bedHour = h
                    bedMinute = m
                    updateTimeButtons()
                },
                bedHour,
                bedMinute,
                true
            ).show()
        }

        binding.btnWakeTime.setOnClickListener {
            TimePickerDialog(
                requireContext(),
                { _, h, m ->
                    wakeHour = h
                    wakeMinute = m
                    updateTimeButtons()
                },
                wakeHour,
                wakeMinute,
                true
            ).show()
        }

        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnSave.setOnClickListener {
            val quality = when (binding.rgQuality.checkedRadioButtonId) {
                binding.rb1.id -> 1
                binding.rb2.id -> 2
                binding.rb4.id -> 4
                binding.rb5.id -> 5
                else -> 3
            }
            val note = binding.etNote.text?.toString()?.trim()
            onSave?.invoke(
                SleepEntry(
                    date = LocalDate.now(),
                    bedTime = String.format("%02d:%02d", bedHour, bedMinute),
                    wakeTime = String.format("%02d:%02d", wakeHour, wakeMinute),
                    quality = quality,
                    note = note?.takeIf { it.isNotBlank() }
                )
            )
            dismiss()
        }
    }

    private fun updateTimeButtons() {
        binding.btnBedTime.text = String.format("%02d:%02d", bedHour, bedMinute)
        binding.btnWakeTime.text = String.format("%02d:%02d", wakeHour, wakeMinute)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
