package com.banglalink.toffee.ui.premium.payment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.R.string
import com.banglalink.toffee.data.network.response.PackPaymentMethod
import com.banglalink.toffee.databinding.FragmentPaymentMethodOptionsBinding
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.navigateTo
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.ui.common.ChildDialogFragment
import com.banglalink.toffee.ui.premium.PremiumViewModel

class PaymentMethodOptionsFragment : ChildDialogFragment() {
    
    private var _binding: FragmentPaymentMethodOptionsBinding? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<PremiumViewModel>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPaymentMethodOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    @SuppressLint("ResourceAsColor")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.selectedDataPackOption.value = null
        viewModel.paymentMethod.value?.let { paymentTypes ->
            with(binding) {
                if (!paymentTypes.free.isNullOrEmpty()) {
                    var blTrialPackMethod: PackPaymentMethod? = null
                    var nonBlTrialPackMethod: PackPaymentMethod? = null
                    
                    paymentTypes.free?.forEach {
                        if (it.isNonBlFree == 1) {
                            nonBlTrialPackMethod = it
                        } else {
                            blTrialPackMethod = it
                        }
                    }
                    
                    var extraValidity = blTrialPackMethod?.packDuration?.minus(nonBlTrialPackMethod?.packDuration ?: 0) ?: 0
                    
                    if (extraValidity == blTrialPackMethod?.packDuration) {
                        extraValidity = 0
                    }
                    trialDetails.isVisible = extraValidity > 0
                    
                    if (blTrialPackMethod != null && mPref.isBanglalinkNumber == "true") {
                        viewModel.selectedDataPackOption.value = blTrialPackMethod
                        trialTitle.text = blTrialPackMethod!!.packDetails.toString()
                        trialDetails.text = String.format(getString(string.extra_for_bl_users_text), extraValidity)

                    } else if (nonBlTrialPackMethod != null && mPref.isBanglalinkNumber == "false") {
                        viewModel.selectedDataPackOption.value = nonBlTrialPackMethod
                        trialTitle.text = nonBlTrialPackMethod?.packDetails.toString()
                        trialDetails.text = String.format(getString(string.extra_for_non_bl_users_text), extraValidity)
                        trialDetails.setTextColor(ContextCompat.getColor(requireContext(), R.color.trial_extra_text_color))


                    }else if (blTrialPackMethod != null){
                        viewModel.selectedDataPackOption.value = blTrialPackMethod
                        trialTitle.text = blTrialPackMethod!!.packDetails.toString()
                        trialDetails.text = String.format(getString(string.extra_for_bl_users_text), extraValidity)
                        trialCard.alpha = 0.3f
                    }
                    else if (nonBlTrialPackMethod != null){
                        viewModel.selectedDataPackOption.value = nonBlTrialPackMethod
                        trialTitle.text = nonBlTrialPackMethod?.packDetails.toString()
                        trialDetails.text = String.format(getString(string.extra_for_non_bl_users_text), extraValidity)
                        trialDetails.setTextColor(ContextCompat.getColor(requireContext(), R.color.trial_extra_text_color))
                        trialCard.alpha = 0.3f
                    }


                    var isTrialPackUsed = false
                    mPref.activePremiumPackList.value?.find {
                        it.packId == viewModel.selectedPremiumPack.value?.id && it.isTrialPackUsed
                    }?.let { isTrialPackUsed = true }
                    
                    if (isTrialPackUsed || (mPref.isBanglalinkNumber != "true" && nonBlTrialPackMethod == null)) {
                        trialCard.alpha = 0.3f
                    }
                    
                    trialCard.safeClick({
                        if (isTrialPackUsed) {
                            requireContext().showToast(getString(string.trial_already_availed_text))
                        } else if (mPref.isBanglalinkNumber != "true" && nonBlTrialPackMethod == null) {
                            requireContext().showToast(getString(string.only_for_bl_users))
                        }
                        else if (mPref.isBanglalinkNumber != "false" && blTrialPackMethod == null) {
                            requireContext().showToast(getString(string.only_for_non_bl_users))
                        }

                        else {
                            findNavController().navigateTo(R.id.activateTrialPackFragment)
                        }
                    })
                } else {
                    trialCard.hide()
                }
                
                blPackPrice.text = String.format(getString(R.string.starting_price), paymentTypes.bl?.minimumPrice?.toString())
                
//                bkashPackPrice.text = String.format(getString(R.string.starting_price), paymentTypes.bkash?.minimumPrice.toString())
                val startingPrice = if (mPref.isBanglalinkNumber == "true") {
                    paymentTypes.bkash?.blPacks?.minOfOrNull { it.packPrice ?: 0 } ?: 0
                } else {
                    paymentTypes.bkash?.nonBlPacks?.minOfOrNull { it.packPrice ?: 0 } ?: 0
                }
                bkashPackPrice.text = String.format(getString(R.string.starting_price), startingPrice)
                bkashPackPrice.isVisible = startingPrice > 0
                
                blPackCard.isVisible = paymentTypes.bl != null && (!paymentTypes.bl?.prepaid.isNullOrEmpty() || !paymentTypes.bl?.postpaid.isNullOrEmpty())
                
                val isBkashAvailable = paymentTypes.bkash != null && (mPref.isBanglalinkNumber == "true" && !paymentTypes.bkash?.blPacks.isNullOrEmpty()) || (mPref.isBanglalinkNumber == "false" && !paymentTypes.bkash?.nonBlPacks.isNullOrEmpty())
                
                bKashPackCard.isVisible = isBkashAvailable
                
                //Disable Banglalink DataPack Option
                if (mPref.isBanglalinkNumber == "false") {
                    blPackCard.alpha = 0.3f
                }
                
                blPackCard.safeClick({
                    if (mPref.isBanglalinkNumber == "false") {
                        requireContext().showToast(getString(string.only_for_bl_users))
                    } else {
                        findNavController().navigateTo(R.id.paymentDataPackOptionsFragment, bundleOf("paymentName" to "blPack"))
                    }
                })
                bKashPackCard.safeClick({
                    findNavController().navigateTo(R.id.paymentDataPackOptionsFragment, bundleOf("paymentName" to "bKash"))
                })
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}