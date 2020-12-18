package com.banglalink.toffee.ui.player

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.R.id
import com.banglalink.toffee.R.string
import com.banglalink.toffee.analytics.HeartBeatManager.heartBeatEventLiveData
import com.banglalink.toffee.analytics.HeartBeatManager.triggerEventViewingContentStart
import com.banglalink.toffee.analytics.HeartBeatManager.triggerEventViewingContentStop
import com.banglalink.toffee.analytics.ToffeeAnalytics.logException
import com.banglalink.toffee.analytics.ToffeeAnalytics.logForcePlay
import com.banglalink.toffee.listeners.OnPlayerControllerChangedListener
import com.banglalink.toffee.listeners.PlaylistListener
import com.banglalink.toffee.model.Channel
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.TOFFEE_HEADER
import com.banglalink.toffee.ui.category.drama.EpisodeListFragment
import com.banglalink.toffee.ui.common.BaseAppCompatActivity
import com.banglalink.toffee.ui.mychannel.MyChannelPlaylistVideosFragment
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player.EventListener
import com.google.android.exoplayer2.SimpleExoPlayer.Builder
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.ext.cast.SessionAvailabilityListener
import com.google.android.exoplayer2.source.BehindLiveWindowException
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.Parameters
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.ParametersBuilder
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DataSource.Factory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
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
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy
import kotlin.math.max


abstract class PlayerPageActivity :
    BaseAppCompatActivity(),
    OnPlayerControllerChangedListener,
    EventListener,
    PlaylistListener,
    SessionAvailabilityListener
{
    protected var player: Player? = null
    private var defaultTrackSelector: DefaultTrackSelector? = null
    private var trackSelectorParameters: Parameters? = null
    private var lastSeenTrackGroupArray: TrackGroupArray? = null
    protected var playlistManager = PlaylistManager()
    private var startAutoPlay = false
    private var startWindow = 0
    private var startPosition: Long = 0
    private val playerEventListener: PlayerEventListener = PlayerEventListener()
    private var defaultCookieManager = CookieManager()

    private var castContext: CastContext? = null
    private var sessionManager: SessionManager? = null

    private var exoPlayer: SimpleExoPlayer? = null
    private var castPlayer: CastPlayer? = null


    init {
        defaultCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER)
    }

    companion object {
        private const val KEY_TRACK_SELECTOR_PARAMETERS = "track_selector_parameters"
        private const val KEY_WINDOW = "window"
        private const val KEY_POSITION = "position"
        private const val KEY_AUTO_PLAY = "auto_play"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (CookieHandler.getDefault() !== defaultCookieManager) {
            CookieHandler.setDefault(defaultCookieManager)
        }

//        castContext = CastContext.getSharedInstance(this)

        if (savedInstanceState != null) {
            trackSelectorParameters = savedInstanceState.getParcelable(KEY_TRACK_SELECTOR_PARAMETERS)
            startAutoPlay = savedInstanceState.getBoolean(KEY_AUTO_PLAY)
            startWindow = savedInstanceState.getInt(KEY_WINDOW)
            startPosition = savedInstanceState.getLong(KEY_POSITION)
        }
        else {
            val builder = ParametersBuilder( /* context= */this)
            trackSelectorParameters = builder.build()
            clearStartPosition()
        }
        heartBeatEventLiveData.observe(this, {    //In each heartbeat we are checking channel's expire date. Seriously??
            val cinfo = playlistManager.getCurrentChannel()
            if (cinfo?.isExpired(mPref.getSystemTime()) == true) {
                player?.stop(true)
                onContentExpired() //content is expired. Notify the subclass
            }
        })
    }

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

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        updateTrackSelectorParameters()
        updateStartPosition()
        outState.putParcelable(KEY_TRACK_SELECTOR_PARAMETERS, trackSelectorParameters)
        outState.putBoolean(KEY_AUTO_PLAY, startAutoPlay)
        outState.putInt(KEY_WINDOW, startWindow)
        outState.putLong(KEY_POSITION, startPosition)
    }

    private fun initializePlayer() {
        initializeLocalPlayer()
        initializeRemotePlayer()
        player = if(castPlayer?.isCastSessionAvailable == true) castPlayer else exoPlayer

        //we are checking whether there is already channelInfo exist. If not null then play it
        if (playlistManager.getCurrentChannel() != null) {
            playChannel(false)
        }
    }

    private fun initializeLocalPlayer() {
        if (exoPlayer == null) {
            val adaptiveTrackSelectionFactory = AdaptiveTrackSelection.Factory()
            defaultTrackSelector = DefaultTrackSelector(this, adaptiveTrackSelectionFactory).apply {
                parameters = trackSelectorParameters!!
            }
            lastSeenTrackGroupArray = null
            exoPlayer = Builder(this)
                .setTrackSelector(defaultTrackSelector!!)
                .build().apply {
                    addListener(playerEventListener)
                    playWhenReady = false
                    if (BuildConfig.DEBUG) {
                        addAnalyticsListener(EventLogger(defaultTrackSelector))
                    }
                }
        }
    }

    private fun initializeRemotePlayer() {
        castContext?.let {
            Log.e("CAST", "Castplayer init")
            castPlayer = CastPlayer(it).apply {
                addListener(this@PlayerPageActivity)
                playWhenReady = false
                setSessionAvailabilityListener(this@PlayerPageActivity)
            }
        }
    }

    private fun releasePlayer() {
        releaseLocalPlayer()
        releaseRemotePlayer()
        player = null
    }

    private fun releaseLocalPlayer() {
        exoPlayer?.let {
            it.removeListener(playerEventListener)
            updateTrackSelectorParameters()
            updateStartPosition()
            it.release()
            defaultTrackSelector = null
        }
        exoPlayer = null
    }

    private fun releaseRemotePlayer() {
        castPlayer?.setSessionAvailabilityListener(null)
        castPlayer?.release()
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
            startWindow = it.currentWindowIndex
            startPosition = max(0, it.contentPosition)
        }
    }

    private fun clearStartPosition() {
        startAutoPlay = true
        startWindow = C.INDEX_UNSET
        startPosition = C.TIME_UNSET
    }

    private fun prepareMedia(mediaItem: MediaItem): MediaSource {
        val dataSourceFactory: Factory = DefaultHttpDataSourceFactory(
            Util.getUserAgent(this, getString(string.app_name))
        )
        val hlsDataSourceFactory = HlsMediaSource.Factory(dataSourceFactory)
        hlsDataSourceFactory.setAllowChunklessPreparation(true)
        return HlsMediaSource.Factory { _: Int ->
            val dataSource: HttpDataSource = DefaultHttpDataSource(TOFFEE_HEADER)
            dataSource.setRequestProperty("TOFFEE-SESSION-TOKEN", mPref.getHeaderSessionToken()!!)
            dataSource
        }
        .createMediaSource(mediaItem)
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
        return when (val fragment = supportFragmentManager.findFragmentById(id.details_viewer)) {
            is MyChannelPlaylistVideosFragment -> {
                fragment.isAutoplayEnabled()
            }
            is EpisodeListFragment -> {
                fragment.isAutoplayEnabled()
            }
            else -> false
        }
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
        val cinfo = playlistManager.getCurrentChannel()
        var isReload = false
        if (cinfo?.id.equals(info.id, ignoreCase = true)) {
            isReload = true
        }
        else {
            playlistManager.setPlaylist(info)
        }
        playChannel(isReload)
    }

    protected fun playChannel(isReload: Boolean) {
        val channelInfo = playlistManager.getCurrentChannel() ?: return
        val uri = Channel.createChannel(channelInfo).getContentUri(this, mPref)
        if (uri == null) { //in this case settings does not allow us to play content. So stop player and trigger event viewing stop
            player?.stop(true)
            channelCannotBePlayedDueToSettings() //notify hook/subclass
            triggerEventViewingContentStop()
            return
        }
        //Checking whether we need to reload or not. Reload happens because of network switch or re-initialization of player
//        boolean isReload = false;
//        ChannelInfo oldChannelInfo = this.channelInfo;
//        this.channelInfo = channelInfo;
//        if(oldChannelInfo != null && oldChannelInfo.getId().equalsIgnoreCase(this.channelInfo.getId())){
//            isReload = true;//that means we have reload situation. We need to start where we left for VODs
//        }
        player?.let {
            triggerEventViewingContentStart(channelInfo.id.toInt(), channelInfo.type ?: "VOD")
            it.playWhenReady = !isReload || it.playWhenReady
            val mediaSource = prepareMedia(MediaItem.fromUri(uri))
            if (isReload) { //We need to start where we left off for VODs
                val haveStartPosition = startWindow != C.INDEX_UNSET
                if (haveStartPosition && !channelInfo.isLive) {
                    if(it is SimpleExoPlayer) {
                        it.setMediaSource(mediaSource, false)
                        it.prepare()
                        //                    player.prepare(mediaSource, false, false);
                        it.seekTo(startWindow, startPosition) //we seek to where we left for VODs
                    } else if(it is CastPlayer){
                        it.loadItem(getMediaInfo(channelInfo), startPosition)
                        it.playWhenReady = true
                    }
                    return
                }
            }
            if(it is SimpleExoPlayer) {
                it.setMediaSource(mediaSource)
                it.prepare()
            } else if(it is CastPlayer) {
                it.loadItem(getMediaInfo(channelInfo), startPosition)
                it.playWhenReady = true
            }
            //            player.prepare(mediaSource);//Non reload event or reload for live. Just prepare the media and play it
        }
    }

    private fun getMediaInfo(info: ChannelInfo): MediaQueueItem {
        val mediaMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE )
        mediaMetadata.putString( MediaMetadata.KEY_TITLE , info.program_name)
        mediaMetadata.addImage(WebImage(Uri.parse(info.landscape_ratio_1280_720)))

        val channelUrl = Channel.createChannel(info).getContentUri(this, mPref)

        val mediaInfo = if (info.isLive) {
            MediaInfo.Builder(channelUrl).apply {
                setContentType(MimeTypes.APPLICATION_M3U8)//"application/x-mpegurl")
                setStreamType( MediaInfo.STREAM_TYPE_BUFFERED )
                setMetadata( mediaMetadata )
            }
    //                    .setStreamDuration(0) // 0 for Infinity
                .build()
        } else {
            MediaInfo.Builder(channelUrl)
                .setContentType(MimeTypes.APPLICATION_M3U8)//"application/x-mpegurl")
                .setStreamType( MediaInfo.STREAM_TYPE_BUFFERED )
                .setMetadata( mediaMetadata )
    //                    .setStreamDuration(MediaInfo.STREAM_TYPE_LIVE)
                .build()
        }
        return MediaQueueItem.Builder(mediaInfo).build()
    }

    //This will be called due to session token change while playing content or after init of player
    protected fun reloadChannel() {
        val cinfo = playlistManager.getCurrentChannel()
        if (cinfo?.isExpired(mPref.getSystemTime()) == true) {
            //channel is expired. Stop the player and notify hook/subclass
            player?.stop(true)
            onContentExpired()
            return
        }
        if (cinfo != null) {
            playChannel(true)
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

    override fun onFullScreenButtonPressed(): Boolean {
        val isPortrait = resources.configuration.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        requestedOrientation = if (isPortrait) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        return true
    }

    override fun onPlayerIdleDueToError() {
        if (player?.playWhenReady == true) {
            logForcePlay()
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

    private inner class PlayerEventListener : EventListener {
        override fun onPlayerError(e: ExoPlaybackException) {
            e.printStackTrace()
            logException(e)
            if (isBehindLiveWindow(e)) {
                clearStartPosition()
                reloadChannel()
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
    }

    abstract fun resetPlayer()

    override fun onCastSessionAvailable() {
        updateStartPosition()
        player?.stop()
        player = castPlayer
        resetPlayer()
        playChannel(true)
    }

    override fun onCastSessionUnavailable() {
        updateStartPosition()
        player?.stop()
        player = exoPlayer
        resetPlayer()
        playChannel(true)
    }
}