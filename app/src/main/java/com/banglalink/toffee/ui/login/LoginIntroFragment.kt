package com.banglalink.toffee.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.databinding.FragmentLoginIntroBinding
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.ui.common.ChildDialogFragment
import com.banglalink.toffee.ui.premium.PremiumViewModel

class LoginIntroFragment : ChildDialogFragment() {
    private var _binding: FragmentLoginIntroBinding? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<PremiumViewModel>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginIntroBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.signInButton.safeClick({
            findNavController().navigate(R.id.action_loginIntroFragment_to_loginContentFragment)
//            findNavController().navigate(R.id.userInterestFragment)
        })
        
        binding.skipSignIn.safeClick({
            //sends firebase event for users aborting premPack after signing popup.
            if (mPref.signingFromPrem.value != null && mPref.signingFromPrem.value == true){
                ToffeeAnalytics.toffeeLogEvent(
                    ToffeeEvents.PACK_ABORT, bundleOf(
                        "source" to if ( mPref.packSource.value==true)"content_click " else "premium_pack_menu",
                        "pack_ID" to viewModel.selectedPremiumPack.value!!.id,
                        "pack_name" to viewModel.selectedPremiumPack.value!!.packTitle,
                        "reason" to "signin",
                        "action" to "continue without sign in "
                    )
                )
                mPref.signingFromPrem.value=false
            }
            
            closeDialog() 
        })
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}