package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

data class GrantTokenResponse (
	@SerializedName("statusMessage")
	val statusMessage : String? = null,
	@SerializedName("id_token")
	val idToken : String? = null,
	@SerializedName("token_type")
	val tokenType : String? = null,
	@SerializedName("expires_in")
	val expiresIn : Int? = null,
	@SerializedName("refresh_token")
	val refreshToken : String? = null
): ExternalBaseResponse()