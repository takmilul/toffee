package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NavigationMenu(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("name")
    val name: String,
    @SerialName("iconResoluteID")
    val iconResoluteID: Int = 0,
    @SerialName("categories")
    val categories: List<NavCategory>? = null,
    @SerialName("hasTopBorder")
    val hasTopBorder: Boolean = false
)