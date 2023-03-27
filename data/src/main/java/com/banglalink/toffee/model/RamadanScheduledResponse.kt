package com.banglalink.toffee.model


import com.google.gson.annotations.SerializedName

data class RamadanScheduledResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("ramadanScheduled")
    val ramadanScheduled: List<RamadanScheduled>,
    @SerializedName("serverDateTime")
    val serverDateTime: String,
    @SerializedName("totalCount")
    val totalCount: Int
) {
    data class RamadanScheduled(
        @SerializedName("bubble_logo_url")
        val bubbleLogoUrl: String,
        @SerializedName("day_name")
        val dayName: String,
        @SerializedName("division")
        val division: String,
        @SerializedName("iftar_start")
        val iftarStart: String,
        @SerializedName("is_eid_start")
        val isEidStart: Int,
        @SerializedName("is_ramadan_start")
        val isRamadanStart: Int,
        @SerializedName("message")
        val message: String,
        @SerializedName("sehri_start")
        val sehriStart: String
    )
}