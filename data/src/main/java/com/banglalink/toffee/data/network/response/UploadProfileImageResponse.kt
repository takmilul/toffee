package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.SubscriberPhotoBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class UploadProfileImageResponse(
    @SerialName("response")
    val response: SubscriberPhotoBean? = null
) : BaseResponse()