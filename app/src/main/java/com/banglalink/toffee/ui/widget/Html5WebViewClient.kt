package com.banglalink.toffee.ui.widget

import android.content.Intent
import android.content.pm.PackageManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.banglalink.toffee.R
import com.banglalink.toffee.extension.openUrlToExternalApp
import java.net.URISyntaxException

open class Html5WebViewClient: WebViewClient() {
    
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        if (view != null) {
            request?.url?.toString()?.let {
                val context = view.context
                return when {
                    it.startsWith("intent://") -> {
                        try {
                            val intent = Intent.parseUri(it, Intent.URI_INTENT_SCHEME)
                            if (intent != null) {
                                view.stopLoading()
                                val packageManager: PackageManager = context.packageManager
                                val info = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
                                if (info != null) {
                                    context.startActivity(intent)
                                }
                            }
                            true
                        } catch (e: URISyntaxException) {
                            e.printStackTrace()
                            false
                        }
                    }
                    it.contains("www.facebook.com", true) -> {
                        val fbUrl = "fb://page${it.replaceBeforeLast("/", "").replace("Toffee.Bangladesh", "100869298504557", true)}"
                        context.openUrlToExternalApp(fbUrl)
                    }
                    it.contains("www.instagram.com", true) ||
                    it.contains("www.youtube.com", true) -> {
                        context.openUrlToExternalApp(it)
                    }
                    else -> {
                        view.loadUrl(it)
                        true
                    }
                }
            }
        }
        return super.shouldOverrideUrlLoading(view, request)
    }
    
    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        view?.let {
//            it.loadUrl("about:blank")
            it.loadDataWithBaseURL(null,
                it.context.getString(R.string.web_error_text),
                "text/html",
                "UTF-8",
                null)
//            it.loadUrl("file:///android_asset/error.html") //TODO: load custom error page from asset
            it.invalidate()
        }
    }
}

