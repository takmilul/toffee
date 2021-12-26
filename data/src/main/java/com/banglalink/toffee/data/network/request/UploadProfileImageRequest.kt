package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName

data class UploadProfileImageRequest(
    @SerializedName("profilePhoto")
    val profilePhoto: String,
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String,
    @SerializedName("isDeletePhoto")
    val isDeletePhoto: Boolean = false
) : BaseRequest(ApiNames.UPDATE_USER_PROFILE_PHOTO)