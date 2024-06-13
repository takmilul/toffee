package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GrantTokenResponse (
	@SerialName("statusMessage")
	val statusMessage : String? = null,
	@SerialName("id_token")
	val idToken : String? = null,
	@SerialName("token_type")
	val tokenType : String? = null,
	@SerialName("expires_in")
	val expiresIn : Int? = null,
	@SerialName("refresh_token")
	val refreshToken : String? = null
): ExternalBaseResponse()