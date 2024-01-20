package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

data class KabbikTopBannerApiResponse(
    @SerializedName("data" ) var bannerItems : List<BannerItemBean> = listOf()
): ExternalBaseResponse(){
    data class BannerItemBean (
        @SerializedName("id"          ) val id          : Int?    = null,
        @SerializedName("en_name"     ) val enName      : String? = null,
        @SerializedName("name"        ) val name        : String? = null,
        @SerializedName("description" ) val description : String? = null,
        @SerializedName("author_name" ) val authorName  : String? = null,
        @SerializedName("premium"     ) val premium     : Int?    = null,
        @SerializedName("thumb_path"  ) val thumbPath   : String? = null,
        @SerializedName("featured_image"  ) val featuredImage   : String? = null,
        @SerializedName("price"       ) val price       : Int?    = null,
        @SerializedName("play_count"  ) val playCount   : Int?    = null,
        @SerializedName("rating"      ) val rating      : Double?    = null
    )
}
