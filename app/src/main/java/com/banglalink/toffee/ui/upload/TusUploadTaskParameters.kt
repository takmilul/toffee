package com.banglalink.toffee.ui.upload

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TusUploadTaskParameters(
    var fingerprint: String,
    var uploadUrl: String? = null,
    var metadata: String? = null
): Parcelable {
    companion object {
        const val FINGERPRINT = "tus-fingerprint"
        const val TUS_UPLOAD_URL = "tus-upload-url"
    }
}