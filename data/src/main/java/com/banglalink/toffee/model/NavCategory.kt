package com.banglalink.toffee.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class NavCategory(
    @SerialName("id")
    val id:Int = 0,
    @SerialName("category_name")
    val categoryName:String,
    @SerialName("sub_categorie")
    val subCategoryList: List<NavSubcategory>? = null,
    @SerialName("bgColor")
    val bgColor: String? = null,
    @SerialName("icon")
    val icon: Int = 0
): Parcelable