package com.banglalink.toffee.ui.login

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.databinding.ActivitySigninByPhoneBinding
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.INVALID_REFERRAL_ERROR_CODE
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.TERMS_AND_CONDITION_URL
import com.banglalink.toffee.ui.common.BaseAppCompatActivity
import com.banglalink.toffee.ui.common.HtmlPageViewActivity
import com.banglalink.toffee.ui.verify.VerifyCodeActivity
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.ui.widget.showAlertDialog
import com.banglalink.toffee.util.unsafeLazy
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.Credentials
import com.google.android.gms.auth.api.credentials.HintRequest
import java.lang.Exception


class SigninByPhoneActivity : BaseAppCompatActivity() {

    lateinit var binding: ActivitySigninByPhoneBinding
    private val RESOLVE_HINT = 2;

    private val viewModel by unsafeLazy {
        getViewModel<SigninByPhoneViewModel>()
    }

    private val progressDialog by unsafeLazy {
        VelBoxProgressDialog(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_signin_by_phone)

        setSpannableTermsAndConditions()

        binding.loginBtn.setOnClickListener {
            handleLogin()
        }
        binding.termsAndConditionsCheckbox.setOnClickListener {
            binding.loginBtn.isEnabled = binding.termsAndConditionsCheckbox.isChecked
        }

        getHintPhoneNumber()

    }

    private fun handleLogin() {
        var phoneNo = binding.phoneNumberEt.text.toString()

        if (TextUtils.isEmpty(phoneNo)) {
            showAlertDialog(this, getString(R.string.phone_no_required_title), "")
            return
        }

        if (phoneNo.startsWith("0")) {
            phoneNo = "+88$phoneNo"
        }

        if (!phoneNo.startsWith("+")) {
            phoneNo = "+$phoneNo"
        }


        progressDialog.show()
        binding.phoneNumberEt.setText(phoneNo)
        binding.phoneNumberEt.setSelection(phoneNo.length)

        observe(viewModel.signIn(phoneNo, binding.refCodeEt.text.toString())) {
            progressDialog.dismiss()
            when (it) {
                is Resource.Success -> {
                    launchActivity<VerifyCodeActivity> {
                        putExtra(
                            VerifyCodeActivity.PHONE_NUMBER,
                            binding.phoneNumberEt.text.toString()
                        )
                        putExtra(
                            VerifyCodeActivity.REFERRAL_CODE,
                            binding.refCodeEt.text.toString()
                        )
                        putExtra(VerifyCodeActivity.REG_SESSION_TOKEN, it.data)
                    }
                    finish()
                }
                is Resource.Failure -> {
                    when (it.error.code) {
                        INVALID_REFERRAL_ERROR_CODE -> {
                            showInvalidReferralCodeDialog(it.error.msg, it.error.additionalMsg)
                        }
                        else -> {
                            binding.root.snack(it.error.msg) {
                                action("Retry") {
                                    handleLogin()
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    private fun showInvalidReferralCodeDialog(
        referralStatusMessage: String, referralStatus: String
    ) {
        val alertView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_invalid_referral_code_layout, null)

        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setView(alertView)
        alertDialog.setCancelable(false)
        alertDialog.window
            ?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        if ("USED".equals(referralStatus, ignoreCase = true)) {
            alertView.findViewById<View>(R.id.retry_btn).visibility = View.GONE
            (alertView.findViewById<View>(R.id.continue_btn) as Button).text =
                getString(R.string.continue_sign_in_txt)
        }

        (alertView.findViewById<View>(R.id.title) as TextView).text = referralStatusMessage
        alertView.findViewById<View>(R.id.continue_btn)
            .setOnClickListener {
                binding.refCodeEt.setText("")
                handleLogin()
                alertDialog.dismiss()
            }
        alertView.findViewById<View>(R.id.retry_btn)
            .setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }

    fun handleHaveReferralOption(view: View) {
        binding.group.visibility = View.VISIBLE
        binding.groupHaveRef.visibility = View.GONE
    }

    private fun setSpannableTermsAndConditions() {
        val ss = SpannableString(getString(R.string.terms_and_conditions_text))
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                val intent = Intent(this@SigninByPhoneActivity, HtmlPageViewActivity::class.java)
                intent.putExtra(
                    HtmlPageViewActivity.CONTENT_KEY,
                    TERMS_AND_CONDITION_URL
                )
                intent.putExtra(
                    HtmlPageViewActivity.TITLE_KEY,
                    getString(R.string.terms_and_conditions)
                )
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
            }
        }
        ss.setSpan(clickableSpan, 15, ss.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.termsAndConditionsTv.text = ss
        binding.termsAndConditionsTv.movementMethod = LinkMovementMethod.getInstance()

    }

    private fun getHintPhoneNumber() {
        try{
            val hintRequest = HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .build()
            val intent = Credentials.getClient(this).getHintPickerIntent(hintRequest)
            intent?.intentSender?.let {
                startIntentSenderForResult(
                    it,
                    RESOLVE_HINT, null, 0, 0, 0
                )
            }
        }catch (e:Exception){
            ToffeeAnalytics.logException(e)
            ToffeeAnalytics.logBreadCrumb("Could not retrieve phone number")
        }

    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESOLVE_HINT && resultCode == RESULT_OK && data != null) {
            val credential: Credential? =
                data.getParcelableExtra(Credential.EXTRA_KEY)
            credential?.let {
                binding.phoneNumberEt.setText(it.id)
                binding.phoneNumberEt.setSelection(it.id.length)
            }
        }
    }

}
