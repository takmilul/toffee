package com.banglalink.toffee.ui.login

import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.databinding.AlertDialogVerifyBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.CustomerInfoLogin
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.receiver.SMSBroadcastReceiver
import com.banglalink.toffee.ui.common.ChildDialogFragment
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.unsafeLazy
import com.google.android.gms.auth.api.phone.SmsRetriever
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VerifyLoginFragment2 : ChildDialogFragment() {
    
    private var otp: String = ""
    private var phoneNumber: String = ""
    private var regSessionToken: String = ""
    private var resendBtnPressCount: Int = 0
    private var resendCodeTimer: ResendCodeTimer? = null
    private var verifiedUserData: CustomerInfoLogin? = null
    private var _binding: AlertDialogVerifyBinding ? = null
    private val binding get() = _binding!!
    private var mSmsBroadcastReceiver: SMSBroadcastReceiver? = null
    private val viewModel by viewModels<VerifyCodeViewModel>()
    private val progressDialog by unsafeLazy { VelBoxProgressDialog(requireContext()) }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        phoneNumber = requireArguments().getString(LoginContentFragment2.PHONE_NO_ARG) ?: ""
        regSessionToken = requireArguments().getString(LoginContentFragment2.REG_SESSION_TOKEN_ARG) ?: ""
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
        
        startCountDown(if (resendBtnPressCount <= 1) 1 else 30)
        initSmsBroadcastReceiver()
    }
    
    private fun observeVerifyCode() {
        observe(viewModel.verifyResponse) {
            progressDialog.dismiss()
            when (it) {
                is Resource.Success -> {
                    verifiedUserData = it.data
                    mPref.phoneNumber = phoneNumber
                    if (cPref.isUserInterestSubmitted(phoneNumber)) {
                        reloadContent()
                    }
                    else {
                        findNavController().navigate(R.id.userInterestFragment2)
                    }
                }
                is Resource.Failure -> {
                    ToffeeAnalytics.logApiError("confirmCode",it.error.msg)
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }
    
    private fun reloadContent() {
        closeDialog()
//        requireActivity().overridePendingTransition(0, 0)
        requireActivity().recreate()
    }

    private fun handleResendButton() {
        observe(viewModel.resendCodeResponse) {
            progressDialog.dismiss()
            when (it) {
                is Resource.Success -> {
                    regSessionToken = it.data//update reg session token
                    resendBtnPressCount++
                    binding.resendButton.visibility = View.GONE
                    startCountDown(if (resendBtnPressCount <= 1) 1 else 30)
                }
                is Resource.Failure -> {
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }

    private fun startCountDown(countDownTimeInMinute: Int) {
        binding.countdownTextView.visibility = View.VISIBLE
        resendCodeTimer?.cancel()
        resendCodeTimer = ResendCodeTimer(this, countDownTimeInMinute).also { timer ->
            observe(timer.tickLiveData) {
                val remainingSecs = it / 1000
                val minutes = (remainingSecs / 60).toInt()
                val seconds = (remainingSecs % 60).toInt()
                val timeText = (String.format("%02d", minutes)
                        + ":" + String.format("%02d", seconds))
                binding.countdownTextView.text = "Resend otp in $timeText"
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
    
    private fun initSmsBroadcastReceiver() {
        mSmsBroadcastReceiver = SMSBroadcastReceiver()
        observe(mSmsBroadcastReceiver!!.otpLiveData) {
            binding.otpEditText.setText(it)
            binding.otpEditText.setSelection(it.length)
            otp = binding.otpEditText.text.toString().trim()
            viewModel.verifyCode(otp, regSessionToken, "")
        }
        
        val intentFilter = IntentFilter()
        intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION)
        requireActivity().registerReceiver(mSmsBroadcastReceiver, intentFilter)
        
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