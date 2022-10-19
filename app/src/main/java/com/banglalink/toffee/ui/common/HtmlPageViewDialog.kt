package com.banglalink.toffee.ui.common

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.fragment.app.DialogFragment
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.DialogHtmlPageViewBinding
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.show


class HtmlPageViewDialog : DialogFragment() {
    private var _binding: DialogHtmlPageViewBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var htmlUrl: String
    private var header: String? = ""
    private var isHideToffeeIcon: Boolean = true
    private var isHideCloseIcon: Boolean = false
    
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
        htmlUrl = arguments?.getString("url")!!
        header = arguments?.getString("header")
        isHideToffeeIcon = arguments?.getBoolean("isHideBackIcon",true) ?: true
        isHideCloseIcon = arguments?.getBoolean("isHideCloseIcon",false) ?: false
        
        binding.titleTv.text = arguments?.getString("myTitle", " ") ?: " "
        if (isHideToffeeIcon) binding.backIcon.hide() else binding.backIcon.show()
        if (isHideCloseIcon) binding.closeIv.setImageResource(R.drawable.ic_toffee) else binding.closeIv.setImageResource(R.drawable.ic_close)
        
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
            if (Build.VERSION.SDK_INT >= 21) {
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
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
        }
        if (header.isNullOrEmpty()) {
            binding.webview.loadUrl(htmlUrl)
        } else {
            val headerMap: MutableMap<String, String> = HashMap()
            headerMap["MSISDN"] = header!!
            binding.webview.loadUrl(htmlUrl, headerMap)
        }
        
        binding.closeIv.setOnClickListener {
            dialog?.dismiss()
        }
        
        binding.backIcon.setOnClickListener {
            dialog?.dismiss()
        }
        binding.titleTv.setOnClickListener {
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