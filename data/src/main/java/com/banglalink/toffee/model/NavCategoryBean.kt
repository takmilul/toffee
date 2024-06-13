package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NavCategoryBean(
    @SerialName("categories")
    val categories: NavCategoryGroup
)