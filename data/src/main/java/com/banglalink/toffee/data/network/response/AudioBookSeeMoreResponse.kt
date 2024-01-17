package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

data class AudioBookSeeMoreResponse(
    @SerializedName("data" ) var data : List<CategoryData> = listOf()
): ExternalBaseResponse()

data class CategoryData (
    @SerializedName("name" ) var name : String?         = null,
    @SerializedName("data" ) var data : List<DataBean> = listOf()
)

data class DataBean (
    @SerializedName("id"          ) var id          : Int?    = null,
    @SerializedName("name"        ) var name        : String? = null,
    @SerializedName("description" ) var description : String? = null,
    @SerializedName("author_name" ) var authorName  : String? = null,
    @SerializedName("premium"     ) var premium     : Int?    = null,
    @SerializedName("thumb_path"  ) var thumbPath   : String? = null,
    @SerializedName("price"       ) var price       : Int?    = null,
    @SerializedName("play_count"  ) var playCount   : Int?    = null,
    @SerializedName("rating"      ) var rating      : Double? = null
)
