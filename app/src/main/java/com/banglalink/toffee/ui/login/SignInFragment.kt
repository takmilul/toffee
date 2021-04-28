package com.banglalink.toffee.ui.login

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentSigninDialogBinding
import com.banglalink.toffee.extension.safeClick

class SignInFragment : DialogFragment() {

    private lateinit var navController: NavController

    companion object {
        @JvmStatic
        fun newInstance() =
            SignInFragment()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = FragmentSigninDialogBinding.inflate(layoutInflater)

        val navHostFragment = childFragmentManager.findFragmentById(R.id.signInFragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        binding.closeIv.safeClick({ dismiss() })

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
}