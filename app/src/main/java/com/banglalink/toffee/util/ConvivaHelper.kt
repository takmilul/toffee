package com.banglalink.toffee.util

import android.content.Context
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.model.ChannelInfo
import com.conviva.sdk.ConvivaAdAnalytics
import com.conviva.sdk.ConvivaAnalytics
import com.conviva.sdk.ConvivaSdkConstants
import com.conviva.sdk.ConvivaVideoAnalytics
import com.google.ads.interactivemedia.v3.api.Ad
import com.google.ads.interactivemedia.v3.api.AdErrorEvent
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerLibraryInfo

class ConvivaHelper private constructor() {
    
    var vastTag: String? = null
        private set
    var convivaAdAnalytics: ConvivaAdAnalytics? = null
        private set
    var convivaVideoAnalytics: ConvivaVideoAnalytics? = null
        private set
    
    companion object {
        val instance = ConvivaHelper()
        private var isAdSessionActive = false
        private var isVideoSessionActive = false
        
        fun init(context: Context?, isActive: Boolean) {
            if (isActive) {
                instance.convivaVideoAnalytics = ConvivaAnalytics.buildVideoAnalytics(context)
                instance.convivaAdAnalytics = ConvivaAnalytics.buildAdAnalytics(context, instance.convivaVideoAnalytics)
            }
        }
        
        fun setPlayer(player: ExoPlayer?) {
            instance.convivaVideoAnalytics?.setPlayer(player)
        }
        
        fun setConvivaVideoMetadata(info: ChannelInfo, customerId: Int, seriesName: String? = null, seasonNumber: Int? = 0) {
            if (isVideoSessionActive) {
                endPlayerSession()
            }
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
                ConvivaConstants.EPISODE_NUMBER to (if (info.episodeNo == 0) "N/A" else info.episodeNo.toString()),
                ConvivaConstants.GENRE to "N/A",
                ConvivaConstants.GENRE_LIST to "N/A",
                ConvivaConstants.UTM_TRACKING_URL to "N/A"
            )
            instance.convivaVideoAnalytics?.reportPlaybackRequested(contentInfo)
            isVideoSessionActive = true
        }
        
        fun updateStreamUrl(url: String?) {
            instance.convivaVideoAnalytics?.setContentInfo(mapOf(ConvivaSdkConstants.STREAM_URL to url))
        }
        
        fun onPlaybackError(errorMessage: String) {
            instance.convivaVideoAnalytics?.reportPlaybackError(errorMessage, ConvivaSdkConstants.ErrorSeverity.WARNING)
        }
        
        fun setVastTagUrl(vastTag: String?) {
            instance.vastTag = vastTag
        }
        
        private fun getConvivaAdMetadata(ad: Ad?): Map<String, Any> {
            return mapOf(
                    ConvivaSdkConstants.ASSET_NAME to "${ad?.adId?.let { "[$it]" } ?: ""} ${ad?.title ?: "AD Failed"}",
                    ConvivaSdkConstants.STREAM_URL to (instance.vastTag ?: "N/A"),
                    ConvivaSdkConstants.IS_LIVE to (ad?.isLinear?.toString() ?: "false"),
                    ConvivaSdkConstants.DURATION to (ad?.duration?.toInt() ?: 0),
                    ConvivaSdkConstants.ENCODED_FRAMERATE to 0,
                    ConvivaSdkConstants.FRAMEWORK_NAME to "ExoPlayer IMA Extension",
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
        
        private fun onAdBreakStarted(ad: Ad?) {
            if (isAdSessionActive) {
                onAdBreakEnded()
            }
            instance.convivaVideoAnalytics?.reportAdBreakStarted(ConvivaSdkConstants.AdPlayer.CONTENT, ConvivaSdkConstants.AdType.CLIENT_SIDE,
                getConvivaAdMetadata(ad))
            isAdSessionActive = true
        }
        
        fun onAdLoaded(ad: Ad?) {
            onAdBreakStarted(ad)
            instance.convivaAdAnalytics?.reportAdLoaded(getConvivaAdMetadata(ad))
        }
        
        fun onAdBuffering(ad: Ad?) {
            reportAdMetrics(ad, isBuffering = true)
        }
        
        fun onAdStarted(ad: Ad?) {
            instance.convivaAdAnalytics?.reportAdStarted(getConvivaAdMetadata(ad))
            reportAdMetrics(ad)
        }
        
        fun onAdProgress(ad: Ad?) {
            reportAdMetrics(ad, isPlaying = true)
        }
        
        fun onAdSkipped() {
            instance.convivaAdAnalytics?.reportAdSkipped()
            onAdEnded()
        }
        
        fun onAdEnded() {
            instance.convivaAdAnalytics?.reportAdEnded()
        }
        
        fun onAdFailed(errorMessage: String, ad: Ad? = null) {
            onAdBreakStarted(ad)
            instance.convivaAdAnalytics?.reportAdFailed(errorMessage, getConvivaAdMetadata(ad))
        }
        
        fun onAdError(adErrorEvent: AdErrorEvent?) {
            val errorMessage = adErrorEvent?.error?.message ?: "Unknown error occurred."
            onAdFailed(errorMessage)
            onAdBreakEnded()
        }
        
        private fun reportAdMetrics(ad: Ad?, isBuffering: Boolean = false, isPlaying: Boolean = false) {
            if (isBuffering) {
                instance.convivaAdAnalytics?.reportAdMetric(ConvivaSdkConstants.PLAYBACK.PLAYER_STATE, ConvivaSdkConstants.PlayerState.BUFFERING)
            } else {
                if (isPlaying) {
                    instance.convivaAdAnalytics?.reportAdMetric(ConvivaSdkConstants.PLAYBACK.PLAYER_STATE, ConvivaSdkConstants.PlayerState.PLAYING)
                    instance.convivaAdAnalytics?.reportAdMetric(ConvivaSdkConstants.PLAYBACK.PLAY_HEAD_TIME, ad?.adPodInfo?.adPosition?.toLong() ?: 0L)
                }
                instance.convivaAdAnalytics?.reportAdMetric(ConvivaSdkConstants.PLAYBACK.BITRATE, ad?.vastMediaBitrate ?: 0)
                instance.convivaAdAnalytics?.reportAdMetric(ConvivaSdkConstants.PLAYBACK.RESOLUTION, ad?.vastMediaWidth ?: 0, ad?.vastMediaHeight ?: 0)
            }
        }
        
        fun onAdBreakEnded(isForceClosed: Boolean = false) {
            if (isAdSessionActive) {
                if (isForceClosed) {
                    instance.convivaAdAnalytics?.reportAdSkipped()
                }
                instance.convivaVideoAnalytics?.reportAdBreakEnded()
                isAdSessionActive = false
            }
        }
        
        fun endPlayerSession(isForceClosed: Boolean = false) {
            if (isVideoSessionActive) {
                onAdBreakEnded(isForceClosed)
                instance.convivaVideoAnalytics?.reportPlaybackEnded()
                isVideoSessionActive = false
            }
        }
        
        fun release() {
            endPlayerSession(true)
            instance.convivaAdAnalytics?.release()
            instance.convivaVideoAnalytics?.release()
            ConvivaAnalytics.release()
        }
    }
}