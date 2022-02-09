package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class Profile (
    @SerializedName("subscriber_name")
    val name:String?,
    @SerializedName("address1")
    val address:String?,
    @SerializedName("contact")
    val contact:String?,
    @SerializedName("user_photo")
    val photoUrl:String?,
    @SerializedName("email")
    val email:String?
){

    fun toProfileForm(): EditProfileForm {
        return EditProfileForm().apply {
            fullName = name?:""
            email = this@Profile.email?:""
            address = this@Profile.address?:""
            phoneNo = this@Profile.contact?:""
            photoUrl=this@Profile.photoUrl?:""

        }
    }
}