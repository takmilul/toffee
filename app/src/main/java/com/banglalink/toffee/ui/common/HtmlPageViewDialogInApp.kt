package com.banglalink.toffee.ui.common

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.databinding.HtmlPageViewDialogInAppBinding
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.widget.MyPopupWindow
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HtmlPageViewDialogInApp : DialogFragment(), ProviderIconCallback<ChannelInfo> {
    
    private var header: String? = ""
    private lateinit var htmlUrl: String
    private val binding get() = _binding!!
    @Inject lateinit var mPref: SessionPreference
    private var _binding: HtmlPageViewDialogInAppBinding? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            R.style.FullScreenDialogStyle
        )
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = HtmlPageViewDialogInAppBinding.inflate(layoutInflater)
        
        htmlUrl= arguments?.getString("url")!!
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
            mixedContentMode =  WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            javaScriptEnabled = true
            setSupportZoom(true)
            setNeedInitialFocus(false)
            cacheMode = WebSettings.LOAD_DEFAULT
            databaseEnabled = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
            setSupportMultipleWindows(false)
            javaScriptCanOpenWindowsAutomatically = true
            domStorageEnabled = true
        }
        if (header.isNullOrEmpty()){
            binding.webview.loadUrl(htmlUrl)
        } else {
            val headerMap: MutableMap<String, String> = HashMap()
            headerMap["MSISDN"] = header!!
            binding.webview.loadUrl(htmlUrl,headerMap)
        }
        
        binding.share.setOnClickListener {
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            sharingIntent.putExtra(
                Intent.EXTRA_TEXT, htmlUrl)
            startActivity(Intent.createChooser(sharingIntent, "Share via"))
        }
        
        binding.closeIv.setOnClickListener {
            mPref.isWebViewDialogClosed.postValue(true)
            dialog?.dismiss()
        }
        
        binding.menuMore.setOnClickListener {
            val popupMenu = MyPopupWindow(requireContext(), binding.menuMore)
            popupMenu.inflate(R.menu.menu_browser_item)
            popupMenu.setOnMenuItemClickListener{
                when(it?.itemId){
                    R.id.menu_link ->{
                        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = ClipData.newPlainText("Copied Text", htmlUrl)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(requireContext(), "Link: $htmlUrl is copied", Toast.LENGTH_LONG).show()
                        true
                    }
                    R.id.chrome_open ->{
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(htmlUrl))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.setPackage("com.android.chrome")
                        try {
                            requireContext().startActivity(intent)
                        } catch (ex: ActivityNotFoundException) {
                            // Chrome browser presumably not installed so allow user to choose instead
                            intent.setPackage(null)
                            requireContext().startActivity(intent)
                        }
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
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