package com.banglalink.toffee.model

import android.net.Uri
import android.os.Bundle
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.extension.overrideUrl
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Channel(
    @SerializedName("name")
    private val name: String?,
    @SerializedName("contentId")
    private val contentId: String? = name?.lowercase()?.replace("\\s".toRegex(), ""),
    @SerializedName("provider")
    private val provider: String? = "",
    @SerializedName("uri")
    private var uri: String? = null,
    @SerializedName("type")
    private val type: Int = Samples.TYPE_HLS,
    @SerializedName("imageUrl")
    private var imageUrl: String? = null
) : Serializable//, Samples.Sample(name, contentId, provider, uri, type)
{
    
    @SerializedName("bundle") 
    val bundle: Bundle = Bundle().apply {
        putString("name", name)
        putString("contentid", contentId)
        putString("provider", provider)
        putString("uri", uri)
        putInt("type", type)
        putString("imageurl", imageUrl)
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
    
    fun getContentUri(pref: SessionPreference): String? {
        if (pref.shouldOverrideHlsHostUrl) {
            uri = uri?.overrideUrl(pref.overrideHlsHostUrl)
        }
        return uri?.let {
            if (it.endsWith("/")) {
                Uri.parse(it + pref.sessionToken + "/auto").toString()
            } else {
                Uri.parse(it + "/" + pref.sessionToken + "/auto").toString()
            }
        }
    }
}