package com.banglalink.toffee.ui.QrBasedSigning

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.databinding.FragmentQrCodeReasultBinding
import com.banglalink.toffee.extension.navigatePopUpTo
import com.banglalink.toffee.ui.common.BaseFragment


class QrCodeReasultFragment : BaseFragment() {

    private var _binding: FragmentQrCodeReasultBinding?= null
    val binding get() = _binding!!

//    var qrCodeNumber : String?=null

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

//        qrCodeNumber = arguments?.getString("responseCode")


        if (mPref.qrSignInResponseCode.value!=null && mPref.qrSignInResponseCode.value.equals("1")){

            binding.qrSignInActivatedView.visibility=View.GONE
            binding.qrCodeExpiredView.visibility=View.GONE

            binding.qrSignInActivatedView.visibility=View.VISIBLE
            mPref.qrSignInStatus.value=null

        }
        else if (mPref.qrSignInResponseCode.value!=null &&  mPref.qrSignInResponseCode.value!!.equals("2")){

            binding.qrSignInActivatedView.visibility=View.GONE
            binding.qrCodeExpiredView.visibility=View.VISIBLE

        }
        else{


        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isEnabled) {

                    if (mPref.qrSignInResponseCode.value!=null && mPref.qrSignInResponseCode.value.equals("1") ){

                        /**
                         * when activation is successful user will be send back
                         * to home page when back button is pressed
                         */
                        mPref.qrSignInResponseCode.value="1"
                        findNavController().navigatePopUpTo(
                            resId = R.id.Qr_code_res,
                            popUpTo = R.id.menu_active_tv,
                            inclusive = true
                        )
                    }
                    else if (mPref.qrSignInResponseCode.value!=null && mPref.qrSignInResponseCode.value.equals("2")
                        && mPref.qrSignInStatus.value !="0"){

                        /**
                         * when user enters an EXPIRED CODE using QR SCANNING then
                         * user will be send back to HOME PAGE when BACK BUTTON is
                         * pressed(mPref.qrSignInStatus.value !="0" means user used QR SCANNING to login)
                         */

                        mPref.qrSignInResponseCode.value="2"

                        findNavController().navigatePopUpTo(
                            resId = R.id.Qr_code_res,
                            popUpTo = R.id.menu_active_tv,
                            inclusive = true
                        )

                    }
                    else{
                        /**
                         * when user enters an EXPIRED CODE manually user will
                         * be send back to qr code entry page when BACK BUTTON is pressed.
                         */

                        /**
                         *  signIn response is saved in preference so that user doesn't automatically naviagtes
                         *  to QrCodeResultFragment from ActiveTvQrFragment(Only when code expired)
                         */
                        mPref.qrSignInResponseCode.value="2"
                    }


                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}