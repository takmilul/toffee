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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.databinding.ActivitySigninByPhoneBinding
import com.banglalink.toffee.databinding.FragmentSigninByPhoneBinding
import com.banglalink.toffee.extension.action
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.snack
import com.banglalink.toffee.model.INVALID_REFERRAL_ERROR_CODE
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.TERMS_AND_CONDITION_URL
import com.banglalink.toffee.ui.common.HtmlPageViewActivity
import com.banglalink.toffee.ui.verify.VerifyCodeActivity
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.ui.widget.showAlertDialog
import com.banglalink.toffee.util.unsafeLazy
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.Credentials
import com.google.android.gms.auth.api.credentials.HintRequest

class SigninByPhoneFragment : Fragment() {

    lateinit var binding: FragmentSigninByPhoneBinding
    private val RESOLVE_HINT = 2;

    private val viewModel by viewModels<SigninByPhoneViewModel>()

    private val progressDialog by unsafeLazy {
        VelBoxProgressDialog(requireContext())
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SigninByPhoneFragment()
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_signin_by_phone, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        /*setSpannableTermsAndConditions()
        binding.haveRefTv.setOnClickListener { handleHaveReferralOption() }
        binding.loginBtn.setOnClickListener {
            handleLogin()
        }
        binding.termsAndConditionsCheckbox.setOnClickListener {
            binding.loginBtn.isEnabled = binding.termsAndConditionsCheckbox.isChecked
        }
        binding.signinMotionLayout.addTransitionListener(object : MotionLayout.TransitionListener{
            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
                println("Transition started")
            }

            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {
                println("Transition changed")
            }

            override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
                getHintPhoneNumber()
            }

            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {
                println("Transition triggered")
            }
        })*/
    }

    /*private fun handleLogin() {
        var phoneNo = binding.phoneNumberEt.text.toString()

        if (TextUtils.isEmpty(phoneNo)) {
            showAlertDialog(requireContext(), getString(R.string.phone_no_required_title), "")
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
                    binding.mainLayout.transitionToEnd()
                    requireActivity().launchActivity<VerifyCodeActivity> {
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
                    requireActivity().finish()
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
        val alertView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_invalid_referral_code_layout, null)

        val alertDialog = AlertDialog.Builder(requireContext()).create()
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

    fun handleHaveReferralOption() {
        binding.haveRefTv.isClickable = false
        binding.refCodeEt.visibility = View.VISIBLE
        binding.groupHaveRef.visibility = View.INVISIBLE
    }

    private fun setSpannableTermsAndConditions() {
        val ss = SpannableString(getString(R.string.terms_and_conditions))
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                val intent = Intent(requireContext(), HtmlPageViewActivity::class.java)
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
        ss.setSpan(clickableSpan, 0, ss.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.termsAndConditionsTv.text = ss
        binding.termsAndConditionsTv.movementMethod = LinkMovementMethod.getInstance()

    }

    private fun getHintPhoneNumber() {
        try{
            val hintRequest = HintRequest.Builder().setPhoneNumberIdentifierSupported(true).build()
            val intent = Credentials.getClient(requireActivity()).getHintPickerIntent(hintRequest)
            intent?.intentSender?.let {
                startIntentSenderForResult(it, RESOLVE_HINT, null, 0, 0, 0, null)
            }
        }catch (e: Exception){
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
        if (requestCode == RESOLVE_HINT && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            val credential: Credential? =
                data.getParcelableExtra(Credential.EXTRA_KEY)
            credential?.let {
                binding.phoneNumberEt.setText(it.id)
                binding.phoneNumberEt.setSelection(it.id.length)
            }
        }
    }*/

}