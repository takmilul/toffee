package com.banglalink.toffee.ui.player

import android.net.Uri
import android.util.Base64
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DataSourceInputStream
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.drm.ExoMediaDrm
import androidx.media3.exoplayer.drm.HttpMediaDrmCallback
import androidx.media3.exoplayer.drm.MediaDrmCallback
import androidx.media3.exoplayer.drm.MediaDrmCallbackException
import com.banglalink.toffee.apiservice.DrmLicenseService
import com.banglalink.toffee.util.Log
import com.google.common.collect.ImmutableMap
import kotlinx.coroutines.runBlocking
import java.util.UUID

private const val MAX_MANUAL_REDIRECTS = 5
@UnstableApi
class CustomMediaDrmCallback(
    private val licenseUri: String,
    private val dataSourceFactory: OkHttpDataSource.Factory,
    private val drmLicenseService: DrmLicenseService,
    private val contentId: String
) : MediaDrmCallback {
    
    private val httpMediaDrmCallback = HttpMediaDrmCallback(licenseUri, true, dataSourceFactory)
    
    override fun executeProvisionRequest(uuid: UUID, request: ExoMediaDrm.ProvisionRequest): ByteArray {
        Log.i("DRM_T", "Provision request from media drm callback")
        val url = request.defaultUrl + "&signedRequest=" + Util.fromUtf8Bytes(request.data)
        return executePost(dataSourceFactory, url, null, null)
        
//        try {
//            return httpMediaDrmCallback.executeProvisionRequest(
//                uuid,
//                request
//            ).apply {
//                ToffeeAnalytics.logException(Exception("Provision request success -> ${request.defaultUrl}"))
//            }
//        } catch (ex: Exception) {
//            throw MediaDrmCallbackException(
//                DataSpec.Builder().setUri(Uri.EMPTY).build(),
//                Uri.EMPTY,  /* responseHeaders= */
//                ImmutableMap.of(),  /* bytesLoaded= */
//                0,  /* cause= */
//                ex
//            )
//        }
    }
    
    override fun executeKeyRequest(uuid: UUID, request: ExoMediaDrm.KeyRequest): ByteArray {
        Log.i("DRM_T", "executeKeyRequest: ${request.data}")
        val license = runBlocking {
            try {
                drmLicenseService.execute(
                    licenseServerUrl = licenseUri,
                    payload = Base64.encodeToString(request.data, Base64.NO_WRAP)
                )
            } catch (ex: Exception) {
                null
            }
        } ?: throw MediaDrmCallbackException(
            DataSpec.Builder().setUri(Uri.EMPTY).build(),
            Uri.EMPTY,  /* responseHeaders= */
            ImmutableMap.of(),  /* bytesLoaded= */
            0,  /* cause= */
            ToffeeMediaDrmException("Drm token request failed")
        )
        
        return license
//        return httpMediaDrmCallback.executeKeyRequest(uuid, request)
    }
    
    private fun executePost(
        dataSourceFactory: HttpDataSource.Factory,
        url: String,
        httpBody: ByteArray?,
        requestProperties: Map<String, String>?
    ): ByteArray {
        var mUrl = url
        val dataSource = dataSourceFactory.createDataSource()
        if (requestProperties !=
            /* key= */ null
        ) {
            for ((key, value) in requestProperties) {
                dataSource.setRequestProperty(key, value)
            }
        }
        
        var manualRedirectCount = 0
        while (true) {
            val dataSpec = DataSpec(
                Uri.parse(mUrl),
                DataSpec.HTTP_METHOD_POST,
                httpBody,
                /* absoluteStreamPosition= */ 0,
                /* position= */ 0,
                /* length= */ C.LENGTH_UNSET.toLong(), null,
                DataSpec.FLAG_ALLOW_GZIP
            )
            val inputStream = DataSourceInputStream(dataSource, dataSpec)
            try {
                return Util.toByteArray(inputStream)
            } catch (e: HttpDataSource.InvalidResponseCodeException) {
                // For POST requests, the underlying network stack will not normally follow 307 or 308
                // redirects automatically. Do so manually here.
                val manuallyRedirect = manualRedirectCount++ < MAX_MANUAL_REDIRECTS
                val redirectUrl = (if (manuallyRedirect) getRedirectUrl(e) else null) ?: throw e
                mUrl = redirectUrl
            } finally {
                Util.closeQuietly(inputStream)
            }
        }
    }
    
    private fun getRedirectUrl(exception: HttpDataSource.InvalidResponseCodeException): String? {
        val headerFields = exception.headerFields
        if (headerFields != null) {
            val locationHeaders = headerFields["Location"]
            if (locationHeaders != null && locationHeaders.isNotEmpty()) {
                return locationHeaders[0]
            }
        }
        return null
    }
}