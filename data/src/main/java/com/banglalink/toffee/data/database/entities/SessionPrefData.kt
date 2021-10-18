package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SessionPrefData(
    @PrimaryKey
    val prefName: String,
    val prefValue: String?,
)
