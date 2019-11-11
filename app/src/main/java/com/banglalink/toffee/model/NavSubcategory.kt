package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class NavSubcategory(
    @SerializedName("id")
    val id: Int,
    @SerializedName("category_id")
    val categoryId: String,
    @SerializedName("sub_category_name")
    val subcategoryName: String
)