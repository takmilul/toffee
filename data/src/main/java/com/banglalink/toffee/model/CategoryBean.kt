package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoryBean(
    @SerialName("categories")
    val categories: List<Category>?
)