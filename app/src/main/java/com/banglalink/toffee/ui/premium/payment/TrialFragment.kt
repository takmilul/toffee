package com.banglalink.toffee.ui.premium.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.ButtomSheetEnableTrialBinding
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.ui.common.ChildDialogFragment

class TrialFragment : ChildDialogFragment() {
    
    private var _binding: ButtomSheetEnableTrialBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ButtomSheetEnableTrialBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.enableNow.safeClick({ binding.progressBar.show() })
        binding.backImg.safeClick({ findNavController().navigate(R.id.paymentPackages) })
        binding.termsAndConditionsTwo.safeClick({ showTermsAndConditionDialog() })
    }
    
    private fun showTermsAndConditionDialog() {
        findNavController().navigate(
            R.id.htmlPageViewDialog, bundleOf("myTitle" to getString(R.string.terms_and_conditions), "url" to mPref.termsAndConditionUrl)
        )
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}