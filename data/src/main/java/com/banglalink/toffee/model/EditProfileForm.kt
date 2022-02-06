package com.banglalink.toffee.model

import android.os.Parcelable
import com.google.android.gms.common.annotation.KeepName
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@KeepName
@Parcelize
data class EditProfileForm(
    @SerializedName("fullName")
    var fullName:String = "",
    @SerializedName("email")
    var email:String = "",
    @SerializedName("phoneNo")
    var phoneNo:String = "",
    @SerializedName("address")
    var address:String = "",
    @SerializedName("photoUrl")
    var photoUrl:String = ""
) : Parcelable