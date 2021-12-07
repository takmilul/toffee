package com.banglalink.toffee.data.network.request

import com.google.gson.annotations.SerializedName

data class ContentUploadRequest(
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String,
    @SerializedName("programName")
    val programName: String? = null,
    @SerializedName("bucketContentName")
    val bucketContentName: String,
    @SerializedName("copyrightFileName")
    val copyrightFileName: String? = null,
    @SerializedName("isCopyright")
    val isCopyright: Int = 0,
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
    @SerializedName("contentBanner")
    val contentBanner: String? = null,
    @SerializedName("duration")
    val duration: String? = null,
    @SerializedName("isHorizontal")
    val isHorizontal: Int = 1,
    @SerializedName("msisdn")
    val msisdn: String? = null,
) : BaseRequest("ugcContentUpload")