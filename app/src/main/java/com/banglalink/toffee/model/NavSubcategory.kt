package com.banglalink.toffee.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NavSubcategory(
    @SerializedName("id")
    val id: Int,
    @SerializedName("category_id")
    val categoryId: String,
    @SerializedName("sub_category_name")
    val subcategoryName: String
): Parcelable