package com.banglalink.toffee.ui.common


import android.content.res.AssetManager
import android.os.Bundle
import android.webkit.JavascriptInterface
import androidx.core.os.bundleOf
import com.banglalink.toffee.analytics.FirebaseParams
import com.banglalink.toffee.analytics.HeartBeatManager
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.enums.WebActionType.CLOSE_APP
import com.banglalink.toffee.enums.WebActionType.DEEP_LINK
import com.banglalink.toffee.enums.WebActionType.FORCE_LOGOUT
import com.banglalink.toffee.enums.WebActionType.HOME_SCREEN
import com.banglalink.toffee.enums.WebActionType.LOGIN_DIALOG
import com.banglalink.toffee.enums.WebActionType.MESSAGE_DIALOG
import com.banglalink.toffee.enums.WebActionType.PLAY_CONTENT
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.ui.widget.HTML5WebView
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.system.exitProcess

@AndroidEntryPoint
class Html5PlayerViewActivity : BaseAppCompatActivity() {
    
    private var title: String? = null
    private var htmlUrl: String? = null
    private var shareableUrl: String? = null
    private lateinit var mWebView: HTML5WebView
    private val progressDialog by lazy {
        ToffeeProgressDialog(this)
    }
    @Inject lateinit var heartBeatManager: HeartBeatManager
    
    companion object {
        const val CONTENT_URL = "content_url"
        const val SHAREABLE_URL = "shareable_url"
        const val TITLE = "title"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        MedalliaDigital.disableIntercept()
        val extras = intent.extras
        if (extras != null) {
            title = extras.getString(TITLE)
            htmlUrl = extras.getString(CONTENT_URL)!!
            shareableUrl = extras.getString(SHAREABLE_URL)
        }
        mWebView = HTML5WebView(this, title)

        if (savedInstanceState != null) {
            mWebView.restoreState(savedInstanceState)
        } else {
            val headerMap: MutableMap<String, String> = HashMap()
            headerMap["MSISDN"] = SessionPreference.getInstance().phoneNumber
            htmlUrl?.let {
                ToffeeAnalytics.logEvent(
                    ToffeeEvents.SCREEN_VIEW,
                    bundleOf(FirebaseParams.BROWSER_SCREEN to it))
                mWebView.loadUrl(htmlUrl!!, headerMap)

            }
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
            HOME_SCREEN.value -> {
                finish()
            }
            LOGIN_DIALOG.value -> {
                mPref.loginDialogLiveData.postValue(true)
            }
            MESSAGE_DIALOG.value -> {
                message?.let {
                    mPref.messageDialogLiveData.postValue(it)
                }
            }
            PLAY_CONTENT.value -> {
                mPref.isPaidUser = true
                shareableUrl?.let {
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
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack()
        } else {
            super.onBackPressed()
        }
    }
    
    override fun onDestroy() {
        progressDialog.dismiss()
//        MedalliaDigital.enableIntercept()
        heartBeatManager.triggerEventViewingContentStop()
        super.onDestroy()
    }
}