package com.banglalink.toffee.ui.upload

import net.gotev.uploadservice.persistence.PersistableData

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