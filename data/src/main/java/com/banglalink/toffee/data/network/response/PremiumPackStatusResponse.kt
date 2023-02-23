package com.banglalink.toffee.data.network.response


import com.banglalink.toffee.model.ActivePack
import com.google.gson.annotations.SerializedName

data class PremiumPackStatusResponse(

    @SerializedName("apiLanguage")
    val apiLanguage: String,
    @SerializedName("debugCode")
    val debugCode: Int,
    @SerializedName("debugMsg")
    val debugMsg: String,
    @SerializedName("response")
    val response: Response,

): BaseResponse()

{
    data class Response(
        @SerializedName("login_related_subs_history")
        val loginRelatedSubsHistory: List<ActivePack>
    )
}