package com.banglalink.toffee.data.network.request

data class ContentEditRequest(
    val customerId: Int,
    val password: String,
    val contentId: Int,
    val programName: String? = null,
    val bucketContentName: String,
    val categoryId: Int = 0,
    val subCategoryId: Int = 0,
    val description: String? = null,
    val ageRestriction: String? = null,
    val videoTags: String? = null,
    val keywords: String? = null,
    val contentBanner: String? = null,
):BaseRequest("ugcContentUpdate")