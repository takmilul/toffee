package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Package(
    @SerialName("id")
    val packageId: Int = 0,
    @SerialName("package_name")
    val packageName: String? = null,
    @SerialName("package_badge_name")
    val packageBadgeName: String? = null,
    @SerialName("package_type")
    val packageType: String? = null,
    @SerialName("package_badge_icon")
    val packageIcon: String? = null,
    @SerialName("duration")
    val duration: Int = 0,
    @SerialName("is_initial")
    val isInitial: Int = 0,
    @SerialName("is_base_package")
    val isBasePackage: Int = 0,
    @SerialName("price")
    val price: Int = 0,
    @SerialName("discount")
    val discount: Int = 0,
    @SerialName("programs")
    val programs: Int = 0,
    @SerialName("is_subscribed")
    val isSubscribed: Boolean = false,
    @SerialName("is_commercial")
    val isCommercial: Int = 0,
    @SerialName("is_active")
    val isActive: Int = 0,
    @SerialName("is_percentage")
    var isPercentage: Int = 0,
    @SerialName("is_free")
    val isFree: Int = 0,
    @SerialName("package_mobile_logo")
    val mobileLogo: String? = null,
    @SerialName("package_stb_logo")
    val stbLogo: String? = null,
    @SerialName("package_poster_stb")
    val posterStb: String? = null,
    @SerialName("package_poster_mobile")
    val posterMobile: String? = null,
    @SerialName("package_start_date")
    val startDate: String? = null,
    @SerialName("package_expire_date")
    val expireDate: String? = null,
    @SerialName("is_auto_renew")
    var isAutoRenewable: Int = 0,
    @SerialName("auto_renew_button")
    val autoRenewButton: Int = 0
)