package com.banglalink.toffee.ui.widget

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.webkit.WebChromeClient.CustomViewCallback
import android.widget.FrameLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.banglalink.toffee.R

@SuppressLint("SetJavaScriptEnabled")
class HTML5WebView @JvmOverloads constructor(
    private val mContext: Context, 
    attrs: AttributeSet? = null, 
    defStyle: Int = 0
) : WebView(mContext, attrs, defStyle) {
    
    val layout: FrameLayout = FrameLayout(context)
    private var mCustomView: View? = null
    private val mCustomViewContainer: FrameLayout
    private val mWebChromeClient: MyWebChromeClient
    private lateinit var mCustomViewCallback: CustomViewCallback
    private val _showProgressMutableLiveData = MutableLiveData<Boolean>()
    var showProgressLiveData: LiveData<Boolean> = _showProgressMutableLiveData
    
    init {
        val param = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val mBrowserFrameLayout = LayoutInflater.from(mContext).inflate(R.layout.custom_screen, null, false) as FrameLayout
        val mContentView = mBrowserFrameLayout.findViewById<View>(R.id.main_content) as FrameLayout
        mCustomViewContainer = mBrowserFrameLayout.findViewById<View>(R.id.fullscreen_custom_content) as FrameLayout
        layout.addView(mBrowserFrameLayout, param)
        settings.apply {
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
            javaScriptCanOpenWindowsAutomatically = true
            CookieManager.getInstance().setAcceptCookie(true)
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            userAgentString = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.63 Mobile Safari/537.36"
        }
        mWebChromeClient = MyWebChromeClient()
        webChromeClient = mWebChromeClient
        webViewClient = Html5WebViewClient()
        scrollBarStyle = SCROLLBARS_INSIDE_OVERLAY
        mContentView.addView(this)
        _showProgressMutableLiveData.postValue(true)
    }
    
    fun inCustomView(): Boolean {
        return mCustomView != null
    }
    
    fun hideCustomView() {
        mWebChromeClient.onHideCustomView()
    }
    
    private inner class MyWebChromeClient : WebChromeClient() {
        override fun onShowCustomView(view: View, callback: CustomViewCallback) {
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