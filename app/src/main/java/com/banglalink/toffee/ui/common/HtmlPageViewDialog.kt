package com.banglalink.toffee.ui.common

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.DialogFragment
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.DialogHtmlPageViewBinding
import com.banglalink.toffee.databinding.FragmentHtmlPageViewBinding
import com.banglalink.toffee.databinding.FragmentThumbSelectionMethodBinding


class HtmlPageViewDialog : DialogFragment() {

    private var _binding: DialogHtmlPageViewBinding ? = null
    private val binding get() = _binding!!

    private var htmlUrl: String? = ""
    private var header: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            R.style.FullScreenDialogStyle
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogHtmlPageViewBinding.inflate(layoutInflater)

        ///
        htmlUrl= arguments?.getString("url")
        header= arguments?.getString("header")

        binding.titleTv.text = arguments?.getString("myTitle"," ") ?: " "

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
            if (Build.VERSION.SDK_INT >= 21) {
                mixedContentMode =  WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
            javaScriptEnabled = true
            setSupportZoom(true)
            setNeedInitialFocus(false)
            setAppCacheEnabled(true)
            databaseEnabled = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
            setSupportMultipleWindows(true)
            javaScriptCanOpenWindowsAutomatically = true
            domStorageEnabled = true
        }
        if(header.isNullOrEmpty()){
            binding.webview.loadUrl(htmlUrl)
        }else{
            val headerMap: MutableMap<String, String> = HashMap()
            headerMap["MSISDN"] = header!!
            binding.webview.loadUrl(htmlUrl,headerMap)
        }

        binding.closeIv.setOnClickListener {

            dialog?.dismiss()
        }

        return binding.root
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