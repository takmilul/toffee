package com.banglalink.toffee.ui.premium.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.data.network.request.SubscriberPaymentInitRequest
import com.banglalink.toffee.data.network.response.DiscountInfo
import com.banglalink.toffee.databinding.FragmentSavedAccountBinding
import com.banglalink.toffee.extension.navigateTo
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.premium.PremiumViewModel
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.usecase.PaymentLogFromDeviceData
import com.banglalink.toffee.util.calculateDiscountedPrice
import com.banglalink.toffee.util.unsafeLazy
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@AndroidEntryPoint
class SavedAccountFragment : BaseFragment() {
    @Inject lateinit var json: Json
    private var paymentName: String? = null
    private var transactionIdentifier: String? = null
    private var paymentToken: String? = null
    private var walletNumber: String? = null
    private var paymentPurpose: String? = null
    private var statusCode: String? = null
    private var statusMessage: String? = null
    private lateinit var binding:FragmentSavedAccountBinding
    private val viewModel by activityViewModels<PremiumViewModel>()
    private val progressDialog by unsafeLazy { ToffeeProgressDialog(requireContext()) }
    private var packPriceToPay:Int?=null
    private var discountInfo: DiscountInfo?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSavedAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        paymentName = arguments?.getString("paymentName", "") ?: ""
        paymentToken = arguments?.getString("paymentToken", "") ?: ""
        walletNumber = arguments?.getString("walletNumber", "") ?: ""
        discountInfo = DiscountInfo(
            voucher = arguments?.getString("voucher"),
            campaignType = arguments?.getString("campaignType"),
            partnerName = arguments?.getString("partnerName"),
            partnerId = arguments?.getInt("partnerId"),
            campaignName = arguments?.getString("campaignName"),
            campaignId = arguments?.getInt("campaignId"),
            campaignTypeId = arguments?.getInt("campaignTypeId"),
            campaignExpireDate = arguments?.getString("campaignExpireDate"),
            voucherGeneratedType = arguments?.getInt("voucherGeneratedType")
        )
        observeSubscriberPaymentInit()

        binding.walletNumberTextView.text = walletNumber
        binding.confirmButton.safeClick({
            paymentPurpose = "ECOM_TOKEN_TXN"
            subscriberPaymentInit(paymentPurpose, paymentToken)
        })

        binding.useAnotherAcButton.safeClick({
            paymentPurpose = "ECOM_TXN"
            subscriberPaymentInit(paymentPurpose, null)
        })
        binding.backImg.safeClick({
            findNavController().popBackStack()
        })
    }


    private fun subscriberPaymentInit(paymentPurpose : String?, paymentToken: String?) {
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
                paymentPurpose = paymentPurpose,
                paymentToken = paymentToken,
                geoCity = mPref.geoCity,
                geoLocation = mPref.geoLocation,
                cusEmail = mPref.customerEmail,
                voucher = discountInfo?.voucher,
                campaign_type = discountInfo?.campaignType,
                partner_name = discountInfo?.partnerName,
                partner_id = discountInfo?.partnerId,
                campaign_name = discountInfo?.campaignName,
                campaign_id = discountInfo?.campaignId,
                campaign_type_id = discountInfo?.campaignTypeId,
                campaign_expire_date = discountInfo?.campaignExpireDate,
                voucher_generated_type = discountInfo?.voucherGeneratedType,
                discount = mPref.paymentDiscountPercentage.value?.toInt()?:0, // the percentage of discount applied
                original_price = selectedDataPackOption?.packPrice ?: 0, // actual pack price without discount or else
                dobPrice = viewModel.selectedDataPackOption.value?.dobPrice,
                dobCpId = viewModel.selectedDataPackOption.value?.dobCpId,
                dobSubsOfferId = viewModel.selectedDataPackOption.value?.dobSubsOfferId,
                isAutoRenew = viewModel.selectedDataPackOption.value?.isAutoRenew
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
                is Resource.Success -> {
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
                                paymentPurpose = paymentPurpose,
                                paymentRefId = if (paymentName == "nagad") transactionIdentifier else null,
                                transactionStatus = statusCode,
                                amount = packPriceToPay.toString(),
                                merchantInvoiceNumber = null,
                                rawResponse = json.encodeToString(it),
                                voucher = discountInfo?.voucher ,
                                campaignType = discountInfo?.campaignType ,
                                partnerName = discountInfo?.partnerName,
                                partnerId = discountInfo?.partnerId ?:0,
                                campaignName = discountInfo?.campaignName,
                                campaignId = discountInfo?.campaignId?:0,
                                campaignExpireDate = discountInfo?.campaignExpireDate,
                                discount = mPref.paymentDiscountPercentage.value.toString(),
                                originalPrice = viewModel.selectedDataPackOption.value?.packPrice.toString(),
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
                            "paymentPurpose" to paymentPurpose,
                            "isHideBackIcon" to false,
                            "isHideCloseIcon" to true,
                            "isBkashBlRecharge" to false,
                            "voucher" to discountInfo?.voucher,
                            "campaignType" to discountInfo?.campaignType,
                            "partnerName" to discountInfo?.partnerName,
                            "partnerId" to discountInfo?.partnerId,
                            "campaignName" to discountInfo?.campaignName,
                            "campaignId" to discountInfo?.campaignId,
                            "campaignExpireDate" to discountInfo?.campaignExpireDate
                        )
                        // Navigate to the payment WebView dialog
                        findNavController().navigateTo(R.id.paymentWebViewDialog, args)
                    } ?: requireContext().showToast(getString(R.string.try_again_message))
                }

                is Resource.Failure -> {
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
                            paymentPurpose = paymentPurpose,
                            paymentRefId = if (paymentName == "nagad") transactionIdentifier else null,
                            transactionStatus = statusCode,
                            amount = packPriceToPay.toString(),
                            merchantInvoiceNumber = null,
                            rawResponse = json.encodeToString(it),
                            voucher = discountInfo?.voucher ,
                            campaignType = discountInfo?.campaignType ,
                            partnerName = discountInfo?.partnerName,
                            partnerId = discountInfo?.partnerId ?:0,
                            campaignName = discountInfo?.campaignName,
                            campaignId = discountInfo?.campaignId?:0,
                            campaignExpireDate = discountInfo?.campaignExpireDate,
                            discount = mPref.paymentDiscountPercentage.value.toString(),
                            originalPrice = viewModel.selectedDataPackOption.value?.packPrice.toString(),
                        )
                    )
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }
}