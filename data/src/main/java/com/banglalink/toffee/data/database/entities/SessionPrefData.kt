package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class SessionPrefData(
    @SerializedName("prefName")
    @PrimaryKey
    val prefName: String,
    @SerializedName("prefValue")
    val prefValue: String?,
)
