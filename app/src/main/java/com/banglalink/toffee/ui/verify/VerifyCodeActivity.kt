package com.banglalink.toffee.ui.verify

import android.content.IntentFilter
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.LayoutLoginConfirmBinding
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.receiver.OtpReceiveListener
import com.banglalink.toffee.receiver.SMSBroadcastReceiver
import com.banglalink.toffee.ui.common.BaseAppCompatActivity
import com.banglalink.toffee.ui.home.HomeActivity
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.unsafeLazy
import com.google.android.gms.auth.api.phone.SmsRetriever

class VerifyCodeActivity : BaseAppCompatActivity(),
    OtpReceiveListener {

    companion object {
        const val PHONE_NUMBER = "PHONE"
        const val REFERRAL_CODE = "REFERRAL_CODE"
        const val REG_SESSION_TOKEN = "REG_SESSION_TOKEN"
    }
    private var mSmsBroadcastReceiver: SMSBroadcastReceiver? = null

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

        // init broadcast receiver
        mSmsBroadcastReceiver = SMSBroadcastReceiver()
        mSmsBroadcastReceiver?.setOnOtpListeners(this)
        val intentFilter = IntentFilter()
        intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION)
        applicationContext.registerReceiver(mSmsBroadcastReceiver, intentFilter)
        startSMSListener()
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
                    binding.root.snack(it.error.msg) {
                        action("Retry") {
                            verifyCode(code)
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
        applicationContext.unregisterReceiver(mSmsBroadcastReceiver)
        super.onDestroy()
    }

    private fun startSMSListener() {
        val mClient = SmsRetriever.getClient(this)
        val mTask = mClient.startSmsRetriever()
        mTask.addOnSuccessListener {
            Log.d(TAG,"SMS Retriever starts")
        }
        mTask.addOnFailureListener {
            Log.d(TAG,"SMS Retriever starts error")
        }
    }

    override fun onOtpReceived(otp: String?) {
        binding.codeNumber.setText(otp)
        binding.codeNumber.setSelection(otp!!.length)
        verifyCode(binding.codeNumber.text.toString())
    }

    override fun onOtpTimeout() {
        //empty method
    }
}
