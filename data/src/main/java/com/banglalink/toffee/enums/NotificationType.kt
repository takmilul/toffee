package com.banglalink.toffee.enums

enum class NotificationType(val type: String) {
    SMALL("small"),
    LARGE("large"),
    LOGOUT("logout"),
    OVERLAY("overlay"),
    CHANGE_URL("change_cdn"),
    CLEAR_CACHE("clear_cache"),
    CONTENT_REFRESH("content_refresh"),
    CHANGE_URL_EXTENDED("change_host_url"),
    DRM_LICENSE_RELEASE("drmLicenseRelease"),
    BETA_USER_DETECTION("beta_user_detection"),
}