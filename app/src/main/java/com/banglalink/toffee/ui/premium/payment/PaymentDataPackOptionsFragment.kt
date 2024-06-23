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
import com.banglalink.toffee.data.network.request.TokenizedAccountInfoApiRequest
import com.banglalink.toffee.data.network.response.DiscountInfo
import com.banglalink.toffee.data.network.response.PackPaymentMethod
import com.banglalink.toffee.databinding.FragmentPaymentDataPackOptionsBinding
import com.banglalink.toffee.enums.PaymentMethodName
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
import com.banglalink.toffee.util.calculateDiscountedPrice
import com.banglalink.toffee.util.unsafeLazy
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class PaymentDataPackOptionsFragment : ChildDialogFragment(), DataPackOptionCallback<PackPaymentMethod> {
    
    @Inject lateinit var json: Json
    private var paymentName: String? = null
    private var paymentDiscount: String? = null
    private var pressedButtonName = ""
    private var transactionIdentifier: String? = null
    private var paymentToken: String? = null
    private var walletNumber: String? = null
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
    private var discountInfo:DiscountInfo?=null
    private var packPriceToPay:Int?=null

    private var isPlanFound:Boolean?=false
    private var isDiscountAvailable :Boolean =false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPaymentDataPackOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hidePaymentOption()
        pressedButtonName = ""
        
        paymentName = mPref.selectedPaymentType.value
        paymentDiscount = arguments?.getString("discount", "") ?: ""


        if (mPref.isBanglalinkNumber=="true"){
            viewModel.selectedPackSystemDiscount.value?.BL?.let {
                discountInfo=it
            } ?: run {
                viewModel.selectedPackSystemDiscount.value?.BOTH?.let {
                    discountInfo=it
                }
            }
        }
        else {
            viewModel.selectedPackSystemDiscount.value?.NONBL?.let {
                discountInfo=it
            } ?: run {
                viewModel.selectedPackSystemDiscount.value?.BOTH?.let {
                    discountInfo=it
                }
            }
        }

        isDiscountAvailable = (
                (paymentName == "bkash" && discountInfo?.discountApplyOnPaymentMethod?.BKASH != null) ||
                (paymentName == "nagad" && discountInfo?.discountApplyOnPaymentMethod?.NAGAD != null) ||
                (paymentName == "ssl" && discountInfo?.discountApplyOnPaymentMethod?.SSL != null) ||
                (paymentName == "blPack" && discountInfo?.discountApplyOnPaymentMethod?.DCB != null)
        )



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
            if (!isLoggedInUser){
                ToffeeAnalytics.toffeeLogEvent(
                    ToffeeEvents.LOGIN_SOURCE,
                    bundleOf(
                        "source" to "buy_now_button",
                        "method" to "mobile"
                    )
                )
            }
            requireActivity().checkVerification(shouldReloadAfterLogin = false) {
                if (viewModel.selectedDataPackOption.value?.isDob == 1){
                    paymentName = PaymentMethodString.BLDCB.value
                }
                val MNO = when {
                    (mPref.isBanglalinkNumber).toBoolean() && mPref.isPrepaid -> "BL-prepaid"
                    (mPref.isBanglalinkNumber).toBoolean() && !mPref.isPrepaid -> "BL-postpaid"
                    (!(mPref.isBanglalinkNumber).toBoolean()) -> "non-BL"
                    else -> "N/A"
                }
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
                        "MNO" to MNO,
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
                val MNO = when {
                    (mPref.isBanglalinkNumber).toBoolean() && mPref.isPrepaid -> "BL-prepaid"
                    (mPref.isBanglalinkNumber).toBoolean() && !mPref.isPrepaid -> "BL-postpaid"
                    (!(mPref.isBanglalinkNumber).toBoolean()) -> "non-BL"
                    else -> "N/A"
                }
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
                        "MNO" to MNO,
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
            val MNO = when {
                (mPref.isBanglalinkNumber).toBoolean() && mPref.isPrepaid -> "BL-prepaid"
                (mPref.isBanglalinkNumber).toBoolean() && !mPref.isPrepaid -> "BL-postpaid"
                (!(mPref.isBanglalinkNumber).toBoolean()) -> "non-BL"
                else -> "N/A"
            }
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
                    "MNO" to MNO,
                    "discount" to null,
                )
            )
            homeViewModel.isLogoutCompleted.value = false
            mPref.shouldIgnoreReloadAfterLogout.value = true
            observeLogout()
            homeViewModel.logoutUser()
        })
        
        binding.buySimButton.safeClick({
            val MNO = when {
                (mPref.isBanglalinkNumber).toBoolean() && mPref.isPrepaid -> "BL-prepaid"
                (mPref.isBanglalinkNumber).toBoolean() && !mPref.isPrepaid -> "BL-postpaid"
                (!(mPref.isBanglalinkNumber).toBoolean()) -> "non-BL"
                else -> "N/A"
            }
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
                    "MNO" to MNO,
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
            "nagad" -> {
                // Initiate the Subscriber Payment Initialization for Nagad
                observeTokenizedPaymentMethodsApi()
                tokenizedPaymentMethodsApi()
            }
            "ssl" -> {
                // Initiate the Subscriber Payment Initialization for SSL
                subscriberPaymentInit()
            }
            "blPack" -> {
                // Purchase a Banglalink data pack
                purchaseBlDataPack()
            }
            PaymentMethodString.BLDCB.value->{
                subscriberPaymentInit()
            }
        }
    }
    
    private fun observeLogout() {
        observe(homeViewModel.isLogoutCompleted) {
            if (it) {
                ToffeeAnalytics.toffeeLogEvent(
                    ToffeeEvents.LOGIN_SOURCE,
                    bundleOf(
                        "source" to "signin_with_banglalink",
                        "method" to "mobile"
                    )
                )
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
                    } ?: run {
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
                                binding.needToEnterOtpText.isVisible =viewModel.selectedDataPackOption.value?.isDob == 1 && mPref.isBanglalinkNumber == "true"
                                binding.termsAndConditionsGroup.isVisible =
                                    viewModel.selectedDataPackOption.value?.dataPackCtaButton == 1 || viewModel.selectedDataPackOption.value?.dataPackCtaButton == 2 || viewModel.selectedDataPackOption.value?.dataPackCtaButton == 3
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
            val dcb = paymentTypes.bl?.dcb
            val bKashBlPacks = paymentTypes.bkash?.blPacks
            val bKashNonBlPacks = paymentTypes.bkash?.nonBlPacks
            val sslBlPacks = paymentTypes.ssl?.blPacks
            val sslNonBlPacks = paymentTypes.ssl?.nonBlPacks
            val nagadBlPacks = paymentTypes.nagad?.blPacks
            val nagadNonBlPacks = paymentTypes.nagad?.nonBlPacks
            //show bl pack for bkash and ssl forcefully when user comes from ad and user is not logged in
            val showBlPacks = viewModel.clickableAdInventories.value?.showBlPacks ?: false



            if (paymentName == "bkash") {
                packPaymentMethodList.clear()
                if (bKashBlPacks.isNullOrEmpty() && bKashNonBlPacks.isNullOrEmpty()){ handleInvalidPaymentMethod() }
                else {
                    if (mPref.isBanglalinkNumber == "true" || (showBlPacks && !mPref.isVerifiedUser)) {
                        if (!bKashBlPacks.isNullOrEmpty()){
                            packPaymentMethodList.addAll(bKashBlPacks)
                        } else {
                            showEmptyView(getString(R.string.buy_with_non_bl_number))
                        }
                    } else {
                        if (!bKashNonBlPacks.isNullOrEmpty()){
                            packPaymentMethodList.addAll(bKashNonBlPacks)
                        } else {
                            showEmptyView(getString(R.string.buy_with_bl_number))
                        }
                    }
                }
            }
            else if (paymentName == "nagad"){
                packPaymentMethodList.clear()
                if (nagadBlPacks.isNullOrEmpty() && nagadNonBlPacks.isNullOrEmpty()){ handleInvalidPaymentMethod() }
                else {
                    if (mPref.isBanglalinkNumber == "true" || (showBlPacks && !mPref.isVerifiedUser)) {
                        if (!nagadBlPacks.isNullOrEmpty()){
                            packPaymentMethodList.addAll(nagadBlPacks)
                        } else {
                            showEmptyView(getString(R.string.buy_with_non_bl_number))
                        }
                    } else {
                        if (!nagadNonBlPacks.isNullOrEmpty()){
                            packPaymentMethodList.addAll(nagadNonBlPacks)
                        } else {
                            showEmptyView(getString(R.string.buy_with_bl_number))
                        }
                    }
                }
            }
            else if (paymentName == "ssl"){
                packPaymentMethodList.clear()
                if (sslBlPacks.isNullOrEmpty() && sslNonBlPacks.isNullOrEmpty()){ handleInvalidPaymentMethod() }
                else {
                    if (mPref.isBanglalinkNumber == "true" || (showBlPacks && !mPref.isVerifiedUser)) {
                        if (!sslBlPacks.isNullOrEmpty()){
                            packPaymentMethodList.addAll(sslBlPacks)
                        } else {
                            showEmptyView(getString(R.string.buy_with_non_bl_number))
                        }
                    } else {
                        if (!sslNonBlPacks.isNullOrEmpty()){
                            packPaymentMethodList.addAll(sslNonBlPacks)
                        } else {
                            showEmptyView(getString(R.string.buy_with_bl_number))
                        }
                    }
                }
            }
            else if (paymentName == "blPack") {
                packPaymentMethodList.clear()
                if (prePaid.isNullOrEmpty() && postPaid.isNullOrEmpty() && dcb.isNullOrEmpty()){ handleInvalidPaymentMethod() }
                else {
                    if (mPref.isBanglalinkNumber == "true") {
                         isPlanFound = false

                        //both prepaid and postpaid BL user sees DCB
                        if (!dcb.isNullOrEmpty()){
                            packPaymentMethodList.add(PackPaymentMethod(listTitle = "Access only"))
                            packPaymentMethodList.addAll(dcb)
                            isPlanFound = true
                        }

                        if (mPref.isPrepaid && !prePaid.isNullOrEmpty()) {
                            packPaymentMethodList.add(PackPaymentMethod(listTitle = "Access + Data"))
                            packPaymentMethodList.addAll(prePaid!!)
                            isPlanFound = true
                        } else if (!mPref.isPrepaid && !postPaid.isNullOrEmpty()) {
                            packPaymentMethodList.add(PackPaymentMethod(listTitle = "Access + Data"))
                            packPaymentMethodList.addAll(postPaid!!)
                            isPlanFound = true
                        }

                        if (!isPlanFound!!) {
                            showEmptyView(String.format(getString(R.string.no_pack_option_msg), if (mPref.isPrepaid) "prepaid" else "postpaid"))
                        }

                    } else {
                        // TOF-1181: Non BL users sees DCB and data plans for BL prepaid user
                        if (!dcb.isNullOrEmpty()){
                            packPaymentMethodList.add(PackPaymentMethod(listTitle = "Access only"))
                            packPaymentMethodList.addAll(dcb)
                        }
                        if (!prePaid.isNullOrEmpty()) {
                            packPaymentMethodList.add(PackPaymentMethod(listTitle = "Access + Data"))
                            packPaymentMethodList.addAll(prePaid)
                        } else if (!postPaid.isNullOrEmpty()) {
                            packPaymentMethodList.add(PackPaymentMethod(listTitle = "Access + Data"))
                            packPaymentMethodList.addAll(postPaid)
                        }
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
            setPlanSubTittle()
        }
    }
    private fun showEmptyView(message: String){
        binding.errorMessageTextView.text = message
        binding.emptyView.show()
        binding.contentView.hide()
        hidePaymentOption()
    }

    private fun handleInvalidPaymentMethod(){
        viewModel.clickableAdInventories.value = null
        requireActivity().showToast(getString(R.string.payment_method_invalid))
        mPref.refreshRequiredForClickableAd.value = true // refreshing pack details page to destroy this flow
        this@PaymentDataPackOptionsFragment.closeDialog()
    }
    
    private fun purchaseBlDataPack() {
        if (viewModel.selectedPremiumPack.value != null && viewModel.selectedDataPackOption.value != null) {
            val selectedPremiumPack = viewModel.selectedPremiumPack.value
            val selectedDataPack = viewModel.selectedDataPackOption.value
            
            val dataPackPurchaseRequest = DataPackPurchaseRequest(
                customerId = mPref.customerId,
                password = mPref.password,
                isBanglalinkNumber = (mPref.isBanglalinkNumber == "true").toInt(),
                packId = selectedPremiumPack?.id ?: 0,
                paymentMethodId = selectedDataPack?.paymentMethodId ?: 0,
                packTitle = selectedPremiumPack?.packTitle,
                contentList = selectedPremiumPack?.contentId,
                packCode = selectedDataPack?.packCode,
                packDetails = selectedDataPack?.packDetails,
                packPrice = selectedDataPack?.packPrice,
                packDuration = selectedDataPack?.packDuration,
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
                    it.data?.let {
                        when (it.status) {
                            PaymentStatusDialog.SUCCESS -> {
                                mPref.activePremiumPackList.value = it.loginRelatedSubsHistory
                                val MNO = when {
                                    (mPref.isBanglalinkNumber).toBoolean() && mPref.isPrepaid -> "BL-prepaid"
                                    (mPref.isBanglalinkNumber).toBoolean() && !mPref.isPrepaid -> "BL-postpaid"
                                    (!(mPref.isBanglalinkNumber).toBoolean()) -> "non-BL"
                                    else -> "N/A"
                                }
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
                                        "MNO" to MNO,
                                    )
                                )
                                val args = bundleOf(
                                    PaymentStatusDialog.ARG_STATUS_CODE to (it.status ?: 200)
                                )
                                findNavController().navigateTo(R.id.paymentStatusDialog, args)
                            }
                            
                            PaymentStatusDialog.UN_SUCCESS -> {
                                val MNO = when {
                                    (mPref.isBanglalinkNumber).toBoolean() && mPref.isPrepaid -> "BL-prepaid"
                                    (mPref.isBanglalinkNumber).toBoolean() && !mPref.isPrepaid -> "BL-postpaid"
                                    (!(mPref.isBanglalinkNumber).toBoolean()) -> "non-BL"
                                    else -> "N/A"
                                }
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
                                        "MNO" to MNO,
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
                                val MNO = when {
                                    (mPref.isBanglalinkNumber).toBoolean() && mPref.isPrepaid -> "BL-prepaid"
                                    (mPref.isBanglalinkNumber).toBoolean() && !mPref.isPrepaid -> "BL-postpaid"
                                    (!(mPref.isBanglalinkNumber).toBoolean()) -> "non-BL"
                                    else -> "N/A"
                                }
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
                                        "MNO" to MNO,
                                    )
                                )
                                if (!mPref.isPrepaid) {
                                    val args = bundleOf(
                                        "subTitle" to getString(R.string.insufficient_balance_for_postpaid),
                                        "isBuyWithRechargeHide" to false,
                                        "ctaValue" to ctaButtonValue
                                    )
                                    findNavController().navigateTo(R.id.insufficientBalanceFragment, args)
                                } else {
                                    val argsTwo = bundleOf(
                                        "ctaValue" to ctaButtonValue
                                    )
                                    findNavController().navigateTo(R.id.insufficientBalanceFragment, argsTwo)
                                }
                            }
                            
                            else -> {
                                val MNO = when {
                                    (mPref.isBanglalinkNumber).toBoolean() && mPref.isPrepaid -> "BL-prepaid"
                                    (mPref.isBanglalinkNumber).toBoolean() && !mPref.isPrepaid -> "BL-postpaid"
                                    (!(mPref.isBanglalinkNumber).toBoolean()) -> "non-BL"
                                    else -> "N/A"
                                }
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
                                        "MNO" to MNO,
                                    )
                                )
                                val args = bundleOf(
                                    PaymentStatusDialog.ARG_STATUS_CODE to (it.status ?: 0)
                                )
                                findNavController().navigateTo(R.id.paymentStatusDialog, args)
                            }
                        }
                    } ?: requireContext().showToast(getString(R.string.try_again_message))
                }
                is Failure -> {
                    val MNO = when {
                        (mPref.isBanglalinkNumber).toBoolean() && mPref.isPrepaid -> "BL-prepaid"
                        (mPref.isBanglalinkNumber).toBoolean() && !mPref.isPrepaid -> "BL-postpaid"
                        (!(mPref.isBanglalinkNumber).toBoolean()) -> "non-BL"
                        else -> "N/A"
                    }
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
                            "MNO" to MNO,
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

            if (viewModel.selectedPackSystemDiscount.value==null){
                packPriceToPay=selectedDataPackOption?.packPrice ?: 0
            }
            else{
                try {
                    packPriceToPay = calculateDiscountedPrice(
                        originalPrice = selectedDataPackOption?.packPrice?.toDouble()?:0.0,
                        discountPercentage = mPref.paymentDiscountPercentage.value?.toDouble()?: 0.0
                    ).toInt()
                }catch (e: Exception){
                    // discount is not applicable in this plan
                    packPriceToPay=selectedDataPackOption?.packPrice ?: 0
                }

            }
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
                packPrice = packPriceToPay?:0, // the amount user is paying after discount or else
                packDuration = selectedDataPackOption?.packDuration ?: 0,
                clientType = "MOBILE_APP",
                paymentPurpose = if (paymentName == "nagad") "ECOM_TXN" else null,
                paymentToken = null,
                geoCity = mPref.geoCity,
                geoLocation = mPref.geoLocation,
                cusEmail = mPref.customerEmail,

                voucher = if (isDiscountAvailable) discountInfo?.voucher else null,
                campaign_type = if (isDiscountAvailable) discountInfo?.campaignType else null,
                partner_name = if (isDiscountAvailable) discountInfo?.partnerName else null,
                partner_id = if (isDiscountAvailable) discountInfo?.partnerId else null,
                campaign_name = if (isDiscountAvailable) discountInfo?.campaignName else null,
                campaign_id = if (isDiscountAvailable) discountInfo?.campaignId else null,
                campaign_type_id = if (isDiscountAvailable) discountInfo?.campaignTypeId else null,
                campaign_expire_date = if (isDiscountAvailable) discountInfo?.campaignExpireDate else null,
                voucher_generated_type = if (isDiscountAvailable) discountInfo?.voucherGeneratedType else null,
                discount = if (isDiscountAvailable) mPref.paymentDiscountPercentage.value else null, // the percentage of discount applied
                original_price = if (isDiscountAvailable) selectedDataPackOption?.packPrice ?: 0 else null, // actual pack price without discount or else

                dobPrice = viewModel.selectedDataPackOption.value?.dobPrice,
                dobCpId = viewModel.selectedDataPackOption.value?.dobCpId,
                dobSubsOfferId = viewModel.selectedDataPackOption.value?.dobSubsOfferId,
                isAutoRenew = viewModel.selectedDataPackOption.value?.isAutoRenew
            )
            Timber.tag("TAG").d("subscriberPaymentInitData $request")
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
                                paymentPurpose = if (paymentName == "nagad") "ECOM_TXN" else null,
                                paymentRefId = if (paymentName == "nagad") transactionIdentifier else null,
                                paymentId = if (paymentName == "bkash") transactionIdentifier else null,
                                transactionId = if (paymentName == "ssl") transactionIdentifier else null,
                                requestId = if(paymentName == PaymentMethodString.BLDCB.value) transactionIdentifier else null,
                                transactionStatus = statusCode,
                                amount = packPriceToPay.toString(),
                                merchantInvoiceNumber = null,
                                rawResponse = json.encodeToString(it),

                                voucher = if (isDiscountAvailable) discountInfo?.voucher else null,
                                campaignType = if (isDiscountAvailable) discountInfo?.campaignType else null,
                                partnerName = if (isDiscountAvailable) discountInfo?.partnerName else null,
                                partnerId = if (isDiscountAvailable) discountInfo?.partnerId else null,
                                campaignName = if (isDiscountAvailable) discountInfo?.campaignName else null,
                                campaignId = if (isDiscountAvailable) discountInfo?.campaignId else null,
                                campaignExpireDate = if (isDiscountAvailable) discountInfo?.campaignExpireDate else null,
                                discount = if (isDiscountAvailable) mPref.paymentDiscountPercentage.value else null,
                                originalPrice = if (isDiscountAvailable) viewModel.selectedDataPackOption.value?.packPrice.toString() else null,

                                dobPrice = viewModel.selectedDataPackOption.value?.dobPrice,
                                dobCpId = viewModel.selectedDataPackOption.value?.dobCpId,
                                dobSubsOfferId = viewModel.selectedDataPackOption.value?.dobSubsOfferId,
                            )
                        )

                        if (it.statusCode != 200) {
                            if (it.responseFromWhere == 2){ // show cta button to call banglalink helpline
                                val args = bundleOf(
                                    PaymentStatusDialog.ARG_STATUS_CODE to -2,
                                    PaymentStatusDialog.ARG_STATUS_MESSAGE to it.message
                                )
                                findNavController().navigateTo(
                                    resId = R.id.paymentStatusDialog,
                                    args
                                )
                            }else {
                                requireContext().showToast(it.message.toString())
                                return@observe
                            }
                        } else {
                            if (paymentName == PaymentMethodString.BLDCB.value){
                                // navigating to otp fragment
                                findNavController().navigateTo(
                                    R.id.dcbEnterOtpFragment,
                                    bundleOf(
                                        "requestId" to transactionIdentifier,
                                        "packPriceToPay" to packPriceToPay,
                                        "voucher" to discountInfo?.voucher,
                                        "campaignType" to discountInfo?.campaignType,
                                        "partnerName" to discountInfo?.partnerName,
                                        "partnerId" to discountInfo?.partnerId,
                                        "campaignName" to discountInfo?.campaignName,
                                        "campaignId" to discountInfo?.campaignId,
                                        "campaignTypeId" to discountInfo?.campaignTypeId,
                                        "campaignExpireDate" to discountInfo?.campaignExpireDate,
                                        "voucherGeneratedType" to discountInfo?.voucherGeneratedType,
                                        "isDiscountAvailable" to isDiscountAvailable
                                    )
                                )
                            } else {
                                // Prepare navigation arguments for payment WebView
                                val args = bundleOf(
                                    "myTitle" to "Pack Details",
                                    "url" to it.webViewUrl,
                                    "paymentType" to paymentName,
                                    "paymentPurpose" to if (paymentName == "nagad") "ECOM_TXN" else null,
                                    "isHideBackIcon" to false,
                                    "isHideCloseIcon" to true,
                                    "isBkashBlRecharge" to false,
                                    "payableAmount" to packPriceToPay.toString(),
                                    "voucher" to discountInfo?.voucher,
                                    "campaignType" to discountInfo?.campaignType,
                                    "partnerName" to discountInfo?.partnerName,
                                    "partnerId" to discountInfo?.partnerId,
                                    "campaignName" to discountInfo?.campaignName,
                                    "campaignId" to discountInfo?.campaignId,
                                    "campaignExpireDate" to discountInfo?.campaignExpireDate,
                                    "isDiscountAvailable" to isDiscountAvailable
                                )
                                // Navigate to the payment WebView dialog
                                findNavController().navigateTo(R.id.paymentWebViewDialog, args)
                            }
                        }
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
                            paymentPurpose = if (paymentName == "nagad") "ECOM_TXN" else null,
                            paymentRefId = if (paymentName == "nagad") transactionIdentifier else null,
                            paymentId = if (paymentName == "bkash") transactionIdentifier else null,
                            transactionId = if (paymentName == "ssl") transactionIdentifier else null,
                            requestId = if(paymentName == PaymentMethodString.BLDCB.value) transactionIdentifier else null,
                            transactionStatus = statusCode,
                            amount = packPriceToPay.toString(),
                            merchantInvoiceNumber = null,
                            rawResponse = json.encodeToString(it),

                            voucher = if (isDiscountAvailable) discountInfo?.voucher else null,
                            campaignType = if (isDiscountAvailable) discountInfo?.campaignType else null,
                            partnerName = if (isDiscountAvailable) discountInfo?.partnerName else null,
                            partnerId = if (isDiscountAvailable) discountInfo?.partnerId else null,
                            campaignName = if (isDiscountAvailable) discountInfo?.campaignName else null,
                            campaignId = if (isDiscountAvailable) discountInfo?.campaignId else null,
                            campaignExpireDate = if (isDiscountAvailable) discountInfo?.campaignExpireDate else null,
                            discount = if (isDiscountAvailable) mPref.paymentDiscountPercentage.value else null,
                            originalPrice = if (isDiscountAvailable) viewModel.selectedDataPackOption.value?.packPrice.toString() else null,

                            dobPrice = viewModel.selectedDataPackOption.value?.dobPrice,
                            dobCpId = viewModel.selectedDataPackOption.value?.dobCpId,
                            dobSubsOfferId = viewModel.selectedDataPackOption.value?.dobSubsOfferId,
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
                            amount = packPriceToPay.toString(),
                            merchantInvoiceNumber = null,
                            rawResponse = json.encodeToString(it),

                            voucher = if (isDiscountAvailable) discountInfo?.voucher else null,
                            campaignType = if (isDiscountAvailable) discountInfo?.campaignType else null,
                            partnerName = if (isDiscountAvailable) discountInfo?.partnerName else null,
                            partnerId = if (isDiscountAvailable) discountInfo?.partnerId else null,
                            campaignName = if (isDiscountAvailable) discountInfo?.campaignName else null,
                            campaignId = if (isDiscountAvailable) discountInfo?.campaignId else null,
                            campaignExpireDate = if (isDiscountAvailable) discountInfo?.campaignExpireDate else null,
                            discount = if (isDiscountAvailable) mPref.paymentDiscountPercentage.value else null,
                            originalPrice = if (isDiscountAvailable) viewModel.selectedDataPackOption.value?.packPrice.toString() else null,
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

                            "payableAmount" to packPriceToPay.toString(),
                            "voucher" to discountInfo?.voucher,
                            "campaignType" to discountInfo?.campaignType,
                            "partnerName" to discountInfo?.partnerName,
                            "partnerId" to discountInfo?.partnerId,
                            "campaignName" to discountInfo?.campaignName,
                            "campaignId" to discountInfo?.campaignId,
                            "campaignExpireDate" to discountInfo?.campaignExpireDate
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
                            amount = packPriceToPay.toString(),
                            merchantInvoiceNumber = null,
                            rawResponse = json.encodeToString(it),

                            voucher = if (isDiscountAvailable) discountInfo?.voucher else null,
                            campaignType = if (isDiscountAvailable) discountInfo?.campaignType else null,
                            partnerName = if (isDiscountAvailable) discountInfo?.partnerName else null,
                            partnerId = if (isDiscountAvailable) discountInfo?.partnerId else null,
                            campaignName = if (isDiscountAvailable) discountInfo?.campaignName else null,
                            campaignId = if (isDiscountAvailable) discountInfo?.campaignId else null,
                            campaignExpireDate = if (isDiscountAvailable) discountInfo?.campaignExpireDate else null,
                            discount = if (isDiscountAvailable) mPref.paymentDiscountPercentage.value else null,
                            originalPrice = if (isDiscountAvailable) viewModel.selectedDataPackOption.value?.packPrice.toString() else null,
                        )
                    )
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }

    private fun tokenizedPaymentMethodsApi() {
        val paymentMethodId = viewModel.selectedDataPackOption.value?.paymentMethodId ?: 0
        val request = TokenizedAccountInfoApiRequest(
            customerId = mPref.customerId,
            password = mPref.password
        )
        viewModel.getTokenizedAccountInfo(paymentMethodId, request)
    }
    private fun observeTokenizedPaymentMethodsApi() {
        observe(viewModel.tokenizedAccountInfoResponse) {
            when (it) {
                is Success -> {
                    it.data?.firstOrNull()?.let { accountInfo ->
                        paymentToken = accountInfo.paymentToken
                        walletNumber = accountInfo.walletNumber

                        val args = bundleOf(
                            "paymentName" to paymentName,
                            "paymentToken" to paymentToken,
                            "walletNumber" to walletNumber,
                            "voucher" to discountInfo?.voucher,
                            "campaignType" to discountInfo?.campaignType,
                            "partnerName" to discountInfo?.partnerName,
                            "partnerId" to discountInfo?.partnerId,
                            "campaignName" to discountInfo?.campaignName,
                            "campaignId" to discountInfo?.campaignId,
                            "campaignExpireDate" to discountInfo?.campaignExpireDate,
                            "isDiscountAvailable" to isDiscountAvailable
                        )
                        findNavController().navigateTo(R.id.savedAccountFragment, args)
                    } ?: subscriberPaymentInit()
                }

                is Failure -> {
                    progressDialog.dismiss() // Dismiss the progress dialog
                    requireContext().showToast("Something went wrong. Please try again later.")
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
            "nagad" -> {
                binding.buyNowButton.text = getString(R.string.buy_now_ssl)
                binding.buyNowButton.show()
            }
            "blPack", PaymentMethodString.BLDCB.value -> {
                if (mPref.isBanglalinkNumber == "true") {
                    ctaButtonValue= item.dataPackCtaButton?: 0
                    binding.buyNowButton.isVisible = item.dataPackCtaButton == 1 || item.dataPackCtaButton == 3
                    binding.buyWithRechargeButton.isVisible = (item.dataPackCtaButton == 2 || item.dataPackCtaButton == 3)
                } else {
                    binding.buyNowButton.hide()
                    binding.buyWithRechargeButton.hide()
                }

                binding.signInButton.isVisible = mPref.isBanglalinkNumber == "false"
//                binding.buySimButton.isVisible = mPref.isBanglalinkNumber == "false"
                binding.termsAndConditionsGroup.isVisible = mPref.isBanglalinkNumber == "true"
                binding.needToEnterOtpText.isVisible = item.isDob == 1 && mPref.isBanglalinkNumber == "true"
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

    private fun setPlanSubTittle(){
        if (mPref.isBanglalinkNumber=="true"){

            when(mPref.selectedPaymentType.value){

                PaymentMethodName.BL.value->{

                    if (mPref.isPrepaid && !viewModel.paymentMethod.value?.bl?.top_promotion_msg_for_plan_bl_prepaid.isNullOrEmpty()){
                        if (!isPlanFound!!) {
                            binding.planSubTitle.hide()
                        }else{
                            binding.planSubTitle.show()
                            binding.planSubTitle.text=viewModel.paymentMethod.value?.bl?.top_promotion_msg_for_plan_bl_prepaid
                        }
                    }else if (!mPref.isPrepaid && !viewModel.paymentMethod.value?.bl?.top_promotion_msg_for_plan_bl_postpaid.isNullOrEmpty()) {
                        binding.planSubTitle.show()
                        binding.planSubTitle.text=viewModel.paymentMethod.value?.bl?.top_promotion_msg_for_plan_bl_postpaid
                    }else{
                        binding.planSubTitle.hide()
                    }

                }
                PaymentMethodName.NAGAD.value->{

                    if (!viewModel.paymentMethod.value?.nagad?.topPromotionMsgForBl.isNullOrEmpty()){
                        binding.planSubTitle.show()
                        binding.planSubTitle.text = viewModel.paymentMethod.value?.nagad?.topPromotionMsgForBl
                    }else{
                        binding.planSubTitle.hide()
                    }

                }
                PaymentMethodName.BKASH.value->{

                    if (!viewModel.paymentMethod.value?.bkash?.topPromotionMsgForBl.isNullOrEmpty()){
                        binding.planSubTitle.show()
                        binding.planSubTitle.text = viewModel.paymentMethod.value?.bkash?.topPromotionMsgForBl
                    }else{
                        binding.planSubTitle.hide()
                    }

                }
                PaymentMethodName.SSL.value->{

                    if (!viewModel.paymentMethod.value?.ssl?.topPromotionMsgForBl.isNullOrEmpty()){
                        binding.planSubTitle.show()
                        binding.planSubTitle.text = viewModel.paymentMethod.value?.ssl?.topPromotionMsgForBl
                    }else{
                        binding.planSubTitle.hide()
                    }

                }
            }

        }else{

            when(mPref.selectedPaymentType.value){

                PaymentMethodName.BL.value->{
                    if (!viewModel.paymentMethod.value?.bl?.top_promotion_msg_for_plan_nonbl_prepaid.isNullOrEmpty()){
                        binding.planSubTitle.show()
                        binding.planSubTitle.text = viewModel.paymentMethod.value?.bl?.top_promotion_msg_for_plan_nonbl_prepaid
                    }
                    else if (!viewModel.paymentMethod.value?.bl?.top_promotion_msg_for_plan_nonbl_postpaid.isNullOrEmpty() ){
                        binding.planSubTitle.show()
                        binding.planSubTitle.text = viewModel.paymentMethod.value?.bl?.top_promotion_msg_for_plan_nonbl_postpaid
                    }
                    else{
                        binding.planSubTitle.hide()
                    }

                }
                PaymentMethodName.NAGAD.value->{
                    if (!viewModel.paymentMethod.value?.nagad?.topPromotionMsgForNonBl.isNullOrEmpty()){
                        binding.planSubTitle.show()
                        binding.planSubTitle.text = viewModel.paymentMethod.value?.nagad?.topPromotionMsgForNonBl
                    }else{
                        binding.planSubTitle.hide()
                    }

                }
                PaymentMethodName.BKASH.value->{
                    if (!viewModel.paymentMethod.value?.bkash?.topPromotionMsgForNonBl.isNullOrEmpty()){
                        binding.planSubTitle.show()
                        binding.planSubTitle.text = viewModel.paymentMethod.value?.bkash?.topPromotionMsgForNonBl
                    }else{
                        binding.planSubTitle.hide()
                    }
                }
                PaymentMethodName.SSL.value->{
                    if (!viewModel.paymentMethod.value?.ssl?.topPromotionMsgForNonBl.isNullOrEmpty()){
                        binding.planSubTitle.show()
                        binding.planSubTitle.text = viewModel.paymentMethod.value?.ssl?.topPromotionMsgForNonBl
                    }else{
                        binding.planSubTitle.hide()
                    }
                }

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        progressDialog.dismiss()
        _binding = null
    }
}