package com.banglalink.toffee.model

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.ui.player.Samples
import com.banglalink.toffee.util.Utils
import com.google.android.exoplayer2.C
import java.net.MalformedURLException
import java.net.URL
import java.util.*

/**
 * Created by shantanu on 8/31/16.
 */
class Channel(
    private val name: String?,
    contentId: String? = name?.toLowerCase(Locale.US)?.replace("\\s".toRegex(), ""),
    provider: String? = "",
    uri: String? = null,
    type: Int = C.TYPE_HLS,
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

    fun getContentUri(context: Context, pref: Preference): String? {
        val isWifiConnected = Utils.checkWifiOnAndConnected(context)
        if (!isWifiConnected && pref.watchOnlyWifi()) {
            return null
        }
        val text = if (isWifiConnected) {
            if (pref.wifiProfileStatus == 6) {
                "/auto"
            } else {
                "/" + pref.wifiProfileStatus
            }
        } else {
            "/" + pref.cellularProfileStatus
        }
        if (pref.shouldOverrideHlsUrl()) {
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
        fun createChannel(channelInfo: ChannelInfo): Channel {
            return Channel(channelInfo.program_name, uri = channelInfo.hlsLinks!![0].hls_url_mobile)
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