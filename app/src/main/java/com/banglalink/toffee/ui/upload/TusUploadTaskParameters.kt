package com.banglalink.toffee.ui.upload

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TusUploadTaskParameters(
    var fingerprint: String,
    var uploadUrl: String? = null,
    var metadata: String? = null,
    var resumeOffset: Long = -1,
): Parcelable {
    companion object {
        const val FINGERPRINT = "tus-fingerprint"
        const val TUS_UPLOAD_URL = "tus-upload-url"
        const val TUS_RESUME_OFFSET = "tus-resume-offset"
    }
}