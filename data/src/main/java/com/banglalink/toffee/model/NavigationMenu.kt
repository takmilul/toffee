package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class NavigationMenu(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("iconResoluteID")
    val iconResoluteID: Int,
    @SerializedName("categories")
    val categories: List<NavCategory>,
    @SerializedName("hasTopBorder")
    val hasTopBorder: Boolean = false
)