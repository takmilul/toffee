package com.banglalink.toffee.ui.common

import android.content.res.AssetManager
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import com.banglalink.toffee.analytics.HeartBeatManager
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.enums.WebActionType.*
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.ui.widget.HTML5WebView
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class Html5PlayerViewActivity : BaseAppCompatActivity() {
    
    @Inject lateinit var heartBeatManager: HeartBeatManager
    
    companion object {
        const val CONTENT_URL = "content_url"
    }

    private val progressDialog by lazy {
        VelBoxProgressDialog(this)
    }
    private lateinit var mWebView: HTML5WebView
    private lateinit var htmlUrl: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val extras = intent.extras
        if (extras != null) {
            htmlUrl = extras.getString(CONTENT_URL)!!
        }
        mWebView = HTML5WebView(this)

        if (savedInstanceState != null) {
            mWebView.restoreState(savedInstanceState);
        } else {
            val headerMap: MutableMap<String, String> = HashMap()
            headerMap["MSISDN"] = SessionPreference.getInstance().phoneNumber
            mWebView.loadUrl(htmlUrl, headerMap)
        }

        setContentView(mWebView.layout)

        with(mWebView.settings) {
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
        
        mWebView.addJavascriptInterface(this, "Android")
        
        observe(mWebView.showProgressLiveData){
            when(it){
                true->progressDialog.show()
                false->{
                    if(progressDialog.isShowing)
                        progressDialog.dismiss()
                }
            }
        }
    }
    
    @JavascriptInterface
    fun callback(type: Int, message: String? = null, url: String? = null) {
        when(type) {
            LOGIN_DIALOG.value -> { mPref.loginDialogLiveData.value = true }
            MESSAGE_DIALOG.value -> { message?.let { mPref.messageDialogLiveData.value = it } }
            PLAY_CONTENT.value -> { url?.let { mPref.shareableUrlLiveData.value = it } }
            TV_CHANNELS.value -> { mPref.shareableUrlLiveData.value = "https://toffeelive.com?routing=internal&page=tv_channels" }
        }
        this.finish()
    }
    
    override fun onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack()
        } else {
            super.onBackPressed()
        }
    }
    
    override fun onDestroy() {
        progressDialog.dismiss()
        heartBeatManager.triggerEventViewingContentStop()
        super.onDestroy()
    }

    //For Android 5.0.0 webkit UI bug fix
    override fun getAssets(): AssetManager {
        return resources.assets
    }
}