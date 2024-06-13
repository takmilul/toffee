package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    @SerialName("subscriber_name")
    val name: String? = null,
    @SerialName("address1")
    val address: String? = null,
    @SerialName("contact")
    val contact: String? = null,
    @SerialName("user_photo")
    val photoUrl: String? = null,
    @SerialName("email")
    val email: String? = null,
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