package com.banglalink.toffee.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class OffenseType(
    val id: Long = 0L,

    @SerializedName("head_name")
    val type: String,
    
    @SerializedName("head_sorting")
    val sorting: Int,

    @SerializedName("subHeads")
    val offenseList: List<Offense>? = null
): Parcelable {
    override fun toString(): String = type
}