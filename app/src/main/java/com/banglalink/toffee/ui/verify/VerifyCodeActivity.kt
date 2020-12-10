package com.banglalink.toffee.ui.verify

import android.content.IntentFilter
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.databinding.LayoutLoginConfirmBinding
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.receiver.SMSBroadcastReceiver
import com.banglalink.toffee.ui.common.BaseAppCompatActivity
import com.banglalink.toffee.ui.home.HomeActivity
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.unsafeLazy
import com.google.android.gms.auth.api.phone.SmsRetriever

class VerifyCodeActivity : BaseAppCompatActivity(){

    companion object {
        const val PHONE_NUMBER = "PHONE"
        const val REFERRAL_CODE = "REFERRAL_CODE"
        const val REG_SESSION_TOKEN = "REG_SESSION_TOKEN"
    }
    private lateinit var mSmsBroadcastReceiver: SMSBroadcastReceiver

    private val TAG = "VerifyCodeActivity"

    private val phoneNumber by unsafeLazy {
        intent.getStringExtra(PHONE_NUMBER)
    }

    private val referralCode by unsafeLazy {
        intent.getStringExtra(REFERRAL_CODE)
    }

    private lateinit var regSessionToken: String

    private val progressDialog by unsafeLazy {
        VelBoxProgressDialog(this)
    }

    private val viewModel by unsafeLazy {
        getViewModel<VerifyCodeViewModel>()
    }

    private var resendCodeTimer: ResendCodeTimer? = null

    private var resendBtnPressCount: Int = 0

    private lateinit var binding: LayoutLoginConfirmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.layout_login_confirm)

        binding.resend.paintFlags = binding.resend.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.resend.setOnClickListener {
            handleResendButton()
        }
        binding.confirmBtn.setOnClickListener {
            verifyCode(binding.codeNumber.text.toString())
        }

        regSessionToken = intent.getStringExtra(REG_SESSION_TOKEN) ?: ""
        startCountDown(if (resendBtnPressCount <= 1) 1 else 30)

       initSmsBroadcastReceiver()
    }

    private fun initSmsBroadcastReceiver(){
        // init broadcast receiver
        mSmsBroadcastReceiver = SMSBroadcastReceiver()
        observe(mSmsBroadcastReceiver.otpLiveData){
            binding.codeNumber.setText(it)
            binding.codeNumber.setSelection(it.length)
            verifyCode(binding.codeNumber.text.toString())
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION)
        registerReceiver(mSmsBroadcastReceiver, intentFilter)

        val mClient = SmsRetriever.getClient(this)
        mClient.startSmsRetriever()
    }

    private fun verifyCode(code: String) {
        progressDialog.show()
        observe(viewModel.verifyCode(code, regSessionToken, referralCode)) {
            progressDialog.dismiss()
            when (it) {
                is Resource.Success -> {
                    launchActivity<HomeActivity>() {
                        if (it.data.referralStatus == "Valid") {
                            putExtra(
                                HomeActivity.INTENT_REFERRAL_REDEEM_MSG,
                                it.data.referralStatusMessage
                            )
                        }
                    }
                    finish()
                }
                is Resource.Failure -> {
                    ToffeeAnalytics.logApiError("confirmCode",it.error.msg)
                    binding.root.snack(it.error.msg) {
                        action("Retry") {
                            verifyCode(binding.codeNumber.text.toString())
                        }
                    }
                }
            }
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
                    binding.resend.visibility = View.INVISIBLE
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
        unregisterReceiver(mSmsBroadcastReceiver)
        super.onDestroy()
    }
}
