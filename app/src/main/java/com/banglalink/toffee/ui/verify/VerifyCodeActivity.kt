package com.banglalink.toffee.ui.verify

import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import com.banglalink.toffee.R
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseAppCompatActivity
import com.banglalink.toffee.ui.home.HomeActivity
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog

class VerifyCodeActivity : BaseAppCompatActivity() {

    companion object{
        const val PHONE_NUMBER = "PHONE"
    }

    private val phoneNumber by lazy {
        intent.getStringExtra(PHONE_NUMBER)
    }
    private val countdownTV by lazy {
        findViewById<TextView>(R.id.countdown_tv)
    }
    private val resendButton by lazy {
        findViewById<TextView>(R.id.resend)
    }
    private val confirmButton by lazy {
        findViewById<Button>(R.id.confirm_btn)
    }

    private val progressDialog by lazy {
        VelBoxProgressDialog(this)
    }

    private val viewModel by lazy {
        ViewModelProviders.of(this).get(VerifyCodeViewModel::class.java)
    }

    private var resendBtnPressCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_login_confirm)

        val codeEditText = findViewById<EditText>(R.id.code_number)
        resendButton.paintFlags = resendButton.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        resendButton.setOnClickListener{
            handleResendButton()
        }
        confirmButton.setOnClickListener{
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
        resendButton.visibility = View.INVISIBLE
        startCountDown(if (resendBtnPressCount <= 1) 1 else 30)

        progressDialog.show()
        viewModel.resendCode(phoneNumber)
    }
    private fun startCountDown(countDownTimeInMinute: Int) {
        countdownTV.visibility = View.VISIBLE
        object : CountDownTimer((countDownTimeInMinute * 60000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val remainingSecs = millisUntilFinished / 1000
                val minutes = (remainingSecs / 60).toInt()
                val seconds = (remainingSecs % 60).toInt()
                val timeText = (String.format("%02d", minutes)
                        + ":" + String.format("%02d", seconds))
                countdownTV.text = "Resend option will be activated after $timeText"
            }

            override fun onFinish() {
                resendButton.isEnabled = true
                resendButton.visibility = View.VISIBLE
                countdownTV.visibility = View.INVISIBLE
                countdownTV.text = ""
            }
        }.start()
    }
}
