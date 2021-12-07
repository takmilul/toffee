package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.SubscriberPhotoBean
import com.google.gson.annotations.SerializedName

class UploadProfileImageResponse(
    @SerializedName("response")
    val response: SubscriberPhotoBean
) : BaseResponse()