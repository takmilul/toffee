package com.banglalink.toffee.ui.premium.payment

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.data.network.request.DataPackPurchaseRequest
import com.banglalink.toffee.databinding.FragmentReedemVoucherCodeBinding
import com.banglalink.toffee.extension.navigateTo
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.extension.toInt
import com.banglalink.toffee.extension.validateInput
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.ChildDialogFragment
import com.banglalink.toffee.ui.premium.PremiumViewModel
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.util.unsafeLazy

class RedeemVoucherCodeFragment : ChildDialogFragment() {

    private lateinit var binding: FragmentReedemVoucherCodeBinding
    private var voucherCode: String = ""
    private val viewModel by activityViewModels<PremiumViewModel>()
    private val progressDialog by unsafeLazy { ToffeeProgressDialog(requireContext()) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentReedemVoucherCodeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.giftVoucherCode.setCompoundDrawablesWithIntrinsicBounds(null,null, null, null)
        binding.backImg.safeClick({
            findNavController().popBackStack()
        })

        observeVoucherPurchase()
        observeVoucher()


        val spannableString = SpannableString("By clicking below button you are agreeing to Terms & Conditions")
        val clickTerms: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                showTermsAndConditionDialog()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }
        // Character starting from 45, - 63 is Terms & Conditions.
        spannableString.setSpan(clickTerms, 45, 63, 0)
        spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#FF3988")), 45, 63, 0)
        binding.termsAndConditionsOne.movementMethod = LinkMovementMethod.getInstance()
        binding.termsAndConditionsOne.setText(spannableString, TextView.BufferType.SPANNABLE)
        binding.termsAndConditionsOne.isSelected = true

        binding.redeemVoucherBtn.safeClick({
            val selectedPremiumPack = viewModel.selectedPremiumPack.value
            val selectedDataPackOption = viewModel.selectedDataPackOption.value
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
                    "pack_ID" to selectedPremiumPack?.id,
                    "pack_name" to selectedPremiumPack?.packTitle,
                    "currency" to "BDT",
                    "amount" to selectedDataPackOption?.packPrice,
                    "validity" to selectedPremiumPack?.expiryDate,
                    "provider" to "Coupon",
                    "type" to "coupon",
                    "subtype" to null,
                    "MNO" to MNO,
                    "discount" to null,
                )
            )

            voucherCode = binding.giftVoucherCode.text.toString().trim()

            if (voucherCode.isBlank()) {
                binding.giftVoucherCode.validateInput(
                    binding.tvGiftVoucherCodeError,
                    R.string.voucher_code_not_valid,
                    R.color.pink_to_accent_color,
                    R.drawable.error_single_line_input_text_bg_coupon
                )
                binding.tvGiftVoucherCodeError.text= getString(R.string.gift_voucher_enter_code)
                binding.tvGiftVoucherCodeError.visibility = View.VISIBLE
                binding.giftVoucherCode.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(requireContext(),R.drawable.ic_not_verified), null)
            }
            else{
                binding.giftVoucherCode.setBackgroundResource(R.drawable.error_solved_single_line_input_text_bg)
                binding.tvGiftVoucherCodeError.visibility = View.GONE
                binding.giftVoucherCode.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                progressDialog.show()
                viewModel.voucherValidate(viewModel.selectedPremiumPack.value!!.id, voucherCode, viewModel.selectedPremiumPack.value!!.packTitle.toString())
            }
        })

    }

    private fun observeVoucher() {

        observe(viewModel.voucherPaymentState) {
            when(it) {

                is Resource.Success -> {

                    if (it.data?.isValidVoucher==true && it.data!=null){

                        val selectedPremiumPack = viewModel.selectedPremiumPack.value!!

                        val dataPackPurchaseRequest = DataPackPurchaseRequest(

                            customerId = mPref.customerId,
                            password = mPref.password,
                            isBanglalinkNumber = (mPref.isBanglalinkNumber == "true").toInt(),
                            packId = selectedPremiumPack.id,
                            paymentMethodId = 12,
                            packTitle = selectedPremiumPack.packTitle,
                            contentList = selectedPremiumPack.contentId,
                            packCode = "",
                            packDetails = "",
                            packPrice = 0,
                            packDuration = it.data?.campaignsDuration ?: 0,

                            voucher = voucherCode,
                            partnerType = it.data?.partnerType,
                            partnerName = it.data?.partnerName,
                            partnerId =  it.data?.partnerId,
                            partnerCampaignsName = it.data?.partnerCampaignsName,
                            partnerCampaignsId = it.data?.partnerCampaignsId,
                            campaignsExpireDate = it.data?.campaignsExpireDate,
                            isPrepaid = if (mPref.isPrepaid) 1 else 0
                        )
                        viewModel.purchaseDataPackVoucher(dataPackPurchaseRequest)

                    }else{
                        binding.giftVoucherCode.validateInput(
                            binding.tvGiftVoucherCodeError,
                            R.string.voucher_code_not_valid,
                            R.color.pink_to_accent_color,
                            R.drawable.error_single_line_input_text_bg_coupon
                        )
                        binding.tvGiftVoucherCodeError.text= it.data!!.message
                        binding.tvGiftVoucherCodeError.visibility = View.VISIBLE
                        binding.giftVoucherCode.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(requireContext(),R.drawable.ic_not_verified), null)
                        progressDialog.dismiss()
                    }



                }
                is Resource.Failure -> {

                    progressDialog.dismiss()
                    requireContext().showToast(it.error.msg)

                }
            }
        }
    }

    private fun observeVoucherPurchase() {
        observe(viewModel.packPurchaseResponseVoucher) {
            progressDialog.dismiss()
            when (it) {
                is Resource.Success -> {
                    if (it.data?.status == PaymentStatusDialog.SUCCESS) {
                        val selectedPremiumPack = viewModel.selectedPremiumPack.value
                        val selectedDataPackOption = viewModel.selectedDataPackOption.value
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
                                "pack_ID" to selectedPremiumPack?.id,
                                "pack_name" to selectedPremiumPack?.packTitle,
                                "currency" to "BDT",
                                "amount" to selectedDataPackOption?.packPrice,
                                "validity" to selectedPremiumPack?.expiryDate,
                                "provider" to "Coupon",
                                "type" to "coupon",
                                "MNO" to MNO,
                            )
                        )
                        mPref.activePremiumPackList.value = it.data?.loginRelatedSubsHistory
                        val args = bundleOf(
                            PaymentStatusDialog.ARG_STATUS_CODE to (it.data?.status ?: 200),
                            PaymentStatusDialog.ARG_STATUS_TITLE to "Access Coupon Redemption is Successful",
                            PaymentStatusDialog.ARG_STATUS_MESSAGE to "Please wait while we redirect you"
                        )
                        findNavController().navigateTo(R.id.paymentStatusDialog, args)
                    }
                    else if (it.data?.status == PaymentStatusDialog.UN_SUCCESS) {
                        val selectedPremiumPack = viewModel.selectedPremiumPack.value
                        val selectedDataPackOption = viewModel.selectedDataPackOption.value
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
                                "pack_ID" to selectedPremiumPack?.id,
                                "pack_name" to selectedPremiumPack?.packTitle,
                                "currency" to "BDT",
                                "amount" to selectedDataPackOption?.packPrice,
                                "validity" to selectedPremiumPack?.expiryDate,
                                "provider" to "Coupon",
                                "type" to "coupon",
                                "MNO" to MNO,
                            )
                        )
                        val args = bundleOf(
                            PaymentStatusDialog.ARG_STATUS_CODE to  0,
                            PaymentStatusDialog.ARG_STATUS_TITLE to "Access Coupon Redemption Failed!",
                            PaymentStatusDialog.ARG_STATUS_MESSAGE to "Due to some technical error, the Coupon activation failed. Please retry"
                        )
                        findNavController().navigateTo(R.id.paymentStatusDialog, args)
                    } else {
                        requireContext().showToast(getString(R.string.try_again_message))
                    }
                }
                is Resource.Failure -> {
                    val selectedPremiumPack = viewModel.selectedPremiumPack.value
                    val selectedDataPackOption = viewModel.selectedDataPackOption.value
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
                            "pack_ID" to selectedPremiumPack?.id,
                            "pack_name" to selectedPremiumPack?.packTitle,
                            "currency" to "BDT",
                            "amount" to selectedDataPackOption?.packPrice,
                            "validity" to selectedPremiumPack?.expiryDate,
                            "provider" to "Coupon",
                            "type" to "coupon",
                            "MNO" to MNO,
                        )
                    )
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }
    private fun showTermsAndConditionDialog() {
        val args = bundleOf(
            "myTitle" to getString(R.string.terms_and_conditions),
            "url" to  mPref.blDataPackTermsAndConditionsUrl
        )
        findNavController().navigateTo(
            resId = R.id.htmlPageViewDialog,
            args = args
        )
    }
}