package com.banglalink.toffee.ui.common

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.banglalink.toffee.databinding.FragmentHtmlPageViewBinding


class HtmlPageViewFragment : BaseFragment() {

    private var _binding: FragmentHtmlPageViewBinding ? = null
    private val binding get() = _binding!!

    private var htmlUrl: String? = ""
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

        htmlUrl= arguments?.getString("url")
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

        binding.webview.settings.javaScriptEnabled = true
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
            webViewClient = null
            clearHistory()
            removeAllViews()
            destroyDrawingCache()
            destroy()
        }

        super.onDestroyView()
        _binding = null
    }
}