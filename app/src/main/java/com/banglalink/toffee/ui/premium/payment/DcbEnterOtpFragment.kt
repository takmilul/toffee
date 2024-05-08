package com.banglalink.toffee.ui.premium.payment

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentDcbEnterOtpBinding
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.invisible
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.ui.common.ChildDialogFragment
import com.banglalink.toffee.ui.login.ResendCodeTimer

class DcbEnterOtpFragment : ChildDialogFragment() {
    private lateinit var binding : FragmentDcbEnterOtpBinding
    private var resendCodeTimer: ResendCodeTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDcbEnterOtpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startCountDown()

        binding.resendButton.safeClick({
            handleResendButton()
        })
    }

    private fun handleResendButton(){
        binding.resendButton.hide()
        startCountDown()
    }

    private fun startCountDown() {
        binding.countdownTextView.show()
        resendCodeTimer?.cancel()
        resendCodeTimer = ResendCodeTimer(this, 1).also { timer ->
            observe(timer.tickLiveData) {
                val remainingSecs = it / 1000
                val minutes = (remainingSecs / 60).toInt()
                val seconds = (remainingSecs % 60).toInt()
                val timeText = (String.format("%02d", minutes) + ":" + String.format("%02d", seconds))
                val countDownText = String.format(getString(R.string.sign_in_countdown_text), timeText)
                val str = SpannableString(countDownText)
                str.setSpan(
                    StyleSpan(Typeface.BOLD), countDownText.indexOf(timeText), countDownText.indexOf(timeText) + timeText.length, Spannable
                    .SPAN_EXCLUSIVE_EXCLUSIVE)
                binding.countdownTextView.text = str
            }

            observe(timer.finishLiveData) {
                binding.resendButton.isEnabled = true
                binding.resendButton.show()
                binding.countdownTextView.invisible()
                binding.countdownTextView.text = ""

                timer.finishLiveData.removeObservers(this)
                timer.tickLiveData.removeObservers(this)
            }
        }
        resendCodeTimer?.start()
    }
}