package com.banglalink.toffee.ui.login

import android.os.Bundle
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.databinding.AlertDialogLoginBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.bottomsheet.BasicInfoBottomSheetFragmentDirections
import com.banglalink.toffee.ui.common.ChildDialogFragment
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.unsafeLazy
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginContentFragment2 : ChildDialogFragment(), TextWatcher {
    
    private var phoneNo: String = ""
    private var regSessionToken: String = ""
    private var _binding: AlertDialogLoginBinding? = null
    private val binding get() = _binding !!
    private val viewModel by viewModels<LoginViewModel2>()
    private val progressDialog by unsafeLazy { VelBoxProgressDialog(requireContext()) }
    
    companion object {
        const val PHONE_NO_ARG = "phoneNo"
        const val REG_SESSION_TOKEN_ARG = "regSessionToken"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = AlertDialogLoginBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setSpannableTermsAndConditions()
        
        with(binding) {
            verifyButton.safeClick({
                progressDialog.show()
                handleLogin()
                observeLogin()
                viewModel.login(phoneNo)
            })
            termsAndConditionsCheckbox.setOnClickListener {
                verifyButton.isEnabled = binding.termsAndConditionsCheckbox.isChecked && phoneNo.isNotBlank() && phoneNo.length >= 11
            }
            phoneNumberEditText.addTextChangedListener(this@LoginContentFragment2)
        }
    }
    
    private fun handleLogin() {
        phoneNo = binding.phoneNumberEditText.text.toString().trim()
        
        if (phoneNo.startsWith("0")) {
            phoneNo = "+88$phoneNo"
        }
        
        if (! phoneNo.startsWith("+")) {
            phoneNo = "+$phoneNo"
        }
        
        binding.phoneNumberEditText.setText(phoneNo)
        binding.phoneNumberEditText.setSelection(phoneNo.length)
    }
    
    private fun observeLogin() {
        observe(viewModel.loginByPhoneResponse) {
            progressDialog.dismiss()
            when (it) {
                is Resource.Success -> {
                    if (it.data is String) {
                        regSessionToken = it.data
                        findNavController().navigate(R.id.verifyLoginFragment2,
                            Bundle().apply { 
                                putString(PHONE_NO_ARG, phoneNo)
                                putString(REG_SESSION_TOKEN_ARG, regSessionToken)
                            }
                        )
                    }
                }
                is Resource.Failure -> {
                    ToffeeAnalytics.logApiError("reRegistration", it.error.msg, phoneNo)
                    requireActivity().showToast(it.error.msg)
                }
            }
        }
    }
    
    private fun setSpannableTermsAndConditions() {
        val ss = SpannableString(getString(R.string.terms_and_conditions))
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                showTermsAndConditionDialog()
            }
            
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
            }
        }
        ss.setSpan(clickableSpan, 0, ss.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        
        binding.termsAndConditionsTv.text = ss
        binding.termsAndConditionsTv.movementMethod = LinkMovementMethod.getInstance()
    }
    
    private fun showTermsAndConditionDialog() {
        val action = LoginContentFragment2Directions
            .actionLoginContentFragment2ToHtmlPageViewDialog(
                "Terms & Conditions",
                mPref.termsAndConditionUrl
            )
        findNavController().navigate(action)
    }
    
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    
    override fun afterTextChanged(s: Editable?) {
        phoneNo = s.toString()
        binding.verifyButton.isEnabled = binding.termsAndConditionsCheckbox.isChecked && phoneNo.isNotBlank() && phoneNo.length >= 11
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}