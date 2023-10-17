package com.banglalink.toffee.ui.premium.payment

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.FirebaseParams
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.data.network.request.BaseRequest
import com.banglalink.toffee.data.network.request.DataPackPurchaseRequest
import com.banglalink.toffee.data.storage.CommonPreference
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

        if (viewModel.selectedDataPackOption.value?.packDuration==1)  binding.trialValidity.text = String.format(getString(R.string.single_day_trial_validity_text), viewModel.selectedDataPackOption.value?.packDuration ?: 0)
        else binding.trialValidity.text = String.format(getString(R.string.trial_validity_text), viewModel.selectedDataPackOption.value?.packDuration ?: 0)

        binding.enableNow.safeClick({
            // Send Log to FirebaseAnalytics
            ToffeeAnalytics.toffeeLogEvent(
                ToffeeEvents.BEGIN_PURCHASE,
                bundleOf(
                    "source" to if (mPref.clickedFromChannelItem.value == true) "content_click" else "premium_pack_menu",
                    "pack_ID" to viewModel.selectedPremiumPack.value?.id.toString(),
                    "pack_name" to viewModel.selectedPremiumPack.value?.packTitle.toString(),
                    "currency" to "BDT",
                    "amount" to viewModel.selectedDataPackOption.value?.packPrice.toString(),
                    "validity" to viewModel.selectedDataPackOption.value?.packDuration.toString(),
                    "provider" to "Trial",
                    "type" to "trial",
                    "subtype" to null,
                    "MNO" to if ((mPref.isBanglalinkNumber).toBoolean()) "BL" else "non-BL",
                    "discount" to null,
                )
            )
            progressDialog.show()
            activateTrialPack()
        })
        binding.backImg.safeClick({
            viewModel.clickableAdInventories.value?.let {
                this.closeDialog()
                viewModel.clickableAdInventories.value = null
            } ?: run {
                findNavController().navigateTo(R.id.paymentMethodOptions)
            }
        })
        binding.termsAndConditionsTwo.safeClick({ showTermsAndConditionDialog() })
        
        observeActivateTrialPack()
    }
    
    private fun activateTrialPack() {
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
                packDuration = selectedDataPack.packDuration,
                isPrepaid = if (mPref.isPrepaid==true) 1 else 0

            )
            viewModel.purchaseDataPackTrialPack(dataPackPurchaseRequest)
        }
    }
    
    private fun observeActivateTrialPack() {
        observe(viewModel.packPurchaseResponseCodeTrialPack) {
            progressDialog.dismiss()
            when (it) {
                is Success -> {
                    if (it.data.status == PaymentStatusDialog.SUCCESS) {
                        // Send Log to FirebaseAnalytics
                        ToffeeAnalytics.toffeeLogEvent(
                            ToffeeEvents.PACK_SUCCESS,
                            bundleOf(
                                "pack_ID" to viewModel.selectedPremiumPack.value?.id.toString(),
                                "pack_name" to viewModel.selectedPremiumPack.value?.packTitle.toString(),
                                "currency" to "BDT",
                                "amount" to viewModel.selectedDataPackOption.value?.packPrice.toString(),
                                "validity" to viewModel.selectedDataPackOption.value?.packDuration.toString(),
                                "provider" to "Trial",
                                "type" to "trial",
                                "reason" to "N/A",
                                "MNO" to if ((mPref.isBanglalinkNumber).toBoolean()) "BL" else "non-BL",
                            )
                        )
                        mPref.activePremiumPackList.value = it.data.loginRelatedSubsHistory
                        val args = bundleOf(
                            PaymentStatusDialog.ARG_STATUS_CODE to (it.data.status ?: 0)
                        )
                        val selectedPremiumPac = viewModel.selectedPremiumPack.value!!
                        val selectedDataPac = viewModel.selectedDataPackOption.value!!

                        findNavController().navigateTo(R.id.paymentStatusDialog, args)
                    }
                    else if (it.data.status == PaymentStatusDialog.UN_SUCCESS){
                        // Send Log to FirebaseAnalytics
                        ToffeeAnalytics.toffeeLogEvent(
                            ToffeeEvents.PACK_ERROR,
                            bundleOf(
                                "pack_ID" to viewModel.selectedPremiumPack.value?.id.toString(),
                                "pack_name" to viewModel.selectedPremiumPack.value?.packTitle.toString(),
                                "currency" to "BDT",
                                "amount" to viewModel.selectedDataPackOption.value?.packPrice.toString(),
                                "validity" to viewModel.selectedDataPackOption.value?.packDuration.toString(),
                                "provider" to "Trial",
                                "type" to "trial",
                                "reason" to "Due to some technical error, the trial plan activation failed. Please retry.",
                                "MNO" to if ((mPref.isBanglalinkNumber).toBoolean()) "BL" else "non-BL",
                            )
                        )
                        val args = bundleOf(
                            PaymentStatusDialog.ARG_STATUS_CODE to (it.data.status ?: 0),
                            PaymentStatusDialog.ARG_STATUS_TITLE to "Trial Plan Activation Failed!",
                            PaymentStatusDialog.ARG_STATUS_MESSAGE to "Due to some technical error, the trial plan activation failed. Please retry."
                        )
                        findNavController().navigateTo(R.id.paymentStatusDialog, args)
                    }
                }
                is Failure -> {
                    // Send Log to FirebaseAnalytics
                    ToffeeAnalytics.toffeeLogEvent(
                        ToffeeEvents.PACK_ERROR,
                        bundleOf(
                            "pack_ID" to viewModel.selectedPremiumPack.value?.id.toString(),
                            "pack_name" to viewModel.selectedPremiumPack.value?.packTitle.toString(),
                            "currency" to "BDT",
                            "amount" to viewModel.selectedDataPackOption.value?.packPrice.toString(),
                            "validity" to viewModel.selectedDataPackOption.value?.packDuration.toString(),
                            "provider" to "Trial",
                            "type" to "trial",
                            "reason" to it.error.msg,
                            "MNO" to if ((mPref.isBanglalinkNumber).toBoolean()) "BL" else "non-BL",
                        )
                    )
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