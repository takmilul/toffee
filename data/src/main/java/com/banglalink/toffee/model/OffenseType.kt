package com.banglalink.toffee.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize@Serializable
data class OffenseType(
    @SerialName("id")
    val id: Long = 0L,
    @SerialName("head_name")
    val type: String,
    @SerialName("head_sorting")
    val sorting: Int,
    @SerialName("subHeads")
    val offenseList: List<Offense>? = null
): Parcelable {
    override fun toString(): String = type
}