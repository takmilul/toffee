package com.banglalink.toffee.ui.premium.payment

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.R.string
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.data.network.request.*
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.databinding.DialogHtmlPageViewBinding
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.premium.PremiumViewModel
import com.banglalink.toffee.ui.premium.payment.PaymentStatusDialog.Companion.ARG_STATUS_CODE
import com.banglalink.toffee.ui.premium.payment.PaymentStatusDialog.Companion.ARG_STATUS_MESSAGE
import com.banglalink.toffee.ui.premium.payment.PaymentStatusDialog.Companion.ARG_STATUS_TITLE
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.usecase.PaymentLogFromDeviceData
import com.banglalink.toffee.util.Log
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.unsafeLazy
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import java.util.ResourceBundle.*
import javax.inject.Inject

@AndroidEntryPoint
class PaymentWebViewDialog : DialogFragment() {
    
    val TAG = "premium_log"
    @Inject lateinit var json: Json
    private var retryCountBLDataPackPurchase = 0
    private var header: String? = ""
    private var title: String? = null
    private var htmlUrl: String? = null
    private var paymentId: String? = null
    private var paymentMethodId: Int? = null
    private var paymentPurpose: String? = null
    private var paymentTypeFromAddAccount: String? = null
    private var paymentType: String? = null
    private var callBackStatus: String? = null
    private var statusCode: String? = null
    private var sessionToken: String? = null
    private var shareableUrl: String? = null
    private var statusMessage: String? = null
    private var transactionStatus: String? = null
    private var transactionIdentifier: String? = null
    private var transactionId: String? = null
    private var customerMsisdn: String? = null
    private var isHideBackIcon: Boolean = true
    private var isVisibilityHideCloseIcon: Boolean = true
    private var isHideCloseIcon: Boolean = false
    private var isBkashBlRecharge: Boolean = false
    private var purchaseCallAfterRecharge: Boolean = false
    @Inject lateinit var cPref: CommonPreference
    @Inject lateinit var mPref: SessionPreference
    private var _binding: DialogHtmlPageViewBinding? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<PremiumViewModel>()
    private val progressDialog by unsafeLazy { ToffeeProgressDialog(requireContext()) }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogHtmlPageViewBinding.inflate(layoutInflater)
        return binding.root
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        MedalliaDigital.disableIntercept()

        paymentId = arguments?.getString("paymentId")
        paymentMethodId = arguments?.getInt("paymentMethodId")
        paymentPurpose = arguments?.getString("paymentPurpose")
        paymentTypeFromAddAccount = arguments?.getString("paymentType")
        sessionToken = arguments?.getString("token")
        htmlUrl = arguments?.getString("url")
        header = arguments?.getString("header")
        title = arguments?.getString("myTitle", "Pack Details") ?: "Pack Details"
        shareableUrl = arguments?.getString("shareable_url")
        isHideBackIcon = arguments?.getBoolean("isHideBackIcon", true) ?: true
        isHideCloseIcon = arguments?.getBoolean("isHideCloseIcon", false) ?: false
        isVisibilityHideCloseIcon = arguments?.getBoolean("isVisibilityHideCloseIcon", false) ?: false
        isBkashBlRecharge = arguments?.getBoolean("isBkashBlRecharge", false) ?: false
        purchaseCallAfterRecharge = arguments?.getBoolean("isPurchaseCallAfterRecharge", false) ?: true
        
        binding.titleTv.text = title
        if (isHideBackIcon) binding.backIcon.hide() else binding.backIcon.show()
//        if (isHideCloseIcon) binding.closeIv.setImageResource(R.drawable.ic_toffee) else binding.closeIv.setImageResource(R.drawable.ic_close)
        binding.closeIv.visibility = if (isVisibilityHideCloseIcon) View.GONE else View.VISIBLE
        if (!isVisibilityHideCloseIcon) {
            if (isHideCloseIcon) binding.closeIv.setImageResource(R.drawable.ic_toffee) else binding.closeIv.setImageResource(R.drawable.ic_close)
        }
        observeTopBarBackground()
        
        if (htmlUrl == null) {
            requireContext().showToast(getString(R.string.try_again_message))
            findNavController().popBackStack()
        }
        
        binding.webview.webViewClient = object : WebViewClient() {
            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
                Log.i(TAG, "shouldOverrideUrlLoading: $url")

                return when {
                    url.startsWith("http:") || url.startsWith("https:") -> {
                        // If the URL starts with "http:" or "https:", allow the WebView to handle it.
                        false
                    }
                    url.startsWith("tel:") -> {
                        // If the URL starts with "tel:", create an intent to dial the phone number.
                        val tel = Intent(Intent.ACTION_DIAL, Uri.parse(url))
                        startActivity(tel) // Start the dialer activity.
                        true // Indicate that the URL has been handled.
                    }
                    url.startsWith("mailto:") -> {
                        // If the URL starts with "mailto:", extract the email address.
                        val email = url.substringAfter("mailto:")
                        val mail = Intent(Intent.ACTION_SEND)
                        mail.type = "message/rfc822" // Set the MIME type for email.
                        mail.putExtra(Intent.EXTRA_EMAIL, arrayOf(email)) // Set the recipient email address.
                        try {
                            startActivity(mail) // Start the mail app.
                        } catch (e: ActivityNotFoundException) {
                            requireContext().showToast(getString(R.string.try_again_message))
                        }
                        true // Indicate that the URL has been handled.
                    }
                    else -> {
                        // If none of the above conditions are met, allow the WebView to handle the URL.
                        true
                    }
                }
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.progressBar.visibility = View.VISIBLE
                Log.i(TAG, "onPageStarted: $url")
            }

            /**
             * This function is responsible for handling the onPageFinished event in a WebView,
             * particularly for processing payment callback data from a URL and managing various payment scenarios.
             * It parses the URL's query parameters and takes appropriate actions based on the payment status.
             */
            
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.i(TAG, "onPageFinished: $url")

                // Parse the URL and extract query parameters
                url?.let {
                    runCatching {
                        with(Uri.parse(it)) {
                            paymentId = getQueryParameter("paymentID")
                            statusCode = getQueryParameter("statusCode")
                            statusMessage = getQueryParameter("message")
                            transactionIdentifier = getQueryParameter("transactionIdentifier")
                            paymentType = getQueryParameter("paymentType")
                            callBackStatus = getQueryParameter("callBackStatus")
                        }

                        // Handle different payment scenarios based on callback data
                        when {
                            // Handle successful payments or success callbacks
                            (it.contains("success") || callBackStatus == "success") -> {
                                if (isBkashBlRecharge) {
                                    // Handle specific actions for Banglalink recharge
                                    progressDialog.show()

                                    isBkashBlRecharge = false
                                    observeBlDataPackPurchase()
                                    purchaseBlDataPack()
                                }
                                else {
                                    // Handle specific actions for bKask or SSL
                                    progressDialog.show()

                                    //Send Log to the Pub/Sub
                                    viewModel.sendPaymentLogFromDeviceData(PaymentLogFromDeviceData(
                                        id = System.currentTimeMillis() + mPref.customerId,
                                        callingApiName = "${paymentType}SubscriberPaymentRedirectFromAndroid",
                                        packId = if (paymentType == "nagadAddAccount") 0 else viewModel.selectedPremiumPack.value?.id ?: 0,
                                        packTitle = if (paymentType == "nagadAddAccount") null else viewModel.selectedPremiumPack.value?.packTitle.toString(),
                                        dataPackId = if (paymentType == "nagadAddAccount") 0 else viewModel.selectedDataPackOption.value?.dataPackId ?: 0,
                                        dataPackDetails = if (paymentType == "nagadAddAccount") null else viewModel.selectedDataPackOption.value?.packDetails.toString(),
                                        paymentMethodId = (if (paymentType == "nagadAddAccount") paymentMethodId else viewModel.selectedDataPackOption.value?.paymentMethodId ?: 0)!!,
                                        paymentMsisdn = null,
                                        paymentPurpose = if (paymentType == "nagad" || paymentType == "nagadAddAccount") paymentPurpose else null,
                                        paymentRefId = if (paymentType == "nagad" || paymentType == "nagadAddAccount") transactionIdentifier else null,
                                        paymentId = if (paymentType == "bkash") transactionIdentifier else null,
                                        transactionId = if (paymentType == "ssl") transactionIdentifier else null,
                                        transactionStatus = statusCode,
                                        amount = if (paymentType == "nagadAddAccount") "0" else viewModel.selectedDataPackOption.value?.packPrice.toString() ?: "0",
                                        merchantInvoiceNumber = null,
                                        rawResponse = url.toString()
                                    ))

                                    // Navigate or perform actions based on payment type and status code
                                    when (paymentType) {
                                        "ssl" -> {
                                            when (statusCode) {
                                                "200" -> {
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
                                                            "provider" to "SSL Wireless",
                                                            "type" to "aggregator",
                                                            "reason" to "N/A",
                                                            "MNO" to MNO,
                                                        )
                                                    )
                                                    // If statusCode is 200, create args with status code 200 and navigate
                                                    val args = bundleOf(ARG_STATUS_CODE to 200)
                                                    navigateToStatusDialogPage(args)
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
                                                            "provider" to "SSL Wireless",
                                                            "type" to "aggregator",
                                                            "reason" to statusMessage,
                                                            "MNO" to MNO,
                                                        )
                                                    )
                                                    // For any other statusCode, use the default args and navigate
                                                    val args = bundleOf(
                                                        ARG_STATUS_CODE to -2,
                                                        ARG_STATUS_MESSAGE to statusMessage
                                                    )
                                                    navigateToStatusDialogPage(args)
                                                }
                                            }
                                        }
                                        "bkash" -> {
                                            when (statusCode) {
                                                "200" -> {
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
                                                            "provider" to "bKash",
                                                            "type" to "wallet",
                                                            "reason" to "N/A",
                                                            "MNO" to MNO,
                                                        )
                                                    )
                                                    // If statusCode is 200, create args with status code 200 and navigate
                                                    val args = bundleOf(ARG_STATUS_CODE to 200)
                                                    navigateToStatusDialogPage(args)
                                                }
                                                "277" -> {
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
                                                            "provider" to "bKash",
                                                            "type" to "wallet",
                                                            "reason" to statusMessage,
                                                            "MNO" to MNO,
                                                        )
                                                    )
                                                    // If statusCode is 277, create args with status code -2 and the status message
                                                    val args = bundleOf(
                                                        ARG_STATUS_CODE to -2,
                                                        ARG_STATUS_MESSAGE to statusMessage
                                                    )
                                                    navigateToStatusDialogPage(args)
                                                }
                                                "2029", "2062" -> {
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
                                                            "provider" to "bKash",
                                                            "type" to "wallet",
                                                            "reason" to statusMessage,
                                                            "MNO" to MNO,
                                                        )
                                                    )
                                                    // If statusCode is 2029 or 2062, create args with status code -3 and the status message
                                                    val args = bundleOf(
                                                        ARG_STATUS_CODE to -3,
                                                        ARG_STATUS_MESSAGE to statusMessage
                                                    )
                                                    navigateToStatusDialogPage(args)
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
                                                            "provider" to "bKash",
                                                            "type" to "wallet",
                                                            "reason" to statusMessage,
                                                            "MNO" to MNO,
                                                        )
                                                    )
                                                    // For any other statusCode, use the default args and navigate
                                                    val args = bundleOf(
                                                        ARG_STATUS_CODE to -2,
                                                        ARG_STATUS_MESSAGE to statusMessage
                                                    )
                                                    navigateToStatusDialogPage(args)
                                                }
                                            }
                                        }

                                        "nagad" -> {
                                            when (statusCode) {
                                                "200" -> {
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
                                                            "provider" to "nagad",
                                                            "type" to "wallet",
                                                            "reason" to "N/A",
                                                            "MNO" to MNO,
                                                        )
                                                    )
                                                    // If statusCode is 200, create args with status code 200 and navigate
                                                    val args = bundleOf(ARG_STATUS_CODE to 200)
                                                    navigateToStatusDialogPage(args)
                                                }
                                                "277" -> {
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
                                                            "provider" to "nagad",
                                                            "type" to "wallet",
                                                            "reason" to statusMessage,
                                                            "MNO" to MNO,
                                                        )
                                                    )
                                                    // If statusCode is 277, create args with status code -2 and the status message
                                                    val args = bundleOf(
                                                        ARG_STATUS_CODE to -2,
                                                        ARG_STATUS_MESSAGE to statusMessage
                                                    )
                                                    navigateToStatusDialogPage(args)
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
                                                            "provider" to "nagad",
                                                            "type" to "wallet",
                                                            "reason" to statusMessage,
                                                            "MNO" to MNO,
                                                        )
                                                    )
                                                    // For any other statusCode, use the default args and navigate
                                                    val args = bundleOf(
                                                        ARG_STATUS_CODE to -2,
                                                        ARG_STATUS_MESSAGE to statusMessage
                                                    )
                                                    navigateToStatusDialogPage(args)
                                                }
                                            }
                                        }

                                        "nagadAddAccount" -> {
                                            val MNO = when {
                                                (mPref.isBanglalinkNumber).toBoolean() && mPref.isPrepaid -> "BL-prepaid"
                                                (mPref.isBanglalinkNumber).toBoolean() && !mPref.isPrepaid -> "BL-postpaid"
                                                (!(mPref.isBanglalinkNumber).toBoolean()) -> "non-BL"
                                                else -> "N/A"
                                            }
                                            when (statusCode) {
                                                "200" -> {
                                                    requireContext().showToast("Payment method added successfully!")
                                                    // Send Log to FirebaseAnalytics
                                                    ToffeeAnalytics.toffeeLogEvent(
                                                        ToffeeEvents.PACK_SUCCESS,
                                                        bundleOf(
                                                            "pack_ID" to viewModel.selectedPremiumPack.value?.id.toString(),
                                                            "pack_name" to viewModel.selectedPremiumPack.value?.packTitle.toString(),
                                                            "currency" to "BDT",
                                                            "amount" to viewModel.selectedDataPackOption.value?.packPrice.toString(),
                                                            "validity" to viewModel.selectedDataPackOption.value?.packDuration.toString(),
                                                            "provider" to "nagadAddAccount",
                                                            "type" to "wallet",
                                                            "reason" to "N/A",
                                                            "MNO" to MNO,
                                                        )
                                                    )
                                                    mPref.isManagePaymentPageReloaded.value = true
                                                    dialog?.dismiss()
                                                }
                                                else -> {
                                                    // Send Log to FirebaseAnalytics
                                                    ToffeeAnalytics.toffeeLogEvent(
                                                        ToffeeEvents.PACK_SUCCESS,
                                                        bundleOf(
                                                            "pack_ID" to viewModel.selectedPremiumPack.value?.id.toString(),
                                                            "pack_name" to viewModel.selectedPremiumPack.value?.packTitle.toString(),
                                                            "currency" to "BDT",
                                                            "amount" to viewModel.selectedDataPackOption.value?.packPrice.toString(),
                                                            "validity" to viewModel.selectedDataPackOption.value?.packDuration.toString(),
                                                            "provider" to "nagadAddAccount",
                                                            "type" to "wallet",
                                                            "reason" to "N/A",
                                                            "MNO" to MNO,
                                                        )
                                                    )
                                                    requireContext().showToast(statusMessage)
                                                    dialog?.dismiss()
                                                }
                                            }
                                        }

                                        else -> {}
                                    }
                                }
                            }

                            // Handle payment failure scenarios for Banglalink recharge
                            (it.contains("recharge-fail") && !it.contains("callBackStatus")) -> {
                                progressDialog.dismiss()

                                //Send Log to the Pub/Sub
                                viewModel.sendPaymentLogFromDeviceData(PaymentLogFromDeviceData(
                                    id = System.currentTimeMillis() + mPref.customerId,
                                    callingApiName = "bkash-recharge-failure-callback",
                                    packId = viewModel.selectedPremiumPack.value?.id ?: 0,
                                    packTitle = viewModel.selectedPremiumPack.value?.packTitle.toString(),
                                    dataPackId = viewModel.selectedDataPackOption.value?.dataPackId ?: 0,
                                    dataPackDetails = viewModel.selectedDataPackOption.value?.packDetails.toString(),
                                    paymentMethodId = viewModel.selectedDataPackOption.value?.paymentMethodId ?: 0,
                                    paymentMsisdn = null,
                                    paymentId = paymentId,
                                    transactionId = null,
                                    transactionStatus = null,
                                    amount = viewModel.selectedDataPackOption.value?.packPrice.toString(),
                                    merchantInvoiceNumber = mPref.merchantInvoiceNumber,
                                    rawResponse = getString(string.payment_failed_message)
                                ))
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
                                        "type" to "recharge",
                                        "reason" to string.payment_failed_message,
                                        "MNO" to MNO,
                                    )
                                )
                                val args = bundleOf(
                                    ARG_STATUS_CODE to -1,
                                    ARG_STATUS_MESSAGE to getString(string.payment_failed_message)
                                )
                                navigateToStatusDialogPage(args)
                            }

                            // Handle payment cancellation scenarios for Banglalink recharge
                            (it.contains("recharge-cancel") && !it.contains("callBackStatus")) -> {
                                progressDialog.dismiss()

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
                                        "provider" to if(paymentType == "ssl") "SSL Wireless" else "bKash",
                                        "type" to "wallet",
                                        "reason" to "Payment canceled by user",
                                        "MNO" to MNO,
                                    )
                                )
                                //Send Log to the Pub/Sub
                                viewModel.sendPaymentLogFromDeviceData(PaymentLogFromDeviceData(
                                    id = System.currentTimeMillis() + mPref.customerId,
                                    callingApiName = "bkash-recharge-cancel-callback",
                                    packId = viewModel.selectedPremiumPack.value?.id ?: 0,
                                    packTitle = viewModel.selectedPremiumPack.value?.packTitle.toString(),
                                    dataPackId = viewModel.selectedDataPackOption.value?.dataPackId ?: 0,
                                    dataPackDetails = viewModel.selectedDataPackOption.value?.packDetails.toString(),
                                    paymentMethodId = viewModel.selectedDataPackOption.value?.paymentMethodId ?: 0,
                                    paymentMsisdn = null,
                                    paymentId = paymentId,
                                    transactionId = null,
                                    transactionStatus = null,
                                    amount = viewModel.selectedDataPackOption.value?.packPrice.toString(),
                                    merchantInvoiceNumber = mPref.merchantInvoiceNumber,
                                    rawResponse = getString(string.payment_canceled_message)
                                ))
                                val args = bundleOf(
                                    ARG_STATUS_CODE to -1,
                                    ARG_STATUS_MESSAGE to getString(string.payment_canceled_message)
                                )
                                navigateToStatusDialogPage(args)
                            }

                            // Handle failure or cancellation callbacks for bKask or SSL
                            (callBackStatus == "failure" || callBackStatus == "failed" || callBackStatus == "cancel" || callBackStatus == "aborted") -> {
                                progressDialog.dismiss()

                                //Send Log to the Pub/Sub
                                viewModel.sendPaymentLogFromDeviceData(PaymentLogFromDeviceData(
                                    id = System.currentTimeMillis() + mPref.customerId,
                                    callingApiName = "${paymentType}SubscriberPaymentRedirectFromAndroid",
                                    packId = if (paymentType == "nagadAddAccount") 0 else viewModel.selectedPremiumPack.value?.id ?: 0,
                                    packTitle = if (paymentType == "nagadAddAccount") null else viewModel.selectedPremiumPack.value?.packTitle.toString(),
                                    dataPackId = if (paymentType == "nagadAddAccount") 0 else viewModel.selectedDataPackOption.value?.dataPackId ?: 0,
                                    dataPackDetails = if (paymentType == "nagadAddAccount") null else viewModel.selectedDataPackOption.value?.packDetails.toString(),
                                    paymentMethodId = (if (paymentType == "nagadAddAccount") paymentMethodId else viewModel.selectedDataPackOption.value?.paymentMethodId ?: 0)!!,
                                    paymentMsisdn = null,
                                    paymentPurpose = if (paymentType == "nagad" || paymentType == "nagadAddAccount") paymentPurpose else null,
                                    paymentRefId = if (paymentType == "nagad" || paymentType == "nagadAddAccount") transactionIdentifier else null,
                                    paymentId = if (paymentType == "bkash") transactionIdentifier else null,
                                    transactionId = if (paymentType == "ssl") transactionIdentifier else null,
                                    transactionStatus = statusCode,
                                    amount = if (paymentType == "nagadAddAccount") "0" else viewModel.selectedDataPackOption.value?.packPrice.toString() ?: "0",
                                    merchantInvoiceNumber = null,
                                    rawResponse = url.toString()
                                ))

                                if (callBackStatus == "failure" || callBackStatus == "failed"){
                                    val MNO = when {
                                        (mPref.isBanglalinkNumber).toBoolean() && mPref.isPrepaid -> "BL-prepaid"
                                        (mPref.isBanglalinkNumber).toBoolean() && !mPref.isPrepaid -> "BL-postpaid"
                                        (!(mPref.isBanglalinkNumber).toBoolean()) -> "non-BL"
                                        else -> "N/A"
                                    }
                                    val provider = if (paymentType == "ssl") "SSL Wireless" else if (paymentType == "nagad") "Nagad" else "bKash"
                                    // Send Log to FirebaseAnalytics
                                    ToffeeAnalytics.toffeeLogEvent(
                                        ToffeeEvents.PACK_ERROR,
                                        bundleOf(
                                            "pack_ID" to viewModel.selectedPremiumPack.value?.id.toString(),
                                            "pack_name" to viewModel.selectedPremiumPack.value?.packTitle.toString(),
                                            "currency" to "BDT",
                                            "amount" to viewModel.selectedDataPackOption.value?.packPrice.toString(),
                                            "validity" to viewModel.selectedDataPackOption.value?.packDuration.toString(),
                                            "provider" to provider,
                                            "type" to "wallet",
                                            "reason" to statusMessage,
                                            "MNO" to MNO,
                                        )
                                    )
                                }
                                else{
                                    val MNO = when {
                                        (mPref.isBanglalinkNumber).toBoolean() && mPref.isPrepaid -> "BL-prepaid"
                                        (mPref.isBanglalinkNumber).toBoolean() && !mPref.isPrepaid -> "BL-postpaid"
                                        (!(mPref.isBanglalinkNumber).toBoolean()) -> "non-BL"
                                        else -> "N/A"
                                    }
                                    val provider = if (paymentType == "ssl") "SSL Wireless" else if (paymentType == "nagad") "Nagad" else "bKash"
                                    // Send Log to FirebaseAnalytics
                                    ToffeeAnalytics.toffeeLogEvent(
                                        ToffeeEvents.PACK_ERROR,
                                        bundleOf(
                                            "pack_ID" to viewModel.selectedPremiumPack.value?.id.toString(),
                                            "pack_name" to viewModel.selectedPremiumPack.value?.packTitle.toString(),
                                            "currency" to "BDT",
                                            "amount" to viewModel.selectedDataPackOption.value?.packPrice.toString(),
                                            "validity" to viewModel.selectedDataPackOption.value?.packDuration.toString(),
                                            "provider" to provider,
                                            "type" to "wallet",
                                            "reason" to "Payment canceled by user",
                                            "MNO" to MNO,
                                        )
                                    )
                                }

                                when {
                                    paymentType == "ssl" && statusCode != "200" -> {
                                        dialog?.dismiss()
                                    }
                                    paymentType == "bkash" && statusCode != "200" -> {
                                        val args = bundleOf(
                                            ARG_STATUS_CODE to -1,
                                            ARG_STATUS_MESSAGE to statusMessage
                                        )
                                        navigateToStatusDialogPage(args)
                                    }
                                    paymentType == "nagad" && statusCode != "200" -> {
                                        val args = bundleOf(
                                            ARG_STATUS_CODE to -4,
                                            ARG_STATUS_MESSAGE to statusMessage
                                        )
                                        navigateToStatusDialogPage(args)
                                    }
                                    paymentType == "nagadAddAccount" && statusCode != "200" -> {
                                        if (callBackStatus == "failed"){
                                            viewModel.isTokenizedAccountInitFailed.value = true
                                        } else {
                                            requireContext().showToast(statusMessage)
                                        }
                                        dialog?.dismiss()
                                    }
                                    else -> {}
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
        
        binding.webview.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                binding.progressBar.progress = newProgress
                if (newProgress == 100) {
                    binding.progressBar.visibility = View.GONE
                }
            }
            
            override fun onPermissionRequest(request: PermissionRequest) {
                val resources = request.resources
                for (resource in resources) {
                    if (PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID == resource) {
                        request.grant(resources)
                        return
                    }
                }
                super.onPermissionRequest(request)
            }
        }
        
        with(binding.webview.settings) {
            setSupportZoom(true)
            databaseEnabled = true
            useWideViewPort = true
            domStorageEnabled = true
            javaScriptEnabled = true
            setNeedInitialFocus(false)
            builtInZoomControls = true
            displayZoomControls = false
            setSupportMultipleWindows(false)
            cacheMode = WebSettings.LOAD_NO_CACHE
            mediaPlaybackRequiresUserGesture = false
            javaScriptCanOpenWindowsAutomatically = true
            CookieManager.getInstance().setAcceptCookie(true)
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            userAgentString = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.63 Mobile Safari/537.36"
        }
        binding.webview.scrollBarStyle = WebView.SCROLLBARS_INSIDE_OVERLAY
        htmlUrl?.let {
            if (header.isNullOrEmpty()) {
                binding.webview.loadUrl(it)
            } else {
                val headerMap: MutableMap<String, String> = HashMap()
                headerMap["MSISDN"] = header!!
                binding.webview.loadUrl(it, headerMap)
            }
        }
        
        runCatching {
            binding.closeIv.setOnClickListener {
                dialog?.dismiss()
            }
            binding.backIcon.setOnClickListener {
                dialog?.dismiss()
            }
            if (title == getString(R.string.back_to_toffee_text)) {
                binding.titleTv.setOnClickListener {
                    dialog?.dismiss()
                }
            }
        }
    }
    
    private fun navigateToStatusDialogPage(args: Bundle) {
        findNavController().popBackStack().let {
            findNavController().navigateTo(
                resId = R.id.paymentStatusDialog,
                args = args
            )
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
                purchaseCallAfterRecharge=true,
                isPrepaid = if (mPref.isPrepaid==true) 1 else 0
            )
            viewModel.purchaseDataPackBlDataPackOptionsWeb(dataPackPurchaseRequest)
        } else {
            progressDialog.dismiss()
            requireContext().showToast(getString(R.string.try_again_message))
        }
    }
    
    private fun observeBlDataPackPurchase() {
        observe(viewModel.packPurchaseResponseCodeBlDataPackOptionsWeb) {
            when (it) {
                is Success -> {
                    Log.i("Retry_BlDataPackPurchase", "Success BlDataPackPurchase")
                    progressDialog.dismiss()
                    viewModel.sendPaymentLogFromDeviceData(PaymentLogFromDeviceData(
                        id = System.currentTimeMillis() + mPref.customerId,
                        callingApiName = "dataPackPurchase",
                        packId = viewModel.selectedPremiumPack.value?.id ?: 0,
                        packTitle = viewModel.selectedPremiumPack.value?.packTitle.toString(),
                        dataPackId = viewModel.selectedDataPackOption.value?.dataPackId ?: 0,
                        dataPackDetails = viewModel.selectedDataPackOption.value?.packDetails.toString(),
                        paymentMethodId = viewModel.selectedDataPackOption.value?.paymentMethodId ?: 0,
                        paymentMsisdn = null,
                        paymentId = paymentId,
                        transactionId = null,
                        transactionStatus = null,
                        amount = viewModel.selectedDataPackOption.value?.packPrice.toString(),
                        merchantInvoiceNumber = mPref.merchantInvoiceNumber,
                        rawResponse = json.encodeToString(it.data)
                    ))
                    when (it.data.status) {
                        PaymentStatusDialog.SUCCESS -> {
                            mPref.activePremiumPackList.value = it.data.loginRelatedSubsHistory
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
                                    "pack_ID" to viewModel.selectedPremiumPack.value?.id,
                                    "pack_name" to viewModel.selectedPremiumPack.value?.packTitle,
                                    "currency" to "BDT",
                                    "amount" to viewModel.selectedDataPackOption.value?.packPrice.toString(),
                                    "validity" to viewModel.selectedPremiumPack.value?.expiryDate,
                                    "provider" to "Banglalink",
                                    "type" to "recharge",
                                    "reason" to "N/A",
                                    "MNO" to MNO,
                                )
                            )
                            val args = bundleOf(
                                ARG_STATUS_CODE to (it.data.status ?: 200)
                            )
                            navigateToStatusDialogPage(args)
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
                                    "pack_ID" to viewModel.selectedPremiumPack.value?.id,
                                    "pack_name" to viewModel.selectedPremiumPack.value?.packTitle,
                                    "currency" to "BDT",
                                    "amount" to viewModel.selectedDataPackOption.value?.packPrice.toString(),
                                    "validity" to viewModel.selectedPremiumPack.value?.expiryDate,
                                    "provider" to "Banglalink",
                                    "type" to "recharge",
                                    "reason" to R.string.due_some_technical_issue,
                                    "MNO" to MNO,
                                )
                            )
                            val args = bundleOf(
                                ARG_STATUS_CODE to (it.data.status ?: 0)
                            )
                            navigateToStatusDialogPage(args)
                        }
                    }
                }
                is Failure -> {
                    Log.i("Retry_BlDataPackPurchase", "Failure BlDataPackPurchase")
                    viewModel.sendPaymentLogFromDeviceData(PaymentLogFromDeviceData(
                        id = System.currentTimeMillis() + mPref.customerId,
                        callingApiName = "dataPackPurchase",
                        packId = viewModel.selectedPremiumPack.value?.id ?: 0,
                        packTitle = viewModel.selectedPremiumPack.value?.packTitle.toString(),
                        dataPackId = viewModel.selectedDataPackOption.value?.dataPackId ?: 0,
                        dataPackDetails = viewModel.selectedDataPackOption.value?.packDetails.toString(),
                        paymentMethodId = viewModel.selectedDataPackOption.value?.paymentMethodId ?: 0,
                        paymentMsisdn = customerMsisdn,
                        paymentId = paymentId,
                        transactionId = transactionId,
                        transactionStatus = transactionStatus,
                        amount = viewModel.selectedDataPackOption.value?.packPrice.toString(),
                        merchantInvoiceNumber = mPref.merchantInvoiceNumber,
                        rawResponse = "${json.encodeToString(it.error.msg)}, RetryCountDataPackPurchase: $retryCountBLDataPackPurchase, RetryingDuration: ${mPref.bkashApiRetryingDuration}"
                    ))
                    viewLifecycleOwner.lifecycleScope.launch {
                        if (retryCountBLDataPackPurchase < mPref.bkashApiRetryingCount) {
                            retryCountBLDataPackPurchase++
                            Log.i("Retry_BlDataPackPurchase", retryCountBLDataPackPurchase.toString())
                            delay(mPref.bkashApiRetryingDuration)
                            observeBlDataPackPurchase()
                            purchaseBlDataPack()
                        }
                        else {
                            progressDialog.dismiss()
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
                                    "pack_ID" to viewModel.selectedPremiumPack.value?.id,
                                    "pack_name" to viewModel.selectedPremiumPack.value?.packTitle,
                                    "currency" to "BDT",
                                    "amount" to viewModel.selectedDataPackOption.value?.packPrice.toString(),
                                    "validity" to viewModel.selectedPremiumPack.value?.expiryDate,
                                    "provider" to "Banglalink",
                                    "type" to "recharge",
                                    "reason" to it.error.msg,
                                    "MNO" to MNO,
                                )
                            )
                            val args = bundleOf(
                                ARG_STATUS_CODE to 0,
                                ARG_STATUS_TITLE to "Data Plan Activation Failed!",
                                ARG_STATUS_MESSAGE to it.error.msg
                            )
                            navigateToStatusDialogPage(args)
                        }
                    }
                }
            }
        }
    }
    
    private fun observeTopBarBackground() {
        val isActive = try {
            mPref.isTopBarActive && Utils.getDate(mPref.topBarStartDate).before(mPref.getSystemTime()) && Utils.getDate(mPref.topBarEndDate).after(mPref.getSystemTime())
        } catch (e: Exception) {
            false
        }
        if (isActive) {
            if (mPref.topBarType == "png") {
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val imagePath = if (cPref.appThemeMode == Configuration.UI_MODE_NIGHT_NO) mPref.topBarImagePathLight else mPref.topBarImagePathDark
                        if (!imagePath.isNullOrBlank()) {
                            binding.toolbarImageView.load(imagePath)
                        }
                    } catch (e: Exception) {
                        ToffeeAnalytics.logException(e)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.let {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            it.window?.setLayout(width, height)
        }
    }
    
    override fun onDestroyView() {
//        MedalliaDigital.enableIntercept()
        binding.webview.run {
            clearCache(false)
            stopLoading()
            onPause()
            webChromeClient = null
            clearHistory()
            removeAllViews()
            destroy()
        }
        super.onDestroyView()
        progressDialog.dismiss()
        _binding = null
    }
}