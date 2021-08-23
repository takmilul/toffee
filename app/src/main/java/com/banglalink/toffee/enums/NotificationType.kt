package com.banglalink.toffee.enums

enum class NotificationType(val type: String) {
    SMALL("small"),
    LARGE("large"),
    LOGOUT("logout"),
    OVERLAY("overlay"),
    CHANGE_URL("change_cdn"),
    DRM_LICENSE_RELEASE("drmLicenseRelease"),
}