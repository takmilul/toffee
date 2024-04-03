package com.banglalink.toffee.ui.premium.payment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.banglalink.toffee.R
import com.banglalink.toffee.R.string
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.network.response.PackPaymentMethod
import com.banglalink.toffee.data.network.response.PackPaymentMethodData
import com.banglalink.toffee.databinding.FragmentPaymentMethodOptionsBinding
import com.banglalink.toffee.extension.navigateTo
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.ui.common.ChildDialogFragment
import com.banglalink.toffee.ui.premium.PremiumViewModel

class PaymentMethodOptionsFragment : ChildDialogFragment(), BaseListItemCallback<PackPaymentMethodData> {
    
    private var _binding: FragmentPaymentMethodOptionsBinding? = null
    private var subType: String? = null
    var isTrialPackUsed = false
    var blTrialPackMethod: PackPaymentMethod? = null
    var nonBlTrialPackMethod: PackPaymentMethod? = null
    private lateinit var mAdapter: PackPaymentMethodAdapter

    private val binding get() = _binding!!
    private val viewModel by activityViewModels<PremiumViewModel>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPaymentMethodOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    @SuppressLint("ResourceAsColor", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = PackPaymentMethodAdapter(mPref, cPref, viewModel, this)
        binding.packCardRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.packCardRecyclerView.adapter = mAdapter


        viewModel.paymentMethod.value?.let { paymentTypes ->
            val packPaymentMethodList : MutableList<PackPaymentMethodData> = mutableListOf()
            paymentTypes.free?.let {
                if (!it.data.isNullOrEmpty() && !it.paymentHeadline.isNullOrEmpty()){
                    packPaymentMethodList.add(
                        it.also {
                            it.paymentMethodName = "free"
                        }
                    )
                }
            }
            paymentTypes.voucher?.let {
                if (!it.data.isNullOrEmpty() && !it.paymentHeadline.isNullOrEmpty()){
                    packPaymentMethodList.add(
                        it.also {
                            it.paymentMethodName = "free"
                        }
                    )
                }
            }
            paymentTypes.bl?.let {
                if ((!it.prepaid.isNullOrEmpty() || !it.prepaid.isNullOrEmpty()) && !it.paymentHeadline.isNullOrEmpty()){
                    packPaymentMethodList.add(
                        it.also {
                            it.paymentMethodName = "blPack"
                        }
                    )
                }
            }
            paymentTypes.bkash?.let {
                if ((!it.blPacks.isNullOrEmpty() || !it.nonBlPacks.isNullOrEmpty()) && !it.paymentHeadline.isNullOrEmpty()){
                    packPaymentMethodList.add(
                        it.also {
                            it.paymentMethodName = "bkash"
                        }
                    )
                }
            }
            paymentTypes.ssl?.let {
                if ((!it.blPacks.isNullOrEmpty() || !it.nonBlPacks.isNullOrEmpty()) && !it.paymentHeadline.isNullOrEmpty()){
                    packPaymentMethodList.add(
                        it.also {
                            it.paymentMethodName = "bkash"
                        }
                    )
                }
            }
            paymentTypes.nagad?.let {
                if ((!it.blPacks.isNullOrEmpty() || !it.nonBlPacks.isNullOrEmpty()) && !it.paymentHeadline.isNullOrEmpty()){
                    packPaymentMethodList.add(
                        it.also {
                            it.paymentMethodName = "bkash"
                        }
                    )
                }
            }

            mAdapter.removeAll()
            mAdapter.addAll(packPaymentMethodList.sortedBy { it.orderIndex })

            with(binding) {

//                /**
//                 * This code manages the display and selection of trial pack options based on user preferences and availability.
//                 * It calculates extra validity, determines which trial pack to display, checks if the trial pack is used,
//                 * and handles user interactions with the trial pack card.
//                 */
//
//                if (!paymentTypes.free?.data.isNullOrEmpty()) {
//
//                    paymentTypes.free?.data?.forEach {
//                        if (it.isNonBlFree == 1) {
//                            nonBlTrialPackMethod = it
//                        } else {
//                            blTrialPackMethod = it
//                        }
//                    }
//
//                    var extraValidity = blTrialPackMethod?.packDuration?.minus(nonBlTrialPackMethod?.packDuration ?: 0) ?: 0
//
//                    if (extraValidity == blTrialPackMethod?.packDuration) {
//                        extraValidity = 0
//                    }
//                    trialDetails.isVisible = extraValidity > 0
//
//                    /**
//                     * This section handles the logics for trail packs title and sub-title depending on user being banglalink user or non-banglalink user.
//                     */
//
//                    if (blTrialPackMethod != null && mPref.isBanglalinkNumber == "true") {
//                        viewModel.selectedDataPackOption.value = blTrialPackMethod
//                        trialTitle.text = blTrialPackMethod!!.packDetails.toString()
//                        trialDetails.text = String.format(getString(string.extra_for_bl_users_text), extraValidity)
//                    } else if (nonBlTrialPackMethod != null && mPref.isBanglalinkNumber == "false") {
//                        viewModel.selectedDataPackOption.value = nonBlTrialPackMethod
//                        trialTitle.text = nonBlTrialPackMethod?.packDetails.toString()
//                        trialDetails.text = String.format(getString(string.extra_for_non_bl_users_text), extraValidity)
//                        trialDetails.setTextColor(ContextCompat.getColor(requireContext(), R.color.trial_extra_text_color))
//                    } else if (blTrialPackMethod != null){
//                        viewModel.selectedDataPackOption.value = blTrialPackMethod
//                        trialTitle.text = blTrialPackMethod!!.packDetails.toString()
//                        trialDetails.text = String.format(getString(string.extra_for_bl_users_text), extraValidity)
//                        trialCard.alpha = 0.3f
//                    } else if (nonBlTrialPackMethod != null){
//                        viewModel.selectedDataPackOption.value = nonBlTrialPackMethod
//                        trialTitle.text = nonBlTrialPackMethod?.packDetails.toString()
//                        trialDetails.text = String.format(getString(string.extra_for_non_bl_users_text), extraValidity)
//                        trialDetails.setTextColor(ContextCompat.getColor(requireContext(), R.color.trial_extra_text_color))
//                        trialCard.alpha = 0.3f
//                    }
//
//
//                    mPref.activePremiumPackList.value?.find {
//                        it.packId == viewModel.selectedPremiumPack.value?.id && it.isTrialPackUsed
//                    }?.let { isTrialPackUsed = true }
//
//                    if (isTrialPackUsed || (mPref.isBanglalinkNumber != "true" && nonBlTrialPackMethod == null)) {
//                        trialCard.alpha = 0.3f
//                    }
//
//                    trialCard.safeClick({
//                        if (isTrialPackUsed) {
//                            requireContext().showToast(getString(string.trial_already_availed_text))
//                        } else if (mPref.isBanglalinkNumber != "true" && nonBlTrialPackMethod == null) {
//                            requireContext().showToast(getString(string.only_for_bl_users))
//                        } else if (mPref.isBanglalinkNumber != "false" && blTrialPackMethod == null) {
//                            requireContext().showToast(getString(string.only_for_non_bl_users))
//                        } else {
//                            findNavController().navigateTo(R.id.activateTrialPackFragment)
//                        }
//                    })
//                } else {
//                    trialCard.hide()
//                }

//                /**
//                 * This code controls the visibility of the gift voucher card based on the availability of voucher options.
//                 * If there are no vouchers available, the card is hidden; otherwise, it is shown to the user.
//                 */
//
//                if (paymentTypes.voucher?.data.isNullOrEmpty()){
//                    giftVoucherCard.hide()
//                }else {
//                    giftVoucherCard.show()
//                }
//
//                /**
//                 * This code determines the starting price for Banglalink packages based on user preferences and available data.
//                 * It considers whether the user has a Banglalink number, whether it's prepaid or postpaid, and fetches the
//                 * minimum pack price accordingly to display to the user.
//                 */
//
//                val blStartingPrice = when {
//                    // If it's not a Banglalink number, check both postpaid and prepaid options
//                    mPref.isBanglalinkNumber == "false" -> {
//                        val postpaidMinPackPrice = paymentTypes.bl?.postpaid?.minOfOrNull { it.packPrice ?: 0 } ?: 0
//                        val prepaidMinPackPrice = paymentTypes.bl?.prepaid?.minOfOrNull { it.packPrice ?: 0 } ?: 0
//
//                        if (postpaidMinPackPrice == 0) {
//                            // If postpaidMinPackPrice is zero, consider prepaidMinPackPrice
//                            paymentTypes.bl?.prepaid?.minOfOrNull { it.packPrice ?: 0 } ?: 0
//                        } else if (prepaidMinPackPrice == 0) {
//                            // If prepaidMinPackPrice is zero, consider postpaidMinPackPrice
//                            paymentTypes.bl?.postpaid?.minOfOrNull { it.packPrice ?: 0 } ?: 0
//                        } else {
//                            // Choose the minimum between postpaidMinPackPrice and prepaidMinPackPrice
//                            minOf(postpaidMinPackPrice, prepaidMinPackPrice)
//                        }
//                    }
//                    // If it's a prepaid Banglalink number, consider only prepaid options
//                    mPref.isPrepaid -> {
//                        paymentTypes.bl?.prepaid?.minOfOrNull { it.packPrice ?: 0 } ?: 0
//                    }
//                    // If it's a postpaid Banglalink number, consider only postpaid options
//                    else -> {
//                        paymentTypes.bl?.postpaid?.minOfOrNull { it.packPrice ?: 0 } ?: 0
//                    }
//                }
//
//                blPackTitle.text = paymentTypes.bl?.paymentHeadline ?: ""
//                blPackPrice.text = if (mPref.isPrepaid) paymentTypes.bl?.paymentSubHeadlineOneForPrepaid ?: "" else paymentTypes.bl?.paymentSubHeadlineOneForPostpaid ?: ""
//
////                blPackPrice.text = String.format(getString(R.string.starting_price_bl), blStartingPrice.toString())
//
//                val isAvailableForBlUsers = mPref.isBanglalinkNumber == "true" && ((mPref.isPrepaid && !paymentTypes.bl?.prepaid.isNullOrEmpty()) || (!mPref.isPrepaid && !paymentTypes.bl?.postpaid.isNullOrEmpty()))
//                blPackCard.isVisible = paymentTypes.bl != null && (mPref.isBanglalinkNumber != "true" || isAvailableForBlUsers)
//
//                /**
//                 * This code determines the starting price for bKash packages based on user preferences and available data.
//                 * It calculates the starting price differently for Banglalink and non-Banglalink users,
//                 * sets the corresponding text view, and controls the visibility of the bKash package card based on availability.
//                 */
//
//                val bKashStartingPrice = if (mPref.isBanglalinkNumber == "true") {
//                    paymentTypes.bkash?.blPacks?.minOfOrNull { it.packPrice ?: 0 } ?: 0
//                } else {
//                    paymentTypes.bkash?.nonBlPacks?.minOfOrNull { it.packPrice ?: 0 } ?: 0
//                }
//
////                bkashPackPrice.text = String.format(getString(R.string.starting_price_bkash), bKashStartingPrice)
////                bkashPackPrice.isVisible = bKashStartingPrice > 0
//
//                bkashPackTitle.text = paymentTypes.bkash?.paymentHeadline ?: ""
//                bkashPackPrice.text = if (mPref.isBanglalinkNumber == "true") paymentTypes.bkash?.paymentSubHeadlineOneForBl ?: "" else paymentTypes.bkash?.paymentSubHeadlineOneForNonBl ?: ""
//                bkashPackPrice.isVisible = bKashStartingPrice > 0
//
//                val isBkashAvailable = paymentTypes.bkash != null && (mPref.isBanglalinkNumber == "true" && !paymentTypes.bkash?.blPacks.isNullOrEmpty()) || (mPref.isBanglalinkNumber == "false" && !paymentTypes.bkash?.nonBlPacks.isNullOrEmpty())
//                bKashPackCard.isVisible = isBkashAvailable
//
//                /**
//                 * This code determines the starting price for SSL payment packages based on user preferences and available data.
//                 * It calculates the starting price differently for Banglalink and non-Banglalink users,
//                 * sets the corresponding text view, and controls the visibility of the SSL payment package card based on availability.
//                 */
//
//                val sslStartingPrice = if (mPref.isBanglalinkNumber == "true") {
//                    paymentTypes.ssl?.blPacks?.minOfOrNull { it.packPrice ?: 0 } ?: 0
//                } else {
//                    paymentTypes.ssl?.nonBlPacks?.minOfOrNull { it.packPrice ?: 0 } ?: 0
//                }
//
////                SslPackPrice.text = String.format(getString(R.string.starting_price_ssl), sslStartingPrice)
////                SslPackPrice.isVisible = sslStartingPrice > 0
//
//                SslPackTitle.text = paymentTypes.ssl?.paymentHeadline ?: ""
//                SslPackPrice.text = if (mPref.isBanglalinkNumber == "true") paymentTypes.ssl?.paymentSubHeadlineOneForBl ?: "" else paymentTypes.ssl?.paymentSubHeadlineOneForNonBl ?: ""
//                SslPackPrice.isVisible = sslStartingPrice > 0
//
//                val isSslAvailable = paymentTypes.ssl != null && (mPref.isBanglalinkNumber == "true" && !paymentTypes.ssl?.blPacks.isNullOrEmpty()) || (mPref.isBanglalinkNumber == "false" && !paymentTypes.ssl?.nonBlPacks.isNullOrEmpty())
//                SslPackCard.isVisible = isSslAvailable
//
//
//                /**
//                 * This code determines the starting price for Nagad payment packages based on user preferences and available data.
//                 * It calculates the starting price differently for Banglalink and non-Banglalink users,
//                 * sets the corresponding text view, and controls the visibility of the Nagad payment package card based on availability.
//                 */
//
//                val nagadStartingPrice = if (mPref.isBanglalinkNumber == "true") {
//                    paymentTypes.nagad?.blPacks?.minOfOrNull { it.packPrice ?: 0 } ?: 0
//                } else {
//                    paymentTypes.nagad?.nonBlPacks?.minOfOrNull { it.packPrice ?: 0 } ?: 0
//                }
//
////                nagadPackPrice.text = String.format(getString(R.string.starting_price_ssl), nagadStartingPrice)
////                nagadPackPrice.isVisible = nagadStartingPrice > 0
//
//                nagadPackTitle.text = paymentTypes.nagad?.paymentHeadline ?: ""
//                nagadPackPrice.text = if (mPref.isBanglalinkNumber == "true") paymentTypes.nagad?.paymentSubHeadlineOneForBl ?: "" else paymentTypes.nagad?.paymentSubHeadlineOneForNonBl ?: ""
//                nagadPackPrice.isVisible = nagadStartingPrice > 0
//
//                val isNagadAvailable = paymentTypes.nagad != null && (mPref.isBanglalinkNumber == "true" && !paymentTypes.nagad?.blPacks.isNullOrEmpty()) || (mPref.isBanglalinkNumber == "false" && !paymentTypes.nagad?.nonBlPacks.isNullOrEmpty())
//                nagadPackCard.isVisible = isNagadAvailable
//
//                /**
//                 * These click handlers navigate to specific fragments based on the selected payment card.
//                 */
//
//                subType = when {
//                    (mPref.isBanglalinkNumber).toBoolean() && mPref.isPrepaid -> "prepaid"
//                    (mPref.isBanglalinkNumber).toBoolean() && !mPref.isPrepaid -> "postpaid"
//                    (!(mPref.isBanglalinkNumber).toBoolean()) -> "N/A"
//                    else -> null
//                }
//
//                viewModel.clickableAdInventories.value?.let {
//                    // navigating to destination by paymentMethodId from deep link
//                    when (it.paymentMethodId) {
//                        PaymentMethod.BKASH.value -> {
//                            findNavController().navigatePopUpTo(
//                                resId = R.id.paymentDataPackOptionsFragment,
//                                args = bundleOf("paymentName" to "bkash")
//                            )
//                        }
//                        PaymentMethod.NAGAD.value -> {
//                            findNavController().navigatePopUpTo(
//                                resId = R.id.paymentDataPackOptionsFragment,
//                                args = bundleOf("paymentName" to "nagad")
//                            )
//                        }
//                        PaymentMethod.BL_PACK.value -> {
//                            findNavController().navigatePopUpTo(
//                                resId = R.id.paymentDataPackOptionsFragment,
//                                args = bundleOf("paymentName" to "blPack")
//                            )
//
//                        }
//                        PaymentMethod.SSL.value -> {
//                            findNavController().navigatePopUpTo(
//                                resId = R.id.paymentDataPackOptionsFragment,
//                                args = bundleOf("paymentName" to "ssl")
//                            )
//                        }
//                        else -> {
//                            viewModel.clickableAdInventories.value = null
//                            requireActivity().showToast(getString(R.string.payment_method_invalid))
//                            mPref.refreshRequiredForClickableAd.value = true // refreshing pack details page to destroy this flow
//                            this@PaymentMethodOptionsFragment.closeDialog()
//                        }
//                    }
//
//                } ?: run {
//                    blPackCard.safeClick({
//                        findNavController().navigateTo(
//                            R.id.paymentDataPackOptionsFragment,
//                            bundleOf("paymentName" to "blPack")
//                        )
//                        //Send Log to FirebaseAnalytics
//                        ToffeeAnalytics.toffeeLogEvent(
//                            ToffeeEvents.PAYMENT_SELECTED,
//                            bundleOf(
//                                "pack_ID" to viewModel.selectedPremiumPack.value?.id.toString(),
//                                "pack_name" to viewModel.selectedPremiumPack.value?.packTitle.toString(),
//                                "provider" to "Banglalink",
//                                "type" to "data pack",
//                                "MNO" to if ((mPref.isBanglalinkNumber).toBoolean()) "BL" else "non-BL",
//                                "subtype" to subType,
//                            )
//                        )
//                    })
//
//                    bKashPackCard.safeClick({
//                        findNavController().navigateTo(
//                            R.id.paymentDataPackOptionsFragment,
//                            bundleOf("paymentName" to "bkash")
//                        )
//                        //Send Log to FirebaseAnalytics
//                        ToffeeAnalytics.toffeeLogEvent(
//                            ToffeeEvents.PAYMENT_SELECTED,
//                            bundleOf(
//                                "pack_ID" to viewModel.selectedPremiumPack.value?.id.toString(),
//                                "pack_name" to viewModel.selectedPremiumPack.value?.packTitle.toString(),
//                                "provider" to "bKash",
//                                "type" to "wallet",
//                                "MNO" to if ((mPref.isBanglalinkNumber).toBoolean()) "BL" else "non-BL",
//                                "subtype" to subType,
//                            )
//                        )
//                    })
//
//                    nagadPackCard.safeClick({
//                        findNavController().navigateTo(
//                            R.id.paymentDataPackOptionsFragment,
//                            bundleOf("paymentName" to "nagad")
//                        )
//                        //Send Log to FirebaseAnalytics
//                        ToffeeAnalytics.toffeeLogEvent(
//                            ToffeeEvents.PAYMENT_SELECTED,
//                            bundleOf(
//                                "pack_ID" to viewModel.selectedPremiumPack.value?.id.toString(),
//                                "pack_name" to viewModel.selectedPremiumPack.value?.packTitle.toString(),
//                                "provider" to "nagad",
//                                "type" to "wallet",
//                                "MNO" to if ((mPref.isBanglalinkNumber).toBoolean()) "BL" else "non-BL",
//                                "subtype" to subType,
//                            )
//                        )
//                    })
//
//                    SslPackCard.safeClick({
//                        findNavController().navigateTo(
//                            R.id.paymentDataPackOptionsFragment,
//                            bundleOf("paymentName" to "ssl")
//                        )
//                        //Send Log to FirebaseAnalytics
//                        ToffeeAnalytics.toffeeLogEvent(
//                            ToffeeEvents.PAYMENT_SELECTED,
//                            bundleOf(
//                                "pack_ID" to viewModel.selectedPremiumPack.value?.id.toString(),
//                                "pack_name" to viewModel.selectedPremiumPack.value?.packTitle.toString(),
//                                "provider" to "SSL Wireless",
//                                "type" to "aggregator",
//                                "MNO" to if ((mPref.isBanglalinkNumber).toBoolean()) "BL" else "non-BL",
//                                "subtype" to subType,
//                            )
//                        )
//                    })
//
//                    giftVoucherCard.safeClick({
//                        findNavController().navigateTo(
//                            R.id.reedemVoucherCodeFragment,
//                            bundleOf("paymentName" to "VOUCHER")
//                        )
//                        //Send Log to FirebaseAnalytics
//                        ToffeeAnalytics.toffeeLogEvent(
//                            ToffeeEvents.PAYMENT_SELECTED,
//                            bundleOf(
//                                "pack_ID" to viewModel.selectedPremiumPack.value?.id.toString(),
//                                "pack_name" to viewModel.selectedPremiumPack.value?.packTitle.toString(),
//                                "provider" to "Banglalink",
//                                "type" to "coupon",
//                                "MNO" to if ((mPref.isBanglalinkNumber).toBoolean()) "BL" else "non-BL",
//                                "subtype" to subType,
//                            )
//                        )
//                    })
//                }
            }
        }
    }

    override fun onItemClicked(item: PackPaymentMethodData) {
        super.onItemClicked(item)
        subType = when {
            (mPref.isBanglalinkNumber).toBoolean() && mPref.isPrepaid -> "prepaid"
            (mPref.isBanglalinkNumber).toBoolean() && !mPref.isPrepaid -> "postpaid"
            (!(mPref.isBanglalinkNumber).toBoolean()) -> "N/A"
            else -> null
        }

        when(item.paymentMethodName) {
            "free" -> {
                mPref.activePremiumPackList.value?.find {
                    it.packId == viewModel.selectedPremiumPack.value?.id && it.isTrialPackUsed
                }?.let { isTrialPackUsed = true }

                if (isTrialPackUsed) {
                    requireContext().showToast(getString(string.trial_already_availed_text))
                } else if (mPref.isBanglalinkNumber != "true" && nonBlTrialPackMethod == null) {
                    requireContext().showToast(getString(string.only_for_bl_users))
                } else if (mPref.isBanglalinkNumber != "false" && blTrialPackMethod == null) {
                    requireContext().showToast(getString(string.only_for_non_bl_users))
                } else {
                    findNavController().navigateTo(R.id.activateTrialPackFragment)
                }
            }

            "VOUCHER" -> {
                //Send Log to FirebaseAnalytics
                ToffeeAnalytics.toffeeLogEvent(
                    ToffeeEvents.PAYMENT_SELECTED,
                    bundleOf(
                        "pack_ID" to viewModel.selectedPremiumPack.value?.id.toString(),
                        "pack_name" to viewModel.selectedPremiumPack.value?.packTitle.toString(),
                        "provider" to "Banglalink",
                        "type" to "coupon",
                        "MNO" to if ((mPref.isBanglalinkNumber).toBoolean()) "BL" else "non-BL",
                        "subtype" to subType,
                    )
                )
                findNavController().navigateTo(
                    R.id.reedemVoucherCodeFragment,
                    bundleOf("paymentName" to item.paymentMethodName)
                )
            }

            "blPack" -> {
                //Send Log to FirebaseAnalytics
                ToffeeAnalytics.toffeeLogEvent(
                    ToffeeEvents.PAYMENT_SELECTED,
                    bundleOf(
                        "pack_ID" to viewModel.selectedPremiumPack.value?.id.toString(),
                        "pack_name" to viewModel.selectedPremiumPack.value?.packTitle.toString(),
                        "provider" to "Banglalink",
                        "type" to "data pack",
                        "MNO" to if ((mPref.isBanglalinkNumber).toBoolean()) "BL" else "non-BL",
                        "subtype" to subType,
                    )
                )
                findNavController().navigateTo(
                    R.id.paymentDataPackOptionsFragment,
                    bundleOf("paymentName" to item.paymentMethodName)
                )
            }

            "bkash" -> {
                //Send Log to FirebaseAnalytics
                ToffeeAnalytics.toffeeLogEvent(
                    ToffeeEvents.PAYMENT_SELECTED,
                    bundleOf(
                        "pack_ID" to viewModel.selectedPremiumPack.value?.id.toString(),
                        "pack_name" to viewModel.selectedPremiumPack.value?.packTitle.toString(),
                        "provider" to "bKash",
                        "type" to "wallet",
                        "MNO" to if ((mPref.isBanglalinkNumber).toBoolean()) "BL" else "non-BL",
                        "subtype" to subType,
                    )
                )
                findNavController().navigateTo(
                    R.id.paymentDataPackOptionsFragment,
                    bundleOf("paymentName" to item.paymentMethodName)
                )
            }

            "ssl" -> {
                //Send Log to FirebaseAnalytics
                ToffeeAnalytics.toffeeLogEvent(
                    ToffeeEvents.PAYMENT_SELECTED,
                    bundleOf(
                        "pack_ID" to viewModel.selectedPremiumPack.value?.id.toString(),
                        "pack_name" to viewModel.selectedPremiumPack.value?.packTitle.toString(),
                        "provider" to "SSL Wireless",
                        "type" to "aggregator",
                        "MNO" to if ((mPref.isBanglalinkNumber).toBoolean()) "BL" else "non-BL",
                        "subtype" to subType,
                    )
                )
                findNavController().navigateTo(
                    R.id.paymentDataPackOptionsFragment,
                    bundleOf("paymentName" to item.paymentMethodName)
                )
            }

            "nagad" -> {
                //Send Log to FirebaseAnalytics
                ToffeeAnalytics.toffeeLogEvent(
                    ToffeeEvents.PAYMENT_SELECTED,
                    bundleOf(
                        "pack_ID" to viewModel.selectedPremiumPack.value?.id.toString(),
                        "pack_name" to viewModel.selectedPremiumPack.value?.packTitle.toString(),
                        "provider" to "nagad",
                        "type" to "wallet",
                        "MNO" to if ((mPref.isBanglalinkNumber).toBoolean()) "BL" else "non-BL",
                        "subtype" to subType,
                    )
                )
                findNavController().navigateTo(
                    R.id.paymentDataPackOptionsFragment,
                    bundleOf("paymentName" to item.paymentMethodName)
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}