package com.banglalink.toffee.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SubCategory(
    val id: Int,
    @SerializedName("sub_category_name")
    val subCategoryName: String
) : Parcelable {
    override fun toString(): String = subCategoryName
}