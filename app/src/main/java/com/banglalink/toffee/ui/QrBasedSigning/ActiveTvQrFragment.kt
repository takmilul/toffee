package com.banglalink.toffee.ui.QrBasedSigning

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentActiveTvQrBinding
import com.banglalink.toffee.databinding.FragmentPremiumPackDetailsBinding
import com.banglalink.toffee.extension.checkVerification
import com.banglalink.toffee.extension.navigateTo
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeViewModel

class ActiveTvQrFragment: BaseFragment() {

    private var _binding: FragmentActiveTvQrBinding? = null
    val binding get() = _binding!!

    var qrCodeNumber : String?=null

    private val viewModel by activityViewModels<ActiveTvQrViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentActiveTvQrBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().checkVerification {

        }

        qrCodeNumber = arguments?.getString("code")

        Log.d("TAG", "qrCodeNumber: "+qrCodeNumber)


        if (!arguments?.getString("code").isNullOrEmpty()){

            binding.enterCodeView.visibility=View.GONE

            binding.activeWithQrView.visibility=View.VISIBLE

        }else{

            binding.enterCodeView.visibility=View.VISIBLE

            binding.activeWithQrView.visibility=View.GONE
        }

        binding.activeNowButton.setOnClickListener {

            Log.d("TAG", "onViewCreated:111111 "+qrCodeNumber)
            viewModel.getSubscriberPaymentInit(qrCodeNumber!!)
            observeSignInStatus()
        }

        binding.etCode1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // This method is called to notify you that the text will change.
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // This method is called to notify you that the text has changed.
                // You can edit the text here if needed.

                if (binding.etCode1.text.isNullOrEmpty()){
                    binding.etCode1.clearFocus()
                    binding.etCode1.hideKeyboard()
                }
                    else{
                        binding.etCode2.requestFocus()
                    }


            }

            override fun afterTextChanged(s: Editable?) {
                // This method is called to notify you that the text has changed after it was modified.
                s?.let {
                    if (it.length > 1) {
                        if (binding.etCode1.selectionStart==2){
                            binding.etCode1.setText(it.get(it.length-1).toString())
                            binding.etCode1.setSelection(1)
                            binding.etCode2.requestFocus()
                        }else{

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
                // This method is called to notify you that the text will change.

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // This method is called to notify you that the text has changed.
                // You can edit the text here if needed.

                if (binding.etCode2.text.isNullOrEmpty()){

                    binding.etCode1.requestFocus()
                }

                else{
                        binding.etCode3.requestFocus()

                }
            }

            override fun afterTextChanged(s: Editable?) {
                // This method is called to notify you that the text has changed after it was modified.
                s?.let {
                    if (it.length > 1) {

                        if (binding.etCode2.selectionStart==2){
                            binding.etCode2.setText(it.get(it.length-1).toString())
                            binding.etCode2.setSelection(1)
                            binding.etCode3.requestFocus()
                        }else{

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
                // This method is called to notify you that the text will change.

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // This method is called to notify you that the text has changed.
                // You can edit the text here if needed.


                if (binding.etCode3.text.isNullOrEmpty()){

                    binding.etCode2.requestFocus()
                }
                else{
                        binding.etCode4.requestFocus()


                }

            }

            override fun afterTextChanged(s: Editable?) {
                // This method is called to notify you that the text has changed after it was modified.
                s?.let {
                    if (it.length > 1) {

                        if (binding.etCode3.selectionStart==2){
                            binding.etCode3.setText(it.get(it.length-1).toString())
                            binding.etCode3.setSelection(1)
                            binding.etCode4.requestFocus()
                        }else{

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
                // This method is called to notify you that the text will change.

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // This method is called to notify you that the text has changed.
                // You can edit the text here if needed.

                if (binding.etCode4.text.isNullOrEmpty()){

                    binding.etCode3.requestFocus()
                }
                else{
                        binding.etCode5.requestFocus()

                }

            }

            override fun afterTextChanged(s: Editable?) {
                // This method is called to notify you that the text has changed after it was modified.

                s?.let {
                    if (it.length > 1) {

                        if (binding.etCode4.selectionStart==2){
                            binding.etCode4.setText(it.get(it.length-1).toString())
                            binding.etCode4.setSelection(1)
                            binding.etCode5.requestFocus()
                        }else{

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
                // This method is called to notify you that the text will change.

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // This method is called to notify you that the text has changed.
                // You can edit the text here if needed.


                if (binding.etCode5.text.isNullOrEmpty()){

                    binding.etCode4.requestFocus()
                }
                else{

                        binding.etCode6.requestFocus()


                }
            }

            override fun afterTextChanged(s: Editable?) {
                // This method is called to notify you that the text has changed after it was modified.

                s?.let {

                    if (it.length > 1) {

                        if (binding.etCode5.selectionStart==2){
                            binding.etCode5.setText(it.get(it.length-1).toString())
                            binding.etCode5.setSelection(1)
                            binding.etCode6.requestFocus()
                        }else{

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
                // This method is called to notify you that the text will change.
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // This method is called to notify you that the text has changed.
                // You can edit the text here if needed.



                if (binding.etCode6.text.isNullOrEmpty()){

                    binding.etCode5.requestFocus()
                }
                else{

                        binding.etCode6.clearFocus()
                        binding.etCode6.hideKeyboard()



                }
            }

            override fun afterTextChanged(s: Editable?) {
                // This method is called to notify you that the text has changed after it was modified.

                s?.let {
                    if (it.length > 1) {

                        if (binding.etCode6.selectionStart==2){
                            binding.etCode6.setText(it.get(it.length-1).toString())
                            binding.etCode6.setSelection(1)
                            binding.etCode6.clearFocus()
                            binding.etCode6.hideKeyboard()
                        }else{

                            binding.etCode6.setText(it.get(0).toString())
                            binding.etCode6.setSelection(1)
                            binding.etCode6.clearFocus()
                            binding.etCode6.hideKeyboard()
                        }
                    }

                }
            }
        })


        binding.pairWithTv.setOnClickListener {

            val input1 =  binding.etCode1.text.toString()
            val input2 =  binding.etCode2.text.toString()
            val input3 =  binding.etCode3.text.toString()
            val input4 =  binding.etCode4.text.toString()
            val input5 =  binding.etCode5.text.toString()
            val input6 =  binding.etCode6.text.toString()


            qrCodeNumber = "$input1$input2$input3$input4$input5$input6"


            Log.d("TAG", "pairWithTv: "+qrCodeNumber)
            viewModel.getSubscriberPaymentInit(qrCodeNumber!!)
            observeSignInStatus()

        }
//        observeSignInStatus()
    }

    fun observeSignInStatus(){

        /**
         * ( 0 = wrong code, 1 = active, 2 = expired )
         */

        observe(viewModel.qrSignInStatus){ responseCode->
            Log.d("TAG", "pairWithTv:11 "+responseCode)
            if (responseCode.equals(0)){

                binding.activeWithQrView.visibility=View.GONE

                binding.enterCodeView.visibility=View.VISIBLE
                binding.wrongCode.visibility=View.VISIBLE

            }
            else if (responseCode.equals(1)){

                findNavController().navigateTo(R.id.Qr_code_res)
            }
            else if (responseCode.equals(2)){

                findNavController().navigateTo(R.id.Qr_code_res)
            }
            else{


            }
        }

    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}