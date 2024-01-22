package com.banglalink.toffee.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize@Serializable
data class Offense(
    @SerialName(value="id"/*, alternate = ["sub_id"]*/)
    val id: Long,
    @SerialName("sub_head_name")
    val name: String,
    @SerialName("sub_head_sorting")
    val sorting: Int = 0
): Parcelable {
    override fun toString() = name
}