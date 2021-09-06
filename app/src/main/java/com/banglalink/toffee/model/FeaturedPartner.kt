package com.banglalink.toffee.model

import com.banglalink.toffee.data.network.request.BaseRequest
import com.banglalink.toffee.data.network.response.BaseResponse
import com.google.gson.annotations.SerializedName

data class FeaturedPartner(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("web_view_url")
    val webViewUrl: String? = null,
    @SerializedName("feature_partner_name")
    val featurePartnerName: String? = null,
    @SerializedName("feature_partner_description")
    val featurePartnerDescription: String? = null,
    @SerializedName("sorting")
    val sorting: Int = 0,
    @SerializedName("profile_url")
    val profileUrl: String? = null,
    @SerializedName("banner_url")
    val bannerUrl: String? = null
)

data class FeaturedPartnerRequest(
    @SerializedName("customerId")
    val customerId:Int,
    @SerializedName("password")
    val password:String,
): BaseRequest("getUgcFeaturePartnerList")

data class FeaturedPartnerResponse(
    val response: FeaturedPartnerBean
): BaseResponse()

data class FeaturedPartnerBean(
    @SerializedName("code")
    val code: Int = 0,
    @SerializedName("count")
    val count: Int = 0,
    @SerializedName("totalCount")
    val totalCount: Int = 0,
    @SerializedName("partners")
    val featuredPartners: List<FeaturedPartner>? = null
)