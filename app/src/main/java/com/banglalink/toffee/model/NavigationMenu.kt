package com.banglalink.toffee.model

data class NavigationMenu(
    val id: Int,
    val name: String,
    val iconResoluteID: Int,
    val categories: List<NavCategory>,
    val hasTopBorder: Boolean = false
)