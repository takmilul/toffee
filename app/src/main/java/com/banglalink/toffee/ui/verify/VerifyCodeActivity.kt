package com.banglalink.toffee.ui.verify

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.LayoutLoginConfirmBinding
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseAppCompatActivity
import com.banglalink.toffee.ui.home.HomeActivity
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.unsafeLazy

class VerifyCodeActivity : BaseAppCompatActivity() {

    companion object{
        const val PHONE_NUMBER = "PHONE"
        const val REFERRAL_CODE = "REFERRAL_CODE"
    }

    private val phoneNumber by unsafeLazy {
        intent.getStringExtra(PHONE_NUMBER)
    }

    private val referralCode by unsafeLazy {
        intent.getStringExtra(REFERRAL_CODE)
    }

    private val progressDialog by unsafeLazy {
        VelBoxProgressDialog(this)
    }

    private val viewModel by unsafeLazy {
        ViewModelProviders.of(this).get(VerifyCodeViewModel::class.java)
    }

    private var resendCodeTimer:ResendCodeTimer? = null

    private var resendBtnPressCount: Int = 0

    private lateinit var binding:LayoutLoginConfirmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.layout_login_confirm)

        val codeEditText = findViewById<EditText>(R.id.code_number)
        binding.resend.paintFlags = binding.resend.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.resend.setOnClickListener{
            handleResendButton()
        }
        binding.confirmBtn.setOnClickListener{
            verifyCode(codeEditText.text.toString())
        }

        startCountDown(if (resendBtnPressCount <= 1) 1 else 30)

        observe(viewModel.verifyCodeLiveData){
            progressDialog.dismiss()
            when(it){
                is Resource.Success ->{
                   launchActivity<HomeActivity>()
                    finish()
                }
                is Resource.Failure->{
                    showToast(it.error.msg)
                }
            }
        }
    }

    private fun verifyCode(code:String){
        progressDialog.show()
        viewModel.verifyCode(code)
    }

    private fun handleResendButton(){
        resendBtnPressCount++
        binding.resend.visibility = View.INVISIBLE
        startCountDown(if (resendBtnPressCount <= 1) 1 else 30)

        viewModel.resendCode(phoneNumber, referralCode)
    }
    private fun startCountDown(countDownTimeInMinute: Int) {
        binding.countdownTv.visibility = View.VISIBLE
        resendCodeTimer?.cancel()
        resendCodeTimer = ResendCodeTimer(this,countDownTimeInMinute).also { timer ->
            observe(timer.tickLiveData){
                val remainingSecs = it / 1000
                val minutes = (remainingSecs / 60).toInt()
                val seconds = (remainingSecs % 60).toInt()
                val timeText = (String.format("%02d", minutes)
                        + ":" + String.format("%02d", seconds))
                binding.countdownTv.text = "Resend option will be activated after $timeText"
            }

            observe(timer.finishLiveData){
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
        super.onDestroy()
    }
}
