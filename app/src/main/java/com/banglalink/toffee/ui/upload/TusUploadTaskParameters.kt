package com.banglalink.toffee.ui.upload

import kotlinx.serialization.Serializable
import net.gotev.uploadservice.persistence.PersistableData

@Serializable
data class TusUploadTaskParameters(
    var fingerprint: String,
    var uploadUrl: String? = null,
    var metadata: String? = null
): PersistableData() {
    companion object {
        const val FINGERPRINT = "tus-fingerprint"
        const val TUS_UPLOAD_URL = "tus-upload-url"
    }
}