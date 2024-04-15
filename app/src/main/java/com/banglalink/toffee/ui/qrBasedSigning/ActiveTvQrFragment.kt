package com.banglalink.toffee.ui.qrBasedSigning

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentActiveTvQrBinding
import com.banglalink.toffee.extension.checkVerification
import com.banglalink.toffee.extension.navigateTo
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.util.unsafeLazy
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ActiveTvQrFragment : BaseFragment() {
    private var _binding: FragmentActiveTvQrBinding? = null
    val binding get() = _binding!!
    private var qrCodeNumber: String? = null
    private val viewModel by activityViewModels<ActiveTvQrViewModel>()
    private val progressDialog by unsafeLazy { ToffeeProgressDialog(requireContext()) }
    private val editTextList: List<EditText> by lazy {
        listOf(
            binding.etCode1, binding.etCode2, binding.etCode3,
            binding.etCode4, binding.etCode5, binding.etCode6
        )
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentActiveTvQrBinding.inflate(layoutInflater)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        activity?.title = "Sign into TV"
        val toolbar = activity?.findViewById<Toolbar>(R.id.toolbar)
        toolbar?.setNavigationIcon(R.drawable.ic_arrow_back)
        qrCodeNumber = arguments?.getString("code")
        /**
         * saving qr code data in session preference so that fragment
         * doesn't loses data when fragment is reloaded/restarted.
         */
        if (qrCodeNumber != null) mPref.qrSignInStatus.value = qrCodeNumber.toString()
        
        observeSignInStatus()
        if (mPref.qrSignInStatus.value == "0" || mPref.qrSignInStatus.value==null) {
            /**
             * this section is executed when user comes from right drawer menu
             */
            requireActivity().checkVerification(shouldReloadAfterLogin = false) {
                binding.enterCodeView.visibility = View.VISIBLE
                binding.activeWithQrView.visibility = View.GONE
            }
        } else {
            /**
             * this section is executed when qr code is scanned (deeplink)
             */
            requireActivity().checkVerification(shouldReloadAfterLogin = false) {
                /**
                 * qrSignInResponseCode= 2 meaning user backpressed from QrCodeReasultFragment
                 * and dont nedd to auto navigate to next page
                 */
//                if (mPref.qrSignInResponseCode.value!="2"){
                progressDialog.show()
                viewModel.getSubscriberPaymentInit(mPref.qrSignInStatus.value!!)
//                }
                binding.enterCodeView.visibility = View.GONE
                binding.activeWithQrView.visibility = View.VISIBLE
            }
            
            binding.enterCodeView.visibility = View.GONE
            binding.activeWithQrView.visibility = View.VISIBLE
        }
        
        binding.activeNowButton.setOnClickListener {
            requireActivity().checkVerification(shouldReloadAfterLogin = false) {
                progressDialog.show()
                viewModel.getSubscriberPaymentInit(mPref.qrSignInStatus.value!!)
            }
        }
        
        binding.etCode1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.etCode1.text.isNullOrEmpty()) {
                    binding.etCode1.clearFocus()
                    binding.etCode1.hideKeyboard()
                } else {
                    binding.etCode2.requestFocus()
                }
            }
            
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (it.length > 1) {
                        if (binding.etCode1.selectionStart == 2) {
                            binding.etCode1.setText(it.get(it.length - 1).toString())
                            binding.etCode1.setSelection(1)
                            binding.etCode2.requestFocus()
                        } else {
                            binding.etCode1.setText(it.get(0).toString())
                            binding.etCode1.setSelection(1)
                            binding.etCode2.requestFocus()
                        }
                    }
                }
            }
        })
        
        binding.etCode2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.etCode2.text.isNullOrEmpty()) {
                    binding.etCode1.requestFocus()
                } else {
                    binding.etCode3.requestFocus()
                }
            }
            
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (it.length > 1) {
                        if (binding.etCode2.selectionStart == 2) {
                            binding.etCode2.setText(it.get(it.length - 1).toString())
                            binding.etCode2.setSelection(1)
                            binding.etCode3.requestFocus()
                        } else {
                            binding.etCode2.setText(it.get(0).toString())
                            binding.etCode2.setSelection(1)
                            binding.etCode3.requestFocus()
                        }
                    }
                }
            }
        })
        
        binding.etCode3.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.etCode3.text.isNullOrEmpty()) {
                    binding.etCode2.requestFocus()
                } else {
                    binding.etCode4.requestFocus()
                }
            }
            
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (it.length > 1) {
                        if (binding.etCode3.selectionStart == 2) {
                            binding.etCode3.setText(it.get(it.length - 1).toString())
                            binding.etCode3.setSelection(1)
                            binding.etCode4.requestFocus()
                        } else {
                            binding.etCode3.setText(it.get(0).toString())
                            binding.etCode3.setSelection(1)
                            binding.etCode4.requestFocus()
                        }
                    }
                }
            }
        })
        
        binding.etCode4.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.etCode4.text.isNullOrEmpty()) {
                    binding.etCode3.requestFocus()
                } else {
                    binding.etCode5.requestFocus()
                }
            }
            
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (it.length > 1) {
                        if (binding.etCode4.selectionStart == 2) {
                            binding.etCode4.setText(it.get(it.length - 1).toString())
                            binding.etCode4.setSelection(1)
                            binding.etCode5.requestFocus()
                        } else {
                            binding.etCode4.setText(it.get(0).toString())
                            binding.etCode4.setSelection(1)
                            binding.etCode5.requestFocus()
                        }
                    }
                }
            }
        })
        
        binding.etCode5.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.etCode5.text.isNullOrEmpty()) {
                    binding.etCode4.requestFocus()
                } else {
                    binding.etCode6.requestFocus()
                }
            }
            
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (it.length > 1) {
                        if (binding.etCode5.selectionStart == 2) {
                            binding.etCode5.setText(it.get(it.length - 1).toString())
                            binding.etCode5.setSelection(1)
                            binding.etCode6.requestFocus()
                        } else {
                            binding.etCode5.setText(it.get(0).toString())
                            binding.etCode5.setSelection(1)
                            binding.etCode6.requestFocus()
                        }
                    }
                }
            }
        })
        
        binding.etCode6.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.etCode6.text.isNullOrEmpty()) {
                    binding.etCode5.requestFocus()
                } else {
                    binding.etCode6.clearFocus()
                    binding.etCode6.hideKeyboard()
                }
            }
            
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (it.length > 1) {
                        if (binding.etCode6.selectionStart == 2) {
                            binding.etCode6.setText(it.get(it.length - 1).toString())
                            binding.etCode6.setSelection(1)
                            binding.etCode6.clearFocus()
                            binding.etCode6.hideKeyboard()
                        } else {
                            binding.etCode6.setText(it.get(0).toString())
                            binding.etCode6.setSelection(1)
                            binding.etCode6.clearFocus()
                            binding.etCode6.hideKeyboard()
                        }
                    }
                }
            }
        })
        
        editTextList.forEach { editText ->
            editText.setOnFocusChangeListener { _, _ ->
                updateButtonState()
            }
        }
        
        binding.pairWithTv.setOnClickListener {
            requireActivity().checkVerification(shouldReloadAfterLogin = false) {
                val input1 = binding.etCode1.text.toString()
                val input2 = binding.etCode2.text.toString()
                val input3 = binding.etCode3.text.toString()
                val input4 = binding.etCode4.text.toString()
                val input5 = binding.etCode5.text.toString()
                val input6 = binding.etCode6.text.toString()

                if (input1.isNotEmpty() && input2.isNotEmpty() && input3.isNotEmpty() && input4.isNotEmpty() && input5.isNotEmpty() && input6.isNotEmpty()) {
                    binding.wrongCode.visibility = View.GONE
                    qrCodeNumber = "$input1$input2$input3$input4$input5$input6"

                    viewModel.getSubscriberPaymentInit(qrCodeNumber!!)
                    observeSignInStatus()
                }
                binding.pairWithTv.hideKeyboard()
            }
        }
//        observeSignInStatus()
        toolbar?.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
    }
    
    private fun observeSignInStatus() {

        observe(viewModel.qrSignInStatus) {
            when(it){
                is Resource.Success->{
                    progressDialog.dismiss()
                    val responseCode = it.data
                    if (responseCode.equals(0)) {
                        if (mPref.qrSignInStatus.value == "0") {
                            binding.activeWithQrView.visibility = View.GONE
                            binding.enterCodeView.visibility = View.VISIBLE
                            binding.wrongCode.visibility = View.VISIBLE
                        } else {
                            mPref.qrSignInResponseCode.value = "0"
                            findNavController().navigateTo(R.id.Qr_code_res)
                        }
                    } else if (responseCode.equals(1)) {
                        mPref.qrSignInResponseCode.value = "1"
                        findNavController().navigateTo(R.id.Qr_code_res)
                    }
                    else if (responseCode.equals(2)) {
                        if (mPref.qrSignInStatus.value == "0") {
                            binding.activeWithQrView.visibility = View.GONE
                            binding.enterCodeView.visibility = View.VISIBLE
                            binding.wrongCode.visibility = View.VISIBLE
                        } else {
                            mPref.qrSignInResponseCode.value = "2"
                            findNavController().navigateTo(R.id.Qr_code_res)
                        }
                    }
                    else {

                        requireActivity().showToast(getString(R.string.no_activity_msg))
                    }
                }
                is Resource.Failure->{
                    progressDialog.dismiss()
                    requireActivity().showToast(it.error.msg)
                }
            }
        }
    }
    
    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
    
    private fun updateButtonState() {
        // Enable the button only if all EditText fields have non-empty values
        val allFieldsFilled = editTextList.all { it.text.isNotBlank() }
        binding.pairWithTv.isEnabled = allFieldsFilled
    }
    
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}