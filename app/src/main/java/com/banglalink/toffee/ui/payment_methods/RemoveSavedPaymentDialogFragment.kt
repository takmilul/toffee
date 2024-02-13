package com.banglalink.toffee.ui.payment_methods

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.opengl.Visibility
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentRemoveSavedAccountBinding
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.show

class RemoveSavedPaymentDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = FragmentRemoveSavedAccountBinding.inflate(layoutInflater)

        binding.closeIv.safeClick({
            runCatching {
                dismiss()
            }
        })
        binding.cancelButton.safeClick({
            runCatching {
                dismiss()
            }
        })
        binding.addNewButton.safeClick({
            runCatching {
                dismiss()
            }
        })
        binding.removeButton.safeClick({
            binding.logo.setImageResource(R.drawable.ic_complete_delete)
            binding.dialogTitleTextView.text = "Account Removed Successfully"
            binding.dialogDescTextView.hide()
            binding.addNewButton.show()
            binding.removeButton.hide()
        })

        return AlertDialog
            .Builder(requireContext())
//            .setCancelable(false)
            .setView(binding.root).create()
            .apply {
                // https://stackoverflow.com/questions/9102074/android-edittext-in-dialog-doesnt-pull-up-soft-keyboard
                setOnShowListener {
                    dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
                    dialog?.setCancelable(false)
//                    dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
                }
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
    }

    companion object{
        const val TAG = "RemoveSavedPaymentDialog"
    }
}