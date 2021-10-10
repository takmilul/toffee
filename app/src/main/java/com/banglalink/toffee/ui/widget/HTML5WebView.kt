package com.banglalink.toffee.ui.widget

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.webkit.WebChromeClient.CustomViewCallback
import android.widget.FrameLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.banglalink.toffee.R

class HTML5WebView : WebView {
    lateinit var layout: FrameLayout
        private set
    private var mContext: Context? = null
    private var mCustomView: View? = null
    private lateinit var mCustomViewContainer: FrameLayout
    private lateinit var mWebChromeClient: MyWebChromeClient
    private lateinit var mCustomViewCallback: CustomViewCallback
    private val _showProgressMutableLiveData = MutableLiveData<Boolean>()
    var showProgressLiveData: LiveData<Boolean> = _showProgressMutableLiveData
    
    private fun init(context: Context) {
        mContext = context
        val activity = mContext as Activity?
        layout = FrameLayout(context)
        val param = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val mBrowserFrameLayout = LayoutInflater.from(activity).inflate(R.layout.custom_screen, null) as FrameLayout
        val mContentView = mBrowserFrameLayout.findViewById<View>(R.id.main_content) as FrameLayout
        mCustomViewContainer = mBrowserFrameLayout.findViewById<View>(R.id.fullscreen_custom_content) as FrameLayout
        layout.addView(mBrowserFrameLayout, param)
        
        // Configure the webview
        settings.apply {
            useWideViewPort = true
            loadWithOverviewMode = true
            javaScriptEnabled = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            setSupportZoom(true)
            setNeedInitialFocus(false)
            cacheMode = WebSettings.LOAD_DEFAULT
            databaseEnabled = true
            builtInZoomControls = true
            displayZoomControls = false
            setSupportMultipleWindows(true)
            javaScriptCanOpenWindowsAutomatically = true
            domStorageEnabled = true
            CookieManager.getInstance().setAcceptCookie(true)
            userAgentString = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.63 Mobile " +
                "Safari/537.36"
        }
        mWebChromeClient = MyWebChromeClient()
        webChromeClient = mWebChromeClient
        webViewClient = Html5WebViewClient()
        scrollBarStyle = SCROLLBARS_INSIDE_OVERLAY
        mContentView.addView(this)
        _showProgressMutableLiveData.postValue(true)
    }
    
    constructor(context: Context) : super(context) {
        init(context)
    }
    
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }
    
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }
    
    fun inCustomView(): Boolean {
        return mCustomView != null
    }
    
    fun hideCustomView() {
        mWebChromeClient.onHideCustomView()
    }
    
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mCustomView == null && canGoBack()) {
                goBack()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
    
    private inner class MyWebChromeClient : WebChromeClient() {
        override fun onShowCustomView(view: View, callback: CustomViewCallback) {
            //Log.i(LOGTAG, "here in on ShowCustomView");
            this@HTML5WebView.visibility = GONE
            
            // if a view already exists then immediately terminate the new one
            if (mCustomView != null) {
                callback.onCustomViewHidden()
                return
            }
            (mContext as Activity?)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
            mCustomViewContainer.addView(view)
            mCustomView = view
            mCustomViewCallback = callback
            mCustomViewContainer.visibility = VISIBLE
        }
        
        override fun onHideCustomView() {
            if (mCustomView == null) return
            (mContext as Activity?)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            // Hide the custom view.
            mCustomView!!.visibility = GONE
            
            // Remove the custom view from its container.
            mCustomViewContainer.removeView(mCustomView)
            mCustomView = null
            mCustomViewContainer.visibility = GONE
            mCustomViewCallback.onCustomViewHidden()
            this@HTML5WebView.visibility = VISIBLE
            goBack()
        }
        
        override fun onReceivedTitle(view: WebView, title: String) {
            (mContext as Activity?)!!.title = title
        }
        
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            if (newProgress > 30) {
                _showProgressMutableLiveData.postValue(false)
            }
        }
        
        override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
            callback.invoke(origin, true, false)
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
}