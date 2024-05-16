package com.banglalink.toffee.ui.premium.payment

import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.data.network.request.DobValidateOtpRequest
import com.banglalink.toffee.data.network.request.SubscriberPaymentInitRequest
import com.banglalink.toffee.data.network.response.DiscountInfo
import com.banglalink.toffee.databinding.FragmentDcbEnterOtpBinding
import com.banglalink.toffee.extension.getPurchasedPack
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.invisible
import com.banglalink.toffee.extension.navigatePopUpTo
import com.banglalink.toffee.extension.navigateTo
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.ChildDialogFragment
import com.banglalink.toffee.ui.login.ResendCodeTimer
import com.banglalink.toffee.ui.premium.PremiumViewModel
import com.banglalink.toffee.ui.premium.payment.PaymentStatusDialog.Companion.SUBSCRIBER_PAYMENT_FAILED
import com.banglalink.toffee.ui.premium.payment.PaymentStatusDialog.Companion.SUCCESS
import com.banglalink.toffee.ui.premium.payment.PaymentStatusDialog.Companion.UN_SUCCESS
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.usecase.PaymentLogFromDeviceData
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.unsafeLazy
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class DcbEnterOtpFragment : ChildDialogFragment() {
    @Inject lateinit var json: Json
    private lateinit var binding : FragmentDcbEnterOtpBinding
    private var resendCodeTimer: ResendCodeTimer? = null
    private val viewModel by activityViewModels<PremiumViewModel>()
    private val progressDialog by unsafeLazy { ToffeeProgressDialog(requireContext()) }

    private var packPriceToPay:Int?=null
    private var requestId:String?=null
    private var paymentName = PaymentMethodString.BLDCB.value
    private var discountInfo: DiscountInfo?=null
    private var statusCode: String? = null
    private var statusMessage: String? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDcbEnterOtpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.sentOtpInfoTitle.text = "We have sent OTP to ${mPref.phoneNumber}. Please enter the OTP to confirm the purchase."

        startCountDown()
        observeDobValidateOtpApiResponse()
        observeSubscriberPaymentInit()

        requestId = arguments?.getString("requestId")
        packPriceToPay = arguments?.getInt("packPriceToPay")

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

        binding.resendButton.safeClick({
            subscriberPaymentInit()
        })



        binding.otpCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (binding.otpCode.text.length>=5){
                    binding.confirmBtn.isEnabled=true
                }else{
                    binding.confirmBtn.isEnabled=false
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.confirmBtn.safeClick({
            if (!binding.otpCode.text.isNullOrEmpty()){
                progressDialog.show()
                viewModel.dobValidateOtp(
                    DobValidateOtpRequest(
                        customerId = mPref.customerId,
                        password = mPref.password,
                        otp = binding.otpCode.text.toString(),
                        msisdn = mPref.phoneNumber,
                        requestId = requestId,
                        paymentMethodId = PaymentMethod.BL_PACK.value
                    )
                )
            } else {
                requireContext().showToast("Please enter the OTP")
            }
        })

        binding.backImg.safeClick({
            findNavController().popBackStack()
        })
    }

    private fun observeDobValidateOtpApiResponse(){
        observe(viewModel.dobValidateOtpResponse){
            when(it){
                is Resource.Success -> {
                    progressDialog.dismiss()

                    it.data?.let {
                        viewModel.sendPaymentLogFromDeviceData(
                            PaymentLogFromDeviceData(
                                id = System.currentTimeMillis() + mPref.customerId,
                                callingApiName = "${paymentName}ValidateOtp",
                                packId = viewModel.selectedPremiumPack.value?.id ?: 0,
                                packTitle = viewModel.selectedPremiumPack.value?.packTitle.toString(),
                                dataPackId = viewModel.selectedDataPackOption.value?.dataPackId ?: 0,
                                dataPackDetails = viewModel.selectedDataPackOption.value?.packDetails.toString(),
                                paymentMethodId = viewModel.selectedDataPackOption.value?.paymentMethodId ?: 0,
                                paymentMsisdn = null,
                                paymentPurpose = "ECOM_TXN",
                                paymentRefId = if (paymentName == "nagad") requestId else null,
                                paymentId = if (paymentName == "bkash") requestId else null,
                                transactionId = if (paymentName == "ssl") requestId else null,
                                requestId = if(paymentName == PaymentMethodString.BLDCB.value) requestId else null,
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
                                discount = mPref.paymentDiscountPercentage.value?.toInt()?:0,
                                originalPrice = viewModel.selectedDataPackOption.value?.packPrice ?: 0,
                                dobPrice = viewModel.selectedDataPackOption.value?.dobPrice,
                                dobCpId = viewModel.selectedDataPackOption.value?.dobCpId,
                                dobSubsOfferId = viewModel.selectedDataPackOption.value?.dobSubsOfferId,
                            )
                        )
                        if (it.status == true){
                            mPref.activePremiumPackList.value = it.loginRelatedSubsHistory

                            val args = bundleOf(
                                PaymentStatusDialog.ARG_STATUS_CODE to SUCCESS
                            )
                            findNavController().navigatePopUpTo(
                                resId = R.id.paymentStatusDialog,
                                popUpTo = R.id.paymentDataPackOptionsFragment,
                                inclusive = false,
                                args = args
                            )

                        }
                        else if(it.status == false && it.responseFromWhere == 1){ // invalid otp
                            requireContext().showToast(it.message.toString())
                        } else {
                            val args = bundleOf(
                                PaymentStatusDialog.ARG_STATUS_CODE to UN_SUCCESS,
                                PaymentStatusDialog.ARG_STATUS_TITLE to "Data Plan Activation Failed!",
                                PaymentStatusDialog.ARG_STATUS_MESSAGE to it.message
                            )
                            findNavController().navigatePopUpTo(
                                resId = R.id.paymentStatusDialog,
                                popUpTo = R.id.paymentDataPackOptionsFragment,
                                inclusive = false,
                                args = args
                            )
                        }
                    } ?: requireContext().showToast(getString(R.string.try_again_message))
                }
                is Resource.Failure -> {
                    progressDialog.dismiss()
                    requireContext().showToast(it.error.msg)

                    viewModel.sendPaymentLogFromDeviceData(
                        PaymentLogFromDeviceData(
                            id = System.currentTimeMillis() + mPref.customerId,
                            callingApiName = "${paymentName}ValidateOtp",
                            packId = viewModel.selectedPremiumPack.value?.id ?: 0,
                            packTitle = viewModel.selectedPremiumPack.value?.packTitle.toString(),
                            dataPackId = viewModel.selectedDataPackOption.value?.dataPackId ?: 0,
                            dataPackDetails = viewModel.selectedDataPackOption.value?.packDetails.toString(),
                            paymentMethodId = viewModel.selectedDataPackOption.value?.paymentMethodId ?: 0,
                            paymentMsisdn = null,
                            paymentPurpose = "ECOM_TXN",
                            paymentRefId = if (paymentName == "nagad") requestId else null,
                            paymentId = if (paymentName == "bkash") requestId else null,
                            transactionId = if (paymentName == "ssl") requestId else null,
                            requestId = if(paymentName == PaymentMethodString.BLDCB.value) requestId else null,
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
                            discount = mPref.paymentDiscountPercentage.value?.toInt()?:0,
                            originalPrice = viewModel.selectedDataPackOption.value?.packPrice ?: 0,
                            dobPrice = viewModel.selectedDataPackOption.value?.dobPrice,
                            dobCpId = viewModel.selectedDataPackOption.value?.dobCpId,
                            dobSubsOfferId = viewModel.selectedDataPackOption.value?.dobSubsOfferId,
                        )
                    )
                }
            }
        }
    }
    private fun handleResendButton(){
        binding.resendButton.hide()
        startCountDown()

    }

    private fun startCountDown() {
        binding.countdownTextView.show()
        resendCodeTimer?.cancel()
        resendCodeTimer = ResendCodeTimer(this, 1).also { timer ->
            observe(timer.tickLiveData) {
                val remainingSecs = it / 1000
                val minutes = (remainingSecs / 60).toInt()
                val seconds = (remainingSecs % 60).toInt()
                val timeText = (String.format("%02d", minutes) + ":" + String.format("%02d", seconds))
                val countDownText = String.format(getString(R.string.sign_in_countdown_text), timeText)
                val str = SpannableString(countDownText)
                str.setSpan(
                    StyleSpan(Typeface.BOLD), countDownText.indexOf(timeText), countDownText.indexOf(timeText) + timeText.length, Spannable
                    .SPAN_EXCLUSIVE_EXCLUSIVE)
                binding.countdownTextView.text = str
            }

            observe(timer.finishLiveData) {
                binding.resendButton.isEnabled = true
                binding.resendButton.show()
                binding.countdownTextView.invisible()
                binding.countdownTextView.text = ""

                timer.finishLiveData.removeObservers(this)
                timer.tickLiveData.removeObservers(this)
            }
        }
        resendCodeTimer?.start()
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
                packPrice = packPriceToPay?:0, // the amount user is paying after discount or else
                packDuration = selectedDataPackOption?.packDuration ?: 0,
                clientType = "MOBILE_APP",
                paymentPurpose = "ECOM_TXN",
                paymentToken = null,
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
                is Resource.Success -> {
                    it.data?.let {
                        requestId = it.transactionIdentifierId
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
                                paymentPurpose = "ECOM_TXN",
                                paymentRefId = if (paymentName == "nagad") requestId else null,
                                paymentId = if (paymentName == "bkash") requestId else null,
                                transactionId = if (paymentName == "ssl") requestId else null,
                                requestId = if(paymentName == PaymentMethodString.BLDCB.value) requestId else null,
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
                                discount = mPref.paymentDiscountPercentage.value?.toInt()?:0,
                                originalPrice = viewModel.selectedDataPackOption.value?.packPrice ?: 0,
                                dobPrice = viewModel.selectedDataPackOption.value?.dobPrice,
                                dobCpId = viewModel.selectedDataPackOption.value?.dobCpId,
                                dobSubsOfferId = viewModel.selectedDataPackOption.value?.dobSubsOfferId,
                            )
                        )

                        if (it.statusCode != 200) {
                            if (it.responseFromWhere == 2){ // show unsuccess popup
                                val args = bundleOf(
                                    PaymentStatusDialog.ARG_STATUS_CODE to SUBSCRIBER_PAYMENT_FAILED,
                                    PaymentStatusDialog.ARG_STATUS_MESSAGE to it.message
                                )
                                findNavController().navigatePopUpTo(
                                    resId = R.id.paymentStatusDialog,
                                    popUpTo = R.id.paymentDataPackOptionsFragment,
                                    inclusive = false,
                                    args = args
                                )
                            }else {
                                requireContext().showToast(it.message.toString())
                                return@observe
                            }
                        }
                        handleResendButton()
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
                            paymentPurpose = "ECOM_TXN",
                            paymentRefId = if (paymentName == "nagad") requestId else null,
                            paymentId = if (paymentName == "bkash") requestId else null,
                            transactionId = if (paymentName == "ssl") requestId else null,
                            requestId = if(paymentName == PaymentMethodString.BLDCB.value) requestId else null,
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
                            discount = mPref.paymentDiscountPercentage.value?.toInt()?:0,
                            originalPrice = viewModel.selectedDataPackOption.value?.packPrice ?: 0,
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
}