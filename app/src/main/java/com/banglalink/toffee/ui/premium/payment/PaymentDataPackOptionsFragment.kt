package com.banglalink.toffee.ui.premium.payment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.Constants
import com.banglalink.toffee.R
import com.banglalink.toffee.data.network.request.CreatePaymentRequest
import com.banglalink.toffee.data.network.request.DataPackPurchaseRequest
import com.banglalink.toffee.data.network.response.PackPaymentMethod
import com.banglalink.toffee.databinding.FragmentPaymentDataPackOptionsBinding
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.navigateTo
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.extension.toInt
import com.banglalink.toffee.listeners.DataPackOptionCallback
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.common.ChildDialogFragment
import com.banglalink.toffee.ui.premium.PremiumViewModel
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.util.unsafeLazy

class PaymentDataPackOptionsFragment : ChildDialogFragment(), DataPackOptionCallback<PackPaymentMethod> {
    
    private var sessionToken = ""
    private var paymentName: String? = null
    private var bKashPaymentId: String? = null
    private lateinit var mAdapter: PaymentDataPackOptionAdapter
    private var _binding: FragmentPaymentDataPackOptionsBinding? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<PremiumViewModel>()
    private val progressDialog by unsafeLazy { ToffeeProgressDialog(requireContext()) }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPaymentDataPackOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hidePaymentOption()
        viewModel.selectedDataPackOption.value = null
        paymentName = arguments?.getString("paymentName", "") ?: ""
        viewModel.paymentMethod.value?.let { paymentTypes ->
            val packPaymentMethodList = mutableListOf<PackPaymentMethod>()
            val prePaid = paymentTypes.bl?.prepaid
            val postPaid = paymentTypes.bl?.postpaid
            val bKashBlPacks = paymentTypes.bkash?.blPacks
            val bKashNonBlPacks = paymentTypes.bkash?.nonBlPacks
            
            if (paymentName == "bKash") {
                packPaymentMethodList.clear()
                if (!bKashBlPacks.isNullOrEmpty()) {
                    packPaymentMethodList.add(PackPaymentMethod(listTitle = "Banglalink Packs", isDisabled = mPref.isBanglalinkNumber == "false"))
                    packPaymentMethodList.addAll(bKashBlPacks.map { 
                        it.isDisabled = mPref.isBanglalinkNumber == "false"
                        it
                    })
                }
                if (!bKashNonBlPacks.isNullOrEmpty()) {
                    packPaymentMethodList.add(PackPaymentMethod(listTitle = "Other Operator Packs"))
                    packPaymentMethodList.addAll(bKashNonBlPacks)
                }
            } else if (paymentName == "blPack") {
                packPaymentMethodList.clear()
                if (!prePaid.isNullOrEmpty()) {
                    packPaymentMethodList.add(PackPaymentMethod(listTitle = "Banglalink Prepaid Packs"))
                    packPaymentMethodList.addAll(prePaid)
                }
                if (!postPaid.isNullOrEmpty()) {
                    packPaymentMethodList.add(PackPaymentMethod(listTitle = "Banglalink PostPaid Packs"))
                    packPaymentMethodList.addAll(postPaid)
                }
            }
            
            packPaymentMethodList.let {
                mAdapter = PaymentDataPackOptionAdapter(requireContext(), mPref, this)
                binding.recyclerView.adapter = mAdapter
                mAdapter.addAll(it.toList())
            }
        }
        observe(viewModel.selectedDataPackOption) {
            if (it.listTitle == null) {
                mAdapter.setSelectedItem(it)
                showPaymentOption()
            }
        }
        
        binding.recyclerView.setPadding(0, 0, 0, 24)
        
        binding.backImg.safeClick({
            findNavController().popBackStack()
        })
        binding.termsAndConditionsTwo.safeClick({
            showTermsAndConditionDialog()
        })
        binding.buyNow.safeClick({
            progressDialog.show()
            if (paymentName == "bKash") {
                grantBkashToken()
            } else if (paymentName == "blPack") {
                callAndObserveDataPackPurchase()
            }
        })
    }
    
    private fun callAndObserveDataPackPurchase() {
        if (viewModel.selectedPremiumPack.value != null && viewModel.selectedDataPackOption.value != null) {
            observe(viewModel.packPurchaseResponseCode) {
                progressDialog.hide()
                when (it) {
                    is Success -> {
                        if (it.data.status == PaymentStatusDialog.SUCCESS) {
                            mPref.activePremiumPackList.value = it.data.loginRelatedSubsHistory
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
            viewModel.purchaseDataPack(dataPackPurchaseRequest)
        }
    }
    
    private fun grantBkashToken() {
        observe(viewModel.bKashGrandTokenLiveData) { response ->
            when (response) {
                is Success -> {
                    sessionToken = response.data.idToken.toString()
                    if (response.data.statusCode != "0000") {
                        progressDialog.hide()
                        requireContext().showToast(response.data.statusMessage)
                        return@observe
                    }
                    createBkashPayment()
                }
                is Failure -> {
                    requireContext().showToast(response.error.msg)
                    progressDialog.hide()
                }
            }
        }
        viewModel.bKashGrandToken()
    }
    
    private fun createBkashPayment() {
        val callBackUrl = "${mPref.bkashCallbackUrl}${viewModel.selectedPremiumPack.value?.id}/${viewModel.selectedDataPackOption.value?.dataPackId}/${mPref.customerId}/${mPref.password}/${mPref.phoneNumber}/${mPref.isBanglalinkNumber}/${Constants.DEVICE_TYPE}/${cPref.deviceId}/${mPref.netType}/${"android_" + Build.VERSION.RELEASE}/${cPref.appVersionName}/${cPref.appTheme}"
        val amount = viewModel.selectedDataPackOption.value?.packPrice.toString()
        
        observe(viewModel.bKashCreatePaymentLiveData) { response ->
            when (response) {
                is Success -> {
                    if (response.data.statusCode != "0000") {
                        progressDialog.hide()
                        requireContext().showToast(response.data.statusMessage)
                        return@observe
                    }
                    progressDialog.hide()
                    bKashPaymentId = response.data.paymentId
                    val args = bundleOf(
                        "myTitle" to "Pack Details",
                        "token" to sessionToken,
                        "paymentId" to bKashPaymentId,
                        "url" to response.data.bKashUrl,
                        "isHideBackIcon" to false,
                        "isHideCloseIcon" to true
                    )
                    findNavController().navigateTo(R.id.paymentWebViewDialog, args)
                }
                is Failure -> {
                    requireContext().showToast(response.error.msg)
                    progressDialog.hide()
                }
            }
        }
        viewModel.bKashCreatePayment(
            sessionToken, CreatePaymentRequest(
                mode = "0011",
                payerReference = "01770618575",
                callbackURL = callBackUrl,
                merchantAssociationInfo = "MI05MID54RF09123456One",
                amount = amount,
                currency = "BDT",
                intent = "sale",
                merchantInvoiceNumber = mPref.merchantInvoiceNumber,
            )
        )
    }
    
    override fun onItemClicked(view: View, item: PackPaymentMethod, position: Int) {
        super.onItemClicked(view, item, position) 
        mAdapter.selectedPosition = position
        viewModel.selectedDataPackOption.value = item
    }
    
    private fun showPaymentOption() {
        binding.recyclerView.setPadding(0, 0, 0, 8)
        binding.termsAndConditionsOne.show()
        binding.termsAndConditionsTwo.show()
        if (paymentName == "bKash"){
            binding.buyNow.text = getString(R.string.buy_now)
            binding.buyNow.show()
        }
        else if(paymentName == "blPack"){
            binding.buyNow.show()
            binding.buyWithRecharge.show()
        }
    }
    
    private fun hidePaymentOption() {
        binding.recyclerView.setPadding(0, 0, 0, 24)
        binding.termsAndConditionsOne.hide()
        binding.termsAndConditionsTwo.hide()
        binding.buyNow.hide()
        binding.buyWithRecharge.hide()
    }
    
    private fun showTermsAndConditionDialog() {
        val args = bundleOf(
            "myTitle" to getString(R.string.terms_and_conditions),
            "url" to if (paymentName == "blPack") mPref.blDataPackTermsAndConditionsUrl else mPref.bkashDataPackTermsAndConditionsUrl
        )
        findNavController().navigateTo(
            resId = R.id.htmlPageViewDialog,
            args = args
        )
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        progressDialog.hide()
        _binding = null
    }
}