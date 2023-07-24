package com.banglalink.toffee.model

import android.os.Parcelable
import com.google.android.gms.common.annotation.KeepName
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


@Parcelize
data class FmRadioContentBean(
    @SerializedName("radio_banner")
    val radio_banner: String?="abbc",
    @SerializedName("channels")
    val channels: List<ChannelInfo>?
): Parcelable