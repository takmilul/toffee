package com.banglalink.toffee.model

import com.google.android.gms.common.annotation.KeepName
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@KeepName
class EditProfileForm: Serializable {
    @SerializedName("fullName")
    var fullName:String=""
    @SerializedName("email")
    var email:String=""
    @SerializedName("phoneNo")
    var phoneNo:String=""
    @SerializedName("address")
    var address:String=""
    @SerializedName("photoUrl")
    var photoUrl:String=""
}