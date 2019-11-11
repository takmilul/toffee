package com.banglalink.toffee.data.network.interceptor

import com.banglalink.toffee.data.network.util.decryptResponse
import com.banglalink.toffee.data.network.util.encryptRequest
import okhttp3.*
import okio.Buffer
import java.io.IOException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException

class AuthInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val string = bodyToString(request.body)
        val builder = FormBody.Builder()
        builder.addEncoded("data", encryptRequest(string))

        val newRequest = request.newBuilder()
            .headers(request.headers)
            .method(request.method, builder.build())
            .build()
        val response = chain.proceed(newRequest)
        try {
            val jsonString =  decryptResponse(response.body!!.string())
            val contentType = response.body!!.contentType()
            val body = ResponseBody.create(contentType, jsonString)
            return response.newBuilder().body(body).build()

        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        }

        return response
    }

    private fun bodyToString(request: RequestBody?): String {
        val buffer = Buffer()
        try {
            if (request != null)
                request.writeTo(buffer)
            else
                return ""
            return buffer.readUtf8()
        } catch (e: IOException) {
            return ""
        } finally {
            buffer.close()
        }

    }
}