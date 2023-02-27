package com.banglalink.toffee.data.network.response


import com.google.gson.annotations.SerializedName

data class PackageWisePremiumPackResponse(

    @SerializedName("apiLanguage")
    val apiLanguage: String,
    @SerializedName("debugCode")
    val debugCode: Int,
    @SerializedName("debugMsg")
    val debugMsg: String,
    @SerializedName("response")
    val response: PackageWisePremiumPackBean

): BaseResponse()
{
    data class PackageWisePremiumPackBean(
        @SerializedName("BKASH")
        val bkash: BKASH,
        @SerializedName("BL")
        val bl: BL,
        @SerializedName("FREE")
        val free: List<PaymentPackDetails>
    ) {
        data class BKASH(
            @SerializedName("data_packs")
            val dataPacks: List<PaymentPackDetails>,
            @SerializedName("minimum_price")
            val minimumPrice: Int
        )

        data class BL(
            @SerializedName("minimum_price")
            val minimumPrice: Int,
            @SerializedName("POSTPAID")
            val pOSTPAID: List<PaymentPackDetails>,
            @SerializedName("PREPAID")
            val pREPAID: List<PaymentPackDetails>
        )


    }
}

data class PaymentPackDetails(
    @SerializedName("data_pack_id")
    val dataPackId: Int,
    @SerializedName("payment_method_id")
    val paymentMethodId: Int,
    @SerializedName("is_non_bl_free")
    val isNonBlFree: Int,
    @SerializedName("pack_code")
    val packCode: String?= null,
    @SerializedName("pack_details")
    val packDetails: String?= null,
    @SerializedName("pack_price")
    val packPrice: Int,
    @SerializedName("pack_duration")
    val packDuration: Int,
    @SerializedName("sort_by_code")
    val sortByCode: Int,
    @SerializedName("is_prepaid")
    val isPrepaid: Int
)