package com.banglalink.toffee.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class HlsLinks(
    @SerializedName("hls_url_mobile")
    val hls_url_mobile: String
) : Parcelable

@Parcelize
class DrmHlsLinks(
    @SerializedName("drm_hls_url")
    private val drm_hls_url: String? = null
) : Parcelable {
    
    fun urlList(): List<String> {
        return drm_hls_url?.split(",")?.map { it.trim() } ?: listOf()
    }
}