package com.banglalink.toffee.ui.widget

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.webkit.WebView
import android.webkit.WebViewClient
import java.net.URISyntaxException

class Html5WebViewClient:WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        if (url!!.startsWith("intent://")) {
            try {
                val context = view!!.context
                val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                if (intent != null) {
                    view.stopLoading()
                    val packageManager: PackageManager = context.packageManager
                    val info = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
                    if (info != null) {
                        context.startActivity(intent)
                    }
                    return true
                }
            } catch (e: URISyntaxException) {
                e.printStackTrace()
            }
        }

        return false
    }
}

