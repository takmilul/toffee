package com.banglalink.toffee.ui.QrBasedSigning

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.banglalink.toffee.databinding.FragmentQrCodeReasultBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.ui.common.BaseFragment


class QrCodeReasultFragment : BaseFragment() {

    private var _binding: FragmentQrCodeReasultBinding?= null
    val binding get() = _binding!!

    var qrCodeNumber : String?=null

    private val viewModel by activityViewModels<ActiveTvQrViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentQrCodeReasultBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeSignInStatus()
    }
    fun observeSignInStatus(){

        /**
         * ( 0 = wrong code, 1 = active, 2 = expired )
         */

        observe(viewModel.qrSignInStatus){ responseCode->
            Log.d("TAG", "pairWithTv:11 "+responseCode)

             if (responseCode.equals(1)){

                binding.qrSignInActivatedView.visibility=View.GONE
                binding.qrCodeExpiredView.visibility=View.GONE


                binding.qrSignInActivatedView.visibility=View.VISIBLE
            }
            else if (responseCode.equals(2)){

                binding.qrSignInActivatedView.visibility=View.GONE

                binding.qrCodeExpiredView.visibility=View.VISIBLE

            }
            else{
                Toast.makeText(requireContext(),"Error Occurred", Toast.LENGTH_SHORT).show()

            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}