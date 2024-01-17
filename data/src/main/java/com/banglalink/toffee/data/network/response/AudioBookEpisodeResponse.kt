package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.lib.R
import com.google.gson.annotations.SerializedName

data class AudioBookEpisodeResponse(
    @SerializedName("approval_status"      ) var approvalStatus      : Int?                = null,
    @SerializedName("author_name"          ) var authorName          : String?             = null,
    @SerializedName("banner_path"          ) var bannerPath          : String?             = null,
    @SerializedName("category_id"          ) var categoryId          : String?             = null,
    @SerializedName("channel_id"           ) var channelId           : Int?                = null,
    @SerializedName("contributing_artists" ) var contributingArtists : String?             = null,
    @SerializedName("created_at"           ) var createdAt           : String?             = null,
    @SerializedName("deleted"              ) var deleted             : Int?                = null,
    @SerializedName("description"          ) var description         : String?             = null,
    @SerializedName("discount_price"       ) var discountPrice       : Int?                = null,
    @SerializedName("for_app"              ) var forApp              : Int?                = null,
    @SerializedName("guid"                 ) var guid                : String?             = null,
    @SerializedName("id"                   ) var id                  : Int?                = null,
    @SerializedName("name"                 ) var name                : String?             = null,
    @SerializedName("en_name"              ) var enName              : String?             = null,
    @SerializedName("play_count"           ) var playCount           : Int?                = null,
    @SerializedName("podcast"              ) var podcast             : Int?                = null,
    @SerializedName("premium"              ) var premium             : Int?                = null,
    @SerializedName("price"                ) var price               : Int?                = null,
    @SerializedName("publish_year"         ) var publishYear         : String?             = null,
    @SerializedName("thumb_path"           ) var thumbPath           : String?             = null,
    @SerializedName("updated_at"           ) var updatedAt           : String?             = null,
    @SerializedName("publisher_id"         ) var publisherId         : Int?                = null,
    @SerializedName("c_name"               ) var cName               : String?             = null,
    @SerializedName("file_name"            ) var fileName            : String?             = null,
    @SerializedName("file_path"            ) var filePath            : String?             = null,
    @SerializedName("rating"               ) var rating              : Double?             = null,
    @SerializedName("rating_count"         ) var ratingCount         : Int?                = null,
    @SerializedName("user_rating"          ) var userRating          : Int?                = null,
    @SerializedName("review"               ) var review              : String?             = null,
    @SerializedName("is_favorite"          ) var isFavorite          : Boolean?            = null,
    @SerializedName("episodes"             ) var episodes            : List<Episodes> = listOf()
): ExternalBaseResponse()

data class Episodes (
    @SerializedName("isSelected") var isSelected: Boolean? = false,
    @SerializedName("id"           ) var id          : Int?    = null,
    @SerializedName("name"         ) var name        : String? = null,
    @SerializedName("description"  ) var description : String? = null,
    @SerializedName("file_name"    ) var fileName    : String? = null,
    @SerializedName("file_path"    ) var filePath    : String? = null,
    @SerializedName("created_at"   ) var createdAt   : String? = null,
    @SerializedName("updated_at"   ) var updatedAt   : String? = null,
    @SerializedName("audiobook_id" ) var audiobookId : Int?    = null,
    @SerializedName("isfree"       ) var isfree      : Int?    = null,
    @SerializedName("play_count"   ) var playCount   : String? = null,
    @SerializedName("duration"     ) var duration    : String? = null

)