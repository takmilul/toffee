package com.banglalink.toffee.ui.player

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.analytics.HeartBeatManager
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.apiservice.DrmTokenService
import com.banglalink.toffee.data.database.dao.DrmLicenseDao
import com.banglalink.toffee.data.database.entities.ContentViewProgress
import com.banglalink.toffee.data.database.entities.ContinueWatchingItem
import com.banglalink.toffee.data.database.entities.DrmLicenseEntity
import com.banglalink.toffee.data.repository.ContentViewPorgressRepsitory
import com.banglalink.toffee.data.repository.ContinueWatchingRepository
import com.banglalink.toffee.data.storage.PlayerPreference
import com.banglalink.toffee.di.DnsHttpClient
import com.banglalink.toffee.exception.ContentExpiredException
import com.banglalink.toffee.extension.getChannelMetadata
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.listeners.OnPlayerControllerChangedListener
import com.banglalink.toffee.listeners.PlaylistListener
import com.banglalink.toffee.model.Channel
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.TOFFEE_HEADER
import com.banglalink.toffee.receiver.ConnectionWatcher
import com.banglalink.toffee.ui.common.BaseAppCompatActivity
import com.banglalink.toffee.ui.home.HomeViewModel
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player.*
import com.google.android.exoplayer2.SimpleExoPlayer.Builder
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.analytics.AnalyticsListener.EventTime
import com.google.android.exoplayer2.drm.*
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.ext.cast.MediaItemConverter
import com.google.android.exoplayer2.ext.cast.SessionAvailabilityListener
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSource
import com.google.android.exoplayer2.source.*
import com.google.android.exoplayer2.source.ads.AdsLoader
import com.google.android.exoplayer2.source.dash.DashUtil
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.Parameters
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.ParametersBuilder
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.framework.*
import com.google.android.gms.common.images.WebImage
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.net.*
import java.util.*
import javax.inject.Inject
import kotlin.math.max
import kotlin.system.measureTimeMillis

@AndroidEntryPoint
abstract class PlayerPageActivity :
    BaseAppCompatActivity(),
    OnPlayerControllerChangedListener,
    Player.EventListener,
    PlaylistListener,
    AnalyticsListener,
    SessionAvailabilityListener
{
    private var startWindow = 0
    private var playCounter: Int = -1
    private var startAutoPlay = false
    private var startPosition: Long = 0
    protected var player: Player? = null
    private var adsLoader: AdsLoader? = null
    private var castPlayer: CastPlayer? = null
    private var exoPlayer: SimpleExoPlayer? = null
    protected var castContext: CastContext? = null
    private var currentlyPlayingVastUrl: String = ""
    private var defaultCookieManager = CookieManager()
    private var trackSelectorParameters: Parameters? = null
    @Inject lateinit var heartBeatManager: HeartBeatManager
    @Inject lateinit var connectionWatcher: ConnectionWatcher
    private var lastSeenTrackGroupArray: TrackGroupArray? = null
    private var defaultTrackSelector: DefaultTrackSelector? = null
    @Inject lateinit var contentViewRepo: ContentViewPorgressRepsitory
    private var playerAnalyticsListener: PlayerAnalyticsListener? = null
    @Inject lateinit var continueWatchingRepo: ContinueWatchingRepository
    private val homeViewModel by viewModels<HomeViewModel>()
    private var httpDataSourceFactory: OkHttpDataSource.Factory? = null
    private val playerViewModel by viewModels<PlayerViewModel>()
    private val playerEventListener: PlayerEventListener = PlayerEventListener()
    @DnsHttpClient @Inject lateinit var dnsHttpClient: OkHttpClient

    private var mOfflineLicenseHelper: OfflineLicenseHelper? = null

    @Inject lateinit var drmTokenApi: DrmTokenService
    @Inject lateinit var drmLicenseDao: DrmLicenseDao

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
            trackSelectorParameters = savedInstanceState.getParcelable(KEY_TRACK_SELECTOR_PARAMETERS)
        }
        else {
            val builder = ParametersBuilder( /* context= */this)
            trackSelectorParameters = builder.build()
            clearStartPosition()
        }
        heartBeatManager.heartBeatEventLiveData.observe(this) {
                //In each heartbeat we are checking channel's expire date. Seriously??
                val cinfo = playlistManager.getCurrentChannel()
                if (cinfo?.isExpired(mPref.getSystemTime()) == true) {
                    ToffeeAnalytics.logException(ContentExpiredException(0, "serverDate: ${mPref.getSystemTime()}, deviceDate: ${Date()}, expireTime: ${cinfo.expireTime}"))
//                    player?.stop(true)
//                    onContentExpired() //content is expired. Notify the subclass
                }
                playerAnalyticsListener?.let {
                    //In every heartbeat event we are sending bandwitdh data to Pubsub
                    Log.e("PLAYER BYTES", "Flushing to pubsub")
                    playerViewModel.reportBandWidthFromPlayerPref(
                        it.durationInSeconds,
                        it.getTotalBytesInMB()
                    )
                    playerAnalyticsListener?.resetData()

                }
            }

        adsLoader = ImaAdsLoader.Builder(this)
//            .setAdMediaMimeTypes(listOf(MimeTypes.VIDEO_MP4))
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
        adsLoader?.release()
        adsLoader = null
        mOfflineLicenseHelper?.release()
        mOfflineLicenseHelper = null
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
        outState.putParcelable(KEY_TRACK_SELECTOR_PARAMETERS, trackSelectorParameters)
    }

    private fun initializePlayer() {
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
                    .newBuilder()
                    .addNetworkInterceptor(
                        HttpLoggingInterceptor()
                            .setLevel(HttpLoggingInterceptor.Level.HEADERS)
                    )
                    .build()
                )
                .setUserAgent(TOFFEE_HEADER)
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
        }
    }

    private fun getDrmSessionManager(mediaItem: MediaItem): DrmSessionManager {
        val channelInfo = mediaItem.getChannelMetadata(player) ?: return DrmSessionManager.DRM_UNSUPPORTED
        val isDrmActive = mPref.isDrmActive && channelInfo.isDrmActive
        if(!isDrmActive || channelInfo.drmCid.isNullOrBlank()) {
            return DrmSessionManager.DRM_UNSUPPORTED
        }
        val drmSessionManager =  DefaultDrmSessionManager
            .Builder()
            .setMultiSession(false)
            .build(ToffeeMediaDrmCallback(
                mPref.drmWidevineLicenseUrl!!,
                httpDataSourceFactory!!,
                drmTokenApi,
                channelInfo.drmCid)
            )

        measureTimeMillis {
            runBlocking {
                val offlineKey = drmLicenseDao.getByChannelId(channelInfo.id.toLong())
                if (offlineKey == null || System.currentTimeMillis() >= offlineKey.expiryTime) {
                    Log.e("DRM_T", "Renewing offline cache")
                    launch(Dispatchers.IO + Job()) {
                        downloadLicense(channelInfo)
                    }
                } else {
                    Log.e(
                        "DRM_T",
                        "Offline key is present. Will expire on ${Date(offlineKey.expiryTime)}"
                    )
                    Log.e("DRM_T", "Using offline license")
                    drmSessionManager.setMode(
                        DefaultDrmSessionManager.MODE_PLAYBACK,
                        offlineKey.license
                    )
                }
            }
        }.also {
            Log.e("DRM_T", "Blocking time -> $it")
        }
        return drmSessionManager
    }

    private suspend fun downloadLicense(channelInfo: ChannelInfo) {
        val token = drmTokenApi.execute(channelInfo.drmCid!!) ?: return
        Log.e("DRM_T", "Downloading offline license")
        httpDataSourceFactory!!.setDefaultRequestProperties(mapOf("pallycon-customdata-v2" to token))

        if(mOfflineLicenseHelper == null) {
            mOfflineLicenseHelper = OfflineLicenseHelper.newWidevineInstance(
                mPref.drmWidevineLicenseUrl!!,
                false,
                httpDataSourceFactory!!,
                DrmSessionEventListener.EventDispatcher()
            )
        }

        val dataSource = httpDataSourceFactory!!.createDataSource()
        val dashManifest = DashUtil.loadManifest(dataSource, Uri.parse(channelInfo.drmDashUrl))
        val drmInitData = DashUtil.loadFormatWithDrmInitData(dataSource, dashManifest.getPeriod(0)) ?: return
        val licenseData = mOfflineLicenseHelper?.downloadLicense(drmInitData) ?: return
        Log.e("DRM_T", "License size -> ${licenseData.size}")
        val remainingTime = mOfflineLicenseHelper?.getLicenseDurationRemainingSec(
            licenseData
        )?.first ?: 0L
        Log.e("DRM_T", "Drm expiry time -> $remainingTime")
        val licenseExpiration = if(remainingTime == Long.MAX_VALUE) {
            remainingTime
        }
        else {
            System.currentTimeMillis() + (remainingTime * 1000)
        }

        Log.e("DRM_T", "Saving offline license")
        val newDrmLicense = DrmLicenseEntity(channelInfo.id.toLong(), channelInfo.drmCid,
            licenseData, licenseExpiration)
        drmLicenseDao.insert(newDrmLicense)
    }

    private val castSessionListener = object: SessionManagerListener<CastSession> {
        override fun onSessionStarting(p0: CastSession?) {
            p0?.castDevice?.friendlyName?.let {
                showToast("Connecting to $it")
            }
        }

        override fun onSessionStarted(p0: CastSession?, p1: String?) {
            p0?.castDevice?.friendlyName?.let {
                showToast("Connected to $it")
            }
        }

        override fun onSessionStartFailed(p0: CastSession?, p1: Int) {
            p0?.castDevice?.friendlyName?.let {
                showToast("Failed to connect to $it")
            }
        }

        override fun onSessionEnding(p0: CastSession?) {}
        override fun onSessionEnded(p0: CastSession?, p1: Int) {}
        override fun onSessionResuming(p0: CastSession?, p1: String?) {}

        override fun onSessionResumed(p0: CastSession?, p1: Boolean) {
            p0?.let {
                val cinfo = try {
                    jsonToChannelInfo(it.remoteMediaClient.currentItem.customData!!)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    null
                }
                if(cinfo != null && playlistManager.getCurrentChannel() == null) {
                    playlistManager.setPlaylist(cinfo)
                    resumeCastSession(cinfo)
                }
            }
        }

        override fun onSessionResumeFailed(p0: CastSession?, p1: Int) {}
        override fun onSessionSuspended(p0: CastSession?, p1: Int) {}
    }

    protected open fun resumeCastSession(info: ChannelInfo) {}

    private fun initializeRemotePlayer() {
        castContext?.let {
            it.sessionManager.addSessionManagerListener(castSessionListener, CastSession::class.java)

            Log.e("CAST", "Castplayer init")
            castPlayer = CastPlayer(it, ToffeeMediaItemConverter()).apply {
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
        Log.e("PLAYER BYTES",""+totalBytes/1024+" KB")
    }

    protected fun updateStartPosition() {
        player?.let {
            startAutoPlay = it.playWhenReady
            startWindow = it.currentWindowIndex
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

    private fun buildMediaItem(uri: String, channelInfo: ChannelInfo, isReload: Boolean): MediaItem {
        val isDrmActive = mPref.isDrmActive && channelInfo.isDrmActive
        Log.e("DRM_T", "Drm Active -> ${mPref.isDrmActive}, ${channelInfo.isDrmActive}")

        var mediaItem = MediaItem.Builder().apply {
            if(isDrmActive && channelInfo.drmCid != null && channelInfo.drmDashUrl != null) {
                httpDataSourceFactory?.setDefaultRequestProperties(emptyMap())
                setDrmUuid(C.WIDEVINE_UUID)
                setMimeType(MimeTypes.APPLICATION_MPD)
                setUri(channelInfo.drmDashUrl)
            } else {
                httpDataSourceFactory?.setDefaultRequestProperties(mapOf("TOFFEE-SESSION-TOKEN" to mPref.getHeaderSessionToken()!!))

                Log.e("DRM_T", "Drm deactivated")
                if (!channelInfo.isBucketUrl) setMimeType(MimeTypes.APPLICATION_M3U8)
                setUri(uri)
            }
            setTag(channelInfo)
        }.build()

        if (!isReload && player is SimpleExoPlayer) playCounter = ++playCounter % mPref.vastFrequency
        homeViewModel.vastTagsMutableLiveData.value?.randomOrNull()?.let { tag ->
            val shouldPlayAd = mPref.isVastActive && playCounter == 0 && !channelInfo.isLive
            val vastTag = if(isReload) currentlyPlayingVastUrl else tag.url
            if (shouldPlayAd && vastTag.isNotBlank()) {
                mediaItem = mediaItem.buildUpon()
                    .setAdTagUri(Uri.parse(vastTag))
                    .build()
                if (!isReload) currentlyPlayingVastUrl = tag.url
            }
        }
        return mediaItem
    }

    abstract fun maximizePlayer()

    private fun playChannel(isReload: Boolean) {
        val channelInfo = playlistManager.getCurrentChannel() ?: return
        val hlsLink = channelInfo.hlsLinks?.get(0)?.hls_url_mobile ?: run {
            player?.stop()
            player?.clearMediaItems()
            maximizePlayer()
            getPlayerView().adViewGroup.removeAllViews()
            heartBeatManager.triggerEventViewingContentStop()
            ToffeeAnalytics.logException(NullPointerException("Channel url is null for id -> ${channelInfo.id}, name -> ${channelInfo.program_name}"))
            return
        }
        val uri = if (channelInfo.isBucketUrl) hlsLink else Channel.createChannel(channelInfo.program_name, hlsLink).getContentUri(mPref, connectionWatcher)
//        val uri = "https://storage.googleapis.com/storage/v1/b/ugc-content-storage/o/18_aab9687b-a56a-44e1-ad66-46f1ffbd83a8.mp4?alt=media"
        //Log.e("PLAY_T", "${channelInfo.hlsLinks?.first()?.hls_url_mobile}")
        //Log.e("PLAY_T", "$uri;;${mPref.sessionToken};;$TOFFEE_HEADER;;$TOFFEE_HEADER")
        if (uri == null) { //in this case settings does not allow us to play content. So stop player and trigger event viewing stop
            player?.stop()
            player?.clearMediaItems()
            getPlayerView().adViewGroup.removeAllViews()
            channelCannotBePlayedDueToSettings() //notify hook/subclass
            maximizePlayer()
            heartBeatManager.triggerEventViewingContentStop()
            return
        }
        //Checking whether we need to reload or not. Reload happens because of network switch or re-initialization of player
//        boolean isReload = false;
//        ChannelInfo oldChannelInfo = this.channelInfo;
//        this.channelInfo = channelInfo;
//        if(oldChannelInfo != null && oldChannelInfo.getId().equalsIgnoreCase(this.channelInfo.getId())){
//            isReload = true;//that means we have reload situation. We need to start where we left for VODs
//        }
//        Log.e("MEDIA_T", "Player is -> ${player?.javaClass?.name}")
        player?.let {
//            Log.e("MEDIA_T", "${it.currentMediaItem?.playbackProperties?.tag}")
            val oldChannelInfo = getCurrentChannelInfo()
            oldChannelInfo?.let { oldInfo ->
                if(oldInfo.id != channelInfo.id && it.playbackState != STATE_ENDED) {
                    insertContentViewProgress(oldInfo, it.currentPosition)
                }
            }
            if(!channelInfo.fcmEventName.isNullOrBlank()){
                if(channelInfo.isFcmEventActive){
                    for (event in channelInfo.fcmEventName.split(",")) {
                        ToffeeAnalytics.logEvent(event)
                    }
                }
            }
            heartBeatManager.triggerEventViewingContentStart(channelInfo.id.toInt(), channelInfo.type ?: "VOD")
            it.playWhenReady = !isReload || it.playWhenReady

            val mediaItem = buildMediaItem(uri, channelInfo, isReload)

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
                    if(it is SimpleExoPlayer) {
//                        getPlayerView().adViewGroup.removeAllViews()
                        it.setMediaItem(mediaItem, false)
                        //                    player.prepare(mediaSource, false, false);
                    } else if(it is CastPlayer){
                        if(mPref.isCastUrlOverride) {
                            mediaItem.buildUpon()
                                .setUri(getCastUrl(uri))
                                .build()
                        }
                        it.setMediaItem(mediaItem, startPosition)
                    }
                    it.prepare()
                    it.playWhenReady = true
                    it.seekTo(startWindow, startPosition) //we seek to where we left for VODs
//                    it.prepare()
                    return
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
            if(it is SimpleExoPlayer) {
                getPlayerView().adViewGroup.removeAllViews()
                it.setMediaItem(mediaItem, startPosition)
                it.prepare()
            } else if(it is CastPlayer) {
                if(mPref.isCastUrlOverride) {
                    mediaItem.buildUpon()
                        .setUri(getCastUrl(uri))
                        .build()
                }
                it.setMediaItem(mediaItem, startPosition)
                it.playWhenReady = true
                it.prepare()
            }
            //            player.prepare(mediaSource);//Non reload event or reload for live. Just prepare the media and play it
        }
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

    private inner class ToffeeMediaItemConverter: MediaItemConverter {
        override fun toMediaQueueItem(mediaItem: MediaItem): MediaQueueItem {
            return getMediaInfo(mediaItem.getChannelMetadata(castPlayer)!!)
        }

        override fun toMediaItem(mediaQueueItem: MediaQueueItem): MediaItem {
//            Log.e("MEDIA_T", "CustomData -> ${mediaQueueItem.customData}")
            return MediaItem.Builder().setUri(mediaQueueItem.media.contentUrl)
                .setMediaMetadata(
                    com.google.android.exoplayer2.MediaMetadata
                        .Builder()
                        .setTitle(mediaQueueItem.media.metadata.getString(MediaMetadata.KEY_TITLE))
                        .build()
                )
                .setMimeType(MimeTypes.APPLICATION_M3U8)
                .setTag(jsonToChannelInfo(mediaQueueItem.customData!!))
                .build()
        }
    }

    private fun getMediaInfo(info: ChannelInfo): MediaQueueItem {
        val mediaMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE )
        mediaMetadata.putString( MediaMetadata.KEY_TITLE , info.program_name ?: "")
        if(info.isLive) {
            mediaMetadata.addImage(WebImage(Uri.parse(info.channel_logo)))
        }
        else {
            mediaMetadata.addImage(WebImage(Uri.parse(info.landscape_ratio_1280_720)))
        }

        val channelUrl = Channel.createChannel(info.program_name, info.getHlsLink()!!).getContentUri(mPref, connectionWatcher)?.let {
            getCastUrl(it)
        }

        val mediaInfo = if (info.isLive) {
            MediaInfo.Builder(channelUrl!!).apply {
                setContentType(MimeTypes.APPLICATION_M3U8)//"application/x-mpegurl")
                setStreamType( MediaInfo.STREAM_TYPE_LIVE )
                setMetadata( mediaMetadata )
            }
    //                    .setStreamDuration(0) // 0 for Infinity
                .build()
        } else {
            MediaInfo.Builder(channelUrl!!)
                .setContentType(MimeTypes.APPLICATION_M3U8)//"application/x-mpegurl")
                .setStreamType( MediaInfo.STREAM_TYPE_BUFFERED )
                .setMetadata( mediaMetadata )
    //                    .setStreamDuration(MediaInfo.STREAM_TYPE_LIVE)
                .build()
        }
        return MediaQueueItem.Builder(mediaInfo).setCustomData(channelInfoToJson(info)/*.apply { Log.e("CAST_T",
            this.toString(4)
        ) }*/).build()
    }

    private fun channelInfoToJson(info: ChannelInfo): JSONObject {
        return JSONObject().apply {
            put("channel_info", Gson().toJson(info))
        }
    }

    private fun jsonToChannelInfo(json: JSONObject): ChannelInfo {
        return Gson().fromJson(json.getString("channel_info"), ChannelInfo::class.java)
    }

    //This will be called due to session token change while playing content or after init of player
    protected fun reloadChannel() {
        val cinfo = playlistManager.getCurrentChannel()
        if (cinfo?.isExpired(mPref.getSystemTime()) == true) {
            ToffeeAnalytics.logException(ContentExpiredException(0, "serverDate: ${mPref.getSystemTime()}, deviceDate: ${Date()}, expireTime: ${cinfo.expireTime}"))
            //channel is expired. Stop the player and notify hook/subclass
//            player?.stop(true)
//            onContentExpired()
//            return
        }
        if (cinfo != null) {
            playChannel(true)
        }
    }

    private fun insertContentViewProgress(cinfo: ChannelInfo, progress: Long) {
        lifecycleScope.launch {
            Log.e("PLAYBACK_STATE", "Saving state - ${cinfo.id} -> $progress")
            if(!cinfo.isLive && progress > 0L) {
                cinfo.viewProgress = progress
                contentViewRepo.insert(
                    ContentViewProgress(
                        customerId = mPref.customerId,
                        contentId = cinfo.id.toLong(),
                        progress = progress
                    )
                )
                Log.e("TOFFEE", "Category - ${cinfo.categoryId}")
                if(cinfo.categoryId == 1 && cinfo.viewProgressPercent() < 970) {
                    continueWatchingRepo.insertItem(
                        ContinueWatchingItem(
                            mPref.customerId,
                            cinfo.id.toLong(),
                            cinfo.type ?: "VOD",
                            cinfo.categoryId,
                            Gson().toJson(cinfo),
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

    private inner class PlayerEventListener : Player.EventListener {
        override fun onPlayerError(e: ExoPlaybackException) {
            e.printStackTrace()
            ToffeeAnalytics.logException(e)
            if (isBehindLiveWindow(e)) {
                clearStartPosition()
                reloadChannel()
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

        private fun isBehindLiveWindow(e: ExoPlaybackException): Boolean {
            if (e.type != ExoPlaybackException.TYPE_SOURCE) {
                return false
            }
            var cause: Throwable? = e.sourceException
            while (cause != null) {
                if (cause is BehindLiveWindowException) {
                    return true
                }
                cause = cause.cause
            }
            return false
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            if(isPlaying) return
            val cinfo = getCurrentChannelInfo()
            if(cinfo is ChannelInfo) {
                if(player?.currentPosition ?: 0 > 0L) {
                    insertContentViewProgress(cinfo, player?.currentPosition ?: -1)
                }
            }
        }
    }

    private fun getCurrentChannelInfo(): ChannelInfo? {
        return player?.currentMediaItem?.getChannelMetadata(player)
    }

    abstract fun resetPlayer()

    override fun onCastSessionAvailable() {
        Log.e("CAST_T", "Cast Session available")
        updateStartPosition()
//        val savedSession = mPref.savedCastInfo
//        if(savedSession != null) {
//            Log.e("CAST_T", "Saved session id -> ${savedSession?.id}")
//            playlistManager.setPlaylist(savedSession)
//            mPref.savedCastInfo = null
//        }
        playlistManager.getCurrentChannel()?.let {
            if(player?.playbackState != STATE_ENDED) {
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
            if(player?.playbackState != STATE_ENDED) {
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
                Log.e(
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

}