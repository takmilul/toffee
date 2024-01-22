package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KabbikTopBannerApiResponse(
    @SerialName("data" ) var bannerItems : List<BannerItemBean> = listOf()
): ExternalBaseResponse(){
    @Serializable
    data class BannerItemBean (
        @SerialName("id"             ) var id            : Int?    = null,
        @SerialName("en_name"        ) var enName        : String? = null,
        @SerialName("name"           ) var name          : String? = null,
        @SerialName("description"    ) var description   : String? = null,
        @SerialName("author_name"    ) var authorName    : String? = null,
        @SerialName("premium"        ) var premium       : Int?    = null,
        @SerialName("thumb_path"     ) var thumbPath     : String? = null,
        @SerialName("isFeatured"     ) var isFeatured    : Int?    = null,
        @SerialName("featured_image" ) var featuredImage : String? = null,
        @SerialName("price"          ) var price         : Int?    = null,
        @SerialName("play_count"     ) var playCount     : Int?    = null,
        @SerialName("rating"         ) var rating        : Double? = null
    )
}
