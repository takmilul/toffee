package com.banglalink.toffee.data.network.request

data class ContentUploadRequest(
    val customerId: Int,
    val password: String,
    val programName: String? = null,
    val bucketContentName: String,
    val categoryId: Int = 0,
    val subCategoryId: Int = 0,
    val description: String? = null,
    val ageRestriction: String? = null,
    val videoTags: String? = null,
    val keywords: String? = null,
    val contentBanner: String? = null,
    val duration: String? = null,
    val isHorizontal: Int = 1,
):BaseRequest("ugcContentUpload")