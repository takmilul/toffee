package com.banglalink.toffee.model

import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.data.network.request.BaseRequest
import com.banglalink.toffee.data.network.response.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeaturedPartner(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("web_view_url")
    val webViewUrl: String? = null,
    @SerialName("feature_partner_name")
    val featurePartnerName: String? = null,
    @SerialName("display_title")
    val partnerName: String? = "",
    @SerialName("feature_partner_description")
    val featurePartnerDescription: String? = null,
    @SerialName("sorting")
    val sorting: Int = 0,
    @SerialName("profile_url")
    val profileUrl: String? = null,
    @SerialName("banner_url")
    val bannerUrl: String? = null,
    @SerialName("is_login_required")
    val isLoginRequired:Boolean = false,
    @SerialName("url_type")
    val url_type: Int = 0
)

@Serializable
data class FeaturedPartnerRequest(
    @SerialName("customerId")
    val customerId:Int = 0,
    @SerialName("password")
    val password:String? = null,
): BaseRequest(ApiNames.GET_FEATURED_PARTNERS)

@Serializable
data class FeaturedPartnerResponse(
    @SerialName("response")
    val response: FeaturedPartnerBean? = null
): BaseResponse()

@Serializable
data class FeaturedPartnerBean(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("count")
    val count: Int = 0,
    @SerialName("totalCount")
    val totalCount: Int = 0,
    @SerialName("partners")
    val featuredPartners: List<FeaturedPartner>? = null
)