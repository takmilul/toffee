package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NavCategoryGroup(
    @SerialName("channels")
    val channels: List<NavCategory>,
    @SerialName("vod")
    val vod: List<NavCategory>,
    @SerialName("catchup")
    val catchup: List<NavCategory>
)