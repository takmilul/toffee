package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName

data class DataPackPurchaseRequest(
    @SerializedName("isBanglalinkNumber")
    val isBanglalinkNumber:Int?,
    @SerializedName("pack_id")
    val packId:Int?,
    @SerializedName("pack_title")
    val packTitle:String?,
    @SerializedName("contents")
    val contents:List<Int>?,
    @SerializedName("payment_method_id")
    val paymentMethodId:Int?,
    @SerializedName("pack_code")
    val packCode:String?,
    @SerializedName("pack_details")
    val packDetails:String?,
    @SerializedName("pack_price")
    val packPrice:Int?,
    @SerializedName("pack_duration")
    val packDuration:Int?
):BaseRequest(ApiNames.DATA_PACK_PURCHASE)
