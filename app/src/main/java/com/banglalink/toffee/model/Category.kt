package com.banglalink.toffee.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Category(
    val id: Long,

    @SerializedName("category_name")
    val categoryName: String,

    @SerializedName("thumbnail_url")
    val thumbnailUrl: String?,

    @SerializedName("updated_by")
    val updatedBy: Int,

    @SerializedName("is_active")
    val isActive: Int,

    @SerializedName("color_code")
    val colorCode: String?,

    @SerializedName("category_icon")
    val categoryIcon: String?,

    val subcategories: List<SubCategory>
): Parcelable {
    override fun toString(): String = categoryName
}