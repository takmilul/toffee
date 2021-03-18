package com.banglalink.toffee.ui.upload

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.databinding.FragmentTermsConditionBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.profile.ViewProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TermsConditionFragment : DialogFragment() {
    
    @Inject lateinit var mPref: Preference
    private var alertDialog: AlertDialog? = null
    private val viewModel by viewModels<ViewProfileViewModel>()
    private lateinit var binding: FragmentTermsConditionBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentTermsConditionBinding.inflate(layoutInflater)
        binding.closeIv.setOnClickListener {
            dismiss()
        }
        alertDialog = AlertDialog
            .Builder(requireContext())
            .setView(binding.root).create()
            .apply {
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
        viewModel.terms()
        observeTermCondition()
        return alertDialog!!
    }
    
    private fun observeTermCondition() {
        observe(viewModel.termsAndConditionResult) {
            when (it) {
                is Resource.Success -> {
                    if (mPref.appThemeMode == Configuration.UI_MODE_NIGHT_YES) {
                        loadTermsAndConditionText(it.data.terms_and_conditions_black!!)
                    }
                    else {
                        loadTermsAndConditionText(it.data.terms_and_conditions_white!!)
                    }
                }
                is Resource.Failure -> {
                    println(it.error)
                    Log.e("data", "data" + it.error.msg)
                    Toast.makeText(requireContext(), it.error.msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadTermsAndConditionText(msg: String){
        binding.webview.loadDataWithBaseURL(null, msg, "text/html", "UTF-8", null)
        binding.webview.computeScroll()
        binding.webview.isVerticalScrollBarEnabled = true
        binding.webview.isHorizontalScrollBarEnabled = true
    }
}