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
import okio.IOException
import java.lang.IllegalStateException
import java.util.*

class ToffeeMediaDrmException(msg: String): Exception(msg)

class ToffeeMediaDrmCallback(/*private val licenseUri: String,
                             dataSourceFactory: OkHttpDataSource.Factory,
                             private val drmTokenApi: DrmTokenService,
                             private val contentId: String*/): MediaDrmCallback {

//    private val httpMediaDrmCallback = HttpMediaDrmCallback(licenseUri, false, dataSourceFactory)

    override fun executeProvisionRequest(
        uuid: UUID,
        request: ExoMediaDrm.ProvisionRequest
    ): ByteArray {
        Log.i("DRM_T", "Provision request from media drm callback")
        throw MediaDrmCallbackException(
            DataSpec.Builder().setUri(Uri.EMPTY).build(),
            Uri.EMPTY,  /* responseHeaders= */
            ImmutableMap.of(),  /* bytesLoaded= */
            0,  /* cause= */
            IOException("Drm provision request ignored")
        )
//        return httpMediaDrmCallback.executeProvisionRequest(uuid, request)//ExoMediaDrm.ProvisionRequest(request.data, licenseUri))
    }

    override fun executeKeyRequest(uuid: UUID, request: ExoMediaDrm.KeyRequest): ByteArray {
//        val token = runBlocking {
//            drmTokenApi.execute(contentId, 120)
//        } ?:
        Log.i("DRM_T", "Key request from media drm callback")
        throw MediaDrmCallbackException(
                DataSpec.Builder().setUri(Uri.EMPTY).build(),
                Uri.EMPTY,  /* responseHeaders= */
                ImmutableMap.of(),  /* bytesLoaded= */
                0,  /* cause= */
                ToffeeMediaDrmException("Drm token request ignored")
            )
//        Log.e("DRM_T", "Requesting auth + key from media drm callback")
//
//        httpMediaDrmCallback.setKeyRequestProperty("pallycon-customdata-v2", token)
//        return httpMediaDrmCallback.executeKeyRequest(uuid, request)
    }
}