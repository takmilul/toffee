package com.banglalink.toffee.ui.upload

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.databinding.FragmentTermsConditionBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.profile.ViewProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TermsConditionFragment : DialogFragment() {
    
    @Inject lateinit var cPref: CommonPreference
    private var alertDialog: AlertDialog? = null
    private val viewModel by viewModels<ViewProfileViewModel>()
    private var _binding: FragmentTermsConditionBinding ? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentTermsConditionBinding.inflate(layoutInflater)
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
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun observeTermCondition() {
        observe(viewModel.termsAndConditionResult) {
            when (it) {
                is Resource.Success -> {
                    if (cPref.appThemeMode == Configuration.UI_MODE_NIGHT_YES) {
                        loadTermsAndConditionText(it.data.terms_and_conditions_black!!)
                    }
                    else {
                        loadTermsAndConditionText(it.data.terms_and_conditions_white!!)
                    }
                }
                is Resource.Failure -> {
                    println(it.error)
                    Log.e("data", "data" + it.error.msg)
                    requireContext().showToast(it.error.msg)
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