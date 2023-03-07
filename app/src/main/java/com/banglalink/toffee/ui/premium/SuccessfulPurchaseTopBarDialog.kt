package com.banglalink.toffee.ui.premium

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.banglalink.toffee.databinding.DialogSuccessfulPurchaseTopBarBinding

class SuccessfulPurchaseTopBarDialog : DialogFragment() {
    
    private var _binding: DialogSuccessfulPurchaseTopBarBinding? = null
    private val binding get() = _binding!!
    private lateinit var alertDialog: AlertDialog
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogSuccessfulPurchaseTopBarBinding.inflate(this.layoutInflater)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(binding.root)
        
        alertDialog = dialogBuilder.create().apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//            window?.setGravity(Gravity.TOP)
        }
        val window: Window? = alertDialog.window
        val layoutParams: WindowManager.LayoutParams = window!!.attributes
        layoutParams.gravity = Gravity.TOP
        layoutParams.verticalMargin = 0.1f
        window.attributes = layoutParams
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        binding.closeDialog.setOnClickListener {
            dismiss()
        }
        
        return alertDialog
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}