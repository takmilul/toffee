package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KabbikHomeApiResponse(
    @SerialName("data" ) val data : List<KabbikCategory> = listOf()
) : ExternalBaseResponse()
@Serializable
data class KabbikCategory (
    @SerialName("name" ) val name : String?         = null,
    @SerialName("data" ) val itemsData : List<KabbikItemBean> = listOf()
)
@Serializable
data class KabbikItemBean (
    @SerialName("id"           ) val id          : Int?    = null,
    @SerialName("name"         ) val name        : String? = null,
    @SerialName("en_name"      ) val enName      : String? = null,
    @SerialName("description"  ) val description : String? = null,
    @SerialName("author_name"  ) val authorName  : String? = null,
    @SerialName("premium"      ) val premium     : Int?    = null,
    @SerialName("thumb_path"   ) val thumbPath   : String? = null,
    @SerialName("price"        ) val price       : Int?    = null,
    @SerialName("play_count"   ) val playCount   : Int?    = null,
    @SerialName("rating"       ) val rating      : Double? = null,
    @SerialName("total_played" ) val totalPlayed : Int?    = null
)