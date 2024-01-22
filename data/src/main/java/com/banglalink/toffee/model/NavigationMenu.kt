package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NavigationMenu(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
    @SerialName("iconResoluteID")
    val iconResoluteID: Int,
    @SerialName("categories")
    val categories: List<NavCategory>,
    @SerialName("hasTopBorder")
    val hasTopBorder: Boolean = false
)