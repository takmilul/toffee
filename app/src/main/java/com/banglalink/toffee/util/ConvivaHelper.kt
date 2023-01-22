package com.banglalink.toffee.util

import android.content.Context
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.model.ChannelInfo
import com.conviva.sdk.*
import com.conviva.sdk.ConvivaSdkConstants.AdPosition.*
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
        private var isAdPaused = false
        private var isAdFailureActive = false
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
        
        fun setConvivaVideoMetadata(info: ChannelInfo, customerId: Int) {
            if (isVideoSessionActive) {
                endPlayerSession()
            }
            val contentInfo = mapOf(
                ConvivaSdkConstants.ASSET_NAME to "[${info.id}] ${info.program_name}",
                ConvivaSdkConstants.IS_LIVE to info.isLinear,
                ConvivaSdkConstants.PLAYER_NAME to "Toffee Android",
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
                ConvivaConstants.SERIES_NAME to (info.seriesName ?: "N/A"),
                ConvivaConstants.SEASON_NUMBER to (info.seasonNo.toString()),
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
                    ConvivaConstants.AD_POSITION to (ad?.adPodInfo?.podIndex?.let { if (it == 0) PREROLL.toString() else if (it == -1) POSTROLL.toString() else MIDROLL.toString() } ?: "N/A"),
                    ConvivaConstants.AD_STITCHER to "N/A",
                    ConvivaConstants.AD_IS_SLATE to false.toString(),
                    ConvivaConstants.AD_MEDIA_FILE_API_FRAMEWORK to "N/A",
                    ConvivaConstants.AD_FIRST_AD_SYSTEM to (ad?.adWrapperSystems?.firstOrNull()?.toString() ?: "N/A"),
                    ConvivaConstants.AD_FIRST_AD_ID to (ad?.adWrapperIds?.firstOrNull()?.toString() ?: "N/A"),
                    ConvivaConstants.AD_FIRST_CREATIVE_ID to (ad?.adWrapperCreativeIds?.firstOrNull()?.toString() ?: "N/A"),
                    ConvivaConstants.AD_CREATIVE_ID to (ad?.creativeId ?: "N/A")
                )
        }
        
        fun onAdBreakStarted(ad: Ad?) {
            if (isAdSessionActive) {
                onAdBreakEnded()
            }
            instance.convivaVideoAnalytics?.reportAdBreakStarted(ConvivaSdkConstants.AdPlayer.CONTENT, ConvivaSdkConstants.AdType.CLIENT_SIDE,
                getConvivaAdMetadata(ad))
            isAdSessionActive = true
        }
        
        fun onAdLoaded(ad: Ad?) {
            instance.convivaAdAnalytics?.reportAdLoaded()
            reportAdMetrics(ad)
        }
        
        fun onAdBuffering(ad: Ad?) {
            reportAdMetrics(ad, isBuffering = true)
        }
        
        fun onAdStarted(ad: Ad?) {
            instance.convivaAdAnalytics?.reportAdStarted(getConvivaAdMetadata(ad))
            reportAdMetrics(ad)
        }
        
        fun onAdPaused(ad: Ad?) {
            isAdPaused = true
            reportAdMetrics(ad, isPaused = true)
        }
        
        fun onAdResumed(ad: Ad?) {
            isAdPaused = false
            reportAdMetrics(ad, isResumed = true)
        }
        
        fun onAdProgress(ad: Ad?) {
            reportAdMetrics(ad, isPlaying = true)
        }
        
        fun onAdSkipped() {
            isAdPaused = false
            instance.convivaAdAnalytics?.reportAdSkipped()
        }
        
        fun onAdEnded() {
            isAdPaused = false
            isAdFailureActive = false
            instance.convivaAdAnalytics?.reportAdEnded()
        }
        
        fun onAllAdEnded() {
            isAdPaused = false
            isAdFailureActive = false
        }
        
        fun onAdFailed(errorMessage: String, ad: Ad? = null) {
            if (!isAdFailureActive) {
                isAdFailureActive = true
                onAdBreakStarted(ad)
                instance.convivaAdAnalytics?.reportAdFailed(errorMessage, getConvivaAdMetadata(ad))
            }
        }
        
        fun onAdError(adErrorEvent: AdErrorEvent?) {
            val errorMessage = adErrorEvent?.error?.message ?: "Unknown error occurred."
            onAdFailed(errorMessage)
            onAdBreakEnded()
            isAdFailureActive = false
        }
        
        private fun reportAdMetrics(ad: Ad?, isBuffering: Boolean = false, isPlaying: Boolean = false, isPaused: Boolean = false, isResumed: Boolean = false) {
            if (isAdSessionActive) {
                when {
                    isBuffering -> {
                        instance.convivaAdAnalytics?.reportAdMetric(ConvivaSdkConstants.PLAYBACK.PLAYER_STATE, ConvivaSdkConstants.PlayerState.BUFFERING)
                    }
                    isPaused -> {
                        instance.convivaAdAnalytics?.reportAdMetric(ConvivaSdkConstants.PLAYBACK.PLAYER_STATE, ConvivaSdkConstants.PlayerState.PAUSED)
                    }
                    isResumed -> {
                        instance.convivaAdAnalytics?.reportAdMetric(ConvivaSdkConstants.PLAYBACK.PLAYER_STATE, ConvivaSdkConstants.PlayerState.PLAYING)
                        instance.convivaAdAnalytics?.reportAdMetric(ConvivaSdkConstants.PLAYBACK.BITRATE, ad?.vastMediaBitrate ?: 0)
                    }
                    isPlaying -> {
                        if (! isAdPaused) {
                            instance.convivaAdAnalytics?.reportAdMetric(ConvivaSdkConstants.PLAYBACK.PLAYER_STATE, ConvivaSdkConstants.PlayerState.PLAYING)
                            instance.convivaAdAnalytics?.reportAdMetric(ConvivaSdkConstants.PLAYBACK.BITRATE, ad?.vastMediaBitrate ?: 0)
                            instance.convivaAdAnalytics?.reportAdMetric(ConvivaSdkConstants.PLAYBACK.PLAY_HEAD_TIME, ad?.adPodInfo?.adPosition?.toLong() ?: 0L)
                        }
                    }
                    else -> {
                        instance.convivaAdAnalytics?.reportAdMetric(ConvivaSdkConstants.PLAYBACK.RESOLUTION, ad?.vastMediaWidth ?: 0, ad?.vastMediaHeight ?: 0)
                    }
                }
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
                isAdFailureActive = false
                isAdSessionActive = false
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