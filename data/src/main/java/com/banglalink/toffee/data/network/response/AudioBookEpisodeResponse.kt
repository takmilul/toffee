package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AudioBookEpisodeResponse(
    @SerialName("approval_status"      ) var approvalStatus      : Int?                = null,
    @SerialName("author_name"          ) var authorName          : String?             = null,
    @SerialName("banner_path"          ) var bannerPath          : String?             = null,
    @SerialName("category_id"          ) var categoryId          : String?             = null,
    @SerialName("channel_id"           ) var channelId           : Int?                = null,
    @SerialName("contributing_artists" ) var contributingArtists : String?             = null,
    @SerialName("created_at"           ) var createdAt           : String?             = null,
    @SerialName("deleted"              ) var deleted             : Int?                = null,
    @SerialName("description"          ) var description         : String?             = null,
    @SerialName("discount_price"       ) var discountPrice       : Int?                = null,
    @SerialName("for_app"              ) var forApp              : Int?                = null,
    @SerialName("guid"                 ) var guid                : String?             = null,
    @SerialName("id"                   ) var id                  : Int?                = null,
    @SerialName("name"                 ) var name                : String?             = null,
    @SerialName("en_name"              ) var enName              : String?             = null,
    @SerialName("play_count"           ) var playCount           : Int?                = null,
    @SerialName("podcast"              ) var podcast             : Int?                = null,
    @SerialName("premium"              ) var premium             : Int?                = null,
    @SerialName("price"                ) var price               : Int?                = null,
    @SerialName("publish_year"         ) var publishYear         : String?             = null,
    @SerialName("thumb_path"           ) var thumbPath           : String?             = null,
    @SerialName("updated_at"           ) var updatedAt           : String?             = null,
    @SerialName("publisher_id"         ) var publisherId         : Int?                = null,
    @SerialName("c_name"               ) var cName               : String?             = null,
    @SerialName("file_name"            ) var fileName            : String?             = null,
    @SerialName("file_path"            ) var filePath            : String?             = null,
    @SerialName("rating"               ) var rating              : Double?             = null,
    @SerialName("rating_count"         ) var ratingCount         : Int?                = null,
    @SerialName("user_rating"          ) var userRating          : Int?                = null,
    @SerialName("review"               ) var review              : String?             = null,
    @SerialName("is_favorite"          ) var isFavorite          : Boolean?            = null,
    @SerialName("episodes"             ) var episodes            : List<Episodes> = listOf()
): ExternalBaseResponse()

@Serializable
data class Episodes (
    @SerialName("isSelected") var isSelected: Boolean? = false,
    @SerialName("id"           ) var id          : Int?    = null,
    @SerialName("name"         ) var name        : String? = null,
    @SerialName("description"  ) var description : String? = null,
    @SerialName("file_name"    ) var fileName    : String? = null,
    @SerialName("file_path"    ) var filePath    : String? = null,
    @SerialName("created_at"   ) var createdAt   : String? = null,
    @SerialName("updated_at"   ) var updatedAt   : String? = null,
    @SerialName("audiobook_id" ) var audiobookId : Int?    = null,
    @SerialName("isfree"       ) var isFree      : Int?    = null,
    @SerialName("play_count"   ) var playCount   : String? = null,
    @SerialName("duration"     ) var duration    : String? = null,
    @SerialName("price"        ) var price       : Int?    = null,
)