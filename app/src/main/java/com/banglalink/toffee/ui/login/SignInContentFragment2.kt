package com.banglalink.toffee.ui.login

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.databinding.AlertDialogLoginBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.unsafeLazy
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SignInContentFragment2 : DialogFragment(), TextWatcher {
    
    private var phoneNo: String = ""
    private var regSessionToken: String = ""
    private var alertDialog: AlertDialog? = null
    @Inject lateinit var mPref: SessionPreference
    private var _binding: AlertDialogLoginBinding? = null
    private val binding get() = _binding !!
    private val viewModel by viewModels<SignInViewModel2>()
    private val progressDialog by unsafeLazy { VelBoxProgressDialog(requireContext()) }
    
    companion object {
        const val PHONE_NO_ARG = "phoneNo"
        const val REG_SESSION_TOKEN_ARG = "regSessionToken"
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = AlertDialogLoginBinding.inflate(layoutInflater)
        alertDialog = AlertDialog
            .Builder(requireContext())
            .setView(binding.root).create()
            .apply {
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
        
        setSpannableTermsAndConditions()
        
        with(binding) {
            closeIv.safeClick({ dismiss() })
            verifyButton.safeClick({
                progressDialog.show()
                handleLogin()
                observeSignIn()
                viewModel.signIn(phoneNo)
            })
            termsAndConditionsCheckbox.setOnClickListener {
                binding.verifyButton.isEnabled = binding.termsAndConditionsCheckbox.isChecked
            }
            phoneNumberEditText.addTextChangedListener(this@SignInContentFragment2)
        }
        
        return alertDialog !!
    }
    
    private fun handleLogin() {
        phoneNo = binding.phoneNumberEditText.text.toString().trim()
        
        if (phoneNo.startsWith("0")) {
            phoneNo = "+88$phoneNo"
        }
        
        if (! phoneNo.startsWith("+")) {
            phoneNo = "+$phoneNo"
        }
        
        binding.phoneNumberEditText.setText(phoneNo)
        binding.phoneNumberEditText.setSelection(phoneNo.length)
    }
    
    private fun observeSignIn() {
        observe(viewModel.signByPhoneResponse) {
            progressDialog.dismiss()
            when (it) {
                is Resource.Success -> {
                    if (it.data is String) {
                        regSessionToken = it.data
                        if (findNavController().currentDestination?.id != R.id.verifySignInFragment && findNavController().currentDestination?.id == R.id.signInDialog) {
                            findNavController().popBackStack().let { 
                                findNavController().navigate(R.id.verifySignInDialog,
                                    Bundle().apply { 
                                        putString(PHONE_NO_ARG, phoneNo)
                                        putString(REG_SESSION_TOKEN_ARG, regSessionToken)
                                    }
                                )
                            }
                        }
                    }
                }
                is Resource.Failure -> {
                    ToffeeAnalytics.logApiError("reRegistration", it.error.msg, phoneNo)
                    requireActivity().showToast(it.error.msg)
                }
            }
        }
    }
    
    private fun setSpannableTermsAndConditions() {
        val ss = SpannableString(getString(R.string.terms_and_conditions))
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                /*val intent = Intent(requireContext(), HtmlPageViewActivity::class.java)
                intent.putExtra(
                    HtmlPageViewActivity.CONTENT_KEY,
                    TERMS_AND_CONDITION_URL
                )
                intent.putExtra(
                    HtmlPageViewActivity.TITLE_KEY,
                    getString(R.string.terms_and_conditions)
                )
                startActivity(intent)*/
                showTermsAndConditionDialog()
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
    
    private fun showTermsAndConditionDialog() {
        if (findNavController().currentDestination?.id != R.id.termsConditionFragment && findNavController().currentDestination?.id == R.id.signInDialog) {
            findNavController().navigate(R.id.termsConditionFragment)
        }
    }
    
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    
    override fun afterTextChanged(s: Editable?) {
        phoneNo = s.toString()
        binding.verifyButton.isEnabled = phoneNo.isNotBlank() && phoneNo.length >= 11
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}