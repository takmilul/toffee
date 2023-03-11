package com.banglalink.toffee.ui.premium

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.databinding.DialogStartWatchingTopBarBinding

class StartWatchingTopBarDialog : DialogFragment() {
    
    private var _binding: DialogStartWatchingTopBarBinding? = null
    private val binding get() = _binding!!
    private lateinit var alertDialog: AlertDialog
    
    companion object {
        fun newInstance() = StartWatchingTopBarDialog()
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogStartWatchingTopBarBinding.inflate(this.layoutInflater)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(binding.root)
        
        alertDialog = dialogBuilder.create().apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        val window: Window? = alertDialog.window
        val layoutParams: WindowManager.LayoutParams = window!!.attributes
        layoutParams.gravity = Gravity.TOP
        layoutParams.verticalMargin = 0.08f
        window.attributes = layoutParams
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        binding.closeDialog.setOnClickListener {
            dismiss()
        }
        alertDialog.setOnKeyListener { _, keyCode, keyEvent ->
            if(keyCode == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_UP) {
                findNavController().popBackStack()
                findNavController().popBackStack()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
        return alertDialog
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}