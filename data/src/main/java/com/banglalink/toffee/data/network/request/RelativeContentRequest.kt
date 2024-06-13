package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RelativeContentRequest(
    @SerialName("contentId")
    val contentId: String = "0",
    @SerialName("videoTag")
    val videoTag: String,
    @SerialName("customerId")
    val customerId: Int,
    @SerialName("password")
    val password: String,
    @SerialName("categoryId")
    val categoryId: Int,
    @SerialName("subCategoryId")
    val subCategoryId: Int,
    @SerialName("offset")
    val offset: Int,
    @SerialName("limit")
    val limit: Int = 30
) : BaseRequest(ApiNames.GET_RELATIVE_CONTENTS)