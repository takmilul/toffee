package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class NavCategoryGroup(
    @SerializedName("channels")
    val channels: List<NavCategory>,
    @SerializedName("vod")
    val vod: List<NavCategory>,
    @SerializedName("catchup")
    val catchup: List<NavCategory>
)