package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentEditRequest(
    @SerialName("customerId")
    val customerId: Int,
    @SerialName("password")
    val password: String,
    @SerialName("contentId")
    val contentId: Int,
    @SerialName("programName")
    val programName: String? = null,
    @SerialName("bucketContentName")
    val bucketContentName: String,
    @SerialName("categoryId")
    val categoryId: Int = 0,
    @SerialName("subCategoryId")
    val subCategoryId: Int = 0,
    @SerialName("description")
    val description: String? = null,
    @SerialName("ageRestriction")
    val ageRestriction: String? = null,
    @SerialName("videoTags")
    val videoTags: String? = null,
    @SerialName("keywords")
    val keywords: String? = null,
    @SerialName("oldContentBanner")
    val oldContentBanner: String? = null,
    @SerialName("contentBanner")
    val contentBanner: String? = null,
) : BaseRequest(ApiNames.EDIT_CONTENT_UPLOAD)