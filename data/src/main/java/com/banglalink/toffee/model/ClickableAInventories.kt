package com.banglalink.toffee.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ClickableAInventories(
    @SerializedName("packId")
    val packId: Int? = null,
    @SerializedName("paymentMethodId")
    val paymentMethodId: Int? = null
) : Parcelable