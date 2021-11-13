package com.banglalink.toffee.model

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import com.banglalink.toffee.data.storage.SessionPreference
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.net.MalformedURLException
import java.net.URL

/**
 * Created by shantanu on 8/31/16.
 */
class Channel(
    @SerializedName("name")
    private val name: String?,
    @SerializedName("contentId")
    private val contentId: String? = name?.lowercase()?.replace("\\s".toRegex(), ""),
    @SerializedName("provider")
    private val provider: String? = "",
    @SerializedName("uri")
    private val uri: String? = null,
    @SerializedName("type")
    private val type: Int = Samples.TYPE_HLS,
    @SerializedName("imageUrl")
    private var imageUrl: String? = null
): Serializable, Samples.Sample(name, contentId, provider, uri, type)
{

    @SerializedName("bundle")
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
            if (pref.wifiProfileStatus == 7) {
                "/auto"
            } else {
                "/" + pref.wifiProfileStatus
            }
        } else {
            if (pref.cellularProfileStatus == 7) {
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