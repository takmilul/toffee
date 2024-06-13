package com.banglalink.toffee.model

import kotlinx.serialization.Serializable

@Serializable
data class ReportListModel(
    val id: Int = 0,
    val title: String? = null
)
