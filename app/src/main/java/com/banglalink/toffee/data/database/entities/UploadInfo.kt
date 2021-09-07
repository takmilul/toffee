package com.banglalink.toffee.data.database.entities

import android.util.Base64
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.banglalink.toffee.ui.upload.UploadStatus
import com.banglalink.toffee.util.UtilsKt

@Entity
data class UploadInfo(
    @PrimaryKey(autoGenerate = true)
    val uploadId: Long? = null,

    val fileUri: String,
    var fileName: String,

    var tusUploadUri: String? = null,
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
    var submitToChallengeIndex: Int = 0,
    val serverContentId: Long,
) {
    fun getFingerprint(): String? {
        if(uploadId != null && uploadId >= 0L) {
            val uploadIdStr = UtilsKt.uploadIdToString(uploadId)
            return Base64.encodeToString("${uploadIdStr}-$fileName".toByteArray(), Base64.NO_WRAP)
        }
        return null
    }

    fun getUploadIdStr(): String? {
        return if(uploadId != null && uploadId >= 0L) {
            UtilsKt.uploadIdToString(uploadId)
        } else null
    }

    fun getCopyrightUploadIdStr(): String? {
        return if(uploadId != null && uploadId >= 0L) {
            UtilsKt.uploadIdToString(uploadId) + "_copyright"
        } else null
    }

    fun getFileNameMetadata() = "filename " + Base64.encodeToString(fileName.toByteArray(), Base64.NO_WRAP)
}