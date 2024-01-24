package com.banglalink.toffee.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class HlsLinks(
    @SerialName("hls_url_mobile") 
    var hlsUrlMobile: String? = null
) : Parcelable

@Parcelize
@Serializable
class DrmHlsLinks(
    @SerialName("drm_hls_url")
    private val drmHlsUrl: String? = null
) : Parcelable {
    
    fun urlList(): List<String> {
        return drmHlsUrl?.split(",")?.map { it.trim() } ?: listOf()
    }
}