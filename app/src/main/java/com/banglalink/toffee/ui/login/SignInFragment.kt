package com.banglalink.toffee.ui.login

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
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
            .setCancelable(false)
            .setView(binding.root).create()
            .apply {
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
    }
}