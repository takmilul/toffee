package com.banglalink.toffee.data.network.interceptor

import com.banglalink.toffee.Constants.CLIENT_API_HEADER
import com.banglalink.toffee.data.exception.AuthEncodeDecodeException
import com.banglalink.toffee.data.exception.AuthInterceptorException
import com.banglalink.toffee.di.ToffeeHeader
import com.banglalink.toffee.util.EncryptionUtil
import com.banglalink.toffee.util.Log
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.io.IOException
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val iGetMethodTracker: IGetMethodTracker,
    @ToffeeHeader private val headerProvider: Provider<String>,
): Interceptor {
    
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

//        Log.i("Header",toffeeHeader)
        val newRequest = request.newBuilder()
            .headers(request.headers)
            .addHeader("User-Agent", headerProvider.get())
            .method(if(convertToGet) "GET" else "POST", if(convertToGet) null else builder.build())
        if(convertToGet){
            newRequest.addHeader(CLIENT_API_HEADER,string)
        }

        val response = try {
            chain.proceed(newRequest.build())
        } catch (ex: IOException) {
            throw ex
        } catch (ex: Exception) {
            throw AuthInterceptorException(ex.message, ex.cause)
        }
        if(!response.isSuccessful){
            if(response.code == 403){
                val msg = "Attention! Toffee is available only within Bangladesh territory. Please use a Bangladesh IP to access.";
                return response.newBuilder()
                    .code(response.code)
                    .message(msg)
                    .build()
            }
            return response.newBuilder().removeHeader("Pragma").build()
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

        } catch (e: Exception) {
            throw AuthEncodeDecodeException(e.message, e.cause)
        }
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