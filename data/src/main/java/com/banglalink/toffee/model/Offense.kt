package com.banglalink.toffee.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Offense(
    @SerializedName(value="id", alternate = ["sub_id"])
    val id: Long,
    @SerializedName("sub_head_name")
    val name: String,
    @SerializedName("sub_head_sorting")
    val sorting: Int = 0
): Parcelable {
    override fun toString() = name
}