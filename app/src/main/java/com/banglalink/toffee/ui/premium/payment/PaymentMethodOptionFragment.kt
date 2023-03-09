package com.banglalink.toffee.ui.premium.payment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.R.string
import com.banglalink.toffee.databinding.FragmentPaymentMethodOptionBinding
import com.banglalink.toffee.ui.common.ChildDialogFragment
import com.banglalink.toffee.ui.premium.PremiumViewModel

class PaymentMethodOptionFragment : ChildDialogFragment() {
    
    private var _binding: FragmentPaymentMethodOptionBinding? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<PremiumViewModel>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPaymentMethodOptionBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    @SuppressLint("ResourceAsColor")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel.paymentMethod.value?.let { paymentTypes ->
            with(binding) {
                if (mPref.isBanglalinkNumber == "true") {
                    trialTitle.text = paymentTypes.free?.getOrNull(1)?.packDetails.toString()
                } else {
                    trialTitle.text = paymentTypes.free?.getOrNull(0)?.packDetails.toString()
                }
                val extraValidity = paymentTypes.free?.getOrNull(1)?.packDuration?.minus(paymentTypes.free?.getOrNull(0)?.packDuration ?: 0) ?: paymentTypes.free?.getOrNull(0)?.packDuration ?: 0
                trialDetails.isVisible = extraValidity > 0
                trialDetails.text = String.format(getString(string.extra_for_trial_pack_text), extraValidity)
                blPackPrice.text = String.format(getString(R.string.starting_price), paymentTypes.bl?.minimumPrice?.toString())
                bkashPackPrice.text = String.format(getString(R.string.starting_price), paymentTypes.bkash?.minimumPrice.toString())
                
                trialCard.isVisible = !paymentTypes.free.isNullOrEmpty()
                blPackCard.isVisible = paymentTypes.bl != null && (!paymentTypes.bl?.prepaid.isNullOrEmpty() || !paymentTypes.bl?.postpaid.isNullOrEmpty())
                bKashPackCard.isVisible = paymentTypes.bkash != null && (!paymentTypes.bkash?.blPacks.isNullOrEmpty() || !paymentTypes.bkash?.nonBlPacks.isNullOrEmpty())
                
                //Disable Banglalink DataPack Option
                if (mPref.isBanglalinkNumber == "false") {
                    blPackCard.isEnabled = false
                    blPackLayout.alpha = 0.3f
                }
                
                trialCard.setOnClickListener {
                    findNavController().navigate(R.id.activateTrialPackFragment)
                }
                blPackCard.setOnClickListener {
                    findNavController().navigate(R.id.paymentDataPackOptionFragment, bundleOf("paymentName" to "blPack"))
                }
                bKashPackCard.setOnClickListener {
                    findNavController().navigate(R.id.paymentDataPackOptionFragment, bundleOf("paymentName" to "bKash"))
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}