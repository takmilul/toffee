package com.banglalink.toffee.ui.common

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import com.banglalink.toffee.analytics.FirebaseParams
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.databinding.FragmentHtmlPageViewBinding
import com.banglalink.toffee.ui.widget.Html5WebViewClient
import com.banglalink.toffee.util.Log
import com.medallia.digital.mobilesdk.MedalliaDigital
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HtmlPageViewFragment : BaseFragment() {
    
    private var header: String? = ""
    private lateinit var htmlUrl: String
    private var _binding: FragmentHtmlPageViewBinding ? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        MedalliaDigital.disableIntercept()
        _binding = FragmentHtmlPageViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listenBackStack()
        htmlUrl= arguments?.getString("url")!!
        header= arguments?.getString("header")
        binding.webview.webViewClient = object : Html5WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
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
                for (i in resources.indices) {
                    if (PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID == resources[i]) {
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
            javaScriptEnabled = true
            domStorageEnabled = true
            setNeedInitialFocus(false)
            builtInZoomControls = true
            displayZoomControls = false
            setSupportMultipleWindows(false)
            cacheMode = WebSettings.LOAD_DEFAULT
            javaScriptCanOpenWindowsAutomatically = true
            CookieManager.getInstance().setAcceptCookie(true)
            mixedContentMode =  WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            userAgentString = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.63 Mobile Safari/537.36"
        }
        if(header.isNullOrEmpty()){
            binding.webview.loadUrl(htmlUrl)
        }else{
            val headerMap: MutableMap<String, String> = HashMap()
            headerMap["MSISDN"] = header!!
            binding.webview.loadUrl(htmlUrl,headerMap)
        }
        ToffeeAnalytics.logEvent(
            ToffeeEvents.SCREEN_VIEW,
            bundleOf(FirebaseParams.BROWSER_SCREEN to htmlUrl)
        )
    }
    
    private fun listenBackStack() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.i("HTM_", "url: ${binding.webview.url}")
                if (binding.webview.canGoBack() /*&& binding.webview.url?.split("/")?.contains("home") != true*/) {
                    binding.webview.goBack()
                } else {
                    OnBackPressedCallback@ this.isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        }
        
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }
    
    override fun onDestroyView() {
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
        MedalliaDigital.enableIntercept()
        super.onDestroyView()
        _binding = null
    }
}