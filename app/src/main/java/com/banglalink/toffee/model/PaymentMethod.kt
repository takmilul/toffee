package com.banglalink.toffee.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PaymentMethod(
    val accountType: String,
    val bankName: String,
    val accountName: String,
    val accountNumber: String,
    val routingNumber: String?,
    val district: String?,
    val branch: String?
) : Parcelable 