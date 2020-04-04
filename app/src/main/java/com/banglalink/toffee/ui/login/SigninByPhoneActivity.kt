package com.banglalink.toffee.ui.login

import android.app.Activity
import android.app.AlertDialog
import android.app.PendingIntent
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
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient


class SigninByPhoneActivity : BaseAppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {

    lateinit var binding: ActivitySigninByPhoneBinding
    var apiClient: GoogleApiClient? = null
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

        apiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .enableAutoManage(this, this)
            .addApi(Auth.CREDENTIALS_API)
            .build();

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
        val hintRequest = HintRequest.Builder()
            .setPhoneNumberIdentifierSupported(true)
            .build()
        val intent: PendingIntent = Auth.CredentialsApi.getHintPickerIntent(
            apiClient, hintRequest
        )
        startIntentSenderForResult(
            intent.intentSender,
            RESOLVE_HINT, null, 0, 0, 0
        )
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESOLVE_HINT && resultCode == Activity.RESULT_OK && data != null) {
            val credential: Credential =
                data.getParcelableExtra(Credential.EXTRA_KEY)
            println("mobile number:" + credential.id)
            binding.phoneNumberEt.setText(credential.id)
            binding.phoneNumberEt.setSelection(credential.id.length)
        }
    }


    override fun onConnected(p0: Bundle?) {
        //No Need to implement
    }

    override fun onConnectionSuspended(p0: Int) {
        //No Need to implement
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        //No Need to implement
    }

}
