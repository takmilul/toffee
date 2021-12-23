package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName

data class ContentEditRequest(
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String,
    @SerializedName("contentId")
    val contentId: Int,
    @SerializedName("programName")
    val programName: String? = null,
    @SerializedName("bucketContentName")
    val bucketContentName: String,
    @SerializedName("categoryId")
    val categoryId: Int = 0,
    @SerializedName("subCategoryId")
    val subCategoryId: Int = 0,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("ageRestriction")
    val ageRestriction: String? = null,
    @SerializedName("videoTags")
    val videoTags: String? = null,
    @SerializedName("keywords")
    val keywords: String? = null,
    @SerializedName("oldContentBanner")
    val oldContentBanner: String? = null,
    @SerializedName("contentBanner")
    val contentBanner: String? = null,
) : BaseRequest(ApiNames.EDIT_CONTENT_UPLOAD)