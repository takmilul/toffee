package com.banglalink.toffee.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Parcelize@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class Offense(
    @JsonNames("sub_id")
    val id: Long = 0,
    @SerialName("sub_head_name")
    val name: String = "",
    @SerialName("sub_head_sorting")
    val sorting: Int = 0
): Parcelable {
    override fun toString() = name
}