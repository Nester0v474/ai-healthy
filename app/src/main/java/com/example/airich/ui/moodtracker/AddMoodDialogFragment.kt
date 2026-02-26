package com.example.airich.ui.moodtracker

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.example.airich.R
import com.example.airich.databinding.DialogAddMoodBinding
import com.google.android.material.button.MaterialButton

class AddMoodDialogFragment : DialogFragment() {

    private var _binding: DialogAddMoodBinding? = null
    private val binding get() = _binding!!

    private var selectedMoodScore: Int? = null
    private var onSaveClick: ((Int, String?) -> Unit)? = null

    companion object {
        fun newInstance(onSaveClick: (Int, String?) -> Unit): AddMoodDialogFragment {
            return AddMoodDialogFragment().apply {
                this.onSaveClick = onSaveClick
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddMoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMoodButtons()

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            val moodScore = selectedMoodScore
            if (moodScore != null) {
                val note = binding.etNote.text?.toString()?.takeIf { it.isNotBlank() }
                onSaveClick?.invoke(moodScore, note)
                dismiss()
            }
        }
    }

    private fun setupMoodButtons() {
        val buttons = listOf(
            binding.btnMood1 to 1,
            binding.btnMood2 to 2,
            binding.btnMood3 to 3,
            binding.btnMood4 to 4,
            binding.btnMood5 to 5
        )

        val moodDescriptions = listOf(
            "Очень плохо",
            "Плохо",
            "Нормально",
            "Хорошо",
            "Отлично"
        )

        buttons.forEachIndexed { index, (button, score) ->

            button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.BLACK))
            button.setTextColor(android.graphics.Color.WHITE)

            button.setOnClickListener {

                buttons.forEach { (btn, _) ->
                    btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.BLACK))
                    btn.setTextColor(android.graphics.Color.WHITE)
                    btn.isSelected = false
                }

                button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE))
                button.setTextColor(android.graphics.Color.BLACK)
                button.isSelected = true
                selectedMoodScore = score

                binding.tvSelectedMood.text = moodDescriptions[index]

                binding.btnSave.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
