package com.banglalink.toffee.ui.common

import android.content.res.AssetManager
import android.os.Bundle
import android.webkit.JavascriptInterface
import com.banglalink.toffee.analytics.HeartBeatManager
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.enums.WebActionType.*
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.ui.widget.HTML5WebView
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.system.exitProcess

@AndroidEntryPoint
class Html5PlayerViewActivity : BaseAppCompatActivity() {
    
    private lateinit var htmlUrl: String
    private lateinit var mWebView: HTML5WebView
    private val progressDialog by lazy {
        VelBoxProgressDialog(this)
    }
    @Inject lateinit var heartBeatManager: HeartBeatManager
    
    companion object {
        const val CONTENT_URL = "content_url"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val extras = intent.extras
        if (extras != null) {
            htmlUrl = extras.getString(CONTENT_URL)!!
        }
        mWebView = HTML5WebView(this)

        if (savedInstanceState != null) {
            mWebView.restoreState(savedInstanceState)
        } else {
            val headerMap: MutableMap<String, String> = HashMap()
            headerMap["MSISDN"] = SessionPreference.getInstance().phoneNumber
            mWebView.loadUrl(htmlUrl, headerMap)
        }

        setContentView(mWebView.layout)

        mWebView.addJavascriptInterface(this, "Android")
        
        observe(mWebView.showProgressLiveData) {
            when (it) {
                true -> progressDialog.show()
                false -> {
                    if (progressDialog.isShowing)
                        progressDialog.dismiss()
                }
            }
        }
    }
    
    @JavascriptInterface
    fun webCallback(type: Int, message: String? = null, url: String? = null) {
        when (type) {
            LOGIN_DIALOG.value -> {
                mPref.loginDialogLiveData.postValue(true)
            }
            MESSAGE_DIALOG.value -> {
                message?.let {
                    mPref.messageDialogLiveData.postValue(it)
                }
            }
            PLAY_CONTENT.value -> {
                url?.let {
                    mPref.isPaidUser = true
                    mPref.shareableUrlLiveData.postValue(it)
                }
            }
            DEEP_LINK.value -> {
                url?.let {
                    mPref.shareableUrlLiveData.postValue(it)
                }
            }
            CLOSE_APP.value -> {
                finishAffinity()
                exitProcess(0)
            }
            FORCE_LOGOUT.value -> {
                mPref.forceLogoutUserLiveData.postValue(true)
            }
        }
        finish()
    }
    
    @JavascriptInterface
    fun isBlNumber(): Boolean {
        return mPref.isHeBanglalinkNumber
    }
    
    //For Android 5.0.0 webkit UI bug fix
    override fun getAssets(): AssetManager {
        return resources.assets
    }
    
    override fun onDestroy() {
        progressDialog.dismiss()
        heartBeatManager.triggerEventViewingContentStop()
        super.onDestroy()
    }
}