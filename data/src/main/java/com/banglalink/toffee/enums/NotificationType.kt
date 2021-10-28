package com.banglalink.toffee.enums

enum class NotificationType(val type: String) {
    SMALL("small"),
    LARGE("large"),
    LOGOUT("logout"),
    OVERLAY("overlay"),
    CHANGE_URL("change_cdn"),
    CLEAR_CACHE("clear_cache"),
    DRM_LICENSE_RELEASE("drmlicenserelease"),
    BETA_USER_DETECTION("beta_user_detection"),
}