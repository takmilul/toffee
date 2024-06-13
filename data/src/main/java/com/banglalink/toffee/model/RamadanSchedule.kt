package com.banglalink.toffee.model

import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RamadanSchedule(
    @PrimaryKey(autoGenerate = true)
    @SerialName("id")
    val id: Long? = null,
    @SerialName("bubble_logo_url")
    val bubbleLogoUrl: String? = null,
    @SerialName("day_name")
    val dayName: String? = null,
    @SerialName("division")
    val division: String? = null,
    @SerialName("iftar_start")
    val iftarStart: String? = null,
    @SerialName("is_eid_start")
    val isEidStart: Int? = null,
    @SerialName("is_ramadan_start")
    val isRamadanStart: Int? = null,
    @SerialName("message")
    val message: String? = null,
    @SerialName("sehri_start")
    val sehriStart: String? = null,
)