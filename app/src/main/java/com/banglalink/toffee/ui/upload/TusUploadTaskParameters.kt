package com.banglalink.toffee.ui.upload

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TusUploadTaskParameters(
    var fingerprint: String? = null,
    var uploadUrl: String? = null,
    var metadata: String? = null,
): Parcelable