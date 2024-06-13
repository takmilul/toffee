package com.banglalink.toffee.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class ClickableAdInventories(
    @SerialName("packId")
    val packId: Int? = null,
    @SerialName("paymentMethodId")
    val paymentMethodId: Int? = null,
    @SerialName("showBlPacks")
    val showBlPacks: Boolean? = false,
) : Parcelable