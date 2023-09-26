package com.banglalink.toffee.ui.common

import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.databinding.DialogHtmlPageViewBinding
import com.banglalink.toffee.enums.WebActionType.CLOSE_APP
import com.banglalink.toffee.enums.WebActionType.DEEP_LINK
import com.banglalink.toffee.enums.WebActionType.FORCE_LOGOUT
import com.banglalink.toffee.enums.WebActionType.HOME_SCREEN
import com.banglalink.toffee.enums.WebActionType.LOGIN_DIALOG
import com.banglalink.toffee.enums.WebActionType.MESSAGE_DIALOG
import com.banglalink.toffee.enums.WebActionType.PLAY_CONTENT
import com.banglalink.toffee.extension.appTheme
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.util.Utils
import com.medallia.digital.mobilesdk.MedalliaDigital
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.system.exitProcess

@AndroidEntryPoint
class HtmlPageViewDialog : DialogFragment() {
    
    private var title: String? = null
    private var header: String? = ""
    private var htmlUrl: String? = null
    private var shareableUrl: String? = null
    private var isHideBackIcon: Boolean = true
    private var isHideCloseIcon: Boolean = false
    @Inject lateinit var cPref: CommonPreference
    @Inject lateinit var mPref: SessionPreference
    private var _binding: DialogHtmlPageViewBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            R.style.FullScreenDialogStyle
        )
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogHtmlPageViewBinding.inflate(layoutInflater)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MedalliaDigital.disableIntercept()
        
        htmlUrl = arguments?.getString("url")
        header = arguments?.getString("header")
        title = arguments?.getString("myTitle", "Toffee") ?: "Toffee"
        shareableUrl = arguments?.getString("shareable_url")
        isHideBackIcon = arguments?.getBoolean("isHideBackIcon",true) ?: true
        isHideCloseIcon = arguments?.getBoolean("isHideCloseIcon",false) ?: false
        
        binding.titleTv.text = title
        if (isHideBackIcon) binding.backIcon.hide() else binding.backIcon.show()
        if (isHideCloseIcon) binding.closeIv.setImageResource(R.drawable.ic_toffee) else binding.closeIv.setImageResource(R.drawable.ic_close)
        observeTopBarBackground()
        binding.webview.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                binding.progressBar.visibility = View.VISIBLE
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
            val urlWithTheme = it.replace("toffee_theme=", "toffee_theme=${cPref.appTheme}")
            if (header.isNullOrEmpty()) {
                binding.webview.loadUrl(urlWithTheme)
            } else {
                val headerMap: MutableMap<String, String> = HashMap()
                headerMap["MSISDN"] = header!!
                binding.webview.loadUrl(urlWithTheme, headerMap)
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
    
    @JavascriptInterface
    fun webCallback(type: Int, message: String? = null, url: String? = null) {
        when (type) {
            HOME_SCREEN.value -> {
                dismiss()
            }
            LOGIN_DIALOG.value -> {
                mPref.loginDialogLiveData.postValue(true)
            }
            MESSAGE_DIALOG.value -> {
                message?.let {
                    mPref.messageDialogLiveData.postValue(it)
                }
            }
            PLAY_CONTENT.value -> {
                mPref.isPaidUser = true
                shareableUrl?.let {
                    mPref.shareableUrlLiveData.postValue(it)
                }
            }
            DEEP_LINK.value -> {
                url?.let {
                    mPref.shareableUrlLiveData.postValue(it)
                }
            }
            CLOSE_APP.value -> {
                requireActivity().finishAffinity()
                exitProcess(0)
            }
            FORCE_LOGOUT.value -> {
                mPref.forceLogoutUserLiveData.postValue(true)
            }
        }
        dismiss()
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
                        if (!imagePath.isNullOrBlank()){
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
// loadUrl("about:blank")
            stopLoading()
            onPause()
            webChromeClient = null
//            webViewClient = null
            clearHistory()
            removeAllViews()
//            destroyDrawingCache()
            destroy()
        }
        super.onDestroyView()
        _binding = null
    }
}