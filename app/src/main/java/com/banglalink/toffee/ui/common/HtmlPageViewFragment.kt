package com.banglalink.toffee.ui.common

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import com.banglalink.toffee.databinding.FragmentHtmlPageViewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HtmlPageViewFragment : BaseFragment() {

    private var _binding: FragmentHtmlPageViewBinding ? = null
    private val binding get() = _binding!!

    private lateinit var htmlUrl: String
    private var header: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHtmlPageViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        htmlUrl= arguments?.getString("url")!!
        header= arguments?.getString("header")

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
        }

        with(binding.webview.settings) {
            mixedContentMode =  WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            javaScriptEnabled = true
            setSupportZoom(true)
            setNeedInitialFocus(false)
            cacheMode = WebSettings.LOAD_DEFAULT
            databaseEnabled = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
            setSupportMultipleWindows(true)
            javaScriptCanOpenWindowsAutomatically = true
            domStorageEnabled = true
            CookieManager.getInstance().setAcceptCookie(true)
            userAgentString = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.63 Mobile Safari/537.36"
        }
        if(header.isNullOrEmpty()){
            binding.webview.loadUrl(htmlUrl)
        }else{
            val headerMap: MutableMap<String, String> = HashMap()
            headerMap["MSISDN"] = header!!
            binding.webview.loadUrl(htmlUrl,headerMap)
        }
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

        super.onDestroyView()
        _binding = null
    }
}