package com.banglalink.toffee.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Category(
    @SerializedName("id")
    val id: Long = 0L,
    @SerializedName("category_name")
    val categoryName: String = "Category",
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String? = null,
    @SerializedName("updated_by")
    val updatedBy: Int = 0,
    @SerializedName("is_active")
    val isActive: Int = 0,
    @SerializedName("color_code")
    val colorCode: String? = null,
    @SerializedName("category_icon")
    val categoryIcon: String? = null,
    @SerializedName("subcategories")
    val subcategories: List<SubCategory>? = null,
    @SerializedName("category_share_url")
    val categoryShareUrl: String? = null
): Parcelable {
    override fun toString(): String = categoryName
}