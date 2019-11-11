package com.banglalink.toffee.ui.common

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.widget.Toolbar
import com.banglalink.toffee.R
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog

class HtmlPageViewActivity :BaseAppCompatActivity(){

    companion object{
        val CONTENT_KEY = "content_key"
        val TITLE_KEY = "title_key"
    }

    private var isLoading = false
    internal var htmlUrl: String? = ""
    internal var title: String? = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_html_page_view)

        val webview = findViewById<WebView>(R.id.webview);
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        val extras = intent.extras
        if (extras != null) {
            htmlUrl = extras.getString(CONTENT_KEY)
            title = extras.getString(TITLE_KEY)
        }
        if (toolbar != null) {
            setSupportActionBar(toolbar)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setTitle(title)
        }
        toolbar.setNavigationOnClickListener { onBackPressed() }

        webview.setHorizontalScrollBarEnabled(true)
        webview.setVerticalScrollbarOverlay(true)
        webview.getSettings().setLoadWithOverviewMode(true)
        webview.getSettings().setUseWideViewPort(true)
        webview.getSettings().setBuiltInZoomControls(true)
        webview.getSettings().setDisplayZoomControls(false)
        webview.getSettings().setJavaScriptEnabled(true)
        webview.getSettings().setDomStorageEnabled(true)

        webview.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        webview.setWebViewClient(object : WebViewClient() {

            var progressDialog: VelBoxProgressDialog? = null

            //Show loader on url load
            override fun onLoadResource(view: WebView, url: String) {
                if (progressDialog == null && !isLoading) {
                    // in standard case YourActivity.this
                    progressDialog = VelBoxProgressDialog(this@HtmlPageViewActivity)
                    progressDialog!!.show()
                    isLoading = true
                }
            }

            override fun onPageFinished(view: WebView, url: String) {
                try {
                    if (progressDialog != null && progressDialog!!.isShowing()) {
                        progressDialog!!.dismiss()
                        progressDialog = null
                    }
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }

            }

        })

        webview.loadUrl(htmlUrl)
    }
}