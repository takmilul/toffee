package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class UserInterestStatus (
    @SerializedName("category_name")
    val interestName: String,
    
    @SerializedName("is_interested")
    var isInterested: Int = 0
)