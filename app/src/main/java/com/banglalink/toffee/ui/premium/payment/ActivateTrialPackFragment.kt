package com.banglalink.toffee.ui.premium.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.data.network.request.DataPackPurchaseRequest
import com.banglalink.toffee.databinding.FragmentActivateTrialPackBinding
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.common.ChildDialogFragment
import com.banglalink.toffee.ui.premium.PremiumViewModel
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.util.Utils
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
        binding.trialValidity.text = String.format(getString(R.string.trial_validity_text), viewModel.selectedDataPackOption.value?.packDuration ?: 0)
        binding.enableNow.safeClick({
            progressDialog.show()
            purchaseDataPack()
        })
        binding.backImg.safeClick({ findNavController().navigateTo(R.id.paymentMethodOptions) })
        binding.termsAndConditionsTwo.safeClick({ showTermsAndConditionDialog() })
        
        observeDataPackPurchase()
    }
    
    private fun purchaseDataPack() {
        if (viewModel.selectedPremiumPack.value != null && viewModel.selectedDataPackOption.value != null) {
            val selectedPremiumPack = viewModel.selectedPremiumPack.value!!
            val selectedDataPack = viewModel.selectedDataPackOption.value!!
            
            val dataPackPurchaseRequest = DataPackPurchaseRequest(
                customerId = mPref.customerId,
                password = mPref.password,
                isBanglalinkNumber = (mPref.isBanglalinkNumber == "true").toInt(),
                packId = selectedPremiumPack.id,
                paymentMethodId = selectedDataPack.paymentMethodId ?: 0,
                packTitle = selectedPremiumPack.packTitle,
                contentList = selectedPremiumPack.contentId,
                packCode = selectedDataPack.packCode,
                packDetails = selectedDataPack.packDetails,
                packPrice = selectedDataPack.packPrice,
                packDuration = selectedDataPack.packDuration
            )
            viewModel.purchaseDataPackTrialPack(dataPackPurchaseRequest)
        }
    }
    
    private fun observeDataPackPurchase() {
        observe(viewModel.packPurchaseResponseCodeTrialPack) {
            progressDialog.dismiss()
            when (it) {
                is Success -> {
                    if (it.data.status == PaymentStatusDialog.SUCCESS) {
                        mPref.activePremiumPackList.value = it.data.loginRelatedSubsHistory?.distinctBy { it.isActive && mPref.getSystemTime().before(Utils.getDate(it.expiryDate)) }
                    }
                    val args = bundleOf(
                        PaymentStatusDialog.ARG_STATUS_CODE to (it.data.status ?: 0)
                    )
                    findNavController().navigateTo(R.id.paymentStatusDialog, args)
                }
                is Failure -> {
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }
    
    private fun showTermsAndConditionDialog() {
        findNavController().navigateTo(
            resId = R.id.htmlPageViewDialog,
            args = bundleOf(
                "myTitle" to getString(R.string.terms_and_conditions),
                "url" to mPref.blDataPackTermsAndConditionsUrl
            )
        )
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        progressDialog.dismiss()
        _binding = null
    }
}