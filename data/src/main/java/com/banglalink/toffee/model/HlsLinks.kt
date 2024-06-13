package com.banglalink.toffee.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Parcelize
@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class HlsLinks (
    @JsonNames("hlsUrlMobile", "hls_url_mobile")
    var hlsUrlMobile: String? = null
) : Parcelable

@Parcelize
@Serializable
@OptIn(ExperimentalSerializationApi::class)
class DrmHlsLinks(
    @JsonNames("drmHlsUrl", "drm_hls_url")
    private val drmHlsUrl: String? = null
) : Parcelable {
    
    fun urlList(): List<String> {
        return drmHlsUrl?.split(",")?.map { it.trim() } ?: listOf()
    }
}