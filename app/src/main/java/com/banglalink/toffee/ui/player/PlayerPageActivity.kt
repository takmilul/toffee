package com.banglalink.toffee.ui.player

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.MediaSessionCompat.Callback
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.State
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.analytics.FirebaseParams
import com.banglalink.toffee.analytics.HeartBeatManager
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.apiservice.DrmTokenService
import com.banglalink.toffee.data.database.entities.ContentViewProgress
import com.banglalink.toffee.data.database.entities.ContinueWatchingItem
import com.banglalink.toffee.data.database.entities.DrmLicenseEntity
import com.banglalink.toffee.data.repository.ContentViewPorgressRepsitory
import com.banglalink.toffee.data.repository.ContinueWatchingRepository
import com.banglalink.toffee.data.repository.DrmLicenseRepository
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.PlayerPreference
import com.banglalink.toffee.di.DnsHttpClient
import com.banglalink.toffee.di.ToffeeHeader
import com.banglalink.toffee.extension.getChannelMetadata
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.overrideUrl
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.listeners.OnPlayerControllerChangedListener
import com.banglalink.toffee.listeners.PlaylistListener
import com.banglalink.toffee.model.Channel
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.receiver.ConnectionWatcher
import com.banglalink.toffee.ui.common.BaseAppCompatActivity
import com.banglalink.toffee.ui.home.*
import com.banglalink.toffee.util.*
import com.google.ads.interactivemedia.v3.api.AdErrorEvent
import com.google.ads.interactivemedia.v3.api.AdEvent
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventType.*
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ExoPlayer.Builder
import com.google.android.exoplayer2.Player.PositionInfo
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.analytics.AnalyticsListener.EventTime
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager
import com.google.android.exoplayer2.drm.DrmSession.DrmSessionException
import com.google.android.exoplayer2.drm.DrmSessionEventListener
import com.google.android.exoplayer2.drm.DrmSessionManager
import com.google.android.exoplayer2.drm.OfflineLicenseHelper
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.ext.cast.SessionAvailabilityListener
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSource
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.LoadEventInfo
import com.google.android.exoplayer2.source.MediaLoadData
import com.google.android.exoplayer2.source.ads.AdsLoader
import com.google.android.exoplayer2.source.dash.DashUtil
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.Parameters
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy
import java.net.URLEncoder
import javax.inject.Inject
import kotlin.math.max
import kotlin.random.Random

@AndroidEntryPoint
abstract class PlayerPageActivity :
    PlaylistListener,
    AnalyticsListener,
    BaseAppCompatActivity(),
    SessionAvailabilityListener,
    OnPlayerControllerChangedListener
{
    private var startWindow = 0
    private var maxBitRate: Int = 0
    private var playCounter: Int = -1
    private var startAutoPlay = false
    private var retryCounter: Int = 0
    private var reloadCounter: Int = 0
    private var startPosition: Long = 0
    private var fallbackCounter: Int = 0
    protected var player: Player? = null
    private var isAppBackgrounded = false
    @Inject lateinit var pingTool: PingTool
    private var adsLoader: AdsLoader? = null
    private var exoPlayer: ExoPlayer? = null
    private var castPlayer: CastPlayer? = null
    protected var castContext: CastContext? = null
    protected var playerErrorMessage: String? = null
    private var currentlyPlayingVastUrl: String = ""
    @Inject lateinit var drmTokenApi: DrmTokenService
    private var defaultCookieManager = CookieManager()
    private var mediaSession: MediaSessionCompat? = null
    @Inject lateinit var heartBeatManager: HeartBeatManager
    private var trackSelectorParameters: Parameters? = null
    @ToffeeHeader @Inject lateinit var toffeeHeader: String
    @Inject lateinit var connectionWatcher: ConnectionWatcher
    @Inject lateinit var drmLicenseRepo: DrmLicenseRepository
    private val playerViewModel by viewModels<PlayerViewModel>()
    private var defaultTrackSelector: DefaultTrackSelector? = null
    @DnsHttpClient @Inject lateinit var dnsHttpClient: OkHttpClient
    @Inject lateinit var playerEventHelper: ToffeePlayerEventHelper
    @Inject lateinit var contentViewRepo: ContentViewPorgressRepsitory
    private var httpDataSourceFactory: OkHttpDataSource.Factory? = null
    private var playerAnalyticsListener: PlayerAnalyticsListener? = null
    @Inject lateinit var continueWatchingRepo: ContinueWatchingRepository
    private val playerEventListener: PlayerEventListener = PlayerEventListener()
    
    init {
        defaultCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER)
    }
    
    companion object {
        private const val KEY_WINDOW = "window"
        private const val KEY_POSITION = "position"
        private const val KEY_VAST_URL = "vast_url"
        private const val KEY_AUTO_PLAY = "auto_play"
        private const val KEY_PLAY_COUNTER = "play_counter"
        private const val KEY_TRACK_SELECTOR_PARAMETERS = "track_selector_parameters"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (CookieHandler.getDefault() !== defaultCookieManager) {
            CookieHandler.setDefault(defaultCookieManager)
        }
        
        if (mPref.isCastEnabled) {
            castContext = try {
                CastContext.getSharedInstance(applicationContext)
            } catch (ex: Exception) {
                ToffeeAnalytics.logException(ex)
                null
            }
        }
        
        if (savedInstanceState != null) {
            startWindow = savedInstanceState.getInt(KEY_WINDOW)
            startPosition = savedInstanceState.getLong(KEY_POSITION)
            playCounter = savedInstanceState.getInt(KEY_PLAY_COUNTER)
            startAutoPlay = savedInstanceState.getBoolean(KEY_AUTO_PLAY)
            currentlyPlayingVastUrl = savedInstanceState.getString(KEY_VAST_URL) ?: ""
            trackSelectorParameters = savedInstanceState.getBundle(KEY_TRACK_SELECTOR_PARAMETERS)?.let { Parameters.CREATOR.fromBundle(it) }
        } else {
            val builder = Parameters.Builder(this)
            trackSelectorParameters = builder.build()
            clearStartPosition()
        }
        heartBeatManager.heartBeatEventLiveData.observe(this) {
            playerAnalyticsListener?.let {
                //In every heartbeat event we are sending bandwitdh data to Pubsub
//                Log.i("PLAYER BYTES", "Flushing to pubsub")
                playerViewModel.reportBandWidthFromPlayerPref(
                    it.durationInSeconds, it.getTotalBytesInMB()
                )
                playerAnalyticsListener?.resetData()
            }
        }
        
        adsLoader = ImaAdsLoader.Builder(this).setAdEventListener {
            onAdEventListener(it)
        }.setAdErrorListener {
            onAdErrorListener(it)
        }.build()
    }
    
    abstract val playlistManager: PlaylistManager
    abstract fun getPlayerView(): StyledPlayerView
    
    protected open fun onContentExpired() {
        //hook for subclass
    }
    
    public override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23 /*&& isAppBackgrounded*/) {
            initializePlayer()
        }
        mediaSession?.isActive = true
    }
    
    public override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 || player == null /*&& isAppBackgrounded*/) {
            initializePlayer()
        }
    }
    
    public override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
            isAppBackgrounded = true
        }
    }
    
    public override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
            isAppBackgrounded = true
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        playChannelJob?.cancel()
        adsLoader?.release()
        adsLoader = null
//        mOfflineLicenseHelper?.release()
//        mOfflineLicenseHelper = null
    }
    
    @SuppressLint("MissingSuperCall")
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        updateTrackSelectorParameters()
        updateStartPosition()
        if (player?.isPlaying == true) {
            playlistManager.getCurrentChannel()?.viewProgress = player?.currentPosition ?: 0
        }
        outState.apply {
            putInt(KEY_WINDOW, startWindow)
            putLong(KEY_POSITION, startPosition)
            putInt(KEY_PLAY_COUNTER, playCounter)
            putBoolean(KEY_AUTO_PLAY, startAutoPlay)
            putString(KEY_VAST_URL, currentlyPlayingVastUrl)
            putBundle(KEY_TRACK_SELECTOR_PARAMETERS, trackSelectorParameters?.toBundle())
        }
    }
    
    private fun initializePlayer() {
        retryCounter = 0
        reloadCounter = 0
        fallbackCounter = 0
        initializeLocalPlayer()
        initializeRemotePlayer()
        player = if (castPlayer?.isCastSessionAvailable == true) castPlayer else exoPlayer
        
        player?.let { pl ->
            if (pl is CastPlayer && playlistManager.getCurrentChannel() == null) {
                val ci = pl.currentMediaItem?.getChannelMetadata(pl)
                ci?.viewProgress = pl.currentPosition
                if (ci != null) playlistManager.setPlaylist(ci)
            }
        }
        
        //we are checking whether there is already channelInfo exist. If not null then play it
        if (playlistManager.getCurrentChannel() != null && player != castPlayer) {
            player?.playWhenReady = true
            playChannel(true)
        }
    }
    
    private fun initializeLocalPlayer() {
        if (exoPlayer == null) {
            val adaptiveTrackSelectionFactory = AdaptiveTrackSelection.Factory()
            defaultTrackSelector = DefaultTrackSelector(this, adaptiveTrackSelectionFactory).apply {
                parameters = trackSelectorParameters!!
            }
            playerAnalyticsListener = PlayerAnalyticsListener()
            httpDataSourceFactory = OkHttpDataSource.Factory(
                dnsHttpClient.apply {
                    if (BuildConfig.DEBUG) {
                        newBuilder()
                            .addNetworkInterceptor(
                                HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS)
                            )
                            .build()
                    }
                }
            )
//            val cacheFolder = File(this.cacheDir, "/toffee_media")
//            val cacheEvictor = LeastRecentlyUsedCacheEvictor(5 * 1024 * 1024) // cache size = 5MB
//            val databaseProvider = StandaloneDatabaseProvider(this)
//            val cache = SimpleCache(cacheFolder, cacheEvictor, databaseProvider)
//            val cacheDataSourceFactory = CacheDataSource.Factory()
//                .setCache(cache)
//                .setUpstreamDataSourceFactory(httpDataSourceFactory)
//                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
            
            val mediaSourceFactory = DefaultMediaSourceFactory(httpDataSourceFactory!!)
                .setAdsLoaderProvider {
                    adsLoader
                }
                .setDrmSessionManagerProvider(this::getDrmSessionManager)
                .setAdViewProvider(getPlayerView())
//                .setLoadErrorHandlingPolicy(DefaultLoadErrorHandlingPolicy(Int.MAX_VALUE))
            
            exoPlayer = Builder(this)
                .setMediaSourceFactory(mediaSourceFactory)
                .setTrackSelector(defaultTrackSelector!!)
                .setLoadControl(DefaultLoadControl.Builder().setBufferDurationsMs(120_000, 120_000, 2_500, 5_000).build())
                .build()
                .apply {
                    addAnalyticsListener(playerAnalyticsListener!!)
                    addListener(playerEventListener)
                    playWhenReady = false
                    if (BuildConfig.DEBUG) {
                        addAnalyticsListener(EventLogger())
                    }
                }
            adsLoader?.setPlayer(exoPlayer)
            ConvivaHelper.setPlayer(exoPlayer)
            mediaSession = MediaSessionCompat(this, packageName)
            mediaSession!!.setCallback(MediaSessionCallback())
            observeNetworkChange()
        }
    }
    
    inner class MediaSessionCallback : Callback() {
        override fun onPlay() {
            super.onPlay()
            player?.play()
            updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
        }
        
        override fun onPause() {
            super.onPause()
            player?.pause()
            updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
        }
        
        override fun onSkipToNext() {
            super.onSkipToNext()
            playNext()
            updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
        }
        
        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            playPrevious()
            updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
        }
    }
    
    private fun getAction(): Long {
        val mediaActionPlayPause = (PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_PLAY_PAUSE)
        val mediaActionPlayPauseNext = (mediaActionPlayPause or PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
        val mediaActionPlayPausePrevious = (mediaActionPlayPause or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
        val mediaActionAll: Long = (mediaActionPlayPause or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
        
        return if (!hasNext() && !hasPrevious()) {
            mediaActionPlayPause
        } else if (hasNext() && !hasPrevious()) {
            mediaActionPlayPauseNext
        } else if (!hasNext() && hasPrevious()) {
            mediaActionPlayPausePrevious
        } else {
            mediaActionAll
        }
    }
    
    private fun updatePlaybackState(@State state: Int) {
        val position = player?.currentPosition?.toInt() ?: 0
        val mediaId = player?.currentMediaItem?.getChannelMetadata(player)?.id?.toLong() ?: 0
        val builder = PlaybackStateCompat.Builder().setActions(getAction()).setActiveQueueItemId(mediaId).setState(state, position.toLong(), 1.0f)
        mediaSession?.setPlaybackState(builder.build())
    }
    
    private fun observeNetworkChange() {
        observe(heartBeatManager.networkChangeEventLiveData) {
            if (it && isPlayerVisible()) {
                val isCellularNetwork = connectionWatcher.isOverCellular
                if ((isCellularNetwork && maxBitRate != mPref.maxBitRateCellular) || (!isCellularNetwork && maxBitRate != mPref.maxBitRateWifi)) {
                    maxBitRate = if (isCellularNetwork) mPref.maxBitRateCellular else mPref.maxBitRateWifi
                    if (player != null && maxBitRate > 0) {
                        val param = defaultTrackSelector?.buildUponParameters()?.setMaxVideoBitrate(maxBitRate)?.build()
                        param?.let { defaultTrackSelector?.parameters = it }
                        player!!.prepare()
                        player!!.playWhenReady = true
                    }
                }
            }
        }
    }
    
    private fun isDrmActiveForChannel(channelInfo: ChannelInfo) = 
        cPref.isDrmModuleAvailable == CommonPreference.DRM_AVAILABLE
        && mPref.isDrmActive && channelInfo.isDrmActive
        && (!channelInfo.drmDashUrl.isNullOrBlank() || !channelInfo.drmDashUrlExt?.get(0)?.urlList()?.randomOrNull().isNullOrEmpty() || !channelInfo.drmDashUrlExtSd?.get(0)?.urlList()?.randomOrNull().isNullOrEmpty())
        && !mPref.drmWidevineLicenseUrl.isNullOrBlank()
    //&& !channelInfo.drmCid.isNullOrBlank()
    // && player is SimpleExoPlayer
    
    private fun getDrmSessionManager(mediaItem: MediaItem?): DrmSessionManager {
        return try {
            ToffeeAnalytics.logBreadCrumb("isMediaItemNull: ${mediaItem == null}")
            val channelInfo = mediaItem?.getChannelMetadata(player) ?: return DrmSessionManager.DRM_UNSUPPORTED
            val isDrmActive = isDrmActiveForChannel(channelInfo)
            ToffeeAnalytics.logBreadCrumb("isDrmActive: $isDrmActive")
            
            if (!isDrmActive) {
                return DrmSessionManager.DRM_UNSUPPORTED
            }
            val drmCid = if (mPref.isGlobalCidActive) mPref.globalCidName else channelInfo.drmCid
            ToffeeAnalytics.logBreadCrumb("isDrmWidevineLicenseUrlNull: ${mPref.drmWidevineLicenseUrl.isNullOrBlank()}, \nisHttpDataSourceFactoryNull: ${httpDataSourceFactory == null}, \nisDrmCidNull: ${drmCid.isNullOrBlank()}")
            
            return if (!mPref.drmWidevineLicenseUrl.isNullOrBlank() && httpDataSourceFactory != null) {
                DefaultDrmSessionManager
                    .Builder()
                    .setMultiSession(false)
                    .build(
                        ToffeeMediaDrmCallback(
                            mPref.drmWidevineLicenseUrl!!, httpDataSourceFactory!!, drmTokenApi, drmCid ?: ""
                        )
                    )
                    .apply {
                        mediaItem.localConfiguration?.drmConfiguration?.keySetId?.let {
                            Log.i("DRM_T", "Using offline key")
                            ToffeeAnalytics.logBreadCrumb("Using offline key")
                            setMode(DefaultDrmSessionManager.MODE_PLAYBACK, it)
                        }
                    }
            } else {
                DrmSessionManager.DRM_UNSUPPORTED
            }
        } catch (e: Exception) {
            ToffeeAnalytics.logBreadCrumb("DefaultDrmSessionManager build error: ${e.message}")
            DrmSessionManager.DRM_UNSUPPORTED
        }
    }
    
    private val castSessionListener = object : SessionManagerListener<CastSession> {
        override fun onSessionStarting(p0: CastSession) {
            p0.castDevice?.friendlyName?.let {
                showToast("Connecting to $it")
            }
        }
        
        override fun onSessionStarted(p0: CastSession, p1: String) {
            p0.castDevice?.friendlyName?.let {
                showToast("Connected to $it")
            }
        }
        
        override fun onSessionStartFailed(castSession: CastSession, p1: Int) {
            castSession.castDevice?.friendlyName?.let {
                showToast("Failed to connect to $it")
            }
        }
        
        override fun onSessionEnding(p0: CastSession) {}
        override fun onSessionEnded(p0: CastSession, p1: Int) {}
        override fun onSessionResuming(p0: CastSession, p1: String) {}
        
        override fun onSessionResumed(castSession: CastSession, p1: Boolean) {
            castSession.let {
                val cInfo = try {
                    val customData = it.remoteMediaClient?.currentItem?.customData!!
                    Gson().fromJson(customData.getString("channel_info"), ChannelInfo::class.java)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    null
                }
                if (cInfo != null && playlistManager.getCurrentChannel() == null) {
                    playlistManager.setPlaylist(cInfo)
                    resumeCastSession(cInfo)
                }
            }
        }
        
        override fun onSessionResumeFailed(p0: CastSession, p1: Int) {}
        override fun onSessionSuspended(p0: CastSession, p1: Int) {}
    }
    
    protected open fun resumeCastSession(info: ChannelInfo) {}
    
    private fun initializeRemotePlayer() {
        castContext?.let {
            it.sessionManager.addSessionManagerListener(castSessionListener, CastSession::class.java)
            
            Log.i("CAST_T", "CastPlayer init")
            castPlayer = CastPlayer(it, ToffeeMediaItemConverter(connectionWatcher.isOverWifi, mPref)).apply {
                addListener(playerEventListener)
                playWhenReady = true
                setSessionAvailabilityListener(this@PlayerPageActivity)
            }
        }
    }
    
    private fun releasePlayer() {
        releaseLocalPlayer()
        releaseRemotePlayer()
        castContext?.sessionManager?.removeSessionManagerListener(castSessionListener, CastSession::class.java)
        player = null
        retryCounter = 0
        reloadCounter = 0
        fallbackCounter = 0
    }
    
    private fun releaseLocalPlayer() {
        exoPlayer?.let {
            it.removeListener(playerEventListener)
            updateTrackSelectorParameters()
            updateStartPosition()
            it.release()
            defaultTrackSelector = null
            playerAnalyticsListener?.let { pal ->
                PlayerPreference.getInstance().savePlayerSessionBandWidth(pal.durationInSeconds, pal.getTotalBytesInMB())
            }
        }
        adsLoader?.setPlayer(null)
        exoPlayer = null
    }
    
    private fun releaseRemotePlayer() {
        castPlayer?.removeListener(playerEventListener)
        castPlayer?.setSessionAvailabilityListener(null)
        try {
            castPlayer?.let {
                val field = it::class.java.getDeclaredField("statusListener")
                field.isAccessible = true
                
                val obj = field.get(it)
                if (obj is SessionManagerListener<*>) {
                    castContext?.sessionManager?.removeSessionManagerListener(obj, CastSession::class.java)
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
//        castPlayer?.release()
//        Log.e("CAST_T", "Release remote player")
//        if(castPlayer?.isPlaying == true && playlistManager.getCurrentChannel() != null) {
//            mPref.savedCastInfo = playlistManager.getCurrentChannel()
//            Log.e("CAST_T", "Saving channel info -> ${mPref.savedCastInfo?.id}")
//        }
        castPlayer = null
    }
    
    private fun updateTrackSelectorParameters() {
        if (defaultTrackSelector != null) {
            trackSelectorParameters = defaultTrackSelector?.parameters
        }
    }
    
    protected fun updateStartPosition() {
        player?.let {
            startAutoPlay = it.playWhenReady
            startWindow = it.currentMediaItemIndex
            startPosition = max(0, it.contentPosition)
        }
    }
    
    private fun clearStartPosition() {
        startAutoPlay = true
        startWindow = C.INDEX_UNSET
        startPosition = C.TIME_UNSET
    }
    
    protected fun setPlayList(data: AddToPlaylistData) {
        playlistManager.setPlayList(data)
    }
    
    override fun hasPrevious(): Boolean {
        return playlistManager.hasPrevious()
    }
    
    override operator fun hasNext(): Boolean {
        return playlistManager.hasNext()
    }
    
    protected open fun playIndex(index: Int) {
        playlistManager.setIndex(index)
        playChannel(false)
    }
    
    protected open fun playChannelId(channelId: Int) {
        playlistManager.setChannelId(channelId)
        playChannel(false)
    }
    
    override fun isAutoplayEnabled(): Boolean {
        return mPref.isAutoplayForRecommendedVideos
    }
    
    override fun playNext() {
        playlistManager.nextChannel()
        if (playlistManager.playlistId != -1L) {
            playChannel(false)
        }
    }
    
    override fun playPrevious() {
        playlistManager.previousChannel()
        playChannel(false)
    }
    
    protected fun addChannelToPlayList(info: ChannelInfo) {
        val cInfo = playlistManager.getCurrentChannel()
        var isReload = false
        if (cInfo?.id.equals(info.id, ignoreCase = true)) {
            isReload = true
        } else {
            playlistManager.setPlaylist(info)
        }
        playChannel(isReload)
    }
    
    private val LICENSE_EXPIRE_BEFORE = /*95_000L */ 604_800_000L // 7 days
    
    private fun isLicenseAlmostExpired(exp: Long): Boolean {
        return exp - LICENSE_EXPIRE_BEFORE < System.currentTimeMillis()
    }
    
    private fun isLicenseExpired(exp: Long) = exp - /*80_000L*/ 21_600_000L < System.currentTimeMillis() // 6 hours
    
    private suspend fun getLicense(channelInfo: ChannelInfo): ByteArray? {
        val channelId = if (mPref.isGlobalCidActive) -1 else channelInfo.id.toLong()
        
        val existingLicense = drmLicenseRepo.getByChannelId(channelId)
        Log.i("DRM_T", "Existing -> $existingLicense")
        if (existingLicense != null && !isLicenseAlmostExpired(existingLicense.expiryTime)) {
            Log.i("DRM_T", "Using existing license")
            return existingLicense.license
        } else if (existingLicense != null && !isLicenseExpired(existingLicense.expiryTime)) {
            Log.i("DRM_T", "License almost expired. requesting new one, but using old one.")
            lifecycleScope.launch(Dispatchers.IO + Job()) {
                downloadLicense(channelInfo)
            }
            return existingLicense.license
        }
        Log.i("DRM_T", "Requesting new license and using that one.")
        lifecycleScope.launch(Dispatchers.IO + Job()) {
            downloadLicense(channelInfo)
        }
        return null
    }
    
    private suspend fun downloadLicense(channelInfo: ChannelInfo): ByteArray? {
        var offlineLicenseHelper: OfflineLicenseHelper? = null
        try {
            val drmCid = if (mPref.isGlobalCidActive) mPref.globalCidName else channelInfo.drmCid
            val token = try {
                drmTokenApi.execute(drmCid!!, 2_592_000 /* 30 days*/)
            } catch (e: Exception) {
                val error = getError(e)
                ToffeeAnalytics.logEvent(
                    ToffeeEvents.EXCEPTION, bundleOf(
                        "api_name" to ApiNames.GET_DRM_TOKEN,
                        FirebaseParams.BROWSER_SCREEN to "Player Page",
                        "error_code" to error.code,
                        "error_description" to error.msg
                    )
                )
                null
            } ?: return null
            Log.i("DRM_T", "Downloading offline license")
            val offlineDataSourceFactory = OkHttpDataSource.Factory(
                dnsHttpClient
//                    .newBuilder()
//                    .addNetworkInterceptor(
//                        HttpLoggingInterceptor()
//                            .setLevel(HttpLoggingInterceptor.Level.HEADERS)
//                    )
//                    .build()
            )
            offlineDataSourceFactory.setDefaultRequestProperties(mapOf("pallycon-customdata-v2" to token))
            
            offlineLicenseHelper = OfflineLicenseHelper.newWidevineInstance(
                mPref.drmWidevineLicenseUrl!!, false, offlineDataSourceFactory, DrmSessionEventListener.EventDispatcher()
            )
            
            val dataSource = httpDataSourceFactory!!.createDataSource()
            val dashManifest = DashUtil.loadManifest(dataSource, Uri.parse(channelInfo.getDrmUrl(connectionWatcher.isOverCellular)))
            val drmInitData = DashUtil.loadFormatWithDrmInitData(dataSource, dashManifest.getPeriod(0)) ?: run {
                offlineLicenseHelper.release()
                return null
            }
            val licenseData = offlineLicenseHelper.downloadLicense(drmInitData)
            Log.i("DRM_T", "License size -> ${licenseData.size}")
            val remainingTime = offlineLicenseHelper.getLicenseDurationRemainingSec(licenseData).first
            Log.i("DRM_T", "Drm expiry time -> $remainingTime")
            val licenseExpiration = if (remainingTime == Long.MAX_VALUE) {
                remainingTime
            } else {
                System.currentTimeMillis() + (remainingTime * 1000)
            }
            
            Log.i("DRM_T", "Saving offline license")
            val newDrmLicense = DrmLicenseEntity(
                if (mPref.isGlobalCidActive) -1 else channelInfo.id.toLong(), drmCid!!, licenseData, licenseExpiration
            )
            drmLicenseRepo.insert(newDrmLicense)
            offlineLicenseHelper.release()
            return newDrmLicense.license
        } catch (ex: Exception) {
            offlineLicenseHelper?.release()
            return null
        }
    }
    
    private suspend fun getDrmMediaItem(channelInfo: ChannelInfo): MediaItem? {
        if (player is CastPlayer) {
            return MediaItem.Builder().apply {
                setMimeType(MimeTypes.APPLICATION_MPD)
                setUri(channelInfo.drmDashUrl)
                setTag(channelInfo)
                setDrmConfiguration(MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID).build())
            }.build()
        }
        val license = getLicense(channelInfo)
        
        val isDataConnection = connectionWatcher.isOverCellular
        val drmUrl = channelInfo.getDrmUrl(isDataConnection)?.let {
            if (mPref.shouldOverrideDrmHostUrl) it.overrideUrl(mPref.overrideDrmHostUrl) else it
        } ?: return null
        return MediaItem.Builder().apply {
//            showToast("Playing DRM -> ${if(license == null) "Requesting new license" else "Using cached license"}\n${channelInfo.drmDashUrl}")
            if (!channelInfo.isStingray) {
                httpDataSourceFactory?.setUserAgent(toffeeHeader)
            }
            setMimeType(MimeTypes.APPLICATION_MPD)
            setUri(drmUrl)
            setTag(channelInfo)
            setDrmConfiguration(
                MediaItem
                    .DrmConfiguration
                    .Builder(C.WIDEVINE_UUID)
                    .setKeySetId(license)
                    .build()
            )
        }.build()
    }
    
    private fun getHlsMediaItem(channelInfo: ChannelInfo): MediaItem? {
        val hlsUrl = if (channelInfo.urlTypeExt == PAYMENT && channelInfo.urlType == PLAY_IN_WEB_VIEW && mPref.isPaidUser) {
            channelInfo.paidPlainHlsUrl
        } else if (channelInfo.urlTypeExt == NON_PAYMENT && (channelInfo.urlType == PLAY_IN_NATIVE_PLAYER || channelInfo.urlType == STINGRAY_CONTENT)) {
            channelInfo.hlsLinks?.get(0)?.hls_url_mobile
        } else {
            null
        }
        hlsUrl ?: return null
        
        val uri = if (channelInfo.isBucketUrl || channelInfo.isStingray) {
            hlsUrl
        } else {
            Channel.createChannel(channelInfo.program_name, hlsUrl).getContentUri(mPref)
        }
        val playingUrl = getGeneratedUrl(uri)
        return MediaItem.Builder().apply {
            if (!channelInfo.isBucketUrl) setMimeType(MimeTypes.APPLICATION_M3U8)
            setUri(playingUrl)
            setTag(channelInfo)
        }.build()
    }
    
    abstract fun isPlayerVisible(): Boolean
    abstract fun maximizePlayer()
    private var playChannelJob: Job? = null
    
    private fun playChannel(isReload: Boolean) {
        if (!isReload) {
            retryCounter = 0
            reloadCounter = 0
            fallbackCounter = 0
        }
        playChannelJob?.cancel()
        Log.i("DRM_T", "New play request")
        playChannelJob = playChannelImpl(isReload)
    }
    
    private fun playChannelImpl(isReload: Boolean) = lifecycleScope.launch {
        maximizePlayer()
        val isWifiConnected = connectionWatcher.isOverWifi
        if (!isWifiConnected && mPref.watchOnlyWifi()) {
            showPlayerError("Please connect to Wifi or disable “Watch only when Wifi is available“ from settings", true)
            return@launch
        }
//        val oldChannelInfo = player?.currentMediaItem?.getChannelMetadata(player)
        val channelInfo = playlistManager.getCurrentChannel() ?: run {
            showPlayerError("Content not found")
            return@launch
        }
        val isExpired = try {
            Utils.getDate(channelInfo.contentExpiryTime).before(mPref.getSystemTime())
        } catch (e: Exception) {
            false
        }
        if (isExpired) {
            onContentExpired()
            return@launch
        }
        val isDrmActive = isDrmActiveForChannel(channelInfo)
        var mediaItem = if (isDrmActive) {
            getDrmMediaItem(channelInfo)
        } else {
            getHlsMediaItem(channelInfo)
        } ?: run {
            showPlayerError("Content url is null")
            ToffeeAnalytics.logException(NullPointerException("Channel url is null for id -> ${channelInfo.id}, name -> ${channelInfo.program_name}"))
            return@launch
        }
        
        val contentUrl = mediaItem.localConfiguration?.uri?.toString()
//        val contentSourceText = if (isDrmActive) "Type: DRM Content\n" else "Type: Non-DRM Content\n"
//        applicationContext.showToast(contentSourceText + "Url: " + contentUrl)
        ConvivaHelper.updateStreamUrl(contentUrl)
        runCatching {
            async{
                playerEventHelper.setEventData(channelInfo, isDrmActive, toffeeHeader, contentUrl, getPingData(mediaItem))
            }
        }
        
        if (!isReload && player is ExoPlayer) playCounter = ++playCounter % mPref.vastFrequency
        
        getVastTagList(channelInfo)
            ?.randomOrNull()
            ?.let { tag ->
                val shouldPlayAd = mPref.isVastActive && playCounter == 0 && channelInfo.isAdActive && !(isReload && channelInfo.isLinear)
                val vastTag = if (isReload) currentlyPlayingVastUrl else tag.url
                ConvivaHelper.setVastTagUrl(vastTag)
                if (shouldPlayAd && vastTag.isNotBlank()) {
                    mediaItem = mediaItem.buildUpon().setAdsConfiguration(MediaItem.AdsConfiguration.Builder(Uri.parse(vastTag)).build()).build()
                    currentlyPlayingVastUrl = vastTag
                }
            }
        
        maxBitRate = if (isWifiConnected) mPref.maxBitRateWifi else mPref.maxBitRateCellular
        if (maxBitRate > 0) {
            val param = defaultTrackSelector?.buildUponParameters()?.setMaxVideoBitrate(maxBitRate)?.build()
            param?.let { defaultTrackSelector?.parameters = it }
        }
        
        player?.let {
            val oldChannelInfo = getCurrentChannelInfo()
            oldChannelInfo?.let { oldInfo ->
                if (oldInfo.id != channelInfo.id && it.playbackState != Player.STATE_ENDED) {
                    insertContentViewProgress(oldInfo, it.currentPosition)
                }
            }
            if (!channelInfo.fcmEventName.isNullOrBlank()) {
                if (channelInfo.isFcmEventActive) {
                    for (event in channelInfo.fcmEventName!!.split(",")) {
                        ToffeeAnalytics.logEvent(event)
                    }
                }
            }
            heartBeatManager.triggerEventViewingContentStart(channelInfo.id.toInt(), channelInfo.type ?: "VOD", channelInfo.dataSource ?: "iptv_programs", channelInfo.channel_owner_id.toString())
            it.playWhenReady = !isReload || it.playWhenReady
            
            if (isReload) { //We need to start where we left off for VODs
                if (channelInfo.viewProgress > 0L) {
                    startPosition = if (channelInfo.viewProgressPercent() >= 990) {
                        C.TIME_UNSET
                    } else {
                        channelInfo.viewProgress
                    }
                }
                val haveStartPosition = startWindow != C.INDEX_UNSET
                if (haveStartPosition && !channelInfo.isLinear) {
                    if (it is ExoPlayer) {
//                        getPlayerView().adViewGroup.removeAllViews()
                        it.setMediaItem(mediaItem, false)
//                        player.prepare(mediaSource, false, false)
                    } else if (it is CastPlayer) {
                        val newMediaItem = if (isDrmActive) {
                            val drmToken = try {
                                drmTokenApi.execute(channelInfo.drmCid!!)
                            } catch (ex: Exception) {
                                null
                            } ?: return@launch
                            mediaItem.buildUpon()
                                .setDrmConfiguration(
                                    mediaItem
                                    .localConfiguration
                                    ?.drmConfiguration
                                    ?.buildUpon()
                                    ?.apply {
                                        setLicenseUri(mPref.drmWidevineLicenseUrl!!)
                                        setMultiSession(false)
                                        setForceDefaultLicenseUri(false)
                                        setLicenseRequestHeaders(mapOf("pallycon-customdata-v2" to drmToken)) }
                                    ?.build()
                                ).build()
                        } else {
                            mediaItem
                        }
                        it.setMediaItem(newMediaItem, startPosition)
                    }
                    it.prepare()
                    it.playWhenReady = true
                    it.seekTo(startWindow, startPosition) //we seek to where we left for VODs
                    return@launch
                }
            }
            startPosition = C.TIME_UNSET
            if (channelInfo.viewProgress > 0L) {
                startPosition = if (channelInfo.viewProgressPercent() >= 990) {
                    C.TIME_UNSET
                } else {
                    channelInfo.viewProgress
                }
            }
            if (it is ExoPlayer) {
                getPlayerView().adViewGroup.removeAllViews()
                it.setMediaItem(mediaItem, startPosition)
                it.prepare()
            } else if (it is CastPlayer) {
                val newMediaItem = if (isDrmActive) {
                    val drmToken = try {
                        drmTokenApi.execute(channelInfo.drmCid!!)
                    } catch (ex: Exception) {
                        null
                    } ?: return@launch
                    mediaItem.buildUpon()
                        .setDrmConfiguration(
                            mediaItem
                                .localConfiguration
                                ?.drmConfiguration
                                ?.buildUpon()
                                ?.apply {
                                    setLicenseUri(mPref.drmWidevineLicenseUrl!!)
                                    setMultiSession(false)
                                    setForceDefaultLicenseUri(false)
                                    setLicenseRequestHeaders(mapOf("pallycon-customdata-v2" to drmToken)) }
                                ?.build()
                        ).build()
                } else {
                    mediaItem
                }
                it.setMediaItem(newMediaItem, startPosition)
                it.playWhenReady = true
                it.prepare()
            }
//            player.prepare(mediaSource);//Non reload event or reload for live. Just prepare the media and play it
        }
    }
    
    private fun getVastTagList(channelInfo: ChannelInfo) = when {
        channelInfo.isStingray -> {
            mPref.stingrayVastTagsMutableLiveData.value
        }
        channelInfo.isLive -> {
            mPref.liveVastTagsMutableLiveData.value
        }
        channelInfo.isVOD -> {
            mPref.vodVastTagsMutableLiveData.value
        }
        else -> {
            null
        }
    }
    
    private fun getGeneratedUrl(url: String?): String? {
        return if (playlistManager.getCurrentChannel()?.isStingray == true) {
            val userAgentString = "Mozilla/5.0 (Linux; Android 4.2.1; SMART-TV; en-us; Nexus 5 Build/JOP40D) AppleWebKit/535.19 (KHTML, like Gecko; googleweblight) Chrome/38.0.1025.166 Mobile Safari/535.19"
            val encodedUserAgent = URLEncoder.encode(userAgentString, "UTF-8")
            val cacheBuster = mPref.customerId.toString() + Random.nextInt() + System.nanoTime()
            
            url?.replace("[APP_BUNDLE]", BuildConfig.VERSION_NAME)
                ?.replace("[DID]", cPref.deviceId)
                ?.replace("[CACHEBUSTER]", cacheBuster)
                ?.replace("[IP]", mPref.userIp)
                ?.replace("[%UA%]", encodedUserAgent)
        } else {
            httpDataSourceFactory?.setUserAgent(toffeeHeader)
            httpDataSourceFactory?.setDefaultRequestProperties(mapOf("TOFFEE-SESSION-TOKEN" to mPref.getHeaderSessionToken()!!))
            url
        }
    }
    
    private suspend fun getPingData(mediaItem: MediaItem?): PingData? {
        val uri = mediaItem?.localConfiguration?.uri
        var pingData: PingData? = null
        uri?.host?.let {
            pingData = pingTool.ping(it)
        }
        return pingData
    }
    
    private fun showPlayerError(errorMessage: String, showMessage: Boolean = false) {
        lifecycleScope.launch {
            playerEventHelper.setPingData(getPingData(player?.currentMediaItem))
            playerEventHelper.setPlayerEvent(errorMessage)
        }
        player?.stop()
        player?.clearMediaItems()
        getPlayerView().adViewGroup.removeAllViews()
        ConvivaHelper.onPlaybackError(errorMessage)
        if (showMessage) {
            channelCannotBePlayedDueToSettings() //notify hook/subclass
        }
        ToffeeAnalytics.playerError(playlistManager.getCurrentChannel()?.program_name ?: "", errorMessage)
        maximizePlayer()
        heartBeatManager.triggerEventViewingContentStop()
    }
    
    //This will be called due to session token change while playing content or after init of player
    protected fun reloadChannel() {
        val channelInfo = playlistManager.getCurrentChannel()
        if (channelInfo != null) {
            playChannel(true)
        }
    }
    
    private fun insertContentViewProgress(channelInfo: ChannelInfo, progress: Long) {
        lifecycleScope.launch {
            Log.i("PLAYBACK_STATE", "Saving state - ${channelInfo.id} -> $progress")
            if (!channelInfo.isLinear && progress > 0L) {
                channelInfo.viewProgress = progress
                contentViewRepo.insert(
                    ContentViewProgress(
                        customerId = mPref.customerId, contentId = channelInfo.id.toLong(), progress = progress
                    )
                )
                Log.i("TOFFEE", "Category - ${channelInfo.categoryId}")
                if (channelInfo.categoryId == 1 && channelInfo.viewProgressPercent() < 970) {
                    continueWatchingRepo.insertItem(
                        ContinueWatchingItem(
                            mPref.customerId,
                            channelInfo.id.toLong(),
                            channelInfo.type ?: "VOD",
                            channelInfo.categoryId,
                            Gson().toJson(channelInfo),
                            progress
                        )
                    )
                }
            }
        }
    }
    
    protected open fun channelCannotBePlayedDueToSettings() {
        //subclass will hook into it
    }
    
    protected fun clearChannel() { //set channelInfo = null
        playlistManager.clearPlaylist()
    }
    
    override fun onPlayButtonPressed(currentState: Int): Boolean {
        return false
    }
    
    abstract fun isVideoPortrait(): Boolean
    
    override fun onFullScreenButtonPressed(): Boolean {
        if (isVideoPortrait()) {
            return true
        }
        val isPortrait = resources.configuration.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        requestedOrientation = if (isPortrait) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        return true
    }
    
    override fun onPlayerIdleDueToError() {
        lifecycleScope.launch {
            playerEventHelper.setPingData(getPingData(player?.currentMediaItem))
            playerEventHelper.setPlayerEvent("playing paused due to error")
        }
        playerErrorMessage = "player idle due to error"
        if (player?.playWhenReady == true && reloadCounter < 3) {
            ToffeeAnalytics.logForcePlay()
            reloadCounter++
            reloadChannel()
        } else {
            ToffeeAnalytics.playerError(playlistManager.getCurrentChannel()?.program_name ?: "", "player idle due to error")
        }
    }
    
    override fun onOptionMenuPressed(): Boolean {
        if (defaultTrackSelector == null || defaultTrackSelector?.currentMappedTrackInfo == null) return false
        val bottomSheetDialog = TrackSelectionDialog(this)
        val maxBitRate = if (connectionWatcher.isOverWifi) mPref.maxBitRateWifi else mPref.maxBitRateCellular
        bottomSheetDialog.init(defaultTrackSelector, maxBitRate)
        lifecycle.addObserver(bottomSheetDialog)
        bottomSheetDialog.setOnDismissListener {
            lifecycle.removeObserver(bottomSheetDialog)
            onTrackerDialogDismissed()
        }
        bottomSheetDialog.setOnCancelListener {
            lifecycle.removeObserver(bottomSheetDialog)
            onTrackerDialogDismissed()
        }
        bottomSheetDialog.show()
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        return true
    }
    
    protected open fun onTrackerDialogDismissed() {
        //hook for subclass to listen the dismiss event
    }
    
    override fun onSeekPosition(position: Int): Boolean {
        return false
    }
    
    override fun onShareButtonPressed(): Boolean {
        val info = playlistManager.getCurrentChannel()
        info?.let {
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/html"
            sharingIntent.putExtra(Intent.EXTRA_TEXT, it.video_share_url)
            startActivity(Intent.createChooser(sharingIntent, "Share via"))
            return true
        }
        return false
    }
    
    private fun getCurrentChannelInfo(): ChannelInfo? {
        return player?.currentMediaItem?.getChannelMetadata(player)
    }
    
    abstract fun setPlayerInPlayerView()
    
    override fun onCastSessionAvailable() {
        Log.i("CAST_T", "Cast Session available")
        updateStartPosition()
//        val savedSession = mPref.savedCastInfo
//        if(savedSession != null) {
//            Log.e("CAST_T", "Saved session id -> ${savedSession?.id}")
//            playlistManager.setPlaylist(savedSession)
//            mPref.savedCastInfo = null
//        }
        playlistManager.getCurrentChannel()?.let {
            if (player?.playbackState != Player.STATE_ENDED) {
                insertContentViewProgress(it, player?.currentPosition ?: -1)
            }
        }
        player?.stop()
        player = castPlayer
        setPlayerInPlayerView()
        playChannel(true)
    }
    
    override fun onCastSessionUnavailable() {
        updateStartPosition()
        playlistManager.getCurrentChannel()?.let {
            if (player?.playbackState != Player.STATE_ENDED) {
                insertContentViewProgress(it, player?.currentPosition ?: -1)
            }
        }
        player?.stop()
        player = exoPlayer
        setPlayerInPlayerView()
        playChannel(true)
    }
    
    var isDrmSessionException = false
    
    fun isCurrentContentDrm(): Boolean {
        playlistManager.getCurrentChannel()?.let { 
            return isDrmActiveForChannel(it)
        }
        return false
    }
    
    private inner class PlayerEventListener : Player.Listener {
        override fun onPlayerError(e: PlaybackException) {
            lifecycleScope.launch {
                playerEventHelper.setPingData(getPingData(player?.currentMediaItem))
                playerEventHelper.setPlayerEvent("player error", e.message, e.cause.toString(), e.errorCode)
            }
            e.printStackTrace()
            if (!isBehindLiveWindow(e)) {
                ToffeeAnalytics.logException(e)
                ToffeeAnalytics.logBreadCrumb("player error occurred")
            }
            playerErrorMessage = e.message ?: e.cause?.message ?: e.cause?.cause?.message
            
            if (!connectionWatcher.isOnline) {
                retryCounter = 0
                reloadCounter = 0
                fallbackCounter = 0
                val message = "Please check your internet and try again later."
                showToast(message, Toast.LENGTH_LONG)
            } else if (isBehindLiveWindow(e)) {
                clearStartPosition()
                reloadChannel()
            } else if (e.cause is DrmSessionException && reloadCounter < 2) {
                reloadCounter++
//                isDrmSessionException = true
                if (e.cause?.cause is IllegalArgumentException && e.cause?.cause?.message == "Failed to restore keys") {
                    lifecycleScope.launch {
                        ToffeeAnalytics.logBreadCrumb("Failed to restore key -> ${playlistManager.getCurrentChannel()?.id}, Reloading")
                        if (mPref.isDrmActive && mPref.isGlobalCidActive) {
                             drmLicenseRepo.deleteByChannelId(-1L)
                        } else {
                            playlistManager.getCurrentChannel()?.id?.let {
                                drmLicenseRepo.deleteByChannelId(it.toLong())
                            }
                        }
                        reloadChannel()
                    }
                } else {
                    ToffeeAnalytics.logBreadCrumb("Failed to restore key -> ${playlistManager.getCurrentChannel()?.id}, Reloading")
                    reloadChannel()
                }
            } else {
                val retryCount = if (mPref.retryCount <= 0) 5 else mPref.retryCount
                if (mPref.isRetryActive && retryCounter < retryCount) {
                    retryCounter++
                    reloadOnFailOver()
                } else if (mPref.isFallbackActive && fallbackCounter < retryCount) {
                    fallbackCounter++
                    val channelInfo = playlistManager.getCurrentChannel()
                    if (channelInfo?.isDrmActive != true && !channelInfo?.getDrmUrl(connectionWatcher.isOverCellular).isNullOrBlank() && !mPref.drmWidevineLicenseUrl.isNullOrBlank() && (!mPref.globalCidName.isNullOrBlank() || !channelInfo?.drmCid.isNullOrBlank())) {
                        playlistManager.getCurrentChannel()?.is_drm_active = 1
                    } else {
                        val hlsUrl = if (channelInfo?.urlTypeExt == PAYMENT && channelInfo.urlType == PLAY_IN_WEB_VIEW && mPref.isPaidUser) {
                            channelInfo.paidPlainHlsUrl
                        } else if (channelInfo?.urlTypeExt == NON_PAYMENT && (channelInfo.urlType == PLAY_IN_NATIVE_PLAYER || channelInfo.urlType == STINGRAY_CONTENT)) {
                            channelInfo.hlsLinks?.get(0)?.hls_url_mobile
                        } else null
                        hlsUrl?.let { playlistManager.getCurrentChannel()?.is_drm_active = 0 }
                    }
                    reloadOnFailOver()
                }
                else {
                    retryCounter = 0
                    reloadCounter = 0
                    fallbackCounter = 0
                    val message = "Something went wrong. Please try again later."
                    showToast(message, Toast.LENGTH_LONG)
                    ToffeeAnalytics.playerError(playlistManager.getCurrentChannel()?.program_name ?: "", playerErrorMessage ?: "")
                }
            }
            
            getCurrentChannelInfo()?.let { channelInfo ->
                if (!channelInfo.isLinear) {
                    insertContentViewProgress(channelInfo, player?.duration ?: -1)
                }
            }
        }
        
        private fun reloadOnFailOver() {
            lifecycleScope.launch {
                delay(mPref.retryWaitDuration.toLong())
                reloadChannel()
            }
        }
        
        private fun isBehindLiveWindow(e: PlaybackException): Boolean {
            return e.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW
        }
        
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
            observeNetworkChange()
            if (isPlaying) {
                if (reloadCounter > 1 || retryCounter > 1 || fallbackCounter > 1) {
                    ToffeeAnalytics.playerError(playlistManager.getCurrentChannel()?.program_name ?: "", playerErrorMessage ?: "", true)
                }
                retryCounter = 0
                reloadCounter = 0
                fallbackCounter = 0
                return
            }
            val channelInfo = getCurrentChannelInfo()
            if (channelInfo is ChannelInfo) {
                if ((player?.currentPosition ?: 0) > 0L) {
                    insertContentViewProgress(channelInfo, player?.currentPosition ?: -1)
                }
            }
        }
    }
    
    private inner class PlayerAnalyticsListener : AnalyticsListener {
        private var totalBytesInMB: Long = 0
        private var initialTimeStamp: Long = 0
        private var durationInMillis: Long = 0
        
        override fun onLoadCompleted(eventTime: EventTime, loadEventInfo: LoadEventInfo, mediaLoadData: MediaLoadData) {
            try {
                totalBytesInMB += loadEventInfo.bytesLoaded
                if (initialTimeStamp == 0L) {
                    PlayerPreference.getInstance().setInitialTime()
                    initialTimeStamp = System.currentTimeMillis()
                } else {
                    durationInMillis = System.currentTimeMillis() - initialTimeStamp
                }
                Log.i("PLAYER BYTES", "Event time " + durationInMillis / 1000 + " Bytes " + totalBytesInMB * 0.000001 + " MB")
            } catch (e: Exception) {
                ToffeeAnalytics.logBreadCrumb("Exception in PlayerAnalyticsListener")
            }
        }
        
        fun getTotalBytesInMB(): Double {
            return totalBytesInMB * 0.000001
        }
        
        val durationInSeconds: Long
            get() = durationInMillis / 1000
        
        fun resetData() {
            totalBytesInMB = 0
            durationInMillis = 0
            initialTimeStamp = 0
        }
        
        override fun onPlaybackStateChanged(eventTime: EventTime, state: Int) {
            when(state) {
                PlaybackState.STATE_BUFFERING -> playerEventHelper.setPlayerEvent("buffering")
                PlaybackState.STATE_CONNECTING -> playerEventHelper.setPlayerEvent("connecting")
                PlaybackState.STATE_PLAYING -> playerEventHelper.setPlayerEvent("playing")
                PlaybackState.STATE_PAUSED -> playerEventHelper.setPlayerEvent("paused")
                PlaybackState.STATE_ERROR -> playerEventHelper.setPlayerEvent("error occurred")
                PlaybackState.STATE_STOPPED -> playerEventHelper.setPlayerEvent("playing stopped")
            }
        }
        
//        override fun onPlayerErrorChanged(eventTime: EventTime, error: PlaybackException?) {
//            super.onPlayerErrorChanged(eventTime, error)
//            playerEventHelper.setPlayerEvent("player error changed", error?.message, error?.cause?.toString(), error?.errorCode)
//        }
        
        override fun onLoadCanceled(eventTime: EventTime, loadEventInfo: LoadEventInfo, mediaLoadData: MediaLoadData) {
            super.onLoadCanceled(eventTime, loadEventInfo, mediaLoadData)
            playerEventHelper.setPlayerEvent("load canceled")
        }
        
        override fun onLoadStarted(eventTime: EventTime, loadEventInfo: LoadEventInfo, mediaLoadData: MediaLoadData) {
            super.onLoadStarted(eventTime, loadEventInfo, mediaLoadData)
//            Log.i(PLAYER_EVENT, "loading started")
        }
        
//        override fun onIsLoadingChanged(eventTime: EventTime, isLoading: Boolean) {
//            super.onIsLoadingChanged(eventTime, isLoading)
//            if (isLoading) {
//                playerEventHelper.setPlayerEvent("loading...")
//            }
//        }
        
        override fun onLoadError(eventTime: EventTime, loadEventInfo: LoadEventInfo, mediaLoadData: MediaLoadData, error: IOException, wasCanceled: Boolean) {
            super.onLoadError(eventTime, loadEventInfo, mediaLoadData, error, wasCanceled)
            lifecycleScope.launch {
                playerEventHelper.setPingData(getPingData(player?.currentMediaItem))
                playerEventHelper.setPlayerEvent("Load error", error.message, error.cause.toString())
            }
        }
        
        override fun onAudioSinkError(eventTime: EventTime, audioSinkError: java.lang.Exception) {
            super.onAudioSinkError(eventTime, audioSinkError)
            playerEventHelper.setPlayerEvent("Audio sink error", audioSinkError.message, audioSinkError.cause.toString())
        }
        
        override fun onAudioCodecError(eventTime: EventTime, audioCodecError: java.lang.Exception) {
            super.onAudioCodecError(eventTime, audioCodecError)
            playerEventHelper.setPlayerEvent("Audio codec error", audioCodecError.message, audioCodecError.cause.toString())
        }
        
        override fun onDownstreamFormatChanged(eventTime: EventTime, mediaLoadData: MediaLoadData) {
            super.onDownstreamFormatChanged(eventTime, mediaLoadData)
            val format = mediaLoadData.trackFormat
            format?.let {
                val bitrate = Utils.readableFileSize(it.bitrate.toLong())
                val profile = "${it.width}x${it.height}"
                val mimeType = it.containerMimeType
                val codec = it.codecs
                val profileTitle = if (it.width == -1 || it.height == -1) "audio" else "video"
                Log.i(PLAYER_EVENT_TAG, "onDownstreamFormatChanged: ${profileTitle}_profile: $profile, bitrate: $bitrate, mime_type: $mimeType, coded: $codec")
                playerEventHelper.setPlayerEvent("${profileTitle}_profile: $profile, bitrate: $bitrate, mime_type: $mimeType, coded: $codec")
            }
        }
        
        override fun onVideoCodecError(eventTime: EventTime, videoCodecError: java.lang.Exception) {
            super.onVideoCodecError(eventTime, videoCodecError)
            playerEventHelper.setPlayerEvent("video codec error", videoCodecError.message, videoCodecError.cause.toString())
        }
        
        override fun onDrmSessionManagerError(eventTime: EventTime, error: java.lang.Exception) {
            super.onDrmSessionManagerError(eventTime, error)
            ToffeeAnalytics.logBreadCrumb("DRM session manager error: ${error.message}, ${error.cause.toString()}")
            if (isCurrentContentDrm()) {
                playerEventHelper.setPlayerEvent("DRM session manager error", error.message, error.cause.toString())
            }
        }
        
        override fun onDrmKeysRestored(eventTime: EventTime) {
            super.onDrmKeysRestored(eventTime)
            if (isCurrentContentDrm()) {
                playerEventHelper.setPlayerEvent("DRM keys restored")
            }
        }
    
        override fun onDrmKeysRemoved(eventTime: EventTime) {
            super.onDrmKeysRemoved(eventTime)
            if (isCurrentContentDrm()) {
                playerEventHelper.setPlayerEvent("DRM key removed")
            }
        }
    
        override fun onDrmSessionReleased(eventTime: EventTime) {
            super.onDrmSessionReleased(eventTime)
            if (isCurrentContentDrm()) {
                playerEventHelper.setPlayerEvent("DRM session released")
            }
        }
    
        override fun onPlayerReleased(eventTime: EventTime) {
            super.onPlayerReleased(eventTime)
//            playerEventHelper.setPlayerEvent("player released")
        }
        
        override fun onPositionDiscontinuity(eventTime: EventTime, oldPosition: PositionInfo, newPosition: PositionInfo, reason: Int) {
            super.onPositionDiscontinuity(eventTime, oldPosition, newPosition, reason)
            if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                playerEventHelper.setPlayerEvent("seek started")
            }
        }
        
        override fun onMediaMetadataChanged(eventTime: EventTime, mediaMetadata: MediaMetadata) {
            super.onMediaMetadataChanged(eventTime, mediaMetadata)
            playerEventHelper.setPlayerEvent("metadata/profile changed")
        }
        
        override fun onPlaylistMetadataChanged(eventTime: EventTime, playlistMetadata: MediaMetadata) {
            super.onPlaylistMetadataChanged(eventTime, playlistMetadata)
            playerEventHelper.setPlayerEvent("playlist metadata changed")
        }
        
        override fun onVolumeChanged(eventTime: EventTime, volume: Float) {
            super.onVolumeChanged(eventTime, volume)
            playerEventHelper.setPlayerEvent("volume changed to: $volume")
        }
        
        override fun onDeviceVolumeChanged(eventTime: EventTime, volume: Int, muted: Boolean) {
            super.onDeviceVolumeChanged(eventTime, volume, muted)
            if (muted && volume < 1) {
                playerEventHelper.setPlayerEvent("volume muted")
            } else {
                playerEventHelper.setPlayerEvent("volume changed to: $volume")
            }
        }
        
        override fun onDroppedVideoFrames(eventTime: EventTime, droppedFrames: Int, elapsedMs: Long) {
            super.onDroppedVideoFrames(eventTime, droppedFrames, elapsedMs)
            playerEventHelper.setPlayerEvent("$droppedFrames frames dropped")
        }
    }
    
    private fun onAdEventListener(it: AdEvent?) {
        when (it?.type) {
            LOG -> {
                val errorMessage = it.adData["errorMessage"] ?: "Unknown error occurred."
                playerEventHelper.setAdData(it.ad, LOG.name, errorMessage)
                ConvivaHelper.onAdFailed(errorMessage, it.ad)
                playerEventHelper.setAdData(null, null, isReset = true)
            }
            AD_BUFFERING -> {
                playerEventHelper.setAdData(it.ad, AD_BUFFERING.name)
                ConvivaHelper.onAdBuffering(it.ad)
            }
            STARTED -> {
                playerEventHelper.setAdData(it.ad, STARTED.name)
                ConvivaHelper.onAdStarted(it.ad)
            }
            PAUSED -> {
                playerEventHelper.setAdData(it.ad, PAUSED.name)
                ConvivaHelper.onAdPaused(it.ad)
            }
            RESUMED -> {
                playerEventHelper.setAdData(it.ad, RESUMED.name)
                ConvivaHelper.onAdResumed(it.ad)
            }
            AD_PROGRESS -> {
                playerEventHelper.setAdData(it.ad, AD_PROGRESS.name)
                ConvivaHelper.onAdProgress(it.ad)
            }
            SKIPPED -> {
                playerEventHelper.setAdData(it.ad, SKIPPED.name)
                ConvivaHelper.onAdSkipped()
            }
            COMPLETED -> {
                playerEventHelper.setAdData(it.ad, COMPLETED.name)
                ConvivaHelper.onAdEnded()
            }
            CONTENT_PAUSE_REQUESTED -> {
                playerEventHelper.setAdData(it.ad, CONTENT_PAUSE_REQUESTED.name)
                ConvivaHelper.onAdBreakStarted(it.ad)
            }
            CONTENT_RESUME_REQUESTED -> {
                playerEventHelper.setAdData(it.ad, CONTENT_RESUME_REQUESTED.name)
                ConvivaHelper.onAdBreakEnded()
            }
            ALL_ADS_COMPLETED -> {
                playerEventHelper.setAdData(it.ad, ALL_ADS_COMPLETED.name)
                ConvivaHelper.onAllAdEnded()
                playerEventHelper.setAdData(null, null, isReset = true)
            }
            else -> {
//                val errorMessage = it?.adData?.get("errorMessage")?.let { ", ErrorMessage-> $it" } ?: ""
//                Log.i("ADs_", "adEventListener: EventType-> ${it?.type}$errorMessage")
            }
        }
    }
    
    private fun onAdErrorListener(it: AdErrorEvent?) {
//        val errorMessage = it?.error?.message?.let { ", ErrorMessage-> $it" } ?: "Unknown error occurred."
//        Log.i("ADs_", "AdErrorEvent: ErrorMessage-> $errorMessage")
        playerEventHelper.setAdData(null, "Ad error", it?.error?.message ?: "Unknown error occurred.")
        ConvivaHelper.onAdError(it)
        playerEventHelper.setAdData(null, null, isReset = true)
    }
}