package com.banglalink.toffee.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Payment(
    @SerialName("id")
    val id: Long = 0L,
    @SerialName("method")
    val method: String = "",
): Parcelable {
    override fun toString(): String = method
}