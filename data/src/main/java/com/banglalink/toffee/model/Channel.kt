package com.banglalink.toffee.model

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import com.banglalink.toffee.data.storage.SessionPreference
import java.net.MalformedURLException
import java.net.URL

/**
 * Created by shantanu on 8/31/16.
 */
class Channel(
    private val name: String?,
    contentId: String? = name?.lowercase()?.replace("\\s".toRegex(), ""),
    provider: String? = "",
    uri: String? = null,
    type: Int = Samples.TYPE_HLS,
    private var imageUrl: String? = null
): Samples.Sample(name, contentId, provider, uri, type)
{

    val bundle: Bundle =  Bundle().apply {
        putString("name", name)
        putString("contentid", contentId)
        putString("provider", provider)
        putString("uri", uri)
        putInt("type", type)
        putString("imageurl", imageUrl)
    }

    fun getContentUri(pref: SessionPreference, isWifiConnected: Boolean): String? {
        val text = if (isWifiConnected) {
            if (pref.wifiProfileStatus == 6) {
                "/auto"
            } else {
                "/" + pref.wifiProfileStatus
            }
        } else {
            if (pref.cellularProfileStatus == 6) {
                "/auto"
            } else {
                "/" + pref.cellularProfileStatus
            }
        }
        if (pref.shouldOverrideHlsUrl) {
            try {
                val url = URL(uri)
                var path = url.path
                if (!TextUtils.isEmpty(url.query)) {
                    path = path + "?" + url.query
                }
                uri = pref.getHlsOverrideUrl() + path
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            }
        }
        val contentUri = if (uri.endsWith("/")) {
            Uri.parse(uri + pref.sessionToken + text)
        } else {
            Uri.parse(uri + "/" + pref.sessionToken + text)
        }
        return contentUri.toString()
    }

    companion object {
        @JvmStatic
        fun createChannel(programName: String?, hlsLink: String): Channel {
            return Channel(programName, uri = hlsLink)
        }

        fun create(bundle: Bundle): Channel {
            return Channel(
                bundle.getString("name"),
                bundle.getString("contentid"),
                bundle.getString("provider"),
                bundle.getString("uri"),
                bundle.getInt("type"),
                bundle.getString("imageurl")
            )
        }
    }
}