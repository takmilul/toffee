package com.banglalink.toffee.ui.login

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.FirebaseParams
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.databinding.AlertDialogLoginBinding
import com.banglalink.toffee.enums.InputType.PHONE
import com.banglalink.toffee.extension.isValid
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.ChildDialogFragment
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.usecase.OTPLogData
import com.banglalink.toffee.util.unsafeLazy
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.Credentials
import com.google.android.gms.auth.api.credentials.HintRequest
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginContentFragment : ChildDialogFragment() {
    
    private var phoneNo: String = ""
    private val binding get() = _binding !!
    private var regSessionToken: String = ""
    private var _binding: AlertDialogLoginBinding? = null
    private var phoneNumberTextWatcher: TextWatcher? = null
    private val homeViewModel: HomeViewModel by activityViewModels()
    private val viewModel by viewModels<LoginViewModel>()
    private val progressDialog by unsafeLazy { ToffeeProgressDialog(requireContext()) }
    
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
                handleLogin()
                if (phoneNo.isValid(PHONE)) {
                    progressDialog.show()
                    observeLogin()
                    ToffeeAnalytics.logEvent(ToffeeEvents.OTP_REQUESTED)
                    viewModel.login(phoneNo)
                } else {
                    requireActivity().showToast("Invalid phone number")
                }
            })
            termsAndConditionsCheckbox.setOnClickListener {
                verifyButton.isEnabled = termsAndConditionsCheckbox.isChecked && phoneNo.isNotBlank() && phoneNo.length >= 11
            }
            phoneNumberTextWatcher = phoneNumberEditText.doAfterTextChanged {
                phoneNo = it.toString()
                verifyButton.isEnabled = termsAndConditionsCheckbox.isChecked && phoneNo.isNotBlank() && phoneNo.length >= 11
            }
            if (mPref.isHeBanglalinkNumber) {
                phoneNumberEditText.setText(mPref.hePhoneNumber)
                phoneNumberEditText.setSelection(mPref.hePhoneNumber.length)
            } else {
                getHintPhoneNumber()
            }
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
                        regSessionToken = it.data as String
                        homeViewModel.sendOtpLogData(OTPLogData("", 1, 0, 0), phoneNo)
                        findNavController().navigate(R.id.verifyLoginFragment,
                            Bundle().apply { 
                                putString(PHONE_NO_ARG, phoneNo)
                                putString(REG_SESSION_TOKEN_ARG, regSessionToken)
                            }
                        )
                        ToffeeAnalytics.logEvent(ToffeeEvents.FETCHING_AD_ID_FAILED, bundleOf("login_status" to "1"))
                    }
                }
                is Resource.Failure -> {
                    ToffeeAnalytics.logEvent(ToffeeEvents.LOGIN, bundleOf("login_status" to "0"))
                    ToffeeAnalytics.logEvent(ToffeeEvents.LOGIN, bundleOf("login_failure_reason" to it.error.msg))
                    requireActivity().showToast(it.error.msg)

                    ToffeeAnalytics.logEvent(ToffeeEvents.EXCEPTION,
                        bundleOf(
                            "api_name" to ApiNames.LOGIN_BY_PHONE_NO,
                            FirebaseParams.BROWSER_SCREEN to "Login With Phone",
                            "error_code" to it.error.code,
                            "error_description" to it.error.msg))
                }
            }
        }
    }
    
    private fun setSpannableTermsAndConditions() {
        val ss = SpannableString(getString(R.string.terms_of_use))
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
        findNavController().navigate(R.id.htmlPageViewDialog, bundleOf(
            "myTitle" to getString(R.string.terms_and_conditions),
            "url" to mPref.termsAndConditionUrl
        ))
    }
    
    private fun getHintPhoneNumber() {
        try{
            val hintRequest = HintRequest.Builder().setPhoneNumberIdentifierSupported(true).build()
            val intent = Credentials.getClient(requireContext()).getHintPickerIntent(hintRequest)
            resultLauncher.launch(IntentSenderRequest.Builder(intent).build())
        }catch (e:Exception){
            ToffeeAnalytics.logException(e)
            ToffeeAnalytics.logBreadCrumb("Could not retrieve phone number")
        }
    }
    
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val credential: Credential? = result.data?.getParcelableExtra(Credential.EXTRA_KEY)
            credential?.let {
                binding.phoneNumberEditText.setText(it.id)
                binding.phoneNumberEditText.setSelection(it.id.length)
            }
        }
    }
    
    override fun onDestroyView() {
        binding.phoneNumberEditText.removeTextChangedListener(phoneNumberTextWatcher)
        phoneNumberTextWatcher = null
        resultLauncher.unregister()
        super.onDestroyView()
        _binding = null
    }
}