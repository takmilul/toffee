package com.banglalink.toffee.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class NavCategory(
    @SerialName("id")
    val id:Int,
    @SerialName("category_name")
    val categoryName:String,
    @SerialName("sub_categorie")
    val subCategoryList: List<NavSubcategory>?,
    @SerialName("bgColor")
    val bgColor: String,
    @SerialName("icon")
    val icon: Int
): Parcelable