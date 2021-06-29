package com.banglalink.toffee.ui.login

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.banglalink.toffee.R
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.databinding.FragmentLoginDialogBinding
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.showToast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : DialogFragment() {

    @Inject lateinit var mPref: SessionPreference
    private lateinit var navController: NavController

    companion object {
        @JvmStatic
        fun newInstance() =
            LoginFragment()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = FragmentLoginDialogBinding.inflate(layoutInflater)

        val navHostFragment = childFragmentManager.findFragmentById(R.id.loginFragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        binding.closeIv.safeClick({
            dismiss().let { 
                if (mPref.isVerifiedUser) {
                    requireActivity().showToast(getString(R.string.verify_success), Toast.LENGTH_LONG).also { 
                        requireActivity().recreate()
                    }
                }
            }
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
}