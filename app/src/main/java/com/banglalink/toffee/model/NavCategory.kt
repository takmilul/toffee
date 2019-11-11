package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class NavCategory(
    @SerializedName("id")
    val id:Int,
    @SerializedName("category_name")
    val categoryName:String,
    @SerializedName("sub_categorie")
    val subCategoryList: List<NavSubcategory>
)