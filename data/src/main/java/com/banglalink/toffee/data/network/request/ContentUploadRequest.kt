package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentUploadRequest(
    @SerialName("customerId")
    val customerId: Int,
    @SerialName("password")
    val password: String,
    @SerialName("programName")
    val programName: String? = null,
    @SerialName("bucketContentName")
    val bucketContentName: String,
    @SerialName("copyrightFileName")
    val copyrightFileName: String? = null,
    @SerialName("isCopyright")
    val isCopyright: Int = 0,
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
    @SerialName("contentBanner")
    val contentBanner: String? = null,
    @SerialName("duration")
    val duration: String? = null,
    @SerialName("isHorizontal")
    val isHorizontal: Int = 1,
    @SerialName("msisdn")
    val msisdn: String? = null,
) : BaseRequest(ApiNames.UPLOAD_CONTENT)