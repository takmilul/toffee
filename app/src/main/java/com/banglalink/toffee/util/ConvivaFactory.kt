package com.banglalink.toffee.util

import android.content.Context
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.model.ChannelInfo
import com.conviva.sdk.ConvivaAdAnalytics
import com.conviva.sdk.ConvivaAnalytics
import com.conviva.sdk.ConvivaSdkConstants
import com.conviva.sdk.ConvivaVideoAnalytics
import com.google.ads.interactivemedia.v3.api.Ad
import com.google.android.exoplayer2.ExoPlayerLibraryInfo

class ConvivaFactory private constructor() {
    var convivaVideoAnalytics: ConvivaVideoAnalytics? = null
        private set
    var convivaAdAnalytics: ConvivaAdAnalytics? = null
        private set
    
    companion object {
        val instance = ConvivaFactory()
        private var isSessionActive = false
        
        fun init(context: Context?, isActive: Boolean) {
            if (isActive) {
                instance.convivaVideoAnalytics = ConvivaAnalytics.buildVideoAnalytics(context)
                instance.convivaAdAnalytics = ConvivaAnalytics.buildAdAnalytics(context, instance.convivaVideoAnalytics)
            }
        }
        
        fun setConvivaVideoMetadata(info: ChannelInfo, customerId: Int, seriesName: String? = null, seasonNumber: Int? = 0) {
            val contentInfo = mapOf(
                ConvivaSdkConstants.ASSET_NAME to "[${info.id}] ${info.program_name}",
                ConvivaSdkConstants.IS_LIVE to info.isLinear,
                ConvivaSdkConstants.PLAYER_NAME to "Android Exoplayer",
                ConvivaSdkConstants.VIEWER_ID to customerId.toString(),
                ConvivaSdkConstants.DURATION to info.durationInSeconds(),
                ConvivaConstants.APP_VERSION to BuildConfig.VERSION_NAME,
                ConvivaConstants.CONTENT_TYPE to (info.type ?: "N/A"),
                ConvivaConstants.CHANNEL to (info.content_provider_name ?: "N/A"),
                ConvivaConstants.BRAND to "N/A",
                ConvivaConstants.AFFILIATE to "N/A",
                ConvivaConstants.CATEGORY_TYPE to (info.category ?: "N/A"),
                ConvivaConstants.NAME to "CMS",
                ConvivaConstants.ID to info.id,
                ConvivaConstants.SERIES_NAME to (seriesName ?: "N/A"),
                ConvivaConstants.SEASON_NUMBER to (seasonNumber?.toString() ?: "N/A"),
                ConvivaConstants.SHOW_TITLE to "N/A",
                ConvivaConstants.EPISODE_NUMBER to (if(info.episodeNo==0) "N/A" else info.episodeNo.toString()),
                ConvivaConstants.GENRE to "N/A",
                ConvivaConstants.GENRE_LIST to "N/A",
                ConvivaConstants.UTM_TRACKING_URL to "N/A"
            )
            instance.convivaVideoAnalytics?.reportPlaybackRequested(contentInfo)
            isSessionActive = true
        }
        
        fun getConvivaAdMetadata(ad: Ad?): Map<String, Any> {
            return mapOf(
                    ConvivaSdkConstants.ASSET_NAME to "[${ad?.adId ?: "N/A"}] ${ad?.title ?: "N/A"}",
                    ConvivaSdkConstants.IS_LIVE to (ad?.isLinear?.toString() ?: "false"),
                    ConvivaSdkConstants.DEFAULT_RESOURCE to "N/A",
                    ConvivaSdkConstants.DURATION to (ad?.duration?.toInt() ?: 0),
                    ConvivaSdkConstants.ENCODED_FRAMERATE to 0,
                    ConvivaSdkConstants.FRAMEWORK_NAME to "ExoPlayer",
                    ConvivaSdkConstants.FRAMEWORK_VERSION to ExoPlayerLibraryInfo.VERSION,
                    ConvivaConstants.APP_VERSION to BuildConfig.VERSION_NAME,
                    ConvivaConstants.AD_TECHNOLOGY to "Client Side",
                    ConvivaConstants.AD_ID to (ad?.adId ?: "N/A"),
                    ConvivaConstants.AD_SYSTEM to (ad?.adWrapperSystems?.contentToString() ?: "N/A"),
                    ConvivaConstants.AD_POSITION to "N/A",
                    ConvivaConstants.AD_STITCHER to "N/A",
                    ConvivaConstants.AD_IS_SLATE to false.toString(),
                    ConvivaConstants.AD_MEDIA_FILE_API_FRAMEWORK to "N/A",
                    ConvivaConstants.AD_FIRST_AD_SYSTEM to (ad?.adWrapperSystems?.firstOrNull()?.toString() ?: "N/A"),
                    ConvivaConstants.AD_FIRST_AD_ID to (ad?.adWrapperIds?.firstOrNull()?.toString() ?: "N/A"),
                    ConvivaConstants.AD_FIRST_CREATIVE_ID to (ad?.adWrapperCreativeIds?.firstOrNull()?.toString() ?: "N/A"),
                    ConvivaConstants.AD_CREATIVE_ID to (ad?.creativeId ?: "N/A")
                )
        }
        
        fun endPlayerSession() {
            if (isSessionActive) {
                instance.convivaVideoAnalytics?.reportPlaybackEnded()
                isSessionActive = false
            }
        }
        
        fun release() {
            endPlayerSession()
            instance.convivaAdAnalytics?.release()
            instance.convivaVideoAnalytics?.release()
            ConvivaAnalytics.release()
        }
    }
}