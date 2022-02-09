package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.FavoriteBean
import com.google.gson.annotations.SerializedName

class FavoriteResponse(
    @SerializedName("response")
    val response: FavoriteBean
) : BaseResponse()