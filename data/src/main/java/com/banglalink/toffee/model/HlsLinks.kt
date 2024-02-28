package com.banglalink.toffee.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Parcelize
@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class HlsLinks (
    @SerialName("hls_url_mobile")
    @JsonNames("hlsUrlMobile")
    var hlsUrlMobile: String? = null
) : Parcelable

@Parcelize
@Serializable
@OptIn(ExperimentalSerializationApi::class)
class DrmHlsLinks(
    @SerialName("drm_hls_url")
    @JsonNames("drmHlsUrl")
    private val drmHlsUrl: String? = null
) : Parcelable {
    
    fun urlList(): List<String> {
        return drmHlsUrl?.split(",")?.map { it.trim() } ?: listOf()
    }
}