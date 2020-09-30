package com.banglalink.toffee.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserChannel(
    val id: String,
    val posterUrl: String,
    val logoUrl: String,
    var title: String,
    var description: String,
    val categoryList: List<String>,
    val selectedCategory: String,
    val subscriptionPriceList: List<String>,
    val selectedSubscriptionPrice: String
) : Parcelable