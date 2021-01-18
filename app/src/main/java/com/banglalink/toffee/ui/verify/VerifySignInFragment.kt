package com.banglalink.toffee.ui.verify

import android.content.IntentFilter
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentVerifySigninBinding
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.CustomerInfoSignIn
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.receiver.SMSBroadcastReceiver
import com.banglalink.toffee.ui.home.HomeActivity
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.unsafeLazy
import com.google.android.gms.auth.api.phone.SmsRetriever

class VerifySignInFragment : Fragment() {
    
    private var phoneNumber: String = ""
    private var referralCode: String = ""
    private var regSessionToken: String = ""
    private var resendBtnPressCount: Int = 0
    private var resendCodeTimer: ResendCodeTimer? = null
    private var verifiedUserData: CustomerInfoSignIn? = null
    private val viewModel by viewModels<VerifyCodeViewModel>()
    private lateinit var binding: FragmentVerifySigninBinding
    private lateinit var mSmsBroadcastReceiver: SMSBroadcastReceiver
    private val progressDialog by unsafeLazy { VelBoxProgressDialog(requireContext()) }

    companion object {
        @JvmStatic
        fun newInstance() = VerifySignInFragment ()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_verify_signin, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        ViewCompat.setTranslationZ(binding.root, 100f)
        
        val args by navArgs<VerifySignInFragmentArgs>()
        args.let { 
            phoneNumber = it.phoneNumber
            referralCode = it.referralCode
            regSessionToken = it.regSessionToken
        }
        
        binding.resend.paintFlags = binding.resend.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.resend.setOnClickListener {
            handleResendButton()
        }
        binding.confirmBtn.setOnClickListener {
            verifyCode(binding.codeNumber.text.toString())
        }
        binding.backButton.setOnClickListener {
            binding.signInVerifyMotionLayout.onTransitionCompletedListener {
                if (it == R.id.start){
                    findNavController().popBackStack()
                }
            }
            binding.signInVerifyMotionLayout.transitionToStart()
        }

        startCountDown(if (resendBtnPressCount <= 1) 1 else 30)

        initSmsBroadcastReceiver()
    }

    private fun initSmsBroadcastReceiver() {
        // init broadcast receiver
        mSmsBroadcastReceiver = SMSBroadcastReceiver()
        observe(mSmsBroadcastReceiver.otpLiveData) {
            binding.codeNumber.setText(it)
            binding.codeNumber.setSelection(it.length)
            verifyCode(binding.codeNumber.text.toString())
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION)
        requireActivity().registerReceiver(mSmsBroadcastReceiver, intentFilter)

        val mClient = SmsRetriever.getClient(requireActivity())
        mClient.startSmsRetriever()
    }

    private fun verifyCode(code: String) {
        progressDialog.show()

        val signInMotionLayout = parentFragment?.parentFragment?.view
        
        observe(viewModel.verifyCode(code, regSessionToken, referralCode)) {
            progressDialog.dismiss()
            when (it) {
                is Resource.Success -> {
                    verifiedUserData = it.data
                    signInMotionLayout?.let { view ->
                        if (view is MotionLayout){
                            view.onTransitionCompletedListener { onLoginSuccessAnimationCompletion() }
                            view.setTransition(R.id.firstEndAnim, R.id.secondEndAmin)
                            view.transitionToEnd()
                        }
                    }
                }
                is Resource.Failure -> {
                    binding.root.snack(it.error.msg) {
                        action("Retry") {
                            verifyCode(binding.codeNumber.text.toString())
                        }
                    }
                }
            }
        }
    }

    private fun onLoginSuccessAnimationCompletion(){
        verifiedUserData?.let {
            requireActivity().launchActivity<HomeActivity>() {
                if (it.referralStatus == "Valid") {
                    putExtra(
                        HomeActivity.INTENT_REFERRAL_REDEEM_MSG,
                        it.referralStatusMessage
                    )
                }
            }
            requireActivity().finish()
        }
    }
    
    private fun handleResendButton() {
        progressDialog.show()
        observe(viewModel.resendCode(phoneNumber, referralCode)) {
            progressDialog.dismiss()
            when (it) {
                is Resource.Success -> {
                    regSessionToken = it.data//update reg session token
                    resendBtnPressCount++
                    binding.resend.visibility = View.GONE
                    startCountDown(if (resendBtnPressCount <= 1) 1 else 30)
                }
                is Resource.Failure -> {
                    binding.root.snack(it.error.msg) {
                        action("Retry") {
                            handleResendButton()
                        }
                    }
                }
            }
        }
    }

    private fun startCountDown(countDownTimeInMinute: Int) {
        binding.countdownTv.visibility = View.VISIBLE
        resendCodeTimer?.cancel()
        resendCodeTimer = ResendCodeTimer(this, countDownTimeInMinute).also { timer ->
            observe(timer.tickLiveData) {
                val remainingSecs = it / 1000
                val minutes = (remainingSecs / 60).toInt()
                val seconds = (remainingSecs % 60).toInt()
                val timeText = (String.format("%02d", minutes)
                        + ":" + String.format("%02d", seconds))
                binding.countdownTv.text = "Resend option will be activated after $timeText"
            }

            observe(timer.finishLiveData) {
                binding.resend.isEnabled = true
                binding.resend.visibility = View.VISIBLE
                binding.countdownTv.visibility = View.INVISIBLE
                binding.countdownTv.text = ""

                timer.finishLiveData.removeObservers(this)
                timer.tickLiveData.removeObservers(this)
            }
        }
        resendCodeTimer?.start()
    }

    override fun onDestroy() {
        resendCodeTimer?.cancelTimer()
        resendCodeTimer = null
        requireActivity().unregisterReceiver(mSmsBroadcastReceiver)
        super.onDestroy()
    }
}