package com.banglalink.toffee.model


import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
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
)