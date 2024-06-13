package com.banglalink.toffee.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Category(
    @SerialName("id")
    val id: Long = 0L,
    @SerialName("category_name")
    val categoryName: String = "Category",
    @SerialName("thumbnail_url")
    val thumbnailUrl: String? = null,
    @SerialName("updated_by")
    val updatedBy: Int = 0,
    @SerialName("is_active")
    val isActive: Int = 0,
    @SerialName("color_code")
    val colorCode: String? = null,
    @SerialName("category_icon")
    val categoryIcon: String? = null,
    @SerialName("subcategories")
    val subcategories: List<SubCategory>? = null,
    @SerialName("category_share_url")
    val categoryShareUrl: String? = null
): Parcelable {
    override fun toString(): String = categoryName
}