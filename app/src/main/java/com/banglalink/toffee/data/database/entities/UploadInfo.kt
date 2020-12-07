package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.banglalink.toffee.ui.upload.UploadStatus

@Entity
data class UploadInfo(
    @PrimaryKey(autoGenerate = true)
    val uploadId: Long? = null,

    val serverContentId: Long,
    val fileUri: String,
    var fileName: String,

    var thumbUri: String? = null,
    var status: Int = UploadStatus.ADDED.value,
    var fileSize: Long = 0L,
    var completedSize: Long = 0L,
    var completedPercent: Int = 0,
    var statusMessage: String? = null,

    var title: String? = null,
    var description: String? = null,
    var tags: String? = null,
    var category: String? = null,
    var categoryIndex: Int = 0,
    var ageGroup: String? = null,
    var ageGroupIndex: Int = 0,
    var submitToChallenge: String? = null,
    var submitToChallengeIndex: Int = 0
)