package com.banglalink.toffee.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Category(val name : String,
                    val icon : Int,
                    val bgColor: String,
                    val numFollowers: Long = 0,
                    val genres: List<String> = listOf()
): Parcelable