package com.banglalink.toffee.data.database.entities

import android.util.Base64
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.banglalink.toffee.enums.UploadStatus
import com.banglalink.toffee.util.UtilsKt
import com.google.gson.annotations.SerializedName

@Entity
data class UploadInfo(
    @SerializedName("uploadId")
    @PrimaryKey(autoGenerate = true)
    val uploadId: Long? = null,
    
    @SerializedName("fileUri")
    val fileUri: String,
    @SerializedName("fileName")
    var fileName: String,
    
    @SerializedName("tusUploadUri")
    var tusUploadUri: String? = null,
    @SerializedName("thumbUri")
    var thumbUri: String? = null,
    @SerializedName("status")
    var status: Int = UploadStatus.ADDED.value,
    @SerializedName("fileSize")
    var fileSize: Long = 0L,
    @SerializedName("completedSize")
    var completedSize: Long = 0L,
    @SerializedName("completedPercent")
    var completedPercent: Int = 0,
    @SerializedName("statusMessage")
    var statusMessage: String? = null,
    
    @SerializedName("title")
    var title: String? = null,
    @SerializedName("description")
    var description: String? = null,
    @SerializedName("tags")
    var tags: String? = null,
    @SerializedName("category")
    var category: String? = null,
    @SerializedName("categoryIndex")
    var categoryIndex: Int = 0,
    @SerializedName("ageGroup")
    var ageGroup: String? = null,
    @SerializedName("ageGroupIndex")
    var ageGroupIndex: Int = 0,
    @SerializedName("submitToChallenge")
    var submitToChallenge: String? = null,
    @SerializedName("submitToChallengeIndex")
    var submitToChallengeIndex: Int = 0,
    @SerializedName("serverContentId")
    val serverContentId: Long,
) {
    fun getFingerprint(): String? {
        if (uploadId != null && uploadId >= 0L) {
            val uploadIdStr = UtilsKt.uploadIdToString(uploadId)
            return Base64.encodeToString("${uploadIdStr}-$fileName".toByteArray(), Base64.NO_WRAP)
        }
        return null
    }
    
    fun getUploadIdStr(): String? {
        return if (uploadId != null && uploadId >= 0L) {
            UtilsKt.uploadIdToString(uploadId)
        } else null
    }
    
    fun getCopyrightUploadIdStr(): String? {
        return if (uploadId != null && uploadId >= 0L) {
            UtilsKt.uploadIdToString(uploadId) + "_copyright"
        } else null
    }
    
    fun getFileNameMetadata() = "filename " + Base64.encodeToString(fileName.toByteArray(), Base64.NO_WRAP)
}