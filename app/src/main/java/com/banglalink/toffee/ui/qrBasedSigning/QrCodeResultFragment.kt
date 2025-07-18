package com.banglalink.toffee.ui.qrBasedSigning

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentQrCodeReasultBinding
import com.banglalink.toffee.extension.navigatePopUpTo
import com.banglalink.toffee.ui.common.BaseFragment

class QrCodeResultFragment : BaseFragment() {
    
    private val binding get() = _binding!!
    private var _binding: FragmentQrCodeReasultBinding? = null
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentQrCodeReasultBinding.inflate(layoutInflater)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = "Sign into TV"
        val toolbar = activity?.findViewById<Toolbar>(R.id.toolbar)
        toolbar?.setNavigationIcon(R.drawable.ic_arrow_back)
        
        if (mPref.qrSignInResponseCode.value != null && mPref.qrSignInResponseCode.value.equals("1")) {
            binding.qrSignInActivatedView.visibility = View.GONE
            binding.qrCodeExpiredView.visibility = View.GONE
            
            binding.qrSignInActivatedView.visibility = View.VISIBLE
            mPref.qrSignInStatus.value = null
        } else if (mPref.qrSignInResponseCode.value != null && (mPref.qrSignInResponseCode.value.equals("2") || mPref.qrSignInResponseCode.value.equals("0"))) {
            binding.qrSignInActivatedView.visibility = View.GONE
            binding.qrCodeExpiredView.visibility = View.VISIBLE
        } else { }
        
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isEnabled) {

                    findNavController().navigatePopUpTo(
                        resId = R.id.Qr_code_res,
                        popUpTo = R.id.menu_active_tv,
                        inclusive = true
                    )
                    
                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        })

        toolbar?.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}