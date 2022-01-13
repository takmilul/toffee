package com.banglalink.toffee.ui.player

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.analytics.HeartBeatManager
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.apiservice.DrmTokenService
import com.banglalink.toffee.data.database.entities.ContentViewProgress
import com.banglalink.toffee.data.database.entities.ContinueWatchingItem
import com.banglalink.toffee.data.database.entities.DrmLicenseEntity
import com.banglalink.toffee.data.exception.ContentExpiredException
import com.banglalink.toffee.data.repository.ContentViewPorgressRepsitory
import com.banglalink.toffee.data.repository.ContinueWatchingRepository
import com.banglalink.toffee.data.repository.DrmLicenseRepository
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.PlayerPreference
import com.banglalink.toffee.di.DnsHttpClient
import com.banglalink.toffee.di.ToffeeHeader
import com.banglalink.toffee.extension.getChannelMetadata
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.listeners.OnPlayerControllerChangedListener
import com.banglalink.toffee.listeners.PlaylistListener
import com.banglalink.toffee.model.Channel
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.receiver.ConnectionWatcher
import com.banglalink.toffee.ui.common.BaseAppCompatActivity
import com.banglalink.toffee.ui.home.*
import com.banglalink.toffee.usecase.SendDrmFallbackEvent
import com.banglalink.toffee.util.ConvivaFactory
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.getError
import com.conviva.sdk.ConvivaAdAnalytics
import com.conviva.sdk.ConvivaSdkConstants
import com.conviva.sdk.ConvivaVideoAnalytics
import com.google.ads.interactivemedia.v3.api.AdErrorEvent
import com.google.ads.interactivemedia.v3.api.AdEvent
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventType.*
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ExoPlayer.Builder
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
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.ads.AdsLoader
import com.google.android.exoplayer2.source.dash.DashUtil
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.Parameters
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.ParametersBuilder
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.*
import javax.inject.Inject
import kotlin.math.max

@AndroidEntryPoint
abstract class PlayerPageActivity :
    BaseAppCompatActivity(),
    OnPlayerControllerChangedListener,
    Player.Listener,
    PlaylistListener,
    AnalyticsListener,
    SessionAvailabilityListener
{
    private var startWindow = 0
    private var playCounter: Int = -1
    private var startAutoPlay = false
    private var reloadCounter: Int = 0
    private var startPosition: Long = 0
    protected var player: Player? = null
    private var isAdBreakStarted = false
    private var adsLoader: AdsLoader? = null
    private var exoPlayer: ExoPlayer? = null
    private var castPlayer: CastPlayer? = null
    protected var castContext: CastContext? = null
    private var currentlyPlayingVastUrl: String = ""
    @Inject lateinit var drmTokenApi: DrmTokenService
    private var defaultCookieManager = CookieManager()
    @Inject lateinit var heartBeatManager: HeartBeatManager
    private var trackSelectorParameters: Parameters? = null
    @ToffeeHeader @Inject lateinit var toffeeHeader: String
    @Inject lateinit var connectionWatcher: ConnectionWatcher
    @Inject lateinit var drmLicenseRepo: DrmLicenseRepository
    protected var convivaAdAnalytics: ConvivaAdAnalytics? = null
    private var lastSeenTrackGroupArray: TrackGroupArray? = null
    @Inject lateinit var drmFallbackService: SendDrmFallbackEvent
    private var defaultTrackSelector: DefaultTrackSelector? = null
    @DnsHttpClient @Inject lateinit var dnsHttpClient: OkHttpClient
    protected var convivaVideoAnalytics: ConvivaVideoAnalytics? = null
    @Inject lateinit var contentViewRepo: ContentViewPorgressRepsitory
    private var httpDataSourceFactory: OkHttpDataSource.Factory? = null
    private var playerAnalyticsListener: PlayerAnalyticsListener? = null
    @Inject lateinit var continueWatchingRepo: ContinueWatchingRepository
    private val homeViewModel by viewModels<HomeViewModel>()
    private val playerViewModel by viewModels<PlayerViewModel>()
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

        if(mPref.isCastEnabled) {
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
        }
        else {
            val builder = ParametersBuilder(this)
            trackSelectorParameters = builder.build()
            clearStartPosition()
        }
        heartBeatManager.heartBeatEventLiveData.observe(this) {
                //In each heartbeat we are checking channel's expire date. Seriously??
                val channelInfo = playlistManager.getCurrentChannel()
                if (channelInfo?.isExpired(mPref.getSystemTime()) == true) {
                    ToffeeAnalytics.logException(ContentExpiredException(0, "serverDate: ${mPref.getSystemTime()}, deviceDate: ${Date()}, expireTime: ${channelInfo.expireTime}"))
//                    player?.stop(true)
//                    onContentExpired() //content is expired. Notify the subclass
                }
                playerAnalyticsListener?.let {
                    //In every heartbeat event we are sending bandwitdh data to Pubsub
                    Log.i("PLAYER BYTES", "Flushing to pubsub")
                    playerViewModel.reportBandWidthFromPlayerPref(
                        it.durationInSeconds,
                        it.getTotalBytesInMB()
                    )
                    playerAnalyticsListener?.resetData()
                }
            }

        adsLoader = ImaAdsLoader
            .Builder(this)
            .setAdEventListener {
                onAdEventListener(it)
            }
            .setAdErrorListener { 
                onAdErrorListener(it)
            }
            .build()
    }
    
    abstract val playlistManager: PlaylistManager
    abstract fun getPlayerView(): StyledPlayerView

    protected open fun onContentExpired() {
        //hook for subclass
    }

    public override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
        }
    }

    public override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer()
        }
    }

    public override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    public override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
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
        if(player?.isPlaying == true) {
            playlistManager.getCurrentChannel()?.viewProgress = player?.currentPosition ?: 0
        }
        outState.putInt(KEY_WINDOW, startWindow)
        outState.putLong(KEY_POSITION, startPosition)
        outState.putInt(KEY_PLAY_COUNTER, playCounter)
        outState.putBoolean(KEY_AUTO_PLAY, startAutoPlay)
        outState.putString(KEY_VAST_URL, currentlyPlayingVastUrl)
        outState.putBundle(KEY_TRACK_SELECTOR_PARAMETERS, trackSelectorParameters?.toBundle())
    }

    private fun initializePlayer() {
        reloadCounter = 0
        initializeLocalPlayer()
        initializeRemotePlayer()
        player = if(castPlayer?.isCastSessionAvailable == true) castPlayer else exoPlayer

        player?.let { pl ->
            if(pl is CastPlayer && playlistManager.getCurrentChannel() == null) {
                val ci = pl.currentMediaItem?.getChannelMetadata(pl)
                ci?.viewProgress = pl.currentPosition
                if(ci != null) playlistManager.setPlaylist(ci)
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
            lastSeenTrackGroupArray = null
            playerAnalyticsListener = PlayerAnalyticsListener()

            httpDataSourceFactory = OkHttpDataSource.Factory(dnsHttpClient
//                    .newBuilder()
//                    .addNetworkInterceptor(
//                        HttpLoggingInterceptor()
//                            .setLevel(HttpLoggingInterceptor.Level.HEADERS)
//                    )
//                    .build()
                )
                .setUserAgent(toffeeHeader)
//                .setDefaultRequestProperties(mapOf("TOFFEE-SESSION-TOKEN" to mPref.getHeaderSessionToken()!!))
            
            val mediaSourceFactory = DefaultMediaSourceFactory(httpDataSourceFactory!!)
                .setAdsLoaderProvider{
                    adsLoader
                }
                .setDrmSessionManagerProvider(this::getDrmSessionManager)
                .setAdViewProvider(getPlayerView())
            
            exoPlayer = Builder(this)
                .setMediaSourceFactory(mediaSourceFactory)
                .setTrackSelector(defaultTrackSelector!!)
                .setLoadControl(DefaultLoadControl.Builder().setBufferDurationsMs(60_000, 120_000, 2_500, 5_000).build())
                .build().apply {
                    addAnalyticsListener(playerAnalyticsListener!!)
                    addListener(playerEventListener)
                    playWhenReady = false
                    if (BuildConfig.DEBUG) {
                        addAnalyticsListener(EventLogger(defaultTrackSelector))
                    }
                }
            adsLoader?.setPlayer(exoPlayer)
            convivaVideoAnalytics?.setPlayer(exoPlayer)
        }
    }

    private fun isDrmActiveForChannel(channelInfo: ChannelInfo) =
        cPref.isDrmModuleAvailable == CommonPreference.DRM_AVAILABLE &&
        mPref.isDrmActive &&
        channelInfo.isDrmActive &&
//        !channelInfo.drmCid.isNullOrBlank() &&
        (!channelInfo.drmDashUrl.isNullOrBlank() || !channelInfo.drmDashUrlExt?.get(0)?.urlList()?.randomOrNull().isNullOrEmpty() || !channelInfo.drmDashUrlExtSd?.get(0)?.urlList()?.randomOrNull().isNullOrEmpty()) &&
        !mPref.drmWidevineLicenseUrl.isNullOrBlank() //&&
//        player is SimpleExoPlayer

    private fun getDrmSessionManager(mediaItem: MediaItem): DrmSessionManager {
        val channelInfo = mediaItem.getChannelMetadata(player) ?: return DrmSessionManager.DRM_UNSUPPORTED

        val isDrmActive = isDrmActiveForChannel(channelInfo)

        if(!isDrmActive) {
            return DrmSessionManager.DRM_UNSUPPORTED
        }
        return DefaultDrmSessionManager
            .Builder()
            .setMultiSession(false)
            .build(ToffeeMediaDrmCallback2(
                mPref.drmWidevineLicenseUrl!!, httpDataSourceFactory!!, drmTokenApi, channelInfo.drmCid!!
            )).apply {
                mediaItem.localConfiguration?.drmConfiguration?.keySetId?.let {
                    Log.i("DRM_T", "Using offline key")
                    setMode(DefaultDrmSessionManager.MODE_PLAYBACK, it)
                }
            }
    }

    private val castSessionListener = object: SessionManagerListener<CastSession> {
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
                if(cInfo != null && playlistManager.getCurrentChannel() == null) {
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
            castPlayer = CastPlayer(it, ToffeeMediaItemConverter(mPref, connectionWatcher.isOverWifi)).apply {
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
        reloadCounter = 0
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
                val pf = it::class.java.getDeclaredField("statusListener")
                pf.isAccessible = true

                val obj = pf.get(it)
                if(obj is SessionManagerListener<*>) {
                    castContext?.sessionManager?.removeSessionManagerListener(obj , CastSession::class.java)
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

    private var totalBytes = 0L
    override fun onLoadCompleted(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData
    ) {
        totalBytes += loadEventInfo.bytesLoaded
        Log.i("PLAYER BYTES",""+totalBytes/1024+" KB")
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
        playChannel(false)
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
        }
        else {
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
        val channelId = if(mPref.isGlobalCidActive) -1 else channelInfo.id.toLong()

        val existingLicense = drmLicenseRepo.getByChannelId(channelId)
        Log.i("DRM_T", "Existing -> $existingLicense")
        if(existingLicense != null && !isLicenseAlmostExpired(existingLicense.expiryTime)) {
            Log.i("DRM_T", "Using existing license")
            return existingLicense.license
        }
        else if(existingLicense != null && !isLicenseExpired(existingLicense.expiryTime)) {
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
            val drmCid = if(mPref.isGlobalCidActive) mPref.globalCidName else channelInfo.drmCid
            val token = try{
                drmTokenApi.execute(drmCid!!, 2_592_000 /* 30 days*/) } catch (e:Exception){
                val error = getError(e)
                ToffeeAnalytics.logEvent(
                    ToffeeEvents.EXCEPTION,
                    bundleOf(
                        "api_name" to ApiNames.GET_DRM_TOKEN,
                        "browser_screen" to "Player Page",
                        "error_code" to error.code,
                        "error_description" to error.msg)
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
                    mPref.drmWidevineLicenseUrl!!,
                    false,
                    offlineDataSourceFactory,
                    DrmSessionEventListener.EventDispatcher()
                )

            val dataSource = httpDataSourceFactory!!.createDataSource()
            val dashManifest = DashUtil.loadManifest(dataSource, Uri.parse(channelInfo.drmDashUrl))
            val drmInitData =
                DashUtil.loadFormatWithDrmInitData(dataSource, dashManifest.getPeriod(0))
                    ?: run{
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
                if(mPref.isGlobalCidActive) -1 else channelInfo.id.toLong(),
                drmCid!!,
                licenseData,
                licenseExpiration
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
        if(player is CastPlayer) {
            return MediaItem.Builder().apply {
                setMimeType(MimeTypes.APPLICATION_MPD)
                setUri(channelInfo.drmDashUrl)
                setTag(channelInfo)
                setDrmConfiguration(MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID).build())
            }.build()
        }
        val license = getLicense(channelInfo)
        
        val isDataConnection = connectionWatcher.isOverCellular
        val drmUrl = channelInfo.getDrmUrl(isDataConnection) ?: return null
        convivaVideoAnalytics?.setContentInfo(mapOf(
            ConvivaSdkConstants.STREAM_URL to drmUrl
        ))
        return MediaItem.Builder().apply {
//            showToast("Playing DRM -> ${if(license == null) "Requesting new license" else "Using cached license"}\n${channelInfo.drmDashUrl}")
            setMimeType(MimeTypes.APPLICATION_MPD)
            setUri(drmUrl)
            setTag(channelInfo)
            setDrmConfiguration(
                MediaItem
                    .DrmConfiguration
                    .Builder(C.WIDEVINE_UUID)
                    .setKeySetId(license)
                    .build())
        }.build()
    }

    private fun getHlsMediaItem(channelInfo: ChannelInfo, isWifiConnected: Boolean): MediaItem? {
        val hlsUrl = if (channelInfo.urlTypeExt == PAYMENT && channelInfo.urlType == PLAY_IN_WEB_VIEW && mPref.isPaidUser) {
            channelInfo.paidPlainHlsUrl
        } else if (channelInfo.urlTypeExt == NON_PAYMENT && channelInfo.urlType == PLAY_IN_NATIVE_PLAYER) {
            channelInfo.hlsLinks?.get(0)?.hls_url_mobile
        } else {
            null
        }
        hlsUrl ?: return null
        
        val uri = if (channelInfo.isBucketUrl || channelInfo.isStingray) {
            hlsUrl
        } else {
            Channel.createChannel(channelInfo.program_name, hlsUrl).getContentUri(mPref, isWifiConnected)
        }
        convivaVideoAnalytics?.setContentInfo(mapOf(
            ConvivaSdkConstants.STREAM_URL to uri
        ))
        return MediaItem.Builder().apply {
            if (channelInfo.isStingray) {
                httpDataSourceFactory?.setUserAgent("")
            } else {
                httpDataSourceFactory?.setDefaultRequestProperties(mapOf("TOFFEE-SESSION-TOKEN" to mPref.getHeaderSessionToken()!!))
            }
            if (!channelInfo.isBucketUrl) setMimeType(MimeTypes.APPLICATION_M3U8)
            setUri(uri)
            setTag(channelInfo)
        }.build()
    }

    abstract fun maximizePlayer()
    private var playChannelJob: Job? = null
    
    private fun playChannel(isReload: Boolean) {
        playChannelJob?.cancel()
        Log.i("DRM_T", "New play request")
        playChannelJob = playChannelImpl(isReload)
    }

    private fun playChannelImpl(isReload: Boolean) = lifecycleScope.launch {
        maximizePlayer()
        val isWifiConnected = connectionWatcher.isOverWifi
        if (!isWifiConnected && mPref.watchOnlyWifi()) {
            showPlayerError(true)
            return@launch
        }
//        val oldChannelInfo = player?.currentMediaItem?.getChannelMetadata(player)
        val channelInfo = playlistManager.getCurrentChannel() ?: run{
            showPlayerError()
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
        var mediaItem = if(isDrmActive) {
            getDrmMediaItem(channelInfo) //?: getHlsMediaItem(channelInfo, isWifiConnected)
        } else {
            getHlsMediaItem(channelInfo, isWifiConnected)
        } ?: run {
            showPlayerError()
            ToffeeAnalytics.logException(NullPointerException("Channel url is null for id -> ${channelInfo.id}, name -> ${channelInfo.program_name}"))
            return@launch
        }
        
        if (!isReload && player is ExoPlayer) playCounter = ++playCounter % mPref.vastFrequency
        homeViewModel.vastTagsMutableLiveData.value?.randomOrNull()?.let { tag ->
            val shouldPlayAd = mPref.isVastActive && playCounter == 0 && !channelInfo.isLive && !channelInfo.isStingray && channelInfo.urlTypeExt != PAYMENT
            val vastTag = if (isReload) currentlyPlayingVastUrl else tag.url
            if (shouldPlayAd && vastTag.isNotBlank()) {
                mediaItem = mediaItem.buildUpon()
                        .setAdsConfiguration(MediaItem.AdsConfiguration.Builder(Uri.parse(vastTag)).build())
                        .build()
                if (!isReload) currentlyPlayingVastUrl = tag.url
                val adInfo = mapOf(
                    ConvivaSdkConstants.AD_TAG_URL to vastTag,
                    ConvivaSdkConstants.AD_PLAYER to ConvivaSdkConstants.AdPlayer.CONTENT.toString()
                )
                convivaAdAnalytics?.setAdListener(adsLoader, adInfo)
            }
        }
        
        player?.let {
            val oldChannelInfo = getCurrentChannelInfo()
            oldChannelInfo?.let { oldInfo ->
                if(oldInfo.id != channelInfo.id && it.playbackState != Player.STATE_ENDED) {
                    insertContentViewProgress(oldInfo, it.currentPosition)
                }
            }
            if(!channelInfo.fcmEventName.isNullOrBlank()){
                if(channelInfo.isFcmEventActive){
                    for (event in channelInfo.fcmEventName!!.split(",")) {
                        ToffeeAnalytics.logEvent(event)
                    }
                }
            }
            heartBeatManager.triggerEventViewingContentStart(channelInfo.id.toInt(), channelInfo.type ?: "VOD")
            it.playWhenReady = !isReload || it.playWhenReady

            if (isReload) { //We need to start where we left off for VODs
                if(channelInfo.viewProgress > 0L) {
                    startPosition = if(channelInfo.viewProgressPercent() >= 990) {
                        C.TIME_UNSET
                    } else {
                        channelInfo.viewProgress
                    }
                }
                val haveStartPosition = startWindow != C.INDEX_UNSET
                if (haveStartPosition && !channelInfo.isLive) {
                    if(it is ExoPlayer) {
//                        getPlayerView().adViewGroup.removeAllViews()
                        it.setMediaItem(mediaItem, false)
                        //                    player.prepare(mediaSource, false, false);
                    } else if(it is CastPlayer){
                        val newMediaItem = if(isDrmActive) {
                            val drmToken = try{
                                drmTokenApi.execute(channelInfo.drmCid!!)
                            } catch (ex: Exception) {
                                null
                            } ?: return@launch
                            mediaItem.buildUpon()
                                .setDrmConfiguration(mediaItem
                                    .localConfiguration
                                    ?.drmConfiguration
                                    ?.buildUpon()
                                    ?.apply {
                                        setLicenseUri(mPref.drmWidevineLicenseUrl!!)
                                        setMultiSession(false)
                                        setForceDefaultLicenseUri(false)
                                        setLicenseRequestHeaders(mapOf("pallycon-customdata-v2" to drmToken)) }
                                    ?.build())
                                .build()
                        } else {
                            mediaItem
                        }
                        it.setMediaItem(newMediaItem, startPosition)
                    }
                    it.prepare()
                    it.playWhenReady = true
                    it.seekTo(startWindow, startPosition) //we seek to where we left for VODs
//                    it.prepare()
                    return@launch
                }
            }
            startPosition = C.TIME_UNSET
            if(channelInfo.viewProgress > 0L) {
                startPosition = if(channelInfo.viewProgressPercent() >= 990) {
                    C.TIME_UNSET
                } else {
                    channelInfo.viewProgress
                }
            }
            if(it is ExoPlayer) {
                getPlayerView().adViewGroup.removeAllViews()
                it.setMediaItem(mediaItem, startPosition)
                it.prepare()
            } else if(it is CastPlayer) {
                val newMediaItem = if(isDrmActive) {
                    val drmToken = try{
                        drmTokenApi.execute(channelInfo.drmCid!!)
                    } catch (ex: Exception) {
                        null
                    } ?: return@launch
                    mediaItem.buildUpon()
                        .setDrmConfiguration(mediaItem
                            .localConfiguration
                            ?.drmConfiguration
                            ?.buildUpon()
                            ?.apply {
                                setLicenseUri(mPref.drmWidevineLicenseUrl!!)
                                setMultiSession(false)
                                setForceDefaultLicenseUri(false)
                                setLicenseRequestHeaders(mapOf("pallycon-customdata-v2" to drmToken)) }
                            ?.build())
                        .build()
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

    private fun showPlayerError(showMessage: Boolean = false) {
        player?.stop()
        player?.clearMediaItems()
        getPlayerView().adViewGroup.removeAllViews()
        if(showMessage) {
            channelCannotBePlayedDueToSettings() //notify hook/subclass
        }
        maximizePlayer()
        heartBeatManager.triggerEventViewingContentStop()
    }

    //This will be called due to session token change while playing content or after init of player
    protected fun reloadChannel() {
        val channelInfo = playlistManager.getCurrentChannel()
        if (channelInfo?.isExpired(mPref.getSystemTime()) == true) {
            ToffeeAnalytics.logException(ContentExpiredException(0, "serverDate: ${mPref.getSystemTime()}, deviceDate: ${Date()}, expireTime: ${channelInfo.expireTime}"))
            //channel is expired. Stop the player and notify hook/subclass
//            player?.stop(true)
//            onContentExpired()
//            return
        }
        if (channelInfo != null) {
            playChannel(true)
        }
    }

    private fun insertContentViewProgress(channelInfo: ChannelInfo, progress: Long) {
        lifecycleScope.launch {
            Log.i("PLAYBACK_STATE", "Saving state - ${channelInfo.id} -> $progress")
            if(!channelInfo.isLive && progress > 0L) {
                channelInfo.viewProgress = progress
                contentViewRepo.insert(
                    ContentViewProgress(
                        customerId = mPref.customerId,
                        contentId = channelInfo.id.toLong(),
                        progress = progress
                    )
                )
                Log.i("TOFFEE", "Category - ${channelInfo.categoryId}")
                if(channelInfo.categoryId == 1 && channelInfo.viewProgressPercent() < 970) {
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
        if(isVideoPortrait()) {
            return true
        }
        val isPortrait = resources.configuration.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        requestedOrientation = if (isPortrait) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        return true
    }

    override fun onPlayerIdleDueToError() {
        if (player?.playWhenReady == true) {
            ToffeeAnalytics.logForcePlay()
            reloadChannel()
        }
    }

    override fun onOptionMenuPressed(): Boolean {
        if (defaultTrackSelector == null || defaultTrackSelector?.currentMappedTrackInfo == null) return false
        val bottomSheetDialog = TrackSelectionDialog(this)
        bottomSheetDialog.init(defaultTrackSelector)
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

    private inner class PlayerEventListener : Player.Listener {
        override fun onPlayerError(e: PlaybackException) {
            e.printStackTrace()
            ToffeeAnalytics.logException(e)
            if (isBehindLiveWindow(e)) {
                clearStartPosition()
                reloadChannel()
            }

//            if(e.cause?.cause?.cause is ToffeeMediaDrmException) {
//                playlistManager.getCurrentChannel()?.is_drm_active = 0
//                reloadChannel()
//            }
            if(e.cause is DrmSessionException && reloadCounter < 3) {
                if (e.cause?.cause is IllegalArgumentException && e.cause?.cause?.message == "Failed to restore keys") {
                    lifecycleScope.launch {
                        ToffeeAnalytics.logBreadCrumb("Failed to restore key -> ${playlistManager.getCurrentChannel()?.id}, Reloading")
                        if (mPref.isDrmActive) {
                            drmLicenseRepo.deleteByChannelId(-1L)
                            reloadChannel()
                        } else {
                            playlistManager.getCurrentChannel()?.id?.let {
                                drmLicenseRepo.deleteByChannelId(it.toLong())
                                reloadChannel()
                            }
                        }
                    }
                } else {
                    ToffeeAnalytics.logBreadCrumb("Failed to restore key -> ${playlistManager.getCurrentChannel()?.id}, Reloading")
                    reloadChannel()
                }
                reloadCounter++
            }
            
            getCurrentChannelInfo()?.let { cinfo->
                if(!cinfo.isLive) {
                    insertContentViewProgress(cinfo, player?.duration ?: -1)
                }
            }
        }

        override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {
            if (trackGroups !== lastSeenTrackGroupArray) {
                lastSeenTrackGroupArray = trackGroups
            }
        }

        private fun isBehindLiveWindow(e: PlaybackException): Boolean {
            return e.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            if(isPlaying) {
                reloadCounter = 0
                return
            }
            val channelInfo = getCurrentChannelInfo()
            if(channelInfo is ChannelInfo) {
                if(player?.currentPosition ?: 0 > 0L) {
                    insertContentViewProgress(channelInfo, player?.currentPosition ?: -1)
                }
            }
        }
    }

    private fun getCurrentChannelInfo(): ChannelInfo? {
        return player?.currentMediaItem?.getChannelMetadata(player)
    }

    abstract fun resetPlayer()

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
            if(player?.playbackState != Player.STATE_ENDED) {
                insertContentViewProgress(it, player?.currentPosition ?: -1)
            }
        }
        player?.stop()
        player = castPlayer
        resetPlayer()
        playChannel(true)
    }

    override fun onCastSessionUnavailable() {
        updateStartPosition()
        playlistManager.getCurrentChannel()?.let {
            if(player?.playbackState != Player.STATE_ENDED) {
                insertContentViewProgress(it, player?.currentPosition ?: -1)
            }
        }
        player?.stop()
        player = exoPlayer
        resetPlayer()
        playChannel(true)
    }

    private class PlayerAnalyticsListener : AnalyticsListener {
        private var totalBytesInMB: Long = 0
        private var initialTimeStamp: Long = 0
        private var durationInMillis: Long = 0

        override fun onLoadCompleted(
            eventTime: EventTime,
            loadEventInfo: LoadEventInfo,
            mediaLoadData: MediaLoadData
        ) {
            try {
                totalBytesInMB += loadEventInfo.bytesLoaded
                if (initialTimeStamp == 0L) {
                    PlayerPreference.getInstance().setInitialTime()
                    initialTimeStamp = System.currentTimeMillis()
                } else {
                    durationInMillis = System.currentTimeMillis() - initialTimeStamp
                }
                Log.i(
                    "PLAYER BYTES",
                    "Event time " + durationInMillis / 1000 + " Bytes " + totalBytesInMB * 0.000001 + " MB"
                )
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
    }
    
    private fun onAdEventListener(it: AdEvent?) {
        when (it?.type) {
            LOG -> {
                if (!isAdBreakStarted) {
                    isAdBreakStarted = true
                    convivaVideoAnalytics?.reportAdBreakStarted(ConvivaSdkConstants.AdPlayer.CONTENT, ConvivaSdkConstants.AdType.CLIENT_SIDE,
                        ConvivaFactory.getConvivaAdMetadata(it.ad))
                    val message = it.adData["errorMessage"]
                    message?.let { msg->
                        convivaAdAnalytics?.reportAdFailed(msg, ConvivaFactory.getConvivaAdMetadata(it.ad))
                    }
                }
            }
            LOADED -> {
                if (!isAdBreakStarted) {
                    isAdBreakStarted = true
                    convivaVideoAnalytics?.reportAdBreakStarted(ConvivaSdkConstants.AdPlayer.CONTENT, ConvivaSdkConstants.AdType.CLIENT_SIDE,
                        ConvivaFactory.getConvivaAdMetadata(it.ad))
                }
                convivaAdAnalytics?.reportAdLoaded(ConvivaFactory.getConvivaAdMetadata(it.ad))
            }
            STARTED -> {
                convivaAdAnalytics?.reportAdStarted(ConvivaFactory.getConvivaAdMetadata(it.ad))
//                convivaAdAnalytics?.reportAdMetric(ConvivaSdkConstants.PLAYBACK.BITRATE, it.ad?.vastMediaBitrate)
            }
            SKIPPED -> {
                convivaAdAnalytics?.reportAdSkipped()
            }
            COMPLETED -> {
                convivaAdAnalytics?.reportAdEnded()
            }
            ALL_ADS_COMPLETED -> {
                convivaVideoAnalytics?.reportAdBreakEnded()
                isAdBreakStarted = false
            }
            AD_PROGRESS -> {
//                convivaAdAnalytics?.reportAdMetric(ConvivaSdkConstants.PLAYBACK.PLAYER_STATE, "Playing")
//                convivaAdAnalytics?.reportAdMetric(ConvivaSdkConstants.PLAYBACK.PLAY_HEAD_TIME, it.ad?.adPodInfo?.adPosition)
            }
//            AD_BUFFERING -> convivaAdAnalytics?.reportAdMetric(ConvivaSdkConstants.PLAYBACK.PLAYER_STATE, "Buffering")
//            PAUSED -> convivaAdAnalytics?.reportAdMetric(ConvivaSdkConstants.PLAYBACK.PLAYER_STATE, "Paused")
//            RESUMED -> convivaAdAnalytics?.reportAdMetric(ConvivaSdkConstants.PLAYBACK.PLAYER_STATE, "Resumed")
            else -> {
                val keys = it?.adData?.keys?.joinToString(",")
                val values = it?.adData?.values?.joinToString(",")
//                Log.i("ADs_", "adEventListener: Type-> ${it?.type}")
//                Log.i("ADs_", "adEventListener: keys-> $keys")
//                Log.i("ADs_", "adEventListener: values-> $values")
            }
        }
    }
    
    private fun onAdErrorListener(it: AdErrorEvent?) {
        val errorMessage = it?.error?.message
        convivaAdAnalytics?.reportAdFailed(errorMessage, ConvivaFactory.getConvivaAdMetadata(null))
        convivaAdAnalytics?.reportAdError(errorMessage, ConvivaSdkConstants.ErrorSeverity.WARNING)
    }
}