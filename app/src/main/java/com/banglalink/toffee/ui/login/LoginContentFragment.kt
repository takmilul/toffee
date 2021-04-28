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
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.databinding.FragmentLoginContentBinding
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.INVALID_REFERRAL_ERROR_CODE
import com.banglalink.toffee.model.LOGIN_ERROR
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.TERMS_AND_CONDITION_URL
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.common.HtmlPageViewActivity
import com.banglalink.toffee.ui.splash.SplashScreenActivity
import com.banglalink.toffee.ui.splash.SplashViewModel
import com.banglalink.toffee.ui.widget.VelBoxAlertDialogBuilder
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.UtilsKt
import com.banglalink.toffee.util.unsafeLazy
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.Credentials
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.android.material.snackbar.Snackbar

class LoginContentFragment : BaseFragment() {
    private var referralCode: String = ""
    private var regSessionToken: String = ""
    private var snackBar: Snackbar? = null
    private var isNumberShown:Boolean = false
    private var _binding: FragmentLoginContentBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<LoginViewModel>()
    private val splashViewModel by activityViewModels<SplashViewModel>()
    private val progressDialog by unsafeLazy { VelBoxProgressDialog(requireContext()) }
    lateinit var phoneNo:String
    companion object {
        @JvmStatic
        fun newInstance() = LoginContentFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginContentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loginContentMotionLayout.setOnClickListener { UtilsKt.hideSoftKeyboard(requireActivity()) }
        setSpannableTermsAndConditions()
        binding.haveRefTv.setOnClickListener { handleHaveReferralOption() }
        binding.loginBtn.safeClick({
            handleLogin()
            observeLogin()
            viewModel.login(phoneNo, referralCode)
        })
        binding.skipButton.safeClick({
            (requireActivity() as SplashScreenActivity).observeApiLogin()
            splashViewModel.credentialResponse()
        })
        binding.termsAndConditionsCheckbox.setOnClickListener {
            binding.loginBtn.isEnabled = binding.termsAndConditionsCheckbox.isChecked
        }
        
        val signinMotionLayout = parentFragment?.parentFragment?.view
        
        signinMotionLayout?.let { 
            if (it is MotionLayout){
                it.onTransitionCompletedListener {
                    if (!isNumberShown) {
                        isNumberShown = true
                        getHintPhoneNumber()
                    }
                }
            }
        }
        if (!mPref.isPreviousDbDeleted){
            viewModel.deletePreviousDatabase()
        }
    }

    private fun handleLogin() {
         phoneNo = binding.phoneNumberEt.text.toString().trim()
        referralCode = binding.refCodeEt.text.toString().trim()
        if (TextUtils.isEmpty(phoneNo)) {
            VelBoxAlertDialogBuilder(requireContext()).apply {
                setText(R.string.phone_no_required_title)
                setPositiveButtonListener("OK") {
                    it?.dismiss()
                }
            }.create().show()
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
    }

    private fun observeLogin(){
        observe(viewModel.loginByPhoneResponse) {
            progressDialog.dismiss()
            when (it) {
                is Resource.Success -> {
                    snackBar?.dismiss()
                    if (it.data is String) {
                       regSessionToken = it.data as String
                       binding.loginContentMotionLayout.onTransitionCompletedListener {
                           if (findNavController().currentDestination?.id != R.id.verifyLoginFragment && findNavController().currentDestination?.id == R.id.loginContentFragment) {
                               findNavController().navigate(
                                   LoginContentFragmentDirections.actionLoginContentFragmentToVerifyLoginFragment(
                                       phoneNo,
                                       referralCode,
                                       regSessionToken
                                   )
                               )
                           }
                       }
                       binding.loginContentMotionLayout.transitionToEnd()
                   }
                }
                is Resource.Failure -> {
                    when (it.error.code) {
                        INVALID_REFERRAL_ERROR_CODE -> {
                            showInvalidReferralCodeDialog(it.error.msg, it.error.additionalMsg)
                        }
                        LOGIN_ERROR -> {
                            snackBar = binding.root.snack(it.error.msg, Snackbar.LENGTH_LONG) {}
                        }
                        else -> {
                            ToffeeAnalytics.logApiError("reRegistration",it.error.msg,phoneNo)
                            snackBar = binding.root.snack(it.error.msg) {
                                action("Retry") {
                                    handleLogin()
                                    viewModel.login(phoneNo, referralCode)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showInvalidReferralCodeDialog(referralStatusMessage: String, referralStatus: String, ) {
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
                viewModel.login(phoneNo, referralCode)
                alertDialog.dismiss()
            }
        alertView.findViewById<View>(R.id.retry_btn)
            .setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }

    private fun handleHaveReferralOption() {
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
        try {
            val hintRequest = HintRequest.Builder().setPhoneNumberIdentifierSupported(true).build()
            val intent = Credentials.getClient(requireActivity()).getHintPickerIntent(hintRequest)
            intent?.intentSender?.let {
                phoneNumberHintResultLauncher.launch(IntentSenderRequest.Builder(it).build())
            }
        } catch (e: Exception) {
            ToffeeAnalytics.logException(e)
            ToffeeAnalytics.logBreadCrumb("Could not retrieve phone number")
        }
    }
    
    private val phoneNumberHintResultLauncher = 
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()){ result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK && result.data != null) {
                val credential: Credential? = result.data!!.getParcelableExtra(Credential.EXTRA_KEY)
                credential?.let {
                    binding.phoneNumberEt.setText(it.id)
                    binding.phoneNumberEt.setSelection(it.id.length)
                }
            }
    }

    override fun onDestroy() {
        snackBar = null
        phoneNumberHintResultLauncher.unregister()
        super.onDestroy()
    }
}