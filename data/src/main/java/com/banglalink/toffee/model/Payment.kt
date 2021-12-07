package com.banglalink.toffee.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Payment(
    @SerializedName("id")
    val id: Long = 0L,
    @SerializedName("method")
    val method: String = "",
): Parcelable {
    override fun toString(): String = method
}