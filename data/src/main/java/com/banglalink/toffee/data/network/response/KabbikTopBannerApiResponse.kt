package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

data class KabbikTopBannerApiResponse(
    @SerializedName("data" ) var bannerItems : List<BannerItemBean> = listOf()
): ExternalBaseResponse(){
    data class BannerItemBean (
        @SerializedName("id"             ) var id            : Int?    = null,
        @SerializedName("en_name"        ) var enName        : String? = null,
        @SerializedName("name"           ) var name          : String? = null,
        @SerializedName("description"    ) var description   : String? = null,
        @SerializedName("author_name"    ) var authorName    : String? = null,
        @SerializedName("premium"        ) var premium       : Int?    = null,
        @SerializedName("thumb_path"     ) var thumbPath     : String? = null,
        @SerializedName("isFeatured"     ) var isFeatured    : Int?    = null,
        @SerializedName("featured_image" ) var featuredImage : String? = null,
        @SerializedName("price"          ) var price         : Int?    = null,
        @SerializedName("play_count"     ) var playCount     : Int?    = null,
        @SerializedName("rating"         ) var rating        : Double? = null
    )
}
