package com.banglalink.toffee.data.network.request

import kotlinx.serialization.Serializable

@Serializable
class DrmLicenseRequest(
    val payload: String = "CAQ=",
    val drmType: String = "WV",
    val contentId: String = "1",
    val providerId: String = "toffee",
    val packageId: String = "1",
    val token: String = "dummy"
)