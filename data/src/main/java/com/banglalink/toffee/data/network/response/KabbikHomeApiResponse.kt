package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

data class KabbikHomeApiResponse(
    @SerializedName("data" ) var data : List<KabbikCategory> = listOf()
) : ExternalBaseResponse()

data class KabbikCategory (
    @SerializedName("name" ) var name : String?         = null,
    @SerializedName("data" ) var itemsData : List<KabbikItemBean> = listOf()
)

data class KabbikItemBean (
    @SerializedName("id"           ) var id          : Int?    = null,
    @SerializedName("name"         ) var name        : String? = null,
    @SerializedName("en_name"      ) var enName      : String? = null,
    @SerializedName("description"  ) var description : String? = null,
    @SerializedName("author_name"  ) var authorName  : String? = null,
    @SerializedName("premium"      ) var premium     : Int?    = null,
    @SerializedName("thumb_path"   ) var thumbPath   : String? = null,
    @SerializedName("price"        ) var price       : Int?    = null,
    @SerializedName("play_count"   ) var playCount   : Int?    = null,
    @SerializedName("rating"       ) var rating      : Double? = null,
    @SerializedName("total_played" ) var totalPlayed : Int?    = null
)