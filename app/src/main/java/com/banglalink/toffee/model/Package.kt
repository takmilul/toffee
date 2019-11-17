package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Package (
    @SerializedName("id")
    val packageId: Int = 0,
    @SerializedName("package_name")
    val packageName: String? = null,
    @SerializedName("package_badge_name")
    val packageBadgeName: String? = null,
    @SerializedName("package_type")
    val packageType: String? = null,
    @SerializedName("package_badge_icon")
    val packageIcon: String? = null,
    @SerializedName("duration")
    val duration: Int = 0,
    @SerializedName("is_initial")
    val isInitial: Int = 0,
    @SerializedName("price")
    val price: Int = 0,
    @SerializedName("discount")
    val discount: Int = 0,
    @SerializedName("programs")
    val programs: Int = 0,
    @SerializedName("is_subscribed")
    val isSubscribed: Boolean = false,
    @SerializedName("is_commercial")
    val isCommercial: Int = 0,
    @SerializedName("is_active")
    val isActive: Int = 0,
    @SerializedName("is_percentage")
    var isPercentage: Int = 0,
    @SerializedName("is_free")
    val isFree: Int = 0,
    @SerializedName("package_mobile_logo")
    val mobileLogo: String? = null,
    @SerializedName("package_stb_logo")
    val stbLogo: String? = null,
    @SerializedName("package_poster_stb")
    val posterStb: String? = null,
    @SerializedName("package_poster_mobile")
    val posterMobile: String? = null,
    @SerializedName("package_start_date")
    val startDate: String? = null,
    @SerializedName("package_expire_date")
    val expireDate: String? = null,
    @SerializedName("autoRenew")
    val isAutoRenewable: Boolean = false
):Serializable