package com.banglalink.toffee.model

import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class RamadanSchedule(
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("bubble_logo_url")
    val bubbleLogoUrl: String? = null,
    @SerializedName("day_name")
    val dayName: String? = null,
    @SerializedName("division")
    val division: String? = null,
    @SerializedName("iftar_start")
    val iftarStart: String? = null,
    @SerializedName("is_eid_start")
    val isEidStart: Int? = null,
    @SerializedName("is_ramadan_start")
    val isRamadanStart: Int? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("sehri_start")
    val sehriStart: String? = null,
)