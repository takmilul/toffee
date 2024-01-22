package com.banglalink.toffee.model

import android.net.Uri
import android.os.Bundle
import com.banglalink.toffee.data.storage.SessionPreference
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Channel(
    @SerialName("name")
    private val name: String?,
    @SerialName("contentId")
    private val contentId: String? = name?.lowercase()?.replace("\\s".toRegex(), ""),
    @SerialName("provider")
    private val provider: String? = "",
    @SerialName("uri")
    private var uri: String? = null,
    @SerialName("type")
    private val type: Int = Samples.TYPE_HLS,
    @SerialName("imageUrl")
    private var imageUrl: String? = null
) {
    
    @Contextual
//    @SerializedName("bundle")
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
        return uri?.let {
            if (it.endsWith("/")) {
                Uri.parse(it + pref.sessionToken + "/auto").toString()
            } else {
                Uri.parse(it + "/" + pref.sessionToken + "/auto").toString()
            }
        }
    }
}