package com.banglalink.toffee.ui.upload

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kotlinx.android.synthetic.main.fragment_terms_condition.*
import kotlinx.android.synthetic.main.fragment_terms_condition.close_iv
import kotlinx.android.synthetic.main.fragment_terms_condition.view.*
import javax.inject.Inject


@AndroidEntryPoint
class TermsConditionFragment : DialogFragment() {
    @Inject
    lateinit var mpref: Preference
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
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
    private fun observeTermCondition() {

        observe(viewModel._data_condition) {
            when (it) {
                is Resource.Success -> {
                    if (mpref.appThemeMode==16){
                      data(it.data.terms_and_conditions_white!!)
                    }
                    else{
                        data(it.data.terms_and_conditions_black!!)
                    }
                }
                is Resource.Failure -> {
                    println(it.error)
                    Log.e("data", "data" + it.error.additionalMsg)
                    Log.e("data", "data" + it.error.code)
                    Log.e("data", "data" + it.error.msg)
                    Toast.makeText(requireContext(), it.error.msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun data(msg: String){
        webview?.loadDataWithBaseURL(null, msg, "text/html", "UTF-8", null)
        webview?.computeScroll()
        webview?.isVerticalScrollBarEnabled = true
        webview?.isHorizontalScrollBarEnabled = true
    }
}