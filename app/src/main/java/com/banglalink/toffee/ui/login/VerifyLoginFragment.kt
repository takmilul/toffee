package com.banglalink.toffee.ui.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.FirebaseParams
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.data.repository.TVChannelRepository
import com.banglalink.toffee.databinding.AlertDialogVerifyBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.extension.toFormattedDate
import com.banglalink.toffee.model.CustomerInfoLogin
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.receiver.SMSBroadcastReceiver
import com.banglalink.toffee.ui.common.ChildDialogFragment
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.usecase.OTPLogData
import com.banglalink.toffee.util.unsafeLazy
import com.google.android.gms.auth.api.phone.SmsRetriever
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class VerifyLoginFragment : ChildDialogFragment() {
    
    private var otp: String = ""
    private var phoneNumber: String = ""
    private var regSessionToken: String = ""
    @Inject lateinit var cacheManager: CacheManager
    private var resendCodeTimer: ResendCodeTimer? = null
    private var verifiedUserData: CustomerInfoLogin? = null
    private var _binding: AlertDialogVerifyBinding ? = null
    private val binding get() = _binding!!
    @Inject lateinit var tVChannelRepository: TVChannelRepository
    private var mSmsBroadcastReceiver: SMSBroadcastReceiver? = null
    private val homeViewModel: HomeViewModel by activityViewModels()
    private val viewModel by viewModels<VerifyCodeViewModel>()
    private val progressDialog by unsafeLazy { ToffeeProgressDialog(requireContext()) }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        phoneNumber = requireArguments().getString(LoginContentFragment.PHONE_NO_ARG) ?: ""
        regSessionToken = requireArguments().getString(LoginContentFragment.REG_SESSION_TOKEN_ARG) ?: ""
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = AlertDialogVerifyBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeVerifyCode()
        with(binding) {
            resendButton.safeClick ({
                progressDialog.show()
                handleResendButton()
                viewModel.resendCode(phoneNumber, "")
            })
            submitButton.safeClick ({
                progressDialog.show()
                binding.otpEditText.clearFocus()
                otp = binding.otpEditText.text.toString().trim()
                viewModel.verifyCode(otp, regSessionToken, "")
            })
            skipButton.safeClick({ closeDialog() })
        }
        
        startCountDown()
        initSmsBroadcastReceiver()
    }
    
    private fun observeVerifyCode() {
        observe(viewModel.verifyResponse) {
            progressDialog.dismiss()
            ToffeeAnalytics.logEvent(ToffeeEvents.OTP_INPUT)
            when (it) {
                is Resource.Success -> {
                    verifiedUserData = it.data
                    if (mPref.phoneNumber != phoneNumber) {
                        homeViewModel.fcmToken.value?.let { homeViewModel.setFcmToken(it) }
                    }
                    mPref.phoneNumber = phoneNumber
                    ToffeeAnalytics.logEvent(ToffeeEvents.CONFIRM_OTP, bundleOf("confirm_otp_status" to 1))
                    mPref.lastLoginDateTime =  System.currentTimeMillis().toFormattedDate()
                    viewModel.sendLoginLogData()
                    homeViewModel.sendOtpLogData(OTPLogData(otp, 0, 0, 1), phoneNumber)

                    if (mPref.newUser.value.equals("Old User")){
                        closeDialog()
                    }
                    else{
                        findNavController().navigate(R.id.userInterestFragment)
                    }
//                    if (cPref.isUserInterestSubmitted(phoneNumber)) {
//                        closeDialog()
//                    }
//                    else {
//                        findNavController().navigate(R.id.userInterestFragment)
//                    }
                }
                is Resource.Failure -> {
                    requireContext().showToast(it.error.msg)
                    ToffeeAnalytics.logApiError("confirmCode",it.error.msg)
                    ToffeeAnalytics.logEvent(ToffeeEvents.CONFIRM_OTP, bundleOf(
                        "confirm_otp_status" to "0",
                        "confirm_otp_failure_reason" to it.error.msg
                    ))
                    ToffeeAnalytics.logEvent(ToffeeEvents.EXCEPTION,
                        bundleOf(
                            "api_name" to ApiNames.LOGIN_BY_PHONE_NO,
                            FirebaseParams.BROWSER_SCREEN to "Enter OTP",
                            "error_code" to it.error.code,
                            "error_description" to it.error.msg))
                }
            }
        }
    }
    
    private fun handleResendButton() {
        observe(viewModel.resendCodeResponse) {
            progressDialog.dismiss()
            ToffeeAnalytics.logEvent(ToffeeEvents.RESEND_OTP)
            when (it) {
                is Resource.Success -> {
                    regSessionToken = it.data//update reg session token
                    binding.resendButton.visibility = View.GONE
                    startCountDown()
                }
                is Resource.Failure -> {
                    ToffeeAnalytics.logEvent(ToffeeEvents.EXCEPTION,
                        bundleOf(
                            "api_name" to ApiNames.LOGIN_BY_PHONE_NO,
                            FirebaseParams.BROWSER_SCREEN to "Enter OTP",
                            "error_code" to it.error.code,
                            "error_description" to it.error.msg))

                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }
    
    private fun startCountDown() {
        binding.countdownTextView.visibility = View.VISIBLE
        resendCodeTimer?.cancel()
        resendCodeTimer = ResendCodeTimer(this, 1).also { timer ->
            observe(timer.tickLiveData) {
                val remainingSecs = it / 1000
                val minutes = (remainingSecs / 60).toInt()
                val seconds = (remainingSecs % 60).toInt()
                val timeText = (String.format("%02d", minutes) + ":" + String.format("%02d", seconds))
                val countDownText = String.format(getString(R.string.sign_in_countdown_text), timeText)
                val str = SpannableString(countDownText)
                str.setSpan(StyleSpan(Typeface.BOLD), countDownText.indexOf(timeText), countDownText.indexOf(timeText) + timeText.length, Spannable
                    .SPAN_EXCLUSIVE_EXCLUSIVE)
                binding.countdownTextView.text = str
            }
            
            observe(timer.finishLiveData) {
                binding.resendButton.isEnabled = true
                binding.resendButton.visibility = View.VISIBLE
                binding.countdownTextView.visibility = View.INVISIBLE
                binding.countdownTextView.text = ""
                
                timer.finishLiveData.removeObservers(this)
                timer.tickLiveData.removeObservers(this)
            }
        }
        resendCodeTimer?.start()
    }
    
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun initSmsBroadcastReceiver() {
        mSmsBroadcastReceiver = SMSBroadcastReceiver()
        observe(mSmsBroadcastReceiver!!.otpLiveData) {
            if (it.isNotBlank()) {
                binding.otpEditText.setText(it)
                binding.otpEditText.setSelection(it.length)
                otp = binding.otpEditText.text.toString().trim()
                homeViewModel.sendOtpLogData(OTPLogData(otp, 0, 1, 0), phoneNumber)
                viewModel.verifyCode(otp, regSessionToken, "")
            }
        }
        
        val intentFilter = IntentFilter()
        intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            requireActivity().registerReceiver(mSmsBroadcastReceiver, intentFilter)
        } else {
            requireActivity().registerReceiver(mSmsBroadcastReceiver, intentFilter, Context.RECEIVER_EXPORTED)
        }
        
        val mClient = SmsRetriever.getClient(requireActivity())
        mClient.startSmsRetriever()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    override fun onDestroy() {
        resendCodeTimer?.cancelTimer()
        resendCodeTimer = null
        mSmsBroadcastReceiver?.abortBroadcast
        if(mSmsBroadcastReceiver != null) {
            requireActivity().unregisterReceiver(mSmsBroadcastReceiver)
        }
        super.onDestroy()
    }
}