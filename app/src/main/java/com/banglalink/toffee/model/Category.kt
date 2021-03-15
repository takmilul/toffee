package com.banglalink.toffee.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Category(
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

    val subcategories: List<SubCategory>? = null
): Parcelable {
    override fun toString(): String = categoryName
}