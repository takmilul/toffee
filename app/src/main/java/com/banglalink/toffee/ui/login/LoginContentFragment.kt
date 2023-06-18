package com.banglalink.toffee.ui.login

import android.app.Activity.RESULT_OK
import android.app.PendingIntent
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
import androidx.activity.result.IntentSenderRequest.Builder
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.Constants
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
import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest
import com.google.android.gms.auth.api.identity.Identity
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
                    if ( it.error.code == Constants.ACCOUNT_DELETED_ERROR_CODE) {
                        closeDialog ()
                        parentFragment?.parentFragment?.parentFragment?.parentFragmentManager?.let { fragmentManager ->
                            UnderDeleteBottomSheetFragment.newInstance(it.error.msg).show(fragmentManager, null)
                        }
                    } else {
                        requireContext().showToast(it.error.msg)
                    }

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
        findNavController().navigate(R.id.htmlPageViewDialog, bundleOf(
            "myTitle" to getString(R.string.terms_and_conditions),
            "url" to mPref.termsAndConditionUrl
        ))
    }
    
    private fun getHintPhoneNumber() {
        try {
            val request: GetPhoneNumberHintIntentRequest = GetPhoneNumberHintIntentRequest.builder().build()
            Identity.getSignInClient(requireActivity())
                .getPhoneNumberHintIntent(request)
                .addOnSuccessListener { result: PendingIntent ->
                    runCatching {
                        resultLauncher.launch(
                            Builder(result).build()
                        )
                    }.onFailure {
                        useAlternateHintMechanism()
                    }
                }
                .addOnFailureListener {
                    useAlternateHintMechanism()
                }
        } catch (e:Exception){
            ToffeeAnalytics.logException(e)
            ToffeeAnalytics.logBreadCrumb("Could not retrieve phone number")
        }
    }
    
    private fun useAlternateHintMechanism() {
        runCatching {
            val hintRequest = HintRequest.Builder().setPhoneNumberIdentifierSupported(true).build()
            val intent = Credentials.getClient(requireContext()).getHintPickerIntent(hintRequest)
            resultLauncher.launch(Builder(intent).build())
        }
    }
    
    private val resultLauncher = registerForActivityResult(StartIntentSenderForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val phoneNumber = try {
                Identity.getSignInClient(requireActivity()).getPhoneNumberFromIntent(result.data)
            } catch (e: Exception) {
                val credential: Credential? = result.data?.getParcelableExtra(Credential.EXTRA_KEY)
                credential?.id
            }
            phoneNumber?.let {
                binding.phoneNumberEditText.setText(it)
                binding.phoneNumberEditText.setSelection(it.length)
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