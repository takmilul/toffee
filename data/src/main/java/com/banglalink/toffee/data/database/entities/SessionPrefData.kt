package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class SessionPrefData(
    @SerialName("prefName")
    @PrimaryKey
    val prefName: String,
    @SerialName("prefValue")
    val prefValue: String?,
)
