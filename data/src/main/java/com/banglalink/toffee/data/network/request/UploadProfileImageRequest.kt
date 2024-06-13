package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadProfileImageRequest(
    @SerialName("profilePhoto")
    val profilePhoto: String,
    @SerialName("customerId")
    val customerId: Int,
    @SerialName("password")
    val password: String,
    @SerialName("isDeletePhoto")
    val isDeletePhoto: Boolean = false
) : BaseRequest(ApiNames.UPDATE_USER_PROFILE_PHOTO)