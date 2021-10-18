package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class CategoryBean(
    @SerializedName("categories")
    val categories: List<Category>?
)