package com.banglalink.toffee.ui.upload

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import javax.inject.Inject


@AndroidEntryPoint
class TermsConditionFragment : DialogFragment() {
    @Inject
    lateinit var mpref: Preference
    private val viewModel by viewModels<ViewProfileViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_terms_condition, container, false)

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.terms()
        observeTermCondition()
        close_iv?.setOnClickListener {
            dismiss()
        }
        constraintLayout?.setOnClickListener {
            dismiss()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            R.style.FullScreenDialogStyle
        )
    }
    private fun observeTermCondition() {

        observe(viewModel._data_condition) {
            when (it) {
                is Resource.Success -> {
                    data(it.data.terms_and_conditions!!)
                    Log.e("data", "data" + it.data.terms_and_conditions)
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
        webview.loadDataWithBaseURL(null, msg, "text/html", "UTF-8", null)
        webview.computeScroll()
        webview.isVerticalScrollBarEnabled = true
        webview.isHorizontalScrollBarEnabled = true
    }
}