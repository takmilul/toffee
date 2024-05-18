package com.banglalink.toffee.ui.player

import android.net.Uri
import android.util.Base64
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.okhttp.OkHttpDataSource.Factory
import androidx.media3.exoplayer.drm.ExoMediaDrm
import androidx.media3.exoplayer.drm.HttpMediaDrmCallback
import androidx.media3.exoplayer.drm.MediaDrmCallback
import androidx.media3.exoplayer.drm.MediaDrmCallbackException
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.apiservice.DrmLicenseService
import com.banglalink.toffee.util.Log
import com.google.common.collect.ImmutableMap
import kotlinx.coroutines.runBlocking
import java.util.*

@UnstableApi
class CustomMediaDrmCallback(
    private val licenseUri: String,
    dataSourceFactory: Factory,
    private val drmLicenseService: DrmLicenseService,
    private val contentId: String? = "1",
    private val packageId: String? = "1"
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
        Log.i("DRM_T", "executeKeyRequest: ${request.data}")
        val license = runBlocking {
            try {
                drmLicenseService.execute(
                    licenseServerUrl = licenseUri,
                    payload = Base64.encodeToString(request.data, Base64.NO_WRAP),
                    contentId = contentId ?: "1",
                    packageId = packageId ?: "1"
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
    }
}