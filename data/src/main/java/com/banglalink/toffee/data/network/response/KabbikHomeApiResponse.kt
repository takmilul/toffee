package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

data class KabbikHomeApiResponse(
    @SerializedName("data" ) val data : List<KabbikCategory> = listOf()
) : ExternalBaseResponse()

data class KabbikCategory (
    @SerializedName("name" ) val name : String?         = null,
    @SerializedName("data" ) val itemsData : List<KabbikItem> = listOf()
)

data class KabbikItem (
    @SerializedName("id"           ) val id          : Int?    = null,
    @SerializedName("name"         ) val name        : String? = null,
    @SerializedName("en_name"      ) val enName      : String? = null,
    @SerializedName("description"  ) val description : String? = null,
    @SerializedName("author_name"  ) val authorName  : String? = null,
    @SerializedName("premium"      ) val premium     : Int?    = null,
    @SerializedName("thumb_path"   ) val thumbPath   : String? = null,
    @SerializedName("isFeatured"     ) var isFeatured    : Int?    = null,
    @SerializedName("featured_image" ) var featuredImage : String? = null,
    @SerializedName("price"        ) val price       : Int?    = null,
    @SerializedName("play_count"   ) val playCount   : Int?    = null,
    @SerializedName("rating"       ) val rating      : Double? = null,
    @SerializedName("total_played" ) val totalPlayed : Int?    = null
)