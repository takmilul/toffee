package com.banglalink.toffee.model

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator

data class UserChannel(
    val id: String,
    val posterUrl: String,
    val logoUrl: String,
    var title: String,
    var description: String,
    val categoryList: List<String>,
    val selectedCategory: String,
    val subscriptionPriceList: List<String>,
    val selectedSubscriptionPrice: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() !!,
        parcel.readString() !!,
        parcel.readString() !!,
        parcel.readString() !!,
        parcel.readString() !!,
        parcel.createStringArrayList() !!,
        parcel.readString() !!,
        parcel.createStringArrayList() !!,
        parcel.readString() !!
    )
    
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(posterUrl)
        parcel.writeString(logoUrl)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeStringList(categoryList)
        parcel.writeString(selectedCategory)
        parcel.writeStringList(subscriptionPriceList)
        parcel.writeString(selectedSubscriptionPrice)
    }
    
    override fun describeContents(): Int {
        return 0
    }
    
    companion object CREATOR : Creator<UserChannel> {
        override fun createFromParcel(parcel: Parcel): UserChannel {
            return UserChannel(parcel)
        }
        
        override fun newArray(size: Int): Array<UserChannel?> {
            return arrayOfNulls(size)
        }
    }
}