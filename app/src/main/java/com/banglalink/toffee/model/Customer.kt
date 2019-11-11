package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class Customer(
    @SerializedName("get_profile")
    val profile: Profile
)