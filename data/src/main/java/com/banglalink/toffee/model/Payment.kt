package com.banglalink.toffee.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Payment(
    val id: Long = 0L,
    val method: String = "",
): Parcelable {
    override fun toString(): String = method
}