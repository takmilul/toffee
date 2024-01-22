package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.SubscriberPhotoBean
import kotlinx.serialization.SerialName

class UploadProfileImageResponse(
    @SerialName("response")
    val response: SubscriberPhotoBean
) : BaseResponse()