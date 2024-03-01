package com.banglalink.toffee.ui.premium

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.OptIn
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.SavedStateHandle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.findNavController
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.data.network.response.MnpStatusBean
import com.banglalink.toffee.data.network.response.PackPaymentMethodBean
import com.banglalink.toffee.data.network.response.PremiumPack
import com.banglalink.toffee.databinding.FragmentPremiumPackDetailsBinding
import com.banglalink.toffee.extension.checkVerification
import com.banglalink.toffee.extension.getBalloon
import com.banglalink.toffee.extension.getPurchasedPack
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.ifNotNullOrEmpty
import com.banglalink.toffee.extension.navigatePopUpTo
import com.banglalink.toffee.extension.navigateTo
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.ClickableAdInventories
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.notification.PubSubMessageUtil.coroutineScope
import com.banglalink.toffee.showAlignBottom
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeActivity
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.util.SingleLiveEvent
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.unsafeLazy
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class PremiumPackDetailsFragment : BaseFragment(){

    private var isFreeTrialOver = false
    private var _binding: FragmentPremiumPackDetailsBinding? = null
    val binding get() = _binding!!
    private val viewModel by activityViewModels<PremiumViewModel>()
    private val progressDialog by unsafeLazy { ToffeeProgressDialog(requireContext()) }
    private var openPlanDetails : Boolean? = false
    private var paymentMethods: PackPaymentMethodBean? = null
    private var isCheckVerification : Boolean = false
//    var selectedPackInfo = SingleLiveEvent<PremiumPack>()
    var selectedPackInfo : PremiumPack? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPremiumPackDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.progressBar.load(R.drawable.content_loader)


        selectedPackInfo =viewModel.selectedPremiumPack.value
        requireActivity().title = "Pack Details"

        observeMnpStatus()
        observePaymentMethodList()
        observePackStatus()
        observePremiumPackDetail()
        if (!checkPackPurchased()){
            triggerButtonSheet()
        }
//        triggerButtonSheet()


        /*
         checking pack purchase separately,
         when user comes from deeplink, first checking the pack is available or not. If available then checking pack purchase status
         Otherwise checking as normal flow
         */

        openPlanDetails =  arguments?.getBoolean("openPlanDetails")
        if (openPlanDetails == true){
            openPlanDetails = false
            val packId = arguments?.getInt("packId")
            val paymentMethodId = arguments?.getInt("paymentMethodId")
            val showBlPacks = arguments?.getBoolean("showBlPacks")

            // Storing the value to access it from different pages
            viewModel.clickableAdInventories.value = ClickableAdInventories(packId, paymentMethodId, showBlPacks)
            observeAndSelectPremiumPack(packId ?: 0)
        } else{
            viewModel.clickableAdInventories.value = null
            checkPackPurchased()
            binding.isVerifiedUser = mPref.isVerifiedUser
            binding.data = viewModel.selectedPremiumPack.value

            viewModel.selectedPremiumPack.value?.let {
                isFreeTrialOver = it.isAvailableFreePeriod == 1 && it.isPurchaseAvailable != 1 && mPref.activePremiumPackList.value?.any { activePack ->
                    it.id == activePack.packId && activePack.isTrialPackUsed
                } ?: false

                viewModel.getPremiumPackDetail(it.id)

            } ?: run {
                binding.progressBar.hide()
                requireContext().showToast(getString(R.string.try_again_message))
            }
        }

        with(binding) {
            if (isFreeTrialOver && mPref.isVerifiedUser) {
                payNowButton.alpha = 0.5f
                payNowButton.isEnabled = false
                payNowButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.disabled_text_color))
            }

            payNowButton.safeClick({
                triggerButtonSheet()
            })
        }

        /**
         * Observes `packDetailsPageRefreshRequired` LiveData and refreshes pack status if `true`.
         */
        observe(mPref.packDetailsPageRefreshRequired) { if (it == true) observePackStatusAfterSubscriberPayment() }


        /**
         * Observe to destrory the clickable ad inventories flow
         */
        observe(mPref.refreshRequiredForClickableAd) {
            if (it == true) {
                findNavController().navigatePopUpTo(
                    resId = R.id.packDetailsFragment,
                    popUpTo = R.id.packDetailsFragment,
                    inclusive = true
                )
            }
        }

        binding.infoIcon.safeClick({
            runCatching {
                it.showAlignBottom(
                    requireContext().getBalloon(
                        resources.getString(R.string.subscription_history_tooltip_text)
                    )
                )
            }
        })


//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//
//
//                if (isEnabled) {
//                    //sends firebase event for users aborting premPack after looking at contents.
//                    ToffeeAnalytics.toffeeLogEvent(
//                        ToffeeEvents.PACK_ABORT, bundleOf(
//                            "source" to if ( mPref.packSource.value==true)"content_click " else "premium_pack_menu",
//                            "pack_ID" to viewModel.selectedPremiumPack.value?.id.toString(),
//                            "pack_name" to viewModel.selectedPremiumPack.value?.packTitle,
//                            "reason" to "content",
//                            "action" to "goes back"
//                        )
//                    )
////                    findNavController().popBackStack()
//                    findNavController().navigatePopUpTo(R.id.packDetailsFragment)
////                    findNavController().navigatePopUpTo(R.id.premiumPackListFragment)
//                    isEnabled = false
//                    requireActivity().onBackPressed()
//                }
//            }
//        })

    }

    private fun observeAndSelectPremiumPack(packId:Int) {
        observe(viewModel.packListState) { response ->
            when(response) {
                is Success -> {
                    binding.progressBar.hide()
                    response.data.ifNotNullOrEmpty { premiumPacks ->
                        var isPackFound = false
                        premiumPacks.forEach { pack->
                            if (pack.id == packId){
                                isPackFound = true

                                viewModel.selectedPremiumPack.value = pack
                                checkPackPurchased()
                                binding.data = pack
                                binding.isVerifiedUser = mPref.isVerifiedUser

                                viewModel.selectedPremiumPack.value?.let {
                                    isFreeTrialOver = it.isAvailableFreePeriod == 1 && it.isPurchaseAvailable != 1 && mPref.activePremiumPackList.value?.any { activePack ->
                                        it.id == activePack.packId && activePack.isTrialPackUsed
                                    } ?: false

                                    viewModel.getPremiumPackDetail(it.id)

                                    // call payment methods
                                    progressDialog.show()
                                    if (!mPref.isMnpStatusChecked && mPref.isVerifiedUser && mPref.isMnpCallForSubscription) {
                                        viewModel.getMnpStatusForPaymentDetail()
                                    } else {
                                        viewModel.selectedPremiumPack.value?.let {
                                            viewModel.getPackStatus(0, it.id)
                                        } ?: run {
                                            progressDialog.dismiss()
                                            requireContext().showToast(getString(R.string.try_again_message))
                                        }
                                    }

                                } ?: run {
                                    binding.progressBar.hide()
                                    requireContext().showToast(getString(R.string.try_again_message))
                                }
                            }
                        }

                        if (!isPackFound){
                            viewModel.selectedPremiumPack.value = null
                            requireActivity().showToast(getString(R.string.selected_pack_not_found))
                            findNavController().navigatePopUpTo(R.id.premiumPackListFragment)
                        }
                    }
                }
                is Failure -> {
                    binding.progressBar.hide()
                    requireContext().showToast(response.error.msg)
                }
            }
        }
        viewModel.getPremiumPackList("0")
    }

    private fun observeMnpStatus() {
        observe(viewModel.mnpStatusLiveDataForPaymentDetail) { response ->
            when (response) {
                is Success -> {
                    if (response.data?.mnpStatus == 200){
                        mPref.isMnpStatusChecked = true
                        viewModel.selectedPremiumPack.value?.let {
                            viewModel.getPackStatus(0, it.id)
                        } ?: requireContext().showToast(getString(R.string.try_again_message))
                    }else{
                        progressDialog.dismiss()
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
        observe(viewModel.activePackListLiveData) {
            when(it) {
                is Success -> {
                    mPref.activePremiumPackList.value = it.data
                    val isPurchased = checkPackPurchased()
                    if (!isPurchased) {
                        viewModel.selectedPremiumPack.value?.id?.let {
                            viewModel.getPackPaymentMethodList(it)
                        } ?: run {
                            progressDialog.dismiss()
                            requireContext().showToast(getString(R.string.try_again_message))
                        }
                    } else {
                        progressDialog.dismiss()
                        findNavController().navigatePopUpTo(R.id.packDetailsFragment)
                    }
                }
                is Failure -> {
                    progressDialog.dismiss()
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }

    /**
     * Observes the pack status after a subscriber's payment is successfully processed.
     * This function updates the active premium pack list, checks for pack purchases, and navigates
     * to appropriate destinations based on the payment flow.
     */
    private fun observePackStatusAfterSubscriberPayment() {
        observe(viewModel.activePackListAfterSubscriberPaymentLiveData) { packStatusResult ->
            when (packStatusResult) {
                is Success -> {
                    // Update the active premium pack list with the received data
                    mPref.activePremiumPackList.value = packStatusResult.data

                    // Check if any premium packs have been purchased
                    checkPackPurchased()

                    // Navigate back to the premium pack list while preserving the back stack
                    findNavController().navigatePopUpTo(
                        resId = R.id.packDetailsFragment,
                        popUpTo = R.id.packDetailsFragment,
                        inclusive = true
                    )

                    // Determine the next destination based on the payment flow
                    if (mPref.prePurchaseClickedContent.value == null) {
                        // If no pre-purchase content was clicked, navigate to the "Start Watching" dialog
                        findNavController().navigateTo(R.id.startWatchingDialog)
                    } else {
                        // Clear the pre-purchase clicked content if available
                        mPref.prePurchaseClickedContent.value = null
                    }
                }
                is Failure -> {
                    // Dismiss the progress dialog and display an error message in case of failure
                    progressDialog.dismiss()
                    requireContext().showToast(packStatusResult.error.msg)
                }
            }
        }

        // Trigger the retrieval of pack status for the selected premium pack
        viewModel.selectedPremiumPack.value?.let {
            viewModel.getPackStatusAfterSubscriberPayment(0, it.id)
        }
    }

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
                    packDetail = if (activePack.isTrialPackUsed) activePack.packDetail else "You have bought ${activePack.packDetail} pack",

                )
                binding.isVerifiedUser = mPref.isVerifiedUser
                binding.data = viewModel.selectedPremiumPack.value
                true
            } ?: run {
                viewModel.selectedPremiumPack.value = selectedPack.copy(isPackPurchased = false)
                binding.isVerifiedUser = mPref.isVerifiedUser
                binding.data = viewModel.selectedPremiumPack.value
                false
            }
        } ?: false
    }

    private fun observePaymentMethodList() {
        observe(viewModel.paymentMethodState) {
            progressDialog.dismiss()
            when(it) {
                is Success -> {
                    paymentMethods = it.data
                    viewModel.paymentMethod.value = paymentMethods
                    findNavController().navigateTo(
                        resId = R.id.bottomSheetPaymentMethods,
                        args = bundleOf("paymentMethods" to paymentMethods)
                    )
                }
                is Failure -> {
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }


    private fun observePremiumPackDetail() {
        observe(viewModel.packDetailState) { response ->
            when (response) {
                is Success -> {
                    binding.progressBar.hide()

                    if (response.data!!.totalCount==0){
                        binding.premiumChannelGroup.hide()
                        binding.premiumContentGroup.hide()
                        binding.emptyView.show()

                    }

                    response.data?.linearChannelList?.ifNotNullOrEmpty {
                        binding.premiumChannelGroup.show()
                        binding.emptyView.hide()
                        viewModel.setLinearContentState(it.toList())
                    }
                    response.data?.vodChannelList?.ifNotNullOrEmpty {
                        Log.d("TAG", "observePremiumPackDetail: Four")
                        binding.premiumContentGroup.show()
                        binding.emptyView.hide()
                        viewModel.setVodContentState(it.toList())
                    }
                }
                is Failure -> {
                    Log.d("TAG", "observePremiumPackDetail: Five")
                    binding.progressBar.hide()
                    requireActivity().showToast(response.error.msg)
                }
            }
        }
    }

    private fun triggerButtonSheet(){

        //sends firebase event for users viewing payment methods.
        ToffeeAnalytics.toffeeLogEvent(
            ToffeeEvents.PACK_ACTIVE, bundleOf(
                "source" to if ( mPref.packSource.value==true)"content_click " else "premium_pack_menu",
                "pack_ID" to viewModel.selectedPremiumPack.value!!.id.toString(),
                "pack_name" to viewModel.selectedPremiumPack.value!!.packTitle
            )
        )
        mPref.signingFromPrem.value = true
        if (!mPref.isVerifiedUser){
            ToffeeAnalytics.toffeeLogEvent(
                ToffeeEvents.LOGIN_SOURCE,
                bundleOf(
                    "source" to "premium_pack_details",
                    "method" to "mobile"
                )
            )
        }
        requireActivity().checkVerification {
            progressDialog.show()
            if (!mPref.isMnpStatusChecked && mPref.isVerifiedUser && mPref.isMnpCallForSubscription) {
                viewModel.getMnpStatusForPaymentDetail()
            } else {
                viewModel.selectedPremiumPack.value?.let {
                    viewModel.getPackStatus(0, it.id)
                } ?: run {
                    progressDialog.hide()
                    requireContext().showToast(getString(R.string.try_again_message))
                }
            }
        }

    }

    override fun onDestroyView() {

        //sends firebase event for users aborting premPack after looking at contents.
        ToffeeAnalytics.toffeeLogEvent(
            ToffeeEvents.PACK_ABORT, bundleOf(
                "source" to if ( mPref.packSource.value==true)"content_click " else "premium_pack_menu",
                "pack_ID" to selectedPackInfo?.id.toString(),
                "pack_name" to selectedPackInfo?.packTitle,
                "reason" to "content",
                "action" to "goes back"
            )
        )

        super.onDestroyView()
        progressDialog.dismiss()
        _binding = null
    }
}