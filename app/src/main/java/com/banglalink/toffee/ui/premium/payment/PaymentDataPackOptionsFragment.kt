package com.banglalink.toffee.ui.premium.payment

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
import com.banglalink.toffee.data.network.request.CreatePaymentRequest
import com.banglalink.toffee.data.network.request.DataPackPurchaseRequest
import com.banglalink.toffee.data.network.request.RechargeByBkashRequest
import com.banglalink.toffee.data.network.response.PackPaymentMethod
import com.banglalink.toffee.databinding.FragmentPaymentDataPackOptionsBinding
import com.banglalink.toffee.extension.checkVerification
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
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.premium.PremiumViewModel
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.usecase.PaymentLogFromDeviceData
import com.banglalink.toffee.util.unsafeLazy
import com.google.gson.Gson

class PaymentDataPackOptionsFragment : ChildDialogFragment(), DataPackOptionCallback<PackPaymentMethod> {
    
    private val gson = Gson()
    private var sessionToken = ""
    private var paymentName: String? = null
    private var bKashNumber: String? = null
    private var bKashPaymentId: String? = null
    private lateinit var mAdapter: PaymentDataPackOptionAdapter
    private var _binding: FragmentPaymentDataPackOptionsBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel by activityViewModels<HomeViewModel>()
    private val viewModel by activityViewModels<PremiumViewModel>()
    private val progressDialog by unsafeLazy { ToffeeProgressDialog(requireContext()) }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPaymentDataPackOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hidePaymentOption()

        bKashNumber = if(mPref.phoneNumber.startsWith("+")){
            mPref.phoneNumber.substring(3)
        } else{
            mPref.phoneNumber
        }
        
        paymentName = arguments?.getString("paymentName", "") ?: ""
        prepareDataPackOptions()
        observe(viewModel.selectedDataPackOption) {
            if (it.listTitle == null) {
                mAdapter.setSelectedItem(it)
            }
        }
        
        observeBlDataPackPurchase()
        
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
                purchaseBlDataPack()
            }
        })
        binding.buyWithRecharge.safeClick({
            callAndObserveRechargeByBkash()
        })
        binding.signInButton.safeClick({
            observe(homeViewModel.isLogoutCompleted) {
                if (it) {
                    requireActivity().checkVerification(shouldReloadAfterLogin = false) {
                        observe(viewModel.mnpStatusLiveData) { response ->
                            when (response) {
                                is Success -> {
                                    prepareDataPackOptions(true)
                                    binding.signInButton.hide()
                                    binding.buySimButton.hide()
                                    if (mAdapter.selectedPosition > -1) {
                                        binding.buyNow.show()
                                        binding.buyWithRecharge.show()
//                                        binding.buyWithRecharge.isEnabled = mPref.isPrepaid
//                                        binding.buyWithRecharge.setBackgroundColor(ContextCompat.getColor(requireContext(), if (mPref.isPrepaid) R.color.colorAccent2 else R.color.btnDisableClr))
//                                        binding.buyWithRecharge.setTextColor(ContextCompat.getColor(requireContext(), if (mPref.isPrepaid) R.color.text_color_white else R.color.txtDisableClr))
                                    }
                                }
                                is Failure -> {
                                    requireContext().showToast(response.error.msg)
                                }
                            }
                        }
                        viewModel.getMnpStatus()
                    }
                }
            }
            mPref.shouldIgnoreReloadAfterLogout.value = true
            homeViewModel.logoutUser()
        })
        binding.buySimButton.safeClick({
            val args = Bundle().apply {
                putString("myTitle", "Back to TOFFEE")
                putString("url", "https://eshop.banglalink.net/")
                putBoolean("isHideBackIcon", false)
                putBoolean("isHideCloseIcon", true)
            }
            findNavController().navigate(R.id.htmlPageViewDialog, args)
        })
    }
    
    private fun prepareDataPackOptions(isRestoreSelection: Boolean = false) {
        viewModel.paymentMethod.value?.let { paymentTypes ->
            val packPaymentMethodList = mutableListOf<PackPaymentMethod>()
            val prePaid = paymentTypes.bl?.prepaid
            val postPaid = paymentTypes.bl?.postpaid
            val bKashBlPacks = paymentTypes.bkash?.blPacks
            val bKashNonBlPacks = paymentTypes.bkash?.nonBlPacks
            
            if (paymentName == "bKash") {
                if (mPref.isBanglalinkNumber == "true") {
                    bKashBlPacks?.let { packPaymentMethodList.addAll(it) }
                        ?: requireContext().showToast(getString(string.try_again_message))
                } else {
                    bKashNonBlPacks?.let { packPaymentMethodList.addAll(it) }
                        ?: requireContext().showToast(getString(string.try_again_message))
                }
            } else if (paymentName == "blPack") {
                if (mPref.isBanglalinkNumber == "true") {
                    if (mPref.isPrepaid && !prePaid.isNullOrEmpty()) {
    //                    packPaymentMethodList.add(PackPaymentMethod(listTitle = "Banglalink Prepaid User"))
                        packPaymentMethodList.addAll(prePaid)
                    }
                    if (!mPref.isPrepaid && !postPaid.isNullOrEmpty()) {
    //                    packPaymentMethodList.add(PackPaymentMethod(listTitle = "Banglalink Postpaid User"))
                        packPaymentMethodList.addAll(postPaid)
                    }
                } else {
                    if (!prePaid.isNullOrEmpty()) {
                        packPaymentMethodList.add(PackPaymentMethod(listTitle = "Banglalink Prepaid User"))
                        packPaymentMethodList.addAll(prePaid)
                    }
                    if (!postPaid.isNullOrEmpty()) {
                        packPaymentMethodList.add(PackPaymentMethod(listTitle = "Banglalink Postpaid User"))
                        packPaymentMethodList.addAll(postPaid)
                    }
                }
            }
            
            packPaymentMethodList.let {
                mAdapter = PaymentDataPackOptionAdapter(requireContext(), mPref, this)
                binding.recyclerView.adapter = mAdapter
                if (isRestoreSelection && viewModel.selectedDataPackOption.value != null) {
                    mAdapter.selectedPosition = it.indexOf(viewModel.selectedDataPackOption.value)
                }
                mAdapter.addAll(it.toList())
                mAdapter.notifyDataSetChanged()
            }
        }
    }
    
    private fun purchaseBlDataPack() {
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
            viewModel.purchaseDataPackBlDataPackOptions(dataPackPurchaseRequest)
        } else {
            progressDialog.dismiss()
            requireContext().showToast(getString(R.string.try_again_message))
        }
    }
    
    private fun observeBlDataPackPurchase() {
        observe(viewModel.packPurchaseResponseCodeBlDataPackOptions) {
            progressDialog.dismiss()
            when (it) {
                is Success -> {
                    when (it.data.status) {
                        PaymentStatusDialog.SUCCESS -> {
                            mPref.activePremiumPackList.value = it.data.loginRelatedSubsHistory
                            val args = bundleOf(
                                PaymentStatusDialog.ARG_STATUS_CODE to (it.data.status ?: 200)
                            )
                            findNavController().navigateTo(R.id.paymentStatusDialog, args)
                        }
                        PaymentStatusDialog.UN_SUCCESS ->{
                            val args = bundleOf(
                                PaymentStatusDialog.ARG_STATUS_CODE to 0,
                                PaymentStatusDialog.ARG_STATUS_TITLE to "Data Plan Purchase Failed!",
                                PaymentStatusDialog.ARG_STATUS_MESSAGE to "Due to some technical issue, the data plan activation failed. Please retry."
                            )
                            findNavController().navigateTo(R.id.paymentStatusDialog, args)
                        }
                        PaymentStatusDialog.DataPackPurchaseFailedBalanceInsufficient_ERROR -> {
                            if (!mPref.isPrepaid){
                                val args = bundleOf(
                                    "subTitle" to getString(R.string.insufficient_balance_for_postpaid),
                                    "isBuyWithRechargeHide" to false,
                                )
                                findNavController().navigateTo(R.id.insufficientBalanceFragment, args)
                            }
                            else{
                                findNavController().navigateTo(R.id.insufficientBalanceFragment)
                            }
                        }
                        else -> {
                            val args = bundleOf(
                                PaymentStatusDialog.ARG_STATUS_CODE to (it.data.status?: 0)
                            )
                            findNavController().navigateTo(R.id.paymentStatusDialog, args)
                        }
                    }
                }
                is Failure -> {
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }
    
    private fun grantBkashToken() {
        observe(viewModel.bKashGrandTokenLiveData) { response ->
            when (response) {
                is Success -> {
                    sessionToken = response.data.idToken.toString()
                    viewModel.sendPaymentLogFromDeviceData(PaymentLogFromDeviceData(
                        id = System.currentTimeMillis() + mPref.customerId,
                        callingApiName = "bkash-grant-token",
                        packId = viewModel.selectedPremiumPack.value?.id ?: 0,
                        packTitle = viewModel.selectedPremiumPack.value?.packTitle.toString(),
                        dataPackId = viewModel.selectedDataPackOption.value?.dataPackId ?: 0,
                        dataPackDetails = viewModel.selectedDataPackOption.value?.packDetails.toString(),
                        paymentMethodId = viewModel.selectedDataPackOption.value?.paymentMethodId ?: 0,
                        paymentMsisdn = null,
                        paymentId = null,
                        transactionId = null,
                        transactionStatus = null,
                        amount = viewModel.selectedDataPackOption.value?.packPrice.toString(),
                        merchantInvoiceNumber = mPref.merchantInvoiceNumber,
                        rawResponse = gson.toJson(response.data)
                    ))
                    if (response.data.statusCode != "0000") {
                        progressDialog.dismiss()
                        requireContext().showToast(response.data.statusMessage)
                        return@observe
                    }
                    createBkashPayment()
                }
                is Failure -> {
                    viewModel.sendPaymentLogFromDeviceData(PaymentLogFromDeviceData(
                        id = System.currentTimeMillis() + mPref.customerId,
                        callingApiName = "bkash-grant-token",
                        packId = viewModel.selectedPremiumPack.value?.id ?: 0,
                        packTitle = viewModel.selectedPremiumPack.value?.packTitle.toString(),
                        dataPackId = viewModel.selectedDataPackOption.value?.dataPackId ?: 0,
                        dataPackDetails = viewModel.selectedDataPackOption.value?.packDetails.toString(),
                        paymentMethodId = viewModel.selectedDataPackOption.value?.paymentMethodId ?: 0,
                        paymentMsisdn = null,
                        paymentId = null,
                        transactionId = null,
                        transactionStatus = null,
                        amount = viewModel.selectedDataPackOption.value?.packPrice.toString(),
                        merchantInvoiceNumber = mPref.merchantInvoiceNumber,
                        rawResponse = gson.toJson(response.error.msg)
                    ))
                    requireContext().showToast(response.error.msg)
                    progressDialog.dismiss()
                }
            }
        }
        viewModel.bKashGrandToken()
    }
    
    private fun createBkashPayment() {
        val amount = viewModel.selectedDataPackOption.value?.packPrice.toString()
        observe(viewModel.bKashCreatePaymentLiveData) { response ->
            progressDialog.dismiss()
            when (response) {
                is Success -> {
                    viewModel.sendPaymentLogFromDeviceData(PaymentLogFromDeviceData(
                        id = System.currentTimeMillis() + mPref.customerId,
                        callingApiName = "bkash-create-payment",
                        packId = viewModel.selectedPremiumPack.value?.id ?: 0,
                        packTitle = viewModel.selectedPremiumPack.value?.packTitle.toString(),
                        dataPackId = viewModel.selectedDataPackOption.value?.dataPackId ?: 0,
                        dataPackDetails = viewModel.selectedDataPackOption.value?.packDetails.toString(),
                        paymentMethodId = viewModel.selectedDataPackOption.value?.paymentMethodId ?: 0,
                        paymentMsisdn = null,
                        paymentId = response.data.paymentId,
                        transactionId = null,
                        transactionStatus = null,
                        amount = viewModel.selectedDataPackOption.value?.packPrice.toString(),
                        merchantInvoiceNumber = mPref.merchantInvoiceNumber,
                        rawResponse = gson.toJson(response.data)
                    ))
                    if (response.data.statusCode != "0000") {
                        requireContext().showToast(response.data.statusMessage)
                        return@observe
                    }
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
                    viewModel.sendPaymentLogFromDeviceData(PaymentLogFromDeviceData(
                        id = System.currentTimeMillis() + mPref.customerId,
                        callingApiName = "bkash-create-payment",
                        packId = viewModel.selectedPremiumPack.value?.id ?: 0,
                        packTitle = viewModel.selectedPremiumPack.value?.packTitle.toString(),
                        dataPackId = viewModel.selectedDataPackOption.value?.dataPackId ?: 0,
                        dataPackDetails = viewModel.selectedDataPackOption.value?.packDetails.toString(),
                        paymentMethodId = viewModel.selectedDataPackOption.value?.paymentMethodId ?: 0,
                        paymentMsisdn = null,
                        paymentId = null,
                        transactionId = null,
                        transactionStatus = null,
                        amount = viewModel.selectedDataPackOption.value?.packPrice.toString(),
                        merchantInvoiceNumber = mPref.merchantInvoiceNumber,
                        rawResponse = gson.toJson(response.error.msg)
                    ))
                    requireContext().showToast(response.error.msg)
                }
            }
        }
        viewModel.bKashCreatePayment(
            sessionToken, CreatePaymentRequest(
                mode = "0011",
                payerReference = bKashNumber,
                callbackURL = mPref.bkashCallbackUrl,
                merchantAssociationInfo = "",
                amount = amount,
                currency = "BDT",
                intent = "sale",
                merchantInvoiceNumber = mPref.merchantInvoiceNumber,
            )
        )
    }
    
    private fun callAndObserveRechargeByBkash() {
        if (viewModel.selectedPremiumPack.value != null && viewModel.selectedDataPackOption.value != null) {
            progressDialog.show()
            observe(viewModel.rechargeByBkashUrlLiveData) { it ->
                progressDialog.dismiss()
                when(it) {
                    is Success -> {
                        viewModel.sendPaymentLogFromDeviceData(PaymentLogFromDeviceData(
                            id = System.currentTimeMillis() + mPref.customerId,
                            callingApiName = "rechargeInitialized",
                            packId = viewModel.selectedPremiumPack.value?.id ?: 0,
                            packTitle = viewModel.selectedPremiumPack.value?.packTitle.toString(),
                            dataPackId = viewModel.selectedDataPackOption.value?.dataPackId ?: 0,
                            dataPackDetails = viewModel.selectedDataPackOption.value?.packDetails.toString(),
                            paymentMethodId = viewModel.selectedDataPackOption.value?.paymentMethodId ?: 0,
                            paymentMsisdn = null,
                            paymentId = null,
                            transactionId = null,
                            transactionStatus = null,
                            amount = viewModel.selectedDataPackOption.value?.packPrice.toString(),
                            merchantInvoiceNumber = mPref.merchantInvoiceNumber,
                            rawResponse = gson.toJson(it.data)
                        ))
                        it.data?.let {
                            if (it.statusCode != 200) {
                                requireContext().showToast(getString(R.string.try_again_message))
                                return@observe
                            }
                            val args = bundleOf(
                                "myTitle" to "Pack Details",
                                "url" to it.data?.bKashWebUrl.toString(),
                                "isHideBackIcon" to false,
                                "isHideCloseIcon" to true,
                                "isBkashBlRecharge" to true,
                            )
                            findNavController().navigateTo(R.id.paymentWebViewDialog, args)
                        } ?: requireContext().showToast(getString(R.string.try_again_message))
                    }
                    is Failure -> {
                        viewModel.sendPaymentLogFromDeviceData(PaymentLogFromDeviceData(
                            id = System.currentTimeMillis() + mPref.customerId,
                            callingApiName = "rechargeInitialized",
                            packId = viewModel.selectedPremiumPack.value?.id ?: 0,
                            packTitle = viewModel.selectedPremiumPack.value?.packTitle.toString(),
                            dataPackId = viewModel.selectedDataPackOption.value?.dataPackId ?: 0,
                            dataPackDetails = viewModel.selectedDataPackOption.value?.packDetails.toString(),
                            paymentMethodId = viewModel.selectedDataPackOption.value?.paymentMethodId ?: 0,
                            paymentMsisdn = null,
                            paymentId = null,
                            transactionId = null,
                            transactionStatus = null,
                            amount = viewModel.selectedDataPackOption.value?.packPrice.toString(),
                            merchantInvoiceNumber = mPref.merchantInvoiceNumber,
                            rawResponse = gson.toJson(it.error.msg)
                        ))
                        requireContext().showToast(it.error.msg)
                    }
                }
            }
            val selectedPremiumPack = viewModel.selectedPremiumPack.value
            val selectedDataPackOption = viewModel.selectedDataPackOption.value
            
            val request = RechargeByBkashRequest(
                customerId = mPref.customerId,
                password = mPref.password,
                paymentMethodId = selectedDataPackOption?.paymentMethodId ?: 0,
                packId = selectedPremiumPack?.id ?: 0,
                packTitle = selectedPremiumPack?.packTitle,
                dataPackId = selectedDataPackOption?.dataPackId ?: 0,
                dataPackCode = selectedDataPackOption?.packCode,
                dataPackDetail = selectedDataPackOption?.packDetails,
                dataPackPrice = selectedDataPackOption?.packPrice ?: 0,
                isPrepaid = selectedDataPackOption?.isPrepaid ?: 1
            )
            viewModel.getRechargeByBkashUrl(request)
        }
    }
    
    override fun onItemClicked(view: View, item: PackPaymentMethod, position: Int) {
        super.onItemClicked(view, item, position)
        showPaymentOption()
        mAdapter.selectedPosition = position
        viewModel.selectedDataPackOption.value = item
//        val isRechargeAvailable = viewModel.paymentMethod.value?.bl?.prepaid?.any { it.dataPackId == item.dataPackId } ?: false
//        binding.buyWithRecharge.isEnabled = mPref.isPrepaid
        
//        binding.buyWithRecharge.setBackgroundColor(ContextCompat.getColor(requireContext(), if (mPref.isPrepaid) R.color.colorAccent2
//        else R.color.btnDisableClr))
//        binding.buyWithRecharge.setTextColor(ContextCompat.getColor(requireContext(), if (mPref.isPrepaid) R.color.text_color_white
//        else R.color.txtDisableClr))
    }
    
    private fun showPaymentOption() {
        binding.recyclerView.setPadding(0, 0, 0, 8)
        binding.termsAndConditionsOne.show()
        binding.termsAndConditionsTwo.show()
        if (paymentName == "bKash"){
            binding.buyNow.text = getString(R.string.buy_bkash)
            binding.buyNow.show()
        }
        else if(paymentName == "blPack"){
            binding.buyNow.show()
            binding.buyWithRecharge.show()
            binding.buyNow.isVisible = mPref.isBanglalinkNumber == "true"
            binding.buyWithRecharge.isVisible = mPref.isBanglalinkNumber == "true"
            binding.signInButton.isVisible = mPref.isBanglalinkNumber == "false"
            binding.buySimButton.isVisible = mPref.isBanglalinkNumber == "false"
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
        progressDialog.dismiss()
        _binding = null
    }
}