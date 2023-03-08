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
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.network.request.ExecutePaymentRequest
import com.banglalink.toffee.data.network.request.QueryPaymentRequest
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.databinding.DialogHtmlPageViewBinding
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.premium.PremiumViewModel
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.util.Log
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.unsafeLazy
import com.medallia.digital.mobilesdk.MedalliaDigital
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PremiumWebViewDialog : DialogFragment() {
    
    val TAG = "premium_log"
    
    private var title: String? = null
    private var header: String? = ""
    private var htmlUrl: String? = null
    private var paymentId: String? = null
    private var sessionToken: String? = null
    private var shareableUrl: String? = null
    private var isHideBackIcon: Boolean = true
    private var isHideCloseIcon: Boolean = false
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
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View {
        _binding = DialogHtmlPageViewBinding.inflate(layoutInflater)
        return binding.root
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?, ) {
        super.onViewCreated(view, savedInstanceState)
        MedalliaDigital.disableIntercept()
        
        paymentId = arguments?.getString("paymentId")
        sessionToken = arguments?.getString("token")
        htmlUrl = arguments?.getString("url")!!
        header = arguments?.getString("header")
        title = arguments?.getString("myTitle", "Toffee") ?: "Toffee"
        shareableUrl = arguments?.getString("shareable_url")
        isHideBackIcon = arguments?.getBoolean("isHideBackIcon", true) ?: true
        isHideCloseIcon = arguments?.getBoolean("isHideCloseIcon", false) ?: false
        
        binding.titleTv.text = title
        if (isHideBackIcon) binding.backIcon.hide() else binding.backIcon.show()
        if (isHideCloseIcon) binding.closeIv.setImageResource(R.drawable.ic_toffee) else binding.closeIv.setImageResource(R.drawable.ic_close)
        observeTopBarBackground()
        
        binding.webview.webViewClient = object : WebViewClient() {
            
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?, ) {
                super.onPageStarted(view, url, favicon)
                binding.progressBar.visibility = View.VISIBLE
                Log.i(TAG, "onPageStarted: $url")
            }
            
            override fun onPageFinished(view: WebView?, url: String?, ) {
                super.onPageFinished(view, url)
                Log.i(TAG, "onPageFinished: $url")
                url?.let {
                    runCatching {
                        val uri = Uri.parse(it)
                        val status = uri.getQueryParameter("status")
                        if (status == "success" && !sessionToken.isNullOrBlank() && !paymentId.isNullOrBlank()) {
                            progressDialog.show()
                            executeBkashPayment()
                        }
                    }
                }
            }
            
            override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?, ) {
                super.onReceivedHttpError(view, request, errorResponse)
                Log.i(TAG, "onReceivedHttpError: url: ${request?.url.toString()} \nstatusCode: ${errorResponse?.statusCode} \nerrorMessage: ${errorResponse?.reasonPhrase} \nheader: ${errorResponse?.responseHeaders}")
            }
            
            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?, ) {
                super.onReceivedSslError(view, handler, error)
                Log.i(TAG, "onReceivedSslError: errorCode: ${error?.primaryError}")
            }
            
            @RequiresApi(VERSION_CODES.O)
            override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?, ): Boolean {
                Log.i(TAG, "onRenderProcessGone: didCrash: ${detail?.didCrash()}")
                return super.onRenderProcessGone(view, detail)
            }
            
            @Deprecated("Deprecated in Java")
            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?, ) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                Log.i(TAG, "onReceivedError: failingUrl: $failingUrl errorCode: $errorCode, errorMsg: $description")
            }
            
            @RequiresApi(VERSION_CODES.M)
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?, ) {
                Log.i(TAG, "onReceivedError: errorCode: ${error?.errorCode}, errorMsg: ${error?.description}")
            }
        }
        
        binding.webview.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int, ) {
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
        observe(viewModel.bKashExecutePaymentState) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data.transactionStatus != "Completed") {
                        queryBkashPayment()
                    } else {
                        progressDialog.hide()
                        requireContext().showToast(response.data.statusMessage)
                        dialog?.dismiss()
                    }
                }
                is Resource.Failure -> {
                    queryBkashPayment()
                    requireContext().showToast("Something went to wrong")
                }
            }
        }
        viewModel.bKashExecutePayment(sessionToken!!, ExecutePaymentRequest(paymentId = paymentId))
    }
    
    private fun queryBkashPayment() {
        observe(viewModel.bKashQueryPaymentState) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data.transactionStatus != "Completed") {
                        dialog?.dismiss()
                        requireContext().showToast(response.data.statusMessage)
                    }
                }
                is Resource.Failure -> {
                    progressDialog.hide()
                    requireContext().showToast("Something went to wrong")
                }
            }
        }
        viewModel.bKashQueryPayment(sessionToken!!, QueryPaymentRequest(paymentID = paymentId))
    }
    
    private fun observeTopBarBackground() {
        val isActive = try {
            mPref.isTopBarActive && Utils.getDate(mPref.topBarStartDate).before(mPref.getSystemTime()) && Utils.getDate(mPref.topBarEndDate).after(mPref.getSystemTime())
        } catch (e: Exception) {
            false
        }
        if (isActive) {
            if (mPref.topBarType == "png") {
                lifecycleScope.launch(Dispatchers.IO) {
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
        _binding = null
    }
}