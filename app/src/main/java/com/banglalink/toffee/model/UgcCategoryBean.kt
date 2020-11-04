package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class UgcCategoryBean(
    @SerializedName("categories")
    val categories: List<UgcCategory>?
)