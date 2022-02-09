package com.banglalink.toffee.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class NavCategory(
    @SerializedName("id")
    val id:Int,
    @SerializedName("category_name")
    val categoryName:String,
    @SerializedName("sub_categorie")
    val subCategoryList: List<NavSubcategory>?,
    @SerializedName("bgColor")
    val bgColor: String,
    @SerializedName("icon")
    val icon: Int
): Parcelable