package com.banglalink.toffee.data.network.util

import android.util.Base64
import android.util.Log
import com.banglalink.toffee.exception.ApiException
import com.banglalink.toffee.data.network.response.BaseResponse
import retrofit2.Response
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

suspend fun <T : BaseResponse> tryIO(block: suspend () -> Response<T>): T {
    val response: Response<T> = block()
    if (response.isSuccessful) {
        val baseResponse = response.body()
        if (baseResponse?.errorCode != 0) {
           throw ApiException(
               baseResponse!!.errorCode,
               baseResponse.errorMsg!!
           )
        }
        return response.body()!!
    }
    throw ApiException(response.code(),response.message())

}


private const val KEY = "1234567891234567"
private val secretKeySpec by lazy {
    SecretKeySpec(KEY.toByteArray(), "AES")
}

private val cipherInstance by lazy {
    Cipher.getInstance("AES/ECB/PKCS5Padding")
}

fun encryptRequest(jsonRequest: String): String {
    val cipher = encrypt(jsonRequest.toByteArray())
    return Base64.encodeToString(cipher, Base64.DEFAULT)
}

fun decryptResponse(response: String): String {
    return String(decrypt(Base64.decode(response, Base64.DEFAULT)))
}

private fun decrypt(data: ByteArray): ByteArray {
    cipherInstance.init(Cipher.DECRYPT_MODE, secretKeySpec)
    return cipherInstance.doFinal(data)
}

private fun encrypt(data: ByteArray): ByteArray {
    cipherInstance.init(Cipher.ENCRYPT_MODE, secretKeySpec)
    return cipherInstance.doFinal(data)
}