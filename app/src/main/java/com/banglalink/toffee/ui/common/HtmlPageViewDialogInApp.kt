package com.banglalink.toffee.ui.common

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.ClipboardManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.databinding.HtmlPageViewDialogInAppBinding
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.home.PLAY_IN_WEB_VIEW
import com.banglalink.toffee.ui.player.PlayerPageActivity
import com.banglalink.toffee.ui.widget.DraggerLayout
import com.banglalink.toffee.ui.widget.MyPopupWindow
import com.firework.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerControlView
import dagger.hilt.android.AndroidEntryPoint

class HtmlPageViewDialogInApp : DialogFragment(), ProviderIconCallback<ChannelInfo> {

    private var _binding: HtmlPageViewDialogInAppBinding? = null
    private val binding get() = _binding!!

    private lateinit var htmlUrl: String
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
        savedInstanceState: Bundle?,
    ): View {
        _binding = HtmlPageViewDialogInAppBinding.inflate(layoutInflater)

        ///
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
            if (Build.VERSION.SDK_INT >= 21) {
                mixedContentMode =  WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
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
        if(header.isNullOrEmpty()){
            binding.webview.loadUrl(htmlUrl)
        }else{
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
            dialog?.dismiss()
        }

        binding.menuMore.setOnClickListener {
            val popupMenu = MyPopupWindow(requireContext(), binding.menuMore)
            popupMenu.inflate(R.menu.menu_browser_item)
            popupMenu.setOnMenuItemClickListener{
                when(it?.itemId){
                    R.id.menu_link ->{
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                            val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.text = htmlUrl
                            Toast.makeText(context, "Link: $htmlUrl is copied", Toast.LENGTH_LONG).show()
                        } else {
                            val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = ClipData.newPlainText("Copied Text", htmlUrl)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Link: $htmlUrl is copied", Toast.LENGTH_LONG).show()
                        }
                        true
                    }
                    R.id.chrome_open ->{
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(htmlUrl))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.setPackage("com.android.chrome")
                        try {
                            context!!.startActivity(intent)
                        } catch (ex: ActivityNotFoundException) {
                            // Chrome browser presumably not installed so allow user to choose instead
                            intent.setPackage(null)
                            context!!.startActivity(intent)
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