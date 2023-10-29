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
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.data.network.request.DataPackPurchaseRequest
import com.banglalink.toffee.data.network.request.RechargeByBkashRequest
import com.banglalink.toffee.data.network.request.SubscriberPaymentInitRequest
import com.banglalink.toffee.data.network.response.PackPaymentMethod
import com.banglalink.toffee.databinding.FragmentPaymentDataPackOptionsBinding
import com.banglalink.toffee.extension.checkVerification
import com.banglalink.toffee.extension.getPurchasedPack
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
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.unsafeLazy
import com.google.gson.Gson

class PaymentDataPackOptionsFragment : ChildDialogFragment(), DataPackOptionCallback<PackPaymentMethod> {
    
    private val gson = Gson()
    private var paymentName: String? = null
    private var pressedButtonName = ""
    private var transactionIdentifier: String? = null
    private var statusCode: String? = null
    private var statusMessage: String? = null
    private var ctaButtonValue = 0
    private var subType: String? = null
    private var type: String? = null
    private var provider: String? = null
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
        pressedButtonName = ""
        
        paymentName = arguments?.getString("paymentName", "") ?: ""
        prepareDataPackOptions()
        observe(viewModel.selectedDataPackOption) {
            if (it.listTitle == null) {
                mAdapter.setSelectedItem(it)
            }
        }
        
        observeMnpStatus()
        observePackStatus()
        observeRechargeByBkash()
        observeBlDataPackPurchase()
        observeSubscriberPaymentInit()
        
        binding.recyclerView.setPadding(0, 0, 0, 24)
        
        binding.backImg.safeClick({
            viewModel.clickableAdInventories.value?.let {
                this.closeDialog()
                viewModel.clickableAdInventories.value = null
            } ?: run {
                findNavController().popBackStack()
            }
        })
        
        binding.termsAndConditionsTwo.safeClick({
            showTermsAndConditionDialog()
        })
        
        binding.buyNowButton.safeClick({
            pressedButtonName = "buyNowButton"
            val isLoggedInUser = mPref.isVerifiedUser
            requireActivity().checkVerification(shouldReloadAfterLogin = false) {
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
                        "provider" to provider,
                        "type" to type,
                        "subtype" to if(paymentName == "blPack") "balance" else null,
                        "MNO" to if ((mPref.isBanglalinkNumber).toBoolean()) "BL" else "non-BL",
                        "discount" to null,
                    )
                )
                
                progressDialog.show()
                
                /* if user was logged in before pressing the buy button then call initiatePurchase function otherwise call mnp, check pack status and reload the page if already the pack is purchase */
                if (isLoggedInUser) {
                    initiatePurchase()
                } else {
                    viewModel.getMnpStatus()
                }
            }
        })
        
        binding.buyWithRechargeButton.safeClick({
            pressedButtonName = "buyWithRechargeButton"
            val isLoggedInUser = mPref.isVerifiedUser
            requireActivity().checkVerification(shouldReloadAfterLogin = false) {
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
                        "provider" to provider,
                        "type" to type,
                        "subtype" to "recharge",
                        "MNO" to if ((mPref.isBanglalinkNumber).toBoolean()) "BL" else "non-BL",
                        "discount" to null,
                    )
                )
                
                progressDialog.show()
                
                /* if user was logged in before pressing the buy button then call rechargeByBkash function otherwise call mnp, check 
                pack status and reload the page if already the pack is purchase */
                if (isLoggedInUser) {
                    rechargeByBkash()
                } else {
                    viewModel.getMnpStatus()
                } 
            }
        })
        
        binding.signInButton.safeClick({
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
                    "provider" to provider,
                    "type" to type,
                    "subtype" to "signin",
                    "MNO" to if ((mPref.isBanglalinkNumber).toBoolean()) "BL" else "non-BL",
                    "discount" to null,
                )
            )
            homeViewModel.isLogoutCompleted.value = false
            mPref.shouldIgnoreReloadAfterLogout.value = true
            observeLogout()
            homeViewModel.logoutUser()
        })
        
        binding.buySimButton.safeClick({
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
                    "provider" to provider,
                    "type" to type,
                    "subtype" to "sim",
                    "MNO" to if ((mPref.isBanglalinkNumber).toBoolean()) "BL" else "non-BL",
                    "discount" to null,
                )
            )
            val args = Bundle().apply {
                putString("myTitle", "Back to TOFFEE")
                putString("url", "https://eshop.banglalink.net/")
                putBoolean("isHideBackIcon", false)
                putBoolean("isHideCloseIcon", true)
            }
            findNavController().navigate(R.id.htmlPageViewDialog, args)
        })
        
        binding.goBackButton.safeClick({
            findNavController().popBackStack()
        })
    }
    
    private fun initiatePurchase() {
        when (paymentName) {
            "bkash" -> {
                // Initiate the Subscriber Payment Initialization for bKash
                subscriberPaymentInit()
            }
            "ssl" -> {
                // Initiate the Subscriber Payment Initialization for SSL
                subscriberPaymentInit()
            }
            "blPack" -> {
                // Purchase a Banglalink data pack
                purchaseBlDataPack()
            }
        }
    }
    
    private fun observeLogout() {
        observe(homeViewModel.isLogoutCompleted) {
            if (it) {
                requireActivity().checkVerification(shouldReloadAfterLogin = false) {
                    progressDialog.show()
                    viewModel.getMnpStatus()
                }
            }
        }
    }
    
    private fun observeMnpStatus() {
        observe(viewModel.mnpStatusLiveData) { response ->
            when (response) {
                is Success -> {
                    viewModel.selectedPremiumPack.value?.let {
                        viewModel.getPackStatusForDataPackOptions(packId = it.id)
                    } ?: {
                        progressDialog.dismiss()
                        requireContext().showToast(getString(R.string.try_again_message))
                    }
                }
                is Failure -> {
                    progressDialog.dismiss()
                    requireContext().showToast(response.error.msg)
                }
            }
        }
    }
    
    private fun observePackStatus() {
        observe(viewModel.activePackListForDataPackOptionsLiveData) {
            progressDialog.dismiss()
            when(it) {
                is Success -> {
                    mPref.activePremiumPackList.value = it.data
                    val isPurchased = checkPackPurchased()
                    if (!isPurchased) {
                        prepareDataPackOptions(true)
                        
                        if (pressedButtonName == "buyNowButton") {
                            if (mAdapter.selectedPosition > -1) {
                                initiatePurchase()
                            }
                        } else if (pressedButtonName == "buyWithRechargeButton") {
                            if (mAdapter.selectedPosition > -1) {
                                rechargeByBkash()
                            }
                        } else {
                            /**
                             * if selected a valid option then hide the Sign in with BL and Buy SIM button
                             * then show/hide the Buy Now and Buy With Recharge button according to CTA Button value from API
                             */
                            binding.signInButton.hide()
                            binding.buySimButton.hide()
                            
                            if (mAdapter.selectedPosition > -1 && mPref.isVerifiedUser && mPref.isBanglalinkNumber == "true") {
                                binding.buyNowButton.isVisible =
                                    viewModel.selectedDataPackOption.value?.dataPackCtaButton == 1 || viewModel.selectedDataPackOption.value?.dataPackCtaButton == 3
                                binding.buyWithRechargeButton.isVisible =
                                    viewModel.selectedDataPackOption.value?.dataPackCtaButton == 2 || viewModel.selectedDataPackOption.value?.dataPackCtaButton == 3
                            } else {
                                binding.buyNowButton.hide()
                                binding.buyWithRechargeButton.hide()
                            }
                        }
                    } else {
                        mPref.packDetailsPageRefreshRequired.value = true
                    }
                }
                is Failure -> {
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }
    
    /**
     * Check if the selected premium pack is purchase or not
     */
    private fun checkPackPurchased(): Boolean {
        return viewModel.selectedPremiumPack.value?.let { selectedPack ->
            mPref.activePremiumPackList.value?.getPurchasedPack(
                viewModel.selectedPremiumPack.value?.id,
                mPref.isVerifiedUser,
                mPref.getSystemTime()
            )?.let { activePack ->
                viewModel.selectedPremiumPack.value = selectedPack.copy(
                    isPackPurchased = activePack.isActive,
                    expiryDate = "Expires on ${Utils.formatPackExpiryDate(activePack.expiryDate)}",
                    packDetail = if (activePack.isTrialPackUsed) activePack.packDetail else "You have bought ${activePack.packDetail} pack"
                )
                true
            } ?: run {
                viewModel.selectedPremiumPack.value = selectedPack.copy(isPackPurchased = false)
                false
            }
        } ?: false
    }
    
    /**
     * initiate the payment data pack option in adapter. for non-bl users, bl_packs will show prepaid and postpaid both options with 
     * the proper title. user can select an option and the related buttons will be appeared. if user selects 'sign in with banglalink'
     * button, the user will be logged out immediately and login popup will be visible.
     * @param isRestoreSelection After logout from non-bl operator to bl operator, the previously selected option will remain 
     * selected if the value is true.
     */
    private fun prepareDataPackOptions(isRestoreSelection: Boolean = false) {
        viewModel.paymentMethod.value?.let { paymentTypes ->
            val packPaymentMethodList = mutableListOf<PackPaymentMethod>()
            val prePaid = paymentTypes.bl?.prepaid
            val postPaid = paymentTypes.bl?.postpaid
            val bKashBlPacks = paymentTypes.bkash?.blPacks
            val bKashNonBlPacks = paymentTypes.bkash?.nonBlPacks
            val sslBlPacks = paymentTypes.ssl?.blPacks
            val sslNonBlPacks = paymentTypes.ssl?.nonBlPacks
            //show bl pack for bkash and ssl forcefully when user comes from ad and user is not logged in
            val showBlPacks = viewModel.clickableAdInventories.value?.showBlPacks ?: false
            
            if (paymentName == "bkash") {
                packPaymentMethodList.clear()
                if (mPref.isBanglalinkNumber == "true" || (showBlPacks && !mPref.isVerifiedUser)) {
                    bKashBlPacks?.let { packPaymentMethodList.addAll(it) } ?: run {
                        viewModel.clickableAdInventories.value?.let {
                            requireActivity().showToast(getString(R.string.payment_method_invalid))
                            this.closeDialog()
                        }?: run{
                            requireContext().showToast(getString(R.string.try_again_message))
                        }
                    }
                } else {
                    bKashNonBlPacks?.let { packPaymentMethodList.addAll(it) } ?: run {
                        viewModel.clickableAdInventories.value?.let {
                            requireActivity().showToast(getString(R.string.payment_method_invalid))
                            this.closeDialog()
                        }?: run{
                            requireContext().showToast(getString(R.string.try_again_message))
                        }
                    }
                }
            }
            else if (paymentName == "ssl"){
                packPaymentMethodList.clear()
                if (mPref.isBanglalinkNumber == "true" || (showBlPacks && !mPref.isVerifiedUser)) {
                    sslBlPacks?.let { packPaymentMethodList.addAll(it) } ?: run {
                        viewModel.clickableAdInventories.value?.let {
                            requireActivity().showToast(getString(R.string.payment_method_invalid))
                            this.closeDialog()
                        }?: run{
                            requireContext().showToast(getString(R.string.try_again_message))
                        }
                    }
                } else {
                    sslNonBlPacks?.let { packPaymentMethodList.addAll(it) } ?: run {
                        viewModel.clickableAdInventories.value?.let {
                            requireActivity().showToast(getString(R.string.payment_method_invalid))
                            this.closeDialog()
                        }?: run{
                            requireContext().showToast(getString(R.string.try_again_message))
                        }
                    }
                }
            }
            else if (paymentName == "blPack") {
                packPaymentMethodList.clear()
                if (mPref.isBanglalinkNumber == "true") {
                    if (mPref.isPrepaid && !prePaid.isNullOrEmpty()) {
                        packPaymentMethodList.addAll(prePaid)
                    } else if (!mPref.isPrepaid && !postPaid.isNullOrEmpty()) {
                        packPaymentMethodList.addAll(postPaid)
                    } else {
                        binding.errorMessageTextView.text = String.format(getString(R.string.no_pack_option_msg), if (mPref.isPrepaid) "prepaid" else "postpaid")
                        binding.emptyView.show()
                        binding.contentView.hide()
                        hidePaymentOption()
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
                isPrepaid = if (mPref.isPrepaid) 1 else 0
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
                            // Send Log to FirebaseAnalytics
                            ToffeeAnalytics.toffeeLogEvent(
                                ToffeeEvents.PACK_SUCCESS,
                                bundleOf(
                                    "pack_ID" to viewModel.selectedPremiumPack.value?.id.toString(),
                                    "pack_name" to viewModel.selectedPremiumPack.value?.packTitle.toString(),
                                    "currency" to "BDT",
                                    "amount" to viewModel.selectedDataPackOption.value?.packPrice.toString(),
                                    "validity" to viewModel.selectedDataPackOption.value?.packDuration.toString(),
                                    "provider" to "Banglalink",
                                    "type" to "data pack",
                                    "reason" to "N/A",
                                    "MNO" to if ((mPref.isBanglalinkNumber).toBoolean()) "BL" else "non-BL",
                                )
                            )
                            val args = bundleOf(
                                PaymentStatusDialog.ARG_STATUS_CODE to (it.data.status ?: 200)
                            )
                            findNavController().navigateTo(R.id.paymentStatusDialog, args)
                        }
                        PaymentStatusDialog.UN_SUCCESS ->{
                            // Send Log to FirebaseAnalytics
                            ToffeeAnalytics.toffeeLogEvent(
                                ToffeeEvents.PACK_ERROR,
                                bundleOf(
                                    "pack_ID" to viewModel.selectedPremiumPack.value?.id.toString(),
                                    "pack_name" to viewModel.selectedPremiumPack.value?.packTitle.toString(),
                                    "currency" to "BDT",
                                    "amount" to viewModel.selectedDataPackOption.value?.packPrice.toString(),
                                    "validity" to viewModel.selectedDataPackOption.value?.packDuration.toString(),
                                    "provider" to "Banglalink",
                                    "type" to "data pack",
                                    "reason" to "Due to some technical issue, the data plan activation failed. Please retry.",
                                    "MNO" to if ((mPref.isBanglalinkNumber).toBoolean()) "BL" else "non-BL",
                                )
                            )
                            val args = bundleOf(
                                PaymentStatusDialog.ARG_STATUS_CODE to 0,
                                PaymentStatusDialog.ARG_STATUS_TITLE to "Data Plan Purchase Failed!",
                                PaymentStatusDialog.ARG_STATUS_MESSAGE to "Due to some technical issue, the data plan activation failed. Please retry."
                            )
                            findNavController().navigateTo(R.id.paymentStatusDialog, args)
                        }
                        PaymentStatusDialog.DataPackPurchaseFailedBalanceInsufficient_ERROR -> {
                            // Send Log to FirebaseAnalytics
                            ToffeeAnalytics.toffeeLogEvent(
                                ToffeeEvents.PACK_ERROR,
                                bundleOf(
                                    "pack_ID" to viewModel.selectedPremiumPack.value?.id.toString(),
                                    "pack_name" to viewModel.selectedPremiumPack.value?.packTitle.toString(),
                                    "currency" to "BDT",
                                    "amount" to viewModel.selectedDataPackOption.value?.packPrice.toString(),
                                    "validity" to viewModel.selectedDataPackOption.value?.packDuration.toString(),
                                    "provider" to "Banglalink",
                                    "type" to "data pack",
                                    "reason" to if (!mPref.isPrepaid) R.string.insufficient_balance_for_postpaid else R.string.insufficient_balance_subtitle,
                                    "MNO" to if ((mPref.isBanglalinkNumber).toBoolean()) "BL" else "non-BL",
                                )
                            )
                            if (!mPref.isPrepaid){
                                val args = bundleOf(
                                    "subTitle" to getString(R.string.insufficient_balance_for_postpaid),
                                    "isBuyWithRechargeHide" to false,
                                    "ctaValue" to ctaButtonValue
                                )
                                findNavController().navigateTo(R.id.insufficientBalanceFragment, args)
                            }
                            else{
                                val argsTwo = bundleOf(
                                    "ctaValue" to ctaButtonValue
                                )
                                findNavController().navigateTo(R.id.insufficientBalanceFragment,argsTwo)
                            }
                        }
                        else -> {
                            // Send Log to FirebaseAnalytics
                            ToffeeAnalytics.toffeeLogEvent(
                                ToffeeEvents.PACK_ERROR,
                                bundleOf(
                                    "pack_ID" to viewModel.selectedPremiumPack.value?.id.toString(),
                                    "pack_name" to viewModel.selectedPremiumPack.value?.packTitle.toString(),
                                    "currency" to "BDT",
                                    "amount" to viewModel.selectedDataPackOption.value?.packPrice.toString(),
                                    "validity" to viewModel.selectedDataPackOption.value?.packDuration.toString(),
                                    "provider" to "Banglalink",
                                    "type" to "data pack",
                                    "reason" to getString(R.string.due_some_technical_issue),
                                    "MNO" to if ((mPref.isBanglalinkNumber).toBoolean()) "BL" else "non-BL",
                                )
                            )
                            val args = bundleOf(
                                PaymentStatusDialog.ARG_STATUS_CODE to (it.data.status?: 0)
                            )
                            findNavController().navigateTo(R.id.paymentStatusDialog, args)
                        }
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
                            "provider" to "Banglalink",
                            "type" to "data pack",
                            "reason" to it.error.msg,
                            "MNO" to if ((mPref.isBanglalinkNumber).toBoolean()) "BL" else "non-BL",
                        )
                    )
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }

    /**
     * Initiates the payment process for bKash and SSL payment methods.
     * This function is responsible for preparing and sending a payment initialization request.
     *
     * @param paymentType The type of payment method (e.g., "bkash" or "ssl").
     */
    private fun subscriberPaymentInit() {
        if (viewModel.selectedPremiumPack.value != null && viewModel.selectedDataPackOption.value != null) {
            // Retrieve selected premium pack and data pack option
            val selectedPremiumPack = viewModel.selectedPremiumPack.value
            val selectedDataPackOption = viewModel.selectedDataPackOption.value

            // Prepare a payment initiation request
            val request = SubscriberPaymentInitRequest(
                customerId = mPref.customerId,
                password = mPref.password,
                is_Bl_Number = if (mPref.isBanglalinkNumber == "true") 1 else 0,
                isPrepaid = if (mPref.isPrepaid) 1 else 0,
                packId = selectedPremiumPack?.id ?: 0,
                packTitle = selectedPremiumPack?.packTitle,
                contents = selectedPremiumPack?.contentId,
                paymentMethodId = selectedDataPackOption?.paymentMethodId ?: 0,
                packCode = selectedDataPackOption?.packCode,
                packDetails = selectedDataPackOption?.packDetails,
                packPrice = selectedDataPackOption?.packPrice ?: 0,
                packDuration = selectedDataPackOption?.packDuration ?: 0,
                geoCity = mPref.geoCity,
                geoLocation = mPref.geoLocation,
                cusEmail = mPref.customerEmail
            )
            // Trigger the payment initiation request, but only if both selectedPremiumPack and selectedDataPackOption are not null
            if (selectedPremiumPack != null && selectedDataPackOption != null) {
                progressDialog.show()
                paymentName?.let { viewModel.getSubscriberPaymentInit(it, request) }
            } else {
                // Handle the case where either selectedPremiumPack or selectedDataPackOption is null
                requireContext().showToast(getString(R.string.try_again_message))
            }
        }
    }
    
    private fun observeSubscriberPaymentInit() {
        observe(viewModel.subscriberPaymentInitLiveData) { it ->
            progressDialog.dismiss() // Dismiss the progress dialog
            when (it) {
                is Success -> {
                    it.data?.let {
                        transactionIdentifier = it.transactionIdentifierId
                        statusCode = it.statusCode.toString()
                        statusMessage = it.message
                        //Send Log to the Pub/Sub
                        viewModel.sendPaymentLogFromDeviceData(
                            PaymentLogFromDeviceData(
                                id = System.currentTimeMillis() + mPref.customerId,
                                callingApiName = "${paymentName}SubscriberPaymentInitFromAndroid",
                                packId = viewModel.selectedPremiumPack.value?.id ?: 0,
                                packTitle = viewModel.selectedPremiumPack.value?.packTitle.toString(),
                                dataPackId = viewModel.selectedDataPackOption.value?.dataPackId ?: 0,
                                dataPackDetails = viewModel.selectedDataPackOption.value?.packDetails.toString(),
                                paymentMethodId = viewModel.selectedDataPackOption.value?.paymentMethodId ?: 0,
                                paymentMsisdn = null,
                                paymentId = if (paymentName == "bkash") transactionIdentifier else null,
                                transactionId = if (paymentName == "ssl") transactionIdentifier else null,
                                transactionStatus = statusCode,
                                amount = viewModel.selectedDataPackOption.value?.packPrice.toString(),
                                merchantInvoiceNumber = null,
                                rawResponse = gson.toJson(it)
                            )
                        )
                        
                        if (it.statusCode != 200) {
                            requireContext().showToast(it.message.toString())
                            return@observe
                        }
                        // Prepare navigation arguments for payment WebView
                        val args = bundleOf(
                            "myTitle" to "Pack Details",
                            "url" to it.webViewUrl,
                            "paymentType" to paymentName,
                            "isHideBackIcon" to false,
                            "isHideCloseIcon" to true,
                            "isBkashBlRecharge" to false,
                        )
                        // Navigate to the payment WebView dialog
                        findNavController().navigateTo(R.id.paymentWebViewDialog, args)
                    } ?: requireContext().showToast(getString(string.try_again_message))
                }
                
                is Failure -> {
                    //Send Log to the Pub/Sub
                    viewModel.sendPaymentLogFromDeviceData(
                        PaymentLogFromDeviceData(
                            id = System.currentTimeMillis() + mPref.customerId,
                            callingApiName = "${paymentName}SubscriberPaymentInitFromAndroid",
                            packId = viewModel.selectedPremiumPack.value?.id ?: 0,
                            packTitle = viewModel.selectedPremiumPack.value?.packTitle.toString(),
                            dataPackId = viewModel.selectedDataPackOption.value?.dataPackId ?: 0,
                            dataPackDetails = viewModel.selectedDataPackOption.value?.packDetails.toString(),
                            paymentMethodId = viewModel.selectedDataPackOption.value?.paymentMethodId ?: 0,
                            paymentMsisdn = null,
                            paymentId = if (paymentName == "bkash") transactionIdentifier else null,
                            transactionId = if (paymentName == "ssl") transactionIdentifier else null,
                            transactionStatus = statusCode,
                            amount = viewModel.selectedDataPackOption.value?.packPrice.toString(),
                            merchantInvoiceNumber = null,
                            rawResponse = gson.toJson(it.error.msg)
                        )
                    )
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }
    
    private fun rechargeByBkash() {
        if (viewModel.selectedPremiumPack.value != null && viewModel.selectedDataPackOption.value != null) {
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
    
    private fun observeRechargeByBkash() {
        observe(viewModel.rechargeByBkashUrlLiveData) { it ->
            progressDialog.dismiss()
            when (it) {
                is Success -> {
                    viewModel.sendPaymentLogFromDeviceData(
                        PaymentLogFromDeviceData(
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
                        )
                    )
                    it.data?.let {
                        if (it.statusCode != 200) {
                            requireContext().showToast(getString(string.try_again_message))
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
                    } ?: requireContext().showToast(getString(string.try_again_message))
                }
                
                is Failure -> {
                    viewModel.sendPaymentLogFromDeviceData(
                        PaymentLogFromDeviceData(
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
                        )
                    )
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }
    
    override fun onItemClicked(view: View, item: PackPaymentMethod, position: Int) {
        super.onItemClicked(view, item, position)
        showPaymentOption(item)
        mAdapter.selectedPosition = position
        viewModel.selectedDataPackOption.value = item

        // Call the function to handle plan selection and FirebaseAnalytics log event
        planSelectedFirebaseAnalyticsLog()
    }
    
    private fun showPaymentOption(item: PackPaymentMethod) {
        binding.recyclerView.setPadding(0, 0, 0, 8)
        binding.termsAndConditionsGroup.show()
        // Customize UI elements based on the selected payment method
        when (paymentName) {
            "bkash" -> {
                binding.buyNowButton.text = getString(R.string.buy_bkash)
                binding.buyNowButton.show()
            }
            "ssl" -> {
                binding.buyNowButton.text = getString(R.string.buy_now_ssl)
                binding.buyNowButton.show()
            }
            "blPack" -> {
                if (mPref.isBanglalinkNumber == "true") {
                    ctaButtonValue= item.dataPackCtaButton!!
                    binding.buyNowButton.isVisible = item.dataPackCtaButton == 1 || item.dataPackCtaButton == 3
                    binding.buyWithRechargeButton.isVisible = item.dataPackCtaButton == 2 || item.dataPackCtaButton == 3
                } else {
                    binding.buyNowButton.hide()
                    binding.buyWithRechargeButton.hide()
                }
                
                binding.signInButton.isVisible = mPref.isBanglalinkNumber == "false"
                binding.buySimButton.isVisible = mPref.isBanglalinkNumber == "false"
                binding.termsAndConditionsGroup.isVisible = mPref.isBanglalinkNumber == "true"
            }
        }
    }
    
    private fun hidePaymentOption() {
        binding.recyclerView.setPadding(0, 0, 0, 24)
        binding.termsAndConditionsGroup.hide()
        binding.buyNowButton.hide()
        binding.buyWithRechargeButton.hide()
        binding.buySimButton.hide()
        binding.signInButton.hide()
    }

    private fun planSelectedFirebaseAnalyticsLog() {
        subType = when {
            (mPref.isBanglalinkNumber).toBoolean() && mPref.isPrepaid -> "prepaid"
            (mPref.isBanglalinkNumber).toBoolean() && !mPref.isPrepaid -> "postpaid"
            (!(mPref.isBanglalinkNumber).toBoolean()) -> "N/A"
            else -> null
        }

        provider = when (paymentName) {
            "blPack" -> "Banglalink"
            "bkash" -> "bKash"
            "ssl" -> "SSL Wireless"
            "nagad" -> "Nagad"
            else -> null
        }

        type = when (paymentName) {
            "blPack" -> "data pack"
            "bkash", "nagad" -> "wallet"
            "ssl" -> "aggregator"
            else -> "null"
        }

        // Send Log to FirebaseAnalytics
        ToffeeAnalytics.toffeeLogEvent(
            ToffeeEvents.PLAN_SELECTED,
            bundleOf(
                "pack_ID" to viewModel.selectedPremiumPack.value?.id.toString(),
                "pack_name" to viewModel.selectedPremiumPack.value?.packTitle.toString(),
                "plan_details " to viewModel.selectedDataPackOption.value?.packDetails.toString(),
                "currency" to "BDT",
                "amount" to viewModel.selectedDataPackOption.value?.packPrice.toString(),
                "provider" to provider,
                "type" to type,
                "subtype" to subType,
                "MNO" to if ((mPref.isBanglalinkNumber).toBoolean()) "BL" else "non-BL",
            )
        )
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