package com.banglalink.toffee.util

import android.content.Context
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.model.ChannelInfo
import com.conviva.sdk.ConvivaAdAnalytics
import com.conviva.sdk.ConvivaAnalytics
import com.conviva.sdk.ConvivaSdkConstants
import com.conviva.sdk.ConvivaVideoAnalytics

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
        
        fun setConvivaMetadata(info: ChannelInfo, customerId: Int, seriesName: String? = null, seasonNumber: Int? = 0) {
            val contentInfo = mapOf(
                ConvivaSdkConstants.ASSET_NAME to "[${info.id}] ${info.program_name}",
                ConvivaSdkConstants.IS_LIVE to info.isLive,
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