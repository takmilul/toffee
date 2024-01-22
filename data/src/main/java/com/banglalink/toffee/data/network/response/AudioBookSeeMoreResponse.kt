package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

data class AudioBookSeeMoreResponse(
    @SerializedName("data" ) var data : List<CategoryData> = listOf()
): ExternalBaseResponse()

data class CategoryData (
    @SerializedName("name" ) var name : String?         = null,
    @SerializedName("data" ) var data : List<KabbikItem> = listOf()
)