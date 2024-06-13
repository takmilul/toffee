package com.banglalink.toffee.model

import android.os.Parcelable
import com.google.android.gms.common.annotation.KeepName
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@KeepName
@Parcelize
@Serializable
data class EditProfileForm(
    @SerialName("fullName")
    var fullName:String = "",
    @SerialName("email")
    var email:String = "",
    @SerialName("phoneNo")
    var phoneNo:String = "",
    @SerialName("address")
    var address:String = "",
    @SerialName("photoUrl")
    var photoUrl:String = ""
) : Parcelable