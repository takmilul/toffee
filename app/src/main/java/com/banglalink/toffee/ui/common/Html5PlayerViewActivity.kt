package com.banglalink.toffee.ui.common

import android.content.res.AssetManager
import android.os.Bundle
import com.banglalink.toffee.analytics.HeartBeatManager
import com.banglalink.toffee.data.storage.Preference
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
    lateinit var mWebView: HTML5WebView
    private var htmlUrl: String? = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val extras = intent.extras
        if (extras != null) {
            htmlUrl = extras.getString(CONTENT_URL)
        }
        mWebView = HTML5WebView(this);

        if (savedInstanceState != null) {
            mWebView.restoreState(savedInstanceState);
        } else {
            val headerMap: MutableMap<String, String> = HashMap()
            headerMap["MSISDN"] = Preference.getInstance().phoneNumber
            mWebView.loadUrl(htmlUrl,headerMap);
        }

        setContentView(mWebView.layout);
        observe(mWebView.showProgressLiveData){
            when(it){
                true->progressDialog.show()
                false->progressDialog.dismiss()
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        progressDialog.dismiss()
        HeartBeatManager.triggerEventViewingContentStop()
    }

    //For Android 5.0.0 webkit UI bug fix
    override fun getAssets(): AssetManager {
        return resources.assets
    }
}