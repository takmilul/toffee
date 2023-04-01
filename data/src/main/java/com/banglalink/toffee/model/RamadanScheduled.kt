package com.banglalink.toffee.model


import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class RamadanScheduled(
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    val id: Long? = null,
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