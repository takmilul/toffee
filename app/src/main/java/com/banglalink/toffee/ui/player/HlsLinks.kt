package com.banglalink.toffee.ui.player

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HlsLinks(val hls_url_mobile: String) : Parcelable

@Parcelize
class DrmHlsLinks(private val drm_hls_url: String? = null) : Parcelable {
    fun urlList(): List<String> {
        return drm_hls_url?.split(",")?.map { it.trim() } ?: listOf()
    }
}