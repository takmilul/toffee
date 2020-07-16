package com.banglalink.toffee.util

import android.util.Base64
import com.banglalink.toffee.data.storage.Preference
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object EncryptionUtil {
    private val TRANSFORMATION = "AES/ECB/PKCS5Padding"
    private val secretKeySpec = SecretKeySpec(Preference.getInstance().key.toByteArray(), "AES")

    fun encryptRequest(jsonRequest: String): String {
        val cipher = encrypt(jsonRequest.toByteArray())
        return Base64.encodeToString(cipher, Base64.DEFAULT)
    }

    fun decryptResponse(response: String): String {
        return String(decrypt(Base64.decode(response, Base64.DEFAULT)))
    }

    private fun decrypt(data: ByteArray): ByteArray {
        val cipherInstance = Cipher.getInstance(TRANSFORMATION)
        cipherInstance.init(Cipher.DECRYPT_MODE, secretKeySpec)
        return cipherInstance.doFinal(data)
    }

    private fun encrypt(data: ByteArray): ByteArray {
        val cipherInstance = Cipher.getInstance(TRANSFORMATION)
        cipherInstance.init(Cipher.ENCRYPT_MODE, secretKeySpec)
        return cipherInstance.doFinal(data)
    }
}