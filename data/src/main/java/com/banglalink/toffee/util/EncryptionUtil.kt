package com.banglalink.toffee.util

import android.util.Base64
import com.banglalink.toffee.Constants.HE_KEY
import com.banglalink.toffee.Constants.TOFFEE_KEY
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object EncryptionUtil {
    private const val TRANSFORMATION = "AES/ECB/PKCS5Padding"
    private val secretKeySpec = SecretKeySpec(TOFFEE_KEY.toByteArray(), "AES")
    private val headerEnrichmentKeySpec = SecretKeySpec(HE_KEY.toByteArray(), "AES")

    fun encryptRequest(jsonRequest: String): String {
        val cipher = encrypt(jsonRequest.toByteArray())
        return Base64.encodeToString(cipher, Base64.NO_WRAP)
    }

    fun decryptResponse(response: String): String {
        return try {
            String(decrypt(Base64.decode(response, Base64.DEFAULT)))
        } catch (e: Exception) {
            String(decryptHeaderEnrichment(Base64.decode(response, Base64.DEFAULT)))
        }
    }

    private fun decryptHeaderEnrichment(data: ByteArray): ByteArray {
        val cipherInstance = Cipher.getInstance(TRANSFORMATION)
        cipherInstance.init(Cipher.DECRYPT_MODE, headerEnrichmentKeySpec)
        return cipherInstance.doFinal(data)
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