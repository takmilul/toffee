package com.banglalink.toffee.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.lifecycle.ViewModelProviders
import com.banglalink.toffee.R
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.TERMS_AND_CONDITION_URL
import com.banglalink.toffee.ui.common.BaseAppCompatActivity
import com.banglalink.toffee.ui.common.HtmlPageViewActivity
import com.banglalink.toffee.ui.verify.VerifyCodeActivity
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.ui.widget.showAlertDialog
import com.banglalink.toffee.util.unsafeLazy

class SigninByPhoneActivity : BaseAppCompatActivity() {

    private val viewModel by unsafeLazy {
        ViewModelProviders.of(this).get(SigninByPhoneViewModel::class.java)
    }
    private val phoneNumber by unsafeLazy {
        findViewById<EditText>(R.id.phone_number_et)
    }

    private val progressDialog by unsafeLazy {
        VelBoxProgressDialog(this)
    }

    private lateinit var referralCodeEt: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin_by_phone)

        referralCodeEt = findViewById(R.id.ref_code_et)
        setSpannableTermsAndConditions()

        val loginBtn = findViewById<Button>(R.id.login_btn)
        loginBtn.setOnClickListener {
            handleLogin()
        }
        val termsAndConditionCheckBox = findViewById<CheckBox>(R.id.checkBox)
        termsAndConditionCheckBox.setOnClickListener {
            loginBtn.isEnabled = termsAndConditionCheckBox.isChecked
        }

        observe(viewModel.signinLiveData) {
            progressDialog.dismiss()
            when (it) {
                is Resource.Success -> {
                    launchActivity<VerifyCodeActivity> {
                        putExtra(VerifyCodeActivity.PHONE_NUMBER, phoneNumber.text.toString())
                        putExtra(VerifyCodeActivity.REFERRAL_CODE, referralCodeEt.text.toString())
                    }
                    finish()
                }
                is Resource.Failure -> {
                    showToast(it.error.msg)
                }
            }
        }
    }

    private fun handleLogin() {
        var phoneNo = phoneNumber.text.toString()

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
        this.phoneNumber.setText(phoneNo)
        viewModel.siginIn(phoneNo, referralCodeEt.text.toString())
    }

    fun handleHaveReferralOption(view: View) {
        findViewById<Group>(R.id.group).visibility = View.VISIBLE
        findViewById<Group>(R.id.group_have_ref).visibility = View.GONE
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

        val textView = findViewById<TextView>(R.id.terms_and_conditions_tv)
        textView.text = ss
        textView.movementMethod = LinkMovementMethod.getInstance()

    }
}
