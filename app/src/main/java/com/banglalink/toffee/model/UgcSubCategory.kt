package com.banglalink.toffee.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UgcSubCategory(
    @SerializedName(value="id", alternate = ["sub_category_Id"])
    val id: Long,

    @SerializedName("category_id")
    var categoryId: Long = 0L,

    @SerializedName("sub_category_name")
    val name: String,

    @SerializedName("updated_by")
    val updatedBy: Int = 0,

    @SerializedName("is_active")
    val isActive: Int = 0,
): Parcelable {
    override fun toString() = name
}