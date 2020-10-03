package com.banglalink.toffee.ui.common

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import com.banglalink.toffee.R


class HtmlPageViewActivity : BaseAppCompatActivity() {

    companion object {
        const val CONTENT_KEY = "content_key"
        const val TITLE_KEY = "title_key"
        const val HEADER = "HEADER"
    }

    private var htmlUrl: String? = ""
    private var title: String? = ""
    private var header: String? = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_html_page_view)

        val webView = findViewById<WebView>(R.id.webview);
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)

        val extras = intent.extras
        if (extras != null) {
            htmlUrl = extras.getString(CONTENT_KEY)
            title = extras.getString(TITLE_KEY)
            header = extras.getString(HEADER)
        }
        if (toolbar != null) {
            setSupportActionBar(toolbar)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setTitle(title)
            toolbar.setNavigationOnClickListener { onBackPressed() }
        }


        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                progressBar.visibility = View.VISIBLE
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
                if (newProgress == 100) {
                    progressBar.visibility = View.GONE
                }
            }
        }

        webView.settings.javaScriptEnabled = true
        if(header.isNullOrEmpty()){
            webView.loadUrl(htmlUrl)
        }else{
            val headerMap: MutableMap<String, String> = HashMap()
            headerMap["MSISDN"] = header!!
            webView.loadUrl(htmlUrl,headerMap)
        }

    }

    //For Android 5.0.0 webkit UI bug fix
    override fun getAssets(): AssetManager {
        return resources.assets
    }
}