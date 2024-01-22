package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    @SerialName("subscriber_name")
    val name: String?,
    @SerialName("address1")
    val address: String?,
    @SerialName("contact")
    val contact: String?,
    @SerialName("user_photo")
    val photoUrl: String?,
    @SerialName("email")
    val email: String?,
) {
    fun toProfileForm(): EditProfileForm {
        return EditProfileForm().apply {
            fullName = name ?: ""
            email = this@Profile.email ?: ""
            address = this@Profile.address ?: ""
            phoneNo = this@Profile.contact ?: ""
            photoUrl = this@Profile.photoUrl ?: ""
        }
    }
}