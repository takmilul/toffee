package com.banglalink.toffee.ui.player

import android.net.Uri
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.drm.ExoMediaDrm
import androidx.media3.exoplayer.drm.HttpMediaDrmCallback
import androidx.media3.exoplayer.drm.MediaDrmCallback
import androidx.media3.exoplayer.drm.MediaDrmCallbackException
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.apiservice.DrmTokenService
import com.banglalink.toffee.util.Log
import com.google.common.collect.ImmutableMap
import java.util.*

class ToffeeMediaDrmException(msg: String) : Exception(msg)

@UnstableApi
class ToffeeMediaDrmCallback(
    licenseUri: String,
    dataSourceFactory: OkHttpDataSource.Factory,
    private val drmTokenApi: DrmTokenService,
    private val contentId: String
) : MediaDrmCallback {
    
    private val httpMediaDrmCallback = HttpMediaDrmCallback(licenseUri, true, dataSourceFactory)
    
    override fun executeProvisionRequest(uuid: UUID, request: ExoMediaDrm.ProvisionRequest): ByteArray {
        Log.i("DRM_T", "Provision request from media drm callback")
        try {
            return httpMediaDrmCallback.executeProvisionRequest(
                uuid,
                request
            ).apply {
                ToffeeAnalytics.logException(Exception("Provision request success -> ${request.defaultUrl}"))
            }
        } catch (ex: Exception) {
            throw MediaDrmCallbackException(
                DataSpec.Builder().setUri(Uri.EMPTY).build(),
                Uri.EMPTY,  /* responseHeaders= */
                ImmutableMap.of(),  /* bytesLoaded= */
                0,  /* cause= */
                ex
            )
        }
    }
    
    override fun executeKeyRequest(uuid: UUID, request: ExoMediaDrm.KeyRequest): ByteArray {
//        val token = runBlocking {
//            try {
//                drmTokenApi.execute(contentId)
//            } catch (ex: Exception) {
//                null
//            }
//        } ?: throw MediaDrmCallbackException(
//            DataSpec.Builder().setUri(Uri.EMPTY).build(),
//            Uri.EMPTY,  /* responseHeaders= */
//            ImmutableMap.of(),  /* bytesLoaded= */
//            0,  /* cause= */
//            ToffeeMediaDrmException("Drm token request failed")
//        )
//        
//        httpMediaDrmCallback.setKeyRequestProperty("pallycon-customdata-v2", token)
        return httpMediaDrmCallback.executeKeyRequest(uuid, request)
    }
}