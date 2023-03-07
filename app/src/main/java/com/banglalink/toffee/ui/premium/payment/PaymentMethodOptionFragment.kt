package com.banglalink.toffee.ui.premium.payment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentPaymentMethodOptionBinding
import com.banglalink.toffee.extension.hide
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
                if (paymentTypes.free != null) {
                    val isBlNumber = if (mPref.isBanglalinkNumber == "true") {
                        trailTitle.text = paymentTypes.free?.get(1)?.packDetails.toString()
                        trailDetails.text = "Includes 15 days extra for Banglalink subscribers"
                    } else {
                        trailTitle.text = paymentTypes.free?.get(0)?.packDetails.toString()
                        trailDetails.text = "Extra 15 days for Banglalink users"
                    }
                } else {
                    trailCard.hide()
                }
                
                if (paymentTypes.bl != null) {
                    blPackPrice.text = "Starting from BDT " + paymentTypes.bl?.minimumPrice?.toString()
                } else {
                    blPackCard.hide()
                }
                if (paymentTypes.bkash != null) {
                    bkashPackPrice.text = "Starting from BDT " + paymentTypes.bkash?.minimumPrice?.toString()
                } else {
                    bkashPackCard.hide()
                }
                
                //Disable Banglalink DataPack Option
                if (mPref.isBanglalinkNumber == "false") {
                    blPackLayout.isEnabled = false
                    blPackCard.isClickable = false
                    blPackCard.isEnabled = false
                    blPackLayout.setAlpha(.3f)
                }
                
                trailCard.setOnClickListener {
                    if (mPref.isBanglalinkNumber == "true") viewModel.selectedPaymentMethod.postValue(paymentTypes.free?.get(1))
                    else viewModel.selectedPaymentMethod.postValue(paymentTypes.free?.get(0))
                    mPref.paymentName.value = "trail"
                    findNavController().navigate(R.id.action_payment_to_trail)
                }
                blPackCard.setOnClickListener {
//                    viewModel.selectedPaymentMethodList.postValue(paymentTypes.bl?.pREPAID)
//                    viewModel.selectedPaymentMethodList.postValue(paymentTypes.bl?.pOSTPAID)
                    // mPref.paymentName.value="blPack"
                    viewModel.selectedPaymentMethod.postValue(paymentTypes.bl?.pREPAID?.get(0))
                    findNavController().navigate(R.id.action_payment_to_pack, bundleOf("paymentName" to "blPack"))
                }
                bkashPackCard.setOnClickListener {
                    viewModel.selectedPaymentMethod.postValue(paymentTypes.bkash?.dataPacks?.get(0))
//                    viewModel.selectedPaymentMethodList.postValue(paymentTypes.bkash?.dataPacks)
                    //mPref.paymentName.value="bKash"
                    findNavController().navigate(R.id.action_payment_to_pack, bundleOf("paymentName" to "bKash"))
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}