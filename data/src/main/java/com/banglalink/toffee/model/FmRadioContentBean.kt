package com.banglalink.toffee.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class FmRadioContentBean(
    @SerialName("radio_banner")
    val radioBanner: String? = null,
    @SerialName("channels")
    val channels: List<ChannelInfo>? = null,
) : Parcelable