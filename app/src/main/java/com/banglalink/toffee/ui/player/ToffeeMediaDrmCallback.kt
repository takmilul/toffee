package com.banglalink.toffee.ui.player

import android.net.Uri
import android.util.Log
import com.banglalink.toffee.apiservice.DrmTokenService
import com.google.android.exoplayer2.drm.ExoMediaDrm
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback
import com.google.android.exoplayer2.drm.MediaDrmCallback
import com.google.android.exoplayer2.drm.MediaDrmCallbackException
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.common.collect.ImmutableMap
import kotlinx.coroutines.runBlocking
import java.lang.IllegalStateException
import java.util.*

class ToffeeMediaDrmCallback(licenseUri: String,
                             dataSourceFactory: OkHttpDataSource.Factory,
                             private val drmTokenApi: DrmTokenService,
                             private val contentId: String): MediaDrmCallback {

    private val httpMediaDrmCallback = HttpMediaDrmCallback(licenseUri, false, dataSourceFactory)

    override fun executeProvisionRequest(
        uuid: UUID,
        request: ExoMediaDrm.ProvisionRequest
    ): ByteArray {
        Log.e("DRM_T", "Provision request from media drm callback")
        return httpMediaDrmCallback.executeProvisionRequest(uuid, request)
    }

    override fun executeKeyRequest(uuid: UUID, request: ExoMediaDrm.KeyRequest): ByteArray {
        val token = runBlocking {
            drmTokenApi.execute(contentId)
        } ?: throw MediaDrmCallbackException(
                DataSpec.Builder().setUri(Uri.EMPTY).build(),
                Uri.EMPTY,  /* responseHeaders= */
                ImmutableMap.of(),  /* bytesLoaded= */
                0,  /* cause= */
                IllegalStateException("Drm token request failed")
            )
        Log.e("DRM_T", "Requesting auth + key from media drm callback")

        httpMediaDrmCallback.setKeyRequestProperty("pallycon-customdata-v2", token)
        return httpMediaDrmCallback.executeKeyRequest(uuid, request)
    }
}