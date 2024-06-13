package com.banglalink.toffee.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class NavSubcategory(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("category_id")
    val categoryId: String? = "0",
    @SerialName("sub_category_name")
    val subcategoryName: String? = null
): Parcelable