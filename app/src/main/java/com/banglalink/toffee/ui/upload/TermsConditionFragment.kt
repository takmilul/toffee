package com.banglalink.toffee.ui.upload

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.banglalink.toffee.R
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.profile.ViewProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_terms_condition.view.*
import javax.inject.Inject

@AndroidEntryPoint
class TermsConditionFragment : DialogFragment() {
    
    @Inject lateinit var mPref: Preference
    private var alertDialog: AlertDialog? = null
    private var webview: WebView? = null
    private val viewModel by viewModels<ViewProfileViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialogView = layoutInflater.inflate(R.layout.fragment_terms_condition, null, false)
        webview=dialogView?.findViewById(R.id.webview)
        with(dialogView) {
            close_iv?.setOnClickListener {
                dismiss()
            }
            alertDialog = AlertDialog
                .Builder(requireContext())
                .setView(dialogView).create()
                .apply {
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                }
            viewModel.terms()
            observeTermCondition()
            return alertDialog!!
        }
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
        webview?.loadDataWithBaseURL(null, msg, "text/html", "UTF-8", null)
        webview?.computeScroll()
        webview?.isVerticalScrollBarEnabled = true
        webview?.isHorizontalScrollBarEnabled = true
    }
}