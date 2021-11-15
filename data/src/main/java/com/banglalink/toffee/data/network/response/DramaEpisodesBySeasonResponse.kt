package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.DramaSeriesContentBean
import com.google.gson.annotations.SerializedName

data class DramaEpisodesBySeasonResponse(
    @SerializedName("response")
    val response: DramaSeriesContentBean
) : BaseResponse()