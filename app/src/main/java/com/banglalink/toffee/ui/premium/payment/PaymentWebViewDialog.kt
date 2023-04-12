package com.banglalink.toffee.ui.premium.payment

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.network.request.*
import com.banglalink.toffee.data.network.response.QueryPaymentResponse
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
import com.banglalink.toffee.usecase.BkashPaymentLogData
import com.banglalink.toffee.util.Log
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.currentDateTime
import com.banglalink.toffee.util.unsafeLazy
import com.medallia.digital.mobilesdk.MedalliaDigital
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import java.util.ResourceBundle.*
import javax.inject.Inject

@AndroidEntryPoint
class PaymentWebViewDialog : DialogFragment() {
    
    val TAG = "premium_log"
    private var retryCount = 0
    private var header: String? = ""
    private var title: String? = null
    private var htmlUrl: String? = null
    private var paymentId: String? = null
    private var statusCode: String? = null
    private var sessionToken: String? = null
    private var shareableUrl: String? = null
    private var statusMessage: String? = null
    private var transactionId: String? = null
    private var customerMsisdn: String? = null
    private var isHideBackIcon: Boolean = true
    private var isHideCloseIcon: Boolean = false
    private var isBkashBlRecharge: Boolean = false
    private var purchaseCallAfterRecharge: Boolean = false
    @Inject lateinit var cPref: CommonPreference
    @Inject lateinit var mPref: SessionPreference
    private var _binding: DialogHtmlPageViewBinding? = null
    private val binding get() = _binding!!
    private var queryPaymentResponse: QueryPaymentResponse? = null
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
        MedalliaDigital.disableIntercept()
        
        paymentId = arguments?.getString("paymentId")
        sessionToken = arguments?.getString("token")
        htmlUrl = arguments?.getString("url")
        header = arguments?.getString("header")
        title = arguments?.getString("myTitle", "Pack Details") ?: "Pack Details"
        shareableUrl = arguments?.getString("shareable_url")
        isHideBackIcon = arguments?.getBoolean("isHideBackIcon", true) ?: true
        isHideCloseIcon = arguments?.getBoolean("isHideCloseIcon", false) ?: false
        isBkashBlRecharge = arguments?.getBoolean("isBkashBlRecharge", false) ?: false
        purchaseCallAfterRecharge = arguments?.getBoolean("isPurchaseCallAfterRecharge", false) ?: true
        
        binding.titleTv.text = title
        if (isHideBackIcon) binding.backIcon.hide() else binding.backIcon.show()
        if (isHideCloseIcon) binding.closeIv.setImageResource(R.drawable.ic_toffee) else binding.closeIv.setImageResource(R.drawable.ic_close)
        observeTopBarBackground()
        
        if (htmlUrl == null || (!isBkashBlRecharge && sessionToken == null)) {
            requireContext().showToast(getString(R.string.try_again_message))
            findNavController().popBackStack()
        }
        
        binding.webview.webViewClient = object : WebViewClient() {
            
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.progressBar.visibility = View.VISIBLE
                Log.i(TAG, "onPageStarted: $url")
            }
            
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.i(TAG, "onPageFinished: $url")
                url?.let {
                    runCatching {
                        val uri = Uri.parse(it)
                        uri.getQueryParameter("paymentID")?.let {
                            paymentId = it
                        }
                        when {
                            it.contains("success") -> {
                                if (isBkashBlRecharge) {
                                    progressDialog.show()
                                    isBkashBlRecharge = false
                                    observeBlDataPackPurchase()
                                    purchaseBlDataPack()
                                }
                                else {
                                    progressDialog.show()
                                    executeBkashPayment()
                                }
                            }
                            it.contains("failure") || it.contains("fail") -> {
                                progressDialog.dismiss()
                                val args = bundleOf(
                                    ARG_STATUS_CODE to -1,
                                    ARG_STATUS_MESSAGE to "Your payment failed due to a technical error. Please try again."
                                )
                                navigateToStatusDialogPage(args)
                            }
                            it.contains("cancel") -> {
                                progressDialog.dismiss()
                                val args = bundleOf(
                                    ARG_STATUS_CODE to -1,
                                    ARG_STATUS_MESSAGE to "Payment canceled by user"
                                )
                                navigateToStatusDialogPage(args)
                            }
                        }
                    }
                }
            }
            
            override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?, ) {
                super.onReceivedHttpError(view, request, errorResponse)
                Log.i(TAG, "onReceivedHttpError: url: ${request?.url.toString()} \nstatusCode: ${errorResponse?.statusCode} \nerrorMessage: ${errorResponse?.reasonPhrase} \nheader: ${errorResponse?.responseHeaders}")
            }
            
            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                super.onReceivedSslError(view, handler, error)
                Log.i(TAG, "onReceivedSslError: errorCode: ${error?.primaryError}")
            }
            
            @RequiresApi(VERSION_CODES.O)
            override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                Log.i(TAG, "onRenderProcessGone: didCrash: ${detail?.didCrash()}")
                return super.onRenderProcessGone(view, detail)
            }
            
            @Deprecated("Deprecated in Java")
            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                Log.i(TAG, "onReceivedError: failingUrl: $failingUrl errorCode: $errorCode, errorMsg: $description")
            }
            
            @RequiresApi(VERSION_CODES.M)
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                Log.i(TAG, "onReceivedError: errorCode: ${error?.errorCode}, errorMsg: ${error?.description}")
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
            cacheMode = WebSettings.LOAD_DEFAULT
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
    
    private fun executeBkashPayment() {
        observe(viewModel.bKashExecutePaymentLiveData) { response ->
            when (response) {
                is Success -> {
                    statusCode = response.data.statusCode
                    statusMessage = response.data.statusMessage
                    transactionId = response.data.transactionId
                    customerMsisdn = response.data.customerMsisdn
                    
//                    if (response.data.transactionStatus != "Completed" && (response.data.statusCode == "2023" || response.data.statusCode == "2003" || response.data.statusCode == "503")) {
//                        progressDialog.dismiss()
//                        val args = bundleOf(
//                            ARG_STATUS_CODE to -1,
//                            ARG_STATUS_MESSAGE to statusMessage
//                        )
//                        navigateToStatusDialogPage(args)
//                        return@observe
//                    }
                    queryBkashPayment()
                }
                is Failure -> {
                    queryBkashPayment()
                }
            }
        }
        viewModel.bKashExecutePayment(sessionToken!!, ExecutePaymentRequest(paymentId = paymentId))
    }
    
    private fun queryBkashPayment() {
        observe(viewModel.bKashQueryPaymentLiveData) { response ->
            when (response) {
                is Success -> {
                    queryPaymentResponse = response.data.copy(
                        statusCode = statusCode,
                        statusMessage = statusMessage,
                        transactionId = transactionId,
                        customerMsisdn = customerMsisdn
                    )
                    viewModel.sendBkashPaymentLogData(BkashPaymentLogData(
                        id = System.currentTimeMillis().toString() + mPref.customerId,
                        callingApiName = "bkash-query-payment",
                        userId = mPref.customerId,
                        packId = viewModel.selectedPremiumPack.value?.id,
                        packTitle = viewModel.selectedPremiumPack.value?.packTitle.toString(),
                        dataPackId = viewModel.selectedDataPackOption.value?.dataPackId,
                        dataPackDetails = viewModel.selectedDataPackOption.value?.packDetails.toString(),
                        paymentMethodId = viewModel.selectedDataPackOption.value?.paymentMethodId.toString(),
                        loginMsisdn = mPref.phoneNumber,
                        paymentMsisdn = response.data.customerMsisdn,
                        paymentId = response.data.paymentID,
                        trxId = response.data.transactionId,
                        transactionStatus = response.data.transactionStatus,
                        amount = viewModel.selectedDataPackOption.value?.packPrice.toString(),
                        merchantInvoiceNumber = mPref.merchantInvoiceNumber,
                        rawResponse = response.data.toString()
                    ))
                    when (response.data.transactionStatus) {
                        "Completed" -> {
                            viewModel.bkashQueryPaymentData.value = queryPaymentResponse
                            callAndObserveBkashDataPackPurchase()
                        }
                        "Initiated" -> {
                            viewLifecycleOwner.lifecycleScope.launch {
                                if (retryCount < mPref.bkashApiRetryingCount) {
                                    retryCount++
                                    delay(mPref.bkashApiRetryingDuration)
                                    viewModel.bKashQueryPayment(sessionToken!!, QueryPaymentRequest(paymentID = paymentId))
                                } else {
                                    progressDialog.dismiss()
                                    val args = bundleOf(
                                        ARG_STATUS_CODE to -1, ARG_STATUS_MESSAGE to statusMessage
                                    )
                                    navigateToStatusDialogPage(args)
                                }
                            }
                        }
                        else -> {
                            progressDialog.dismiss()
                            val args = bundleOf(
                                ARG_STATUS_CODE to -1, ARG_STATUS_MESSAGE to statusMessage
                            )
                            navigateToStatusDialogPage(args)
                        }
                    }
                }
                is Failure -> {
                    progressDialog.dismiss()
                    val args = bundleOf(
                        ARG_STATUS_CODE to -1,
                        ARG_STATUS_MESSAGE to response.error.msg
                    )
                    navigateToStatusDialogPage(args)
                }
            }
        }
        viewModel.bKashQueryPayment(sessionToken!!, QueryPaymentRequest(paymentID = paymentId))
    }
    
    private fun navigateToStatusDialogPage(args: Bundle) {
        findNavController().popBackStack().let {
            findNavController().navigateTo(
                resId = R.id.paymentStatusDialog,
                args = args
            )
        }
    }
    
    private fun callAndObserveBkashDataPackPurchase() {
        if (viewModel.selectedPremiumPack.value != null && viewModel.selectedDataPackOption.value != null) {
            observe(viewModel.packPurchaseResponseCodeWebView) {
                progressDialog.dismiss()
                when (it) {
                    is Success -> {
                        if (it.data.status == PaymentStatusDialog.SUCCESS) {
                            mPref.activePremiumPackList.value = it.data.loginRelatedSubsHistory
                            navigateToStatusDialogPage(bundleOf(ARG_STATUS_CODE to 200))
                        }
                        else if(it.data.status == PaymentStatusDialog.UN_SUCCESS){
                            val args = bundleOf(
                                ARG_STATUS_CODE to 0,
                                ARG_STATUS_TITLE to "bKash Plan Activation Failed!",
                                ARG_STATUS_MESSAGE to "Due to some technical issue, the bKash plan activation failed. Please retry."
                            )
                            navigateToStatusDialogPage(args)
                        }
                    }
                    is Failure -> {
                        val args = bundleOf(
                            ARG_STATUS_CODE to 0,
                            ARG_STATUS_TITLE to "bKash Plan Activation Failed!",
                            ARG_STATUS_MESSAGE to it.error.msg
                        )
                        navigateToStatusDialogPage(args)
                    }
                }
            }
            val selectedPremiumPack = viewModel.selectedPremiumPack.value!!
            val selectedDataPack = viewModel.selectedDataPackOption.value!!
            
            queryPaymentResponse?.let {
                val dataPackPurchaseRequest = DataPackPurchaseRequest(
                    customerId = mPref.customerId,
                    password = mPref.password,
                    isBanglalinkNumber = (mPref.isBanglalinkNumber == "true").toInt(),
                    packId = selectedPremiumPack.id,
                    paymentMethodId = selectedDataPack.paymentMethodId ?: 0,
                    bKashDataPackId = selectedDataPack.dataPackId,

                    bKashRequest = BkashDataPackRequest(
                        amount = it.amount,
                        createTime = it.paymentCreateTime,
                        currency = it.currency,
                        customerMsisdn = it.customerMsisdn,
                        errorCode = null,
                        errorMessage = null,
                        intent = it.intent,
                        merchantInvoiceNumber = it.merchantInvoice,
                        paymentId = it.paymentID,
                        refundAmount = "0",
                        status = it.statusCode == "0000",
                        transactionStatus = it.transactionStatus,
                        transactionId = it.transactionId,
                        updateTime = currentDateTime
                    )
                )
                viewModel.purchaseDataPackWebView(dataPackPurchaseRequest)
            } ?: run {
                progressDialog.dismiss()
                requireContext().showToast(getString(R.string.try_again_message))
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
                purchaseCallAfterRecharge=true
            )
            viewModel.purchaseDataPackBlDataPackOptionsWeb(dataPackPurchaseRequest)
        } else {
            progressDialog.dismiss()
            requireContext().showToast(getString(R.string.try_again_message))
        }
    }
    
    private fun observeBlDataPackPurchase() {
        observe(viewModel.packPurchaseResponseCodeBlDataPackOptionsWeb) {
            progressDialog.dismiss()
            when (it) {
                is Success -> {
                    when (it.data.status) {
                        PaymentStatusDialog.SUCCESS -> {
                            mPref.activePremiumPackList.value = it.data.loginRelatedSubsHistory
                            viewModel.sendBkashPaymentLogData(BkashPaymentLogData(
                                id = System.currentTimeMillis().toString() + mPref.customerId,
                                callingApiName = "dataPackPurchase",
                                userId = mPref.customerId,
                                packId = viewModel.selectedPremiumPack.value?.id,
                                packTitle = viewModel.selectedPremiumPack.value?.packTitle.toString(),
                                dataPackId = viewModel.selectedDataPackOption.value?.dataPackId,
                                dataPackDetails = viewModel.selectedDataPackOption.value?.packDetails.toString(),
                                paymentMethodId = viewModel.selectedDataPackOption.value?.paymentMethodId.toString(),
                                loginMsisdn = mPref.phoneNumber,
                                paymentMsisdn = null,
                                paymentId = null,
                                trxId = null,
                                transactionStatus = null,
                                amount = viewModel.selectedDataPackOption.value?.packPrice.toString(),
                                merchantInvoiceNumber = mPref.merchantInvoiceNumber,
                                rawResponse = it.data.toString()
                            ))
                            val args = bundleOf(
                                ARG_STATUS_CODE to (it.data.status ?: 200)
                            )
                            navigateToStatusDialogPage(args)
                        }
                        PaymentStatusDialog.UN_SUCCESS -> {
                            val args = bundleOf(
                                ARG_STATUS_CODE to 0,
                                ARG_STATUS_TITLE to "Data Plan Purchase Failed!",
                                ARG_STATUS_MESSAGE to "Due to some technical issue, the data plan activation failed. Please retry."
                            )
                            navigateToStatusDialogPage(args)
                        }
                        else -> {
                            val args = bundleOf(
                                ARG_STATUS_CODE to (it.data.status ?: 0)
                            )
                            navigateToStatusDialogPage(args)
                        }
                    }
                }
                is Failure -> {
                    requireContext().showToast(it.error.msg)
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
        MedalliaDigital.enableIntercept()
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