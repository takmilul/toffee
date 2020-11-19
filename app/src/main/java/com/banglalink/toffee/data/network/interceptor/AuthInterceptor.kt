package com.banglalink.toffee.data.network.interceptor

import android.util.Log
import com.banglalink.toffee.model.CLIENT_API_HEADER
import com.banglalink.toffee.model.TOFFEE_HEADER
import com.banglalink.toffee.util.EncryptionUtil
import okhttp3.*
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.io.IOException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException

class AuthInterceptor (private val iGetMethodTracker: IGetMethodTracker): Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        Log.i("Path",request.url.encodedPath)
        val convertToGet = iGetMethodTracker.shouldConvertToGetRequest(request.url.encodedPath)
        val builder = FormBody.Builder()
        val string = EncryptionUtil.encryptRequest(bodyToString(request.body))
        if(!convertToGet){
            builder.addEncoded("data", string)
        }

        Log.i("Header",TOFFEE_HEADER)
        val newRequest = request.newBuilder()
            .headers(request.headers)
            .addHeader("User-Agent", TOFFEE_HEADER)
            .method(if(convertToGet) "GET" else "POST", if(convertToGet) null else builder.build())
        if(convertToGet){
            newRequest.addHeader(CLIENT_API_HEADER,string)
        }
        val response = chain.proceed(newRequest.build())
        if(!response.isSuccessful){
            if(response.code == 403){
                val msg = "Attention! Toffee is available only within Bangladesh territory. Please use a Bangladesh IP to access.";
                return response.newBuilder()
                    .code(response.code)
                    .message(msg)
                    .build()
            }
            return response
        }
        if(response.cacheResponse!=null){
            Log.i("Network","FROM CACHE")
        }
        if(response.networkResponse!=null){
            Log.i("Network","FROM NETWORK")
        }
        try {
            val jsonString =  EncryptionUtil.decryptResponse(response.body!!.string())
            val contentType = response.body!!.contentType()
            val body = jsonString.toResponseBody(contentType)
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
        } catch (e:IllegalArgumentException){
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