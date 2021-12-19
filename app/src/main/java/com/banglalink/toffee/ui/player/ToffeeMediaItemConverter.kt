package com.banglalink.toffee.ui.player

import android.net.Uri
import android.util.Log
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaItem.DrmConfiguration
import com.google.android.exoplayer2.ext.cast.MediaItemConverter
import com.google.android.exoplayer2.util.Assertions
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.common.images.WebImage
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import java.net.MalformedURLException
import java.net.URL
import java.util.*

class ToffeeMediaItemConverter(private val mPref: SessionPreference,
                               private val isOverWifi: Boolean
): MediaItemConverter {
    companion object {
        private const val KEY_MEDIA_ITEM = "mediaItem"
        const val KEY_CHANNEL_INFO = "channel_info"
        private const val KEY_PLAYER_CONFIG = "exoPlayerConfig"
        private const val KEY_URI = "uri"
        private const val KEY_TITLE = "title"
        private const val KEY_MIME_TYPE = "mimeType"
        private const val KEY_DRM_CONFIGURATION = "drmConfiguration"
        private const val KEY_UUID = "uuid"
        private const val KEY_LICENSE_URI = "licenseUri"
        private const val KEY_REQUEST_HEADERS = "requestHeaders"
    }


    override fun toMediaQueueItem(mediaItem: MediaItem): MediaQueueItem {
        val tag = mediaItem.localConfiguration?.tag as ChannelInfo?
        Log.i("MEDIA_T", "Tag -> $tag")
        return getMediaInfo(mediaItem, tag!!)
    }

    override fun toMediaItem(mediaQueueItem: MediaQueueItem): MediaItem {

        // `item` came from `toMediaQueueItem()` so the custom JSON data must be set.
        val mediaInfo: MediaInfo = mediaQueueItem.media!!
        val customData = mediaQueueItem.customData
        Assertions.checkNotNull(mediaInfo)
        return getMediaItem(Assertions.checkNotNull(mediaInfo.customData), customData!!)!!
//
//
//        Log.e("MEDIA_T", "toMediaItem")
//        val customData = mediaQueueItem.customData!!
//
//        return MediaItem.Builder().setUri(mediaQueueItem.media.contentUrl).apply {
//            setMediaMetadata(
//            com.google.android.exoplayer2.MediaMetadata
//                .Builder()
//                .setTitle(mediaQueueItem.media.metadata.getString(MediaMetadata.KEY_TITLE))
//                .build()
//            )
//
//            if(customData.has("drm_info")) {
////                val drmInfo = customData.getJSONObject("drm_info")
//                setDrmUuid(C.WIDEVINE_UUID)
//                setDrmLicenseUri(mPref.drmWidevineLicenseUrl)
//                setDrmMultiSession(false)
//            }
//
//            setMimeType(mediaQueueItem.media.contentType)
//            setTag(jsonToChannelInfo(mediaQueueItem.customData!!))
//        }.build()
    }

    private fun getMediaInfo(mediaItem: MediaItem, info: ChannelInfo): MediaQueueItem {
        val mediaMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE )
        mediaMetadata.putString( MediaMetadata.KEY_TITLE , info.program_name ?: "")
        if(info.isLive) {
            mediaMetadata.addImage(WebImage(Uri.parse(info.channel_logo)))
        }
        else {
            mediaMetadata.addImage(WebImage(Uri.parse(info.landscape_ratio_1280_720)))
        }

        val channelUrl = mediaItem.localConfiguration!!.uri.toString().let {
            if(mediaItem.localConfiguration!!.drmConfiguration == null) {
                getCastUrl(it)
            } else it
        }

        Log.i("MEDIA_T", "Channel Url -> $channelUrl")

        val customData = getCustomData(mediaItem, info).apply {
            Log.i("MEDIA_T", "Custom data -> $this")
        }

        val mediaInfo = MediaInfo.Builder(channelUrl)
                .setContentType(mediaItem.localConfiguration!!.mimeType!!)//"application/x-mpegurl")
                .setStreamType( MediaInfo.STREAM_TYPE_BUFFERED )
                .setMetadata( mediaMetadata )
                .setCustomData(customData)
                .build()

        return MediaQueueItem.Builder(mediaInfo)
            .setCustomData(channelInfoToJson(info))
            .build()
    }

    private fun channelInfoToJson(info: ChannelInfo): JSONObject {
        return JSONObject().apply {
            put("channel_info", Gson().toJson(info))
        }
    }

    private fun jsonToChannelInfo(json: JSONObject): ChannelInfo {
        return Gson().fromJson(json.getString("channel_info"), ChannelInfo::class.java)
    }

    private fun getCastUrl(uri: String): String {
        var newUrl = uri
        if (mPref.isCastUrlOverride && mPref.castOverrideUrl.isNotBlank()) {
            try {
                val url = URL(uri)
                var path = url.path
                if (!url.query.isNullOrEmpty()) {
                    path = path + "?" + url.query
                }
                newUrl = mPref.castOverrideUrl + path
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            }
        }
        return newUrl
    }

    // Deserialization.
    private fun getMediaItem(customData: JSONObject, channelData: JSONObject): MediaItem? {
        return try {
            val mediaItemJson = customData.getJSONObject(KEY_MEDIA_ITEM)
            val builder = MediaItem.Builder()
            builder.setUri(Uri.parse(mediaItemJson.getString(KEY_URI)))
            if (mediaItemJson.has(KEY_TITLE)) {
                val mediaMetadata = com.google.android.exoplayer2.MediaMetadata.Builder()
                    .setTitle(mediaItemJson.getString(KEY_TITLE))
                    .build()
                builder.setMediaMetadata(mediaMetadata)
            }
            if (mediaItemJson.has(KEY_MIME_TYPE)) {
                builder.setMimeType(mediaItemJson.getString(KEY_MIME_TYPE))
            }
            if (mediaItemJson.has(KEY_DRM_CONFIGURATION)) {
                populateDrmConfiguration(
                    mediaItemJson.getJSONObject(KEY_DRM_CONFIGURATION),
                    builder
                )
            }
            builder.setTag(jsonToChannelInfo(channelData))
            builder.build()
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    @Throws(JSONException::class)
    private fun populateDrmConfiguration(json: JSONObject, builder: MediaItem.Builder) {
        val requestHeadersJson = json.getJSONObject(KEY_REQUEST_HEADERS)
        val requestHeaders = HashMap<String, String>()
        val iterator = requestHeadersJson.keys()
        while (iterator.hasNext()) {
            val key = iterator.next()
            requestHeaders[key] = requestHeadersJson.getString(key)
        }
        builder.setDrmConfiguration(DrmConfiguration
            .Builder(UUID.fromString(json.getString(KEY_UUID)))
            .setLicenseUri(json.getString(KEY_LICENSE_URI))
            .setLicenseRequestHeaders(requestHeaders)
            .build()
        )
    }

    // Serialization.
    private fun getCustomData(mediaItem: MediaItem?, channelInfo: ChannelInfo): JSONObject? {
        val json = JSONObject()
        try {
            json.put(KEY_MEDIA_ITEM, getMediaItemJson(mediaItem!!))
            val playerConfigJson = getPlayerConfigJson(mediaItem)
            if (playerConfigJson != null) {
                json.put(KEY_PLAYER_CONFIG, playerConfigJson)
            }
            json.put(KEY_CHANNEL_INFO, Gson().toJson(channelInfo))
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
        return json
    }

    @Throws(JSONException::class)
    fun getMediaItemJson(mediaItem: MediaItem): JSONObject {
        Assertions.checkNotNull(mediaItem.localConfiguration)
        val json = JSONObject()
        json.put(KEY_TITLE, mediaItem.mediaMetadata.title)
        json.put(KEY_URI, mediaItem.localConfiguration!!.uri.toString().let {
            if(mediaItem.localConfiguration!!.drmConfiguration == null) {
                getCastUrl(it)
            } else it
        })
        json.put(KEY_MIME_TYPE, mediaItem.localConfiguration!!.mimeType)
        if (mediaItem.localConfiguration!!.drmConfiguration != null) {
            json.put(
                KEY_DRM_CONFIGURATION,
                getDrmConfigurationJson(mediaItem.localConfiguration!!.drmConfiguration!!)
            )
        }
        return json
    }

    @Throws(JSONException::class)
    fun getDrmConfigurationJson(drmConfiguration: DrmConfiguration): JSONObject? {
        val json = JSONObject()
        json.put(KEY_UUID, drmConfiguration.scheme)
        json.put(KEY_LICENSE_URI, drmConfiguration.licenseUri)
        json.put(
            KEY_REQUEST_HEADERS,
            JSONObject(drmConfiguration.licenseRequestHeaders as Map<*, *>)
        )
        return json
    }

    @Throws(JSONException::class)
    private fun getPlayerConfigJson(mediaItem: MediaItem): JSONObject? {
        if (mediaItem.localConfiguration == null
            || mediaItem.localConfiguration!!.drmConfiguration == null
        ) {
            return null
        }
        val drmConfiguration = mediaItem.localConfiguration!!.drmConfiguration
        val drmScheme: String = when {
            C.WIDEVINE_UUID == drmConfiguration!!.scheme -> {
                "widevine"
            }
            C.PLAYREADY_UUID == drmConfiguration.scheme -> {
                "playready"
            }
            else -> {
                return null
            }
        }
        val exoPlayerConfigJson = JSONObject()
        exoPlayerConfigJson.put("withCredentials", false)
        exoPlayerConfigJson.put("protectionSystem", drmScheme)
        if (drmConfiguration.licenseUri != null) {
            exoPlayerConfigJson.put("licenseUrl", drmConfiguration.licenseUri)
        }
        if (drmConfiguration.licenseRequestHeaders.isNotEmpty()) {
            exoPlayerConfigJson.put("headers", JSONObject(drmConfiguration.licenseRequestHeaders as Map<*, *>))
        }
        return exoPlayerConfigJson
    }
}