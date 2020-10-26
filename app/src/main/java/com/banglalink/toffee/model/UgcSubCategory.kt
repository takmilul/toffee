package com.banglalink.toffee.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UgcSubCategory(
    val id: Long,

    @SerializedName("category_id")
    val categoryId: Long,

    @SerializedName("sub_category_name")
    val name: String,

    @SerializedName("updated_by")
    val updatedBy: Int,

    @SerializedName("is_active")
    val isActive: Int,
): Parcelable