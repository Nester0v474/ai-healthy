package com.example.airich.ui.healthytasktracker

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.example.airich.databinding.DialogAddHealthyTaskBinding

class AddHealthyTaskDialogFragment : DialogFragment() {

    private var _binding: DialogAddHealthyTaskBinding? = null
    private val binding get() = _binding!!

    private var onSaveClick: ((String, String?) -> Unit)? = null

    companion object {
        fun newInstance(onSaveClick: (String, String?) -> Unit): AddHealthyTaskDialogFragment {
            return AddHealthyTaskDialogFragment().apply {
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
        _binding = DialogAddHealthyTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text?.toString()?.trim()
            if (title.isNullOrBlank()) {
                binding.etTitle.error = "Введите название"
                return@setOnClickListener
            }

            val description = binding.etDescription.text?.toString()?.trim()
            onSaveClick?.invoke(title, description)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
