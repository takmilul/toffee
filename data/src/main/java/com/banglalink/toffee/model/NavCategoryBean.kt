package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class NavCategoryBean(
    @SerializedName("categories")
    val categories: NavCategoryGroup
)