package com.banglalink.toffee.util

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object EncryptionUtil {
    private const val KEY = "1234567891234567"
    private val secretKeySpec = SecretKeySpec(KEY.toByteArray(), "AES")

    fun encryptRequest(jsonRequest: String): String {
        val cipher = encrypt(jsonRequest.toByteArray())
        return Base64.encodeToString(cipher, Base64.DEFAULT)
    }

    fun decryptResponse(response: String): String {
        return String(decrypt(Base64.decode(response, Base64.DEFAULT)))
    }

    private fun decrypt(data: ByteArray): ByteArray {
        val cipherInstance = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipherInstance.init(Cipher.DECRYPT_MODE, secretKeySpec)
        return cipherInstance.doFinal(data)
    }

    private fun encrypt(data: ByteArray): ByteArray {
        val cipherInstance = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipherInstance.init(Cipher.ENCRYPT_MODE, secretKeySpec)
        return cipherInstance.doFinal(data)
    }
}