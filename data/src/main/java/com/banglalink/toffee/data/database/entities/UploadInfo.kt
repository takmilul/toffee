package com.banglalink.toffee.data.database.entities

import android.util.Base64
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.banglalink.toffee.enums.UploadStatus
import com.banglalink.toffee.util.Utils
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class UploadInfo(
    @SerialName("uploadId")
    @PrimaryKey(autoGenerate = true)
    val uploadId: Long? = null,
    
    @SerialName("fileUri")
    val fileUri: String,
    @SerialName("fileName")
    var fileName: String,
    
    @SerialName("tusUploadUri")
    var tusUploadUri: String? = null,
    @SerialName("thumbUri")
    var thumbUri: String? = null,
    @SerialName("status")
    var status: Int = UploadStatus.ADDED.value,
    @SerialName("fileSize")
    var fileSize: Long = 0L,
    @SerialName("completedSize")
    var completedSize: Long = 0L,
    @SerialName("completedPercent")
    var completedPercent: Int = 0,
    @SerialName("statusMessage")
    var statusMessage: String? = null,
    
    @SerialName("title")
    var title: String? = null,
    @SerialName("description")
    var description: String? = null,
    @SerialName("tags")
    var tags: String? = null,
    @SerialName("category")
    var category: String? = null,
    @SerialName("categoryIndex")
    var categoryIndex: Int = 0,
    @SerialName("ageGroup")
    var ageGroup: String? = null,
    @SerialName("ageGroupIndex")
    var ageGroupIndex: Int = 0,
    @SerialName("submitToChallenge")
    var submitToChallenge: String? = null,
    @SerialName("submitToChallengeIndex")
    var submitToChallengeIndex: Int = 0,
    @SerialName("serverContentId")
    val serverContentId: Long,
) {
    fun getFingerprint(): String? {
        if (uploadId != null && uploadId >= 0L) {
            val uploadIdStr = Utils.uploadIdToString(uploadId)
            return Base64.encodeToString("${uploadIdStr}-$fileName".toByteArray(), Base64.NO_WRAP)
        }
        return null
    }
    
    fun getUploadIdStr(): String? {
        return if (uploadId != null && uploadId >= 0L) {
            Utils.uploadIdToString(uploadId)
        } else null
    }
    
    fun getCopyrightUploadIdStr(): String? {
        return if (uploadId != null && uploadId >= 0L) {
            Utils.uploadIdToString(uploadId) + "_copyright"
        } else null
    }
    
    fun getFileNameMetadata() = "filename " + Base64.encodeToString(fileName.toByteArray(), Base64.NO_WRAP)
}