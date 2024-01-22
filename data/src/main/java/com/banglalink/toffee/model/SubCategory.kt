package com.banglalink.toffee.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class SubCategory(
    @SerialName(value="id")
    val id: Long,

    @SerialName("category_id")
    var categoryId: Long = 0L,

    @SerialName("sub_category_name")
    val name: String,

    @SerialName("updated_by")
    val updatedBy: Int = 0,

    @SerialName("is_active")
    val isActive: Int = 0,
): Parcelable {
    override fun toString() = name
}