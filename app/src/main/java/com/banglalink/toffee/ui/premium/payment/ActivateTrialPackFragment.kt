package com.banglalink.toffee.ui.premium.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentActivateTrialPackBinding
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.ui.common.ChildDialogFragment
import com.banglalink.toffee.ui.premium.PremiumViewModel
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.util.unsafeLazy

class ActivateTrialPackFragment : ChildDialogFragment() {
    
    private var _binding: FragmentActivateTrialPackBinding? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<PremiumViewModel>()
    private val progressDialog by unsafeLazy { ToffeeProgressDialog(requireContext()) }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentActivateTrialPackBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val selectedTrialPack = viewModel.paymentMethod.value?.let {
            if (mPref.isBanglalinkNumber == "true") {
                it.free?.getOrNull(1) ?: it.free?.getOrNull(0)
            } else {
                it.free?.getOrNull(0)
            }
        }
        binding.trialValidity.text = String.format(getString(R.string.trial_validity_text), selectedTrialPack?.packDuration ?: 0)
        binding.enableNow.safeClick({
        
        })
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