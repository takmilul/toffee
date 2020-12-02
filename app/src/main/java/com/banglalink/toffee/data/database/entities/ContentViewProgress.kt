package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ContentViewProgress (
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val customerId: Int,
    val contentId: Long,
    val progress: Long,
    val watchTime: Long = System.currentTimeMillis(),
)