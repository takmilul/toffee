package com.banglalink.toffee.ui.widget

import android.R.color
import android.animation.ValueAnimator
import android.content.Context
import android.database.ContentObserver
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.os.CountDownTimer
import android.os.Handler
import android.os.Message
import android.provider.Settings
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import android.view.View.OnClickListener
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import com.banglalink.toffee.R.drawable
import com.banglalink.toffee.R.mipmap
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.databinding.MediaControlLayout3Binding
import com.banglalink.toffee.extension.getChannelMetadata
import com.banglalink.toffee.extension.px
import com.banglalink.toffee.listeners.OnPlayerControllerChangedListener
import com.banglalink.toffee.listeners.PlaylistListener
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.PlayerOverlayData
import com.banglalink.toffee.ui.player.PlayerOverlayView
import com.banglalink.toffee.ui.widget.DraggerLayout.OnPositionChangedListener
import com.banglalink.toffee.util.BindingUtil
import com.banglalink.toffee.util.UtilsKt
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.EventListener
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.video.VideoListener
import com.google.android.gms.cast.framework.CastButtonFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.lang.Runnable
import java.util.*
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@AndroidEntryPoint
open class ExoMediaController3 @JvmOverloads constructor(context: Context,
                          attrs: AttributeSet? = null,
                          defStyleAttr: Int = 0
):FrameLayout(context, attrs, defStyleAttr),
    OnClickListener,
    OnSeekBarChangeListener,
    EventListener,
    OnPositionChangedListener,
    SurfaceTextureListener,
    VideoListener
{
    private var handler: MessageHandler
    private val onPlayerControllerChangedListeners = mutableListOf<OnPlayerControllerChangedListener>()
    private var simpleExoPlayer: Player? = null
    private lateinit var mFormatBuilder: StringBuilder
    private lateinit var mFormatter: Formatter
    protected var isMinimize = false
    private var lastPlayerPosition: Long = 0
    var isAutoRotationEnabled = true
    private var mPlayListListener: PlaylistListener? = null
    private var videoWidth = -1
    private var videoHeight = -1
    protected lateinit var binding: MediaControlLayout3Binding
    private val screenWidth = UtilsKt.getScreenWidth()
    private val screenHeight = UtilsKt.getScreenHeight()
    var isVideoPortrait = false
    var channelType: String? = null
    var isFullScreen = false
    var isVideoScalable = false

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var debugJob: Job? = null

    @Inject lateinit var bindingUtil: BindingUtil
    @Inject lateinit var mPref: SessionPreference

    init {
        handler = MessageHandler()
        initView()
    }

    fun addPlayerControllerChangeListener(listener: OnPlayerControllerChangedListener) {
        onPlayerControllerChangedListeners.add(listener)
    }

    fun setPlaylistListener(listener: PlaylistListener?) {
        mPlayListListener = listener
    }

    fun clearListeners() {
        onPlayerControllerChangedListeners.clear()
    }

    private fun initView() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = MediaControlLayout3Binding.inflate(inflater, this, true)
        binding.playerContainer.setResizeMode(scaleType)
        binding.minimize.setOnClickListener(this)
        binding.play.setOnClickListener(this)
        binding.drawer.setOnClickListener(this)
        binding.rotation.setOnClickListener(this)
        binding.playPrev.setOnClickListener(this)
        binding.playNext.setOnClickListener(this)
        if (isAutoRotationEnabled) {
            binding.rotation.setImageResource(drawable.ic_screen_rotate)
        }
        else {
            binding.rotation.setImageResource(mipmap.rotation_off)
        }
        binding.progress.max = 1000
        binding.progress.setOnSeekBarChangeListener(this)
        binding.videoOption.setOnClickListener(this)
        binding.fullscreen.setOnClickListener(this)
        binding.dtInterceptor.setOnClickListener(this)
        binding.share.setOnClickListener(this)
        mFormatBuilder = StringBuilder()
        mFormatter = Formatter(mFormatBuilder, Locale.getDefault())
        setupCastButton()
        setupOverlay()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setupRotationObserver()
        updateRotationStatus(UtilsKt.isSystemRotationOn(context), false)
    }

    private fun setupOverlay() {
        binding.playerOverlay.performListener(object : PlayerOverlayView.PerformListener {
            override fun onAnimationStart() {
                // Do UI changes when circle scaling animation starts (e.g. hide controller views)
                binding.playerOverlay.visibility = View.VISIBLE
            }

            override fun onAnimationEnd() {
                // Do UI changes when circle scaling animation starts (e.g. show controller views)
                binding.playerOverlay.visibility = View.GONE
            }
        })
    }

    private fun setupCastButton() {
        if(mPref.isCastEnabled) {
            CastButtonFactory.setUpMediaRouteButton(context.applicationContext, binding.castButton)
        } else {
            binding.castButton.visibility = View.GONE
        }
    }

    fun showDebugOverlay(data: PlayerOverlayData, cid: String) {
        clearDebugWindow()
        if(!isMinimize) {
            binding.debugContainer.addView(DebugOverlayView(context).apply {
                setPlayerOverlayData(data, cid)
            }, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))

            debugJob = coroutineScope.launch {
                delay(data.params.duration * 1000)
                clearDebugWindow()
            }
        }
    }

    private val rotationObserver = object: ContentObserver(Handler()){
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            updateRotationStatus(UtilsKt.isSystemRotationOn(context))
        }
    }

    private fun setupRotationObserver(){
        context.contentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION),
            true,
            rotationObserver
        )
    }

    private fun removeRotationObserver() {
        context.contentResolver.unregisterContentObserver(rotationObserver)
    }

    fun showCastingText(show: Boolean, deviceName: String? = null) {
        if(show) {
            binding.controllerBg.visibility = View.VISIBLE
            binding.textCasting.visibility = View.VISIBLE
            binding.textCasting.text = if(deviceName != null) "Playing on $deviceName" else "Casting..."
        } else {
            binding.textCasting.visibility = View.GONE
            if(binding.controller.visibility != View.VISIBLE) {
                binding.controllerBg.visibility = View.GONE
            }
        }
    }

    fun getDebugOverLay(): View? {
        if(binding.debugContainer.childCount > 0) return binding.debugContainer.getChildAt(0)
        return null
    }

    fun clearDebugWindow() {
        if(binding.debugContainer.childCount > 0) {
            binding.debugContainer.removeAllViews()
        }
    }

    override fun onDetachedFromWindow() {
        debugJob?.cancel()
        clearDebugWindow()
        removeRotationObserver()
        super.onDetachedFromWindow()
    }

    //Use this method to set and unset the player
    fun setPlayer(newPlayer: Player?) {
        if (this.simpleExoPlayer === newPlayer) {
            return
        }
        binding.playerOverlay.player(newPlayer)
        binding.textureView.surfaceTextureListener = this
        if (binding.textureView.isAvailable) {
            binding.preview.setImageBitmap(binding.textureView.bitmap)
        }
        val oldPlayer = this.simpleExoPlayer //get reference of old player which attached previously
        if (oldPlayer != null) { //if old player not null then clear it
            oldPlayer.removeListener(this)
            if(oldPlayer is SimpleExoPlayer) {
                oldPlayer.removeVideoListener(this)
            }
            oldPlayer.videoComponent?.clearVideoTextureView(binding.textureView)
        }
        this.simpleExoPlayer = newPlayer
        if (this.simpleExoPlayer != null) {
            this.simpleExoPlayer?.addListener(this)
            this.simpleExoPlayer?.let {
                if(it is SimpleExoPlayer) it.addVideoListener(this)
            }
            if (binding.textureView.isAvailable) {
                this.simpleExoPlayer?.videoComponent?.setVideoTextureView(binding.textureView)
            }
        }

        simpleExoPlayer?.currentMediaItem?.getChannelMetadata(simpleExoPlayer)?.let {
            isVideoPortrait = it.isHorizontal != 1
            binding.rotation.visibility = if(isVideoPortrait || !UtilsKt.isSystemRotationOn(context)) View.GONE else View.VISIBLE
            binding.share.visibility = if(it.isApproved == 1) View.VISIBLE else View.GONE
        }
    }

    fun showWifiOnlyMessage() {
        bindingUtil.loadImageFromResource(binding.preview, drawable.watch_wifi_only_msg)
        hideControls(0)
        binding.dtInterceptor.setOnClickListener(null)
    }

    val isControllerHidden: Boolean
        get() = binding.controller.visibility != VISIBLE

    fun moveController(offset: Float) {
        val intOffset = if (offset < 0.0f) {
            (48.px * (1.0f + offset)).toInt()
        }
        else {
            ((104 - 48).px * offset).toInt() + 48.px
        }
        binding.playerBottomSpace.minimumHeight = intOffset
    }

    private fun showControls(): Boolean {
        var status = false
        handler.removeCallbacks(hideRunnable)
        if (binding.controller.visibility != VISIBLE && !isMinimize) {
            binding.controller.visibility = VISIBLE
            binding.controllerBg.visibility = View.VISIBLE
            onPlayerControllerChangedListeners.forEach {
                it.onControllerVisible()
            }

            status = true
        }
        updateSeekBar()
        return status
    }

    private var hideRunnable = Runnable {
        if (binding.controller.visibility != GONE) {
            binding.controller.visibility = GONE
            if(binding.textCasting.visibility != View.VISIBLE) {
                binding.controllerBg.visibility = View.GONE
            }

            onPlayerControllerChangedListeners.forEach {
                it.onControllerInVisible()
            }
        }
    }

    private fun updateSeekBar() {
        simpleExoPlayer?.let {
            lastPlayerPosition = it.currentPosition
            val duration = it.duration
            if (duration > 0 && !it.isCurrentWindowLive) {
                // use long to avoid overflow
                val pos = 1000L * lastPlayerPosition / duration
                binding.progress.isEnabled = true
                binding.progress.visibility = VISIBLE
                binding.progress.progress = pos.toInt()
                binding.duration.visibility = VISIBLE
                binding.timeSeperator.visibility = VISIBLE
                binding.currentTime.visibility = VISIBLE
                nextButtonVisibility(simpleExoPlayer?.playbackState == Player.STATE_READY)
                prevButtonVisibility(simpleExoPlayer?.playbackState == Player.STATE_READY)
            }
            else {
                nextButtonVisibility(false)
                prevButtonVisibility(false)
                binding.progress.isEnabled = false
                binding.progress.visibility = GONE
                binding.duration.visibility = INVISIBLE
                binding.timeSeperator.visibility = INVISIBLE
                binding.currentTime.visibility = INVISIBLE
            }
            val percent = it.bufferedPercentage
            binding.progress.secondaryProgress = percent * 10
            binding.duration.text = stringForTime(duration)
            binding.currentTime.text = stringForTime(lastPlayerPosition)
            if (visibility == VISIBLE && it.isPlaying) {
//                val msg = Message()
//                msg.what = UPDATE_PROGRESS
                handler.sendMessageDelayed(Message().apply {
                    what = UPDATE_PROGRESS
                }, 1000)
            }
        }
    }

    private fun stringForTime(timeMs: Long): String {
        val totalSeconds = timeMs / 1000
        val seconds = (totalSeconds % 60).toInt()
        val minutes = (totalSeconds / 60 % 60).toInt()
        val hours = (totalSeconds / 3600).toInt()
        mFormatBuilder.setLength(0)
        return if (hours > 0) {
            mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        }
        else {
            mFormatter.format("%02d:%02d", minutes, seconds).toString()
        }
    }

    private fun hideControls(delay: Long) {
        handler.removeCallbacks(hideRunnable)
        handler.postDelayed(hideRunnable, delay)
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (!fromUser && seekBar === binding.progress) {
            return
        }
        if (seekBar === binding.progress) {
            simpleExoPlayer?.let {
                val newPosition = it.duration * progress / 1000L
                it.seekTo(newPosition)
                binding.currentTime.text = stringForTime(newPosition)
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}
    override fun onStopTrackingTouch(seekBar: SeekBar) {
        updateSeekBar()
    }

    override fun onViewMinimize() {
        isMinimize = true
//        binding.textureView.setOnClickListener(null)
        hideControls(0)
    }

    override fun onViewMaximize() {
        isMinimize = false
//        binding.textureView.setOnClickListener(this)
        if (simpleExoPlayer?.isPlaying == true) {
            hideControls(2000)
        }
        else {
            showControls()
        }
    }

    override fun onViewDestroy() {
        if(simpleExoPlayer !is CastPlayer) {
            simpleExoPlayer?.stop()
        }
    }

    override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, i: Int, i1: Int) {
        simpleExoPlayer?.videoComponent?.setVideoTextureView(binding.textureView)
    }

    override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, i: Int, i1: Int) {}
    override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
        return false
    }

    override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {}
    fun showContentExpiredMessage() {
        if (binding.textureView.isAvailable) {
            binding.textureView.visibility = INVISIBLE
        }
        binding.videoOption.isEnabled = false
        binding.share.isEnabled = false
        bindingUtil.loadImageFromResource(binding.preview, drawable.content_expired)
        hideControls(0)
        binding.dtInterceptor.setOnClickListener(null)
    }

    private inner class MessageHandler : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                UPDATE_PROGRESS -> updateSeekBar()
                else -> super.handleMessage(msg)
            }
        }
    }

    fun onFullScreen(state: Boolean) {
        if (state) { //fullscreen
            binding.minimize.visibility = GONE
            binding.drawer.visibility = INVISIBLE
            binding.fullscreen.setImageResource(drawable.ic_fullscreen_exit)
        }
        else {
            binding.minimize.visibility = VISIBLE
            binding.drawer.visibility = VISIBLE
            binding.fullscreen.setImageResource(drawable.ic_fullscreen_enter)
        }
    }

    private fun nextButtonVisibility(visible: Boolean) {
        if(!visible) {
            binding.playNext.visibility = View.INVISIBLE
        } else {
            binding.playNext.visibility = if(mPlayListListener?.hasNext() == true) View.VISIBLE else View.INVISIBLE
//            binding.playNext.isEnabled = mPlayListListener?.hasNext() == true
        }
    }

    private fun prevButtonVisibility(visible: Boolean) {
        if(!visible) {
            binding.playPrev.visibility = View.INVISIBLE
        } else {
            binding.playPrev.visibility = if(mPlayListListener?.hasPrevious() == true) View.VISIBLE else View.INVISIBLE
//            binding.playPrev.isEnabled = mPlayListListener?.hasPrevious() == true
        }
    }

    fun updateRotationStatus(status: Boolean, invokeListener: Boolean = true) {
        binding.rotation.visibility = if(status && !isVideoPortrait) View.VISIBLE else View.GONE
        isAutoRotationEnabled = status
        binding.rotation.setImageResource(if (!isAutoRotationEnabled) mipmap.rotation_off else drawable.ic_screen_rotate)
        if(invokeListener) {
            onPlayerControllerChangedListeners.forEach {
                it.onRotationLock(isAutoRotationEnabled)
            }
        }
    }

    override fun onClick(v: View) {
        when(v) {
            binding.play-> {
                simpleExoPlayer?.let {
                    if (it.isPlaying) {
                        it.playWhenReady = false
                        showControls()
                    }
                    else {
                        if (it.playbackState == Player.STATE_ENDED) {
                            it.seekTo(0)
                        }
                        it.playWhenReady = true
                        hideControls(3000)
                        if (it.playWhenReady && it.playbackState == Player.STATE_IDLE) {
                            onPlayerControllerChangedListeners.forEach { listener->
                                listener.onPlayerIdleDueToError()
                            }
                        }
                    }
                    updateSeekBar()
                    onPlayerControllerChangedListeners.forEach { listener->
                        listener.onPlayButtonPressed(it.playbackState)
                    }
                }
            }
            binding.videoOption-> {
                if(binding.videoOption.isEnabled) {
                    onPlayerControllerChangedListeners.forEach {
                        it.onOptionMenuPressed()
                    }
                }
            }
            binding.fullscreen -> {
                isFullScreen = isFullScreen.not()
                onPlayerControllerChangedListeners.forEach {
                    it.onFullScreenButtonPressed()
                }
            }
            binding.share -> {
                onPlayerControllerChangedListeners.forEach {
                    it.onShareButtonPressed()
                }
            }
            binding.minimize -> {
                onPlayerControllerChangedListeners.forEach {
                    it.onMinimizeButtonPressed()
                }
            }
            binding.drawer -> {
                onPlayerControllerChangedListeners.forEach {
                    it.onDrawerButtonPressed()
                }
            }
            binding.dtInterceptor -> {
                if (showControls()) {
                    if (simpleExoPlayer?.isPlaying == true) {
                        hideControls(3000)
                    }
                }
                else {
                    hideControls(0)
                }
            }
            binding.playPrev -> {
                mPlayListListener?.playPrevious()
            }
            binding.playNext -> {
                mPlayListListener?.playNext()
            }
            binding.rotation -> {
                if (isAutoRotationEnabled) {
                    isAutoRotationEnabled = false
                    binding.rotation.setImageResource(mipmap.rotation_off)
                }
                else {
                    isAutoRotationEnabled = true
                    binding.rotation.setImageResource(drawable.ic_screen_rotate)
                }
                onPlayerControllerChangedListeners.forEach {
                    it.onRotationLock(isAutoRotationEnabled)
                }
            }
        }
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        //Log.e("CAST_T", "Player state changed")
        when (playbackState) {
            Player.STATE_BUFFERING -> {
                binding.dtInterceptor.setOnClickListener(this)
//                binding.preview.setImageResource(color.black)
                binding.play.visibility = GONE
                nextButtonVisibility(false)
                prevButtonVisibility(false)
                binding.autoplayProgress.visibility = GONE
                binding.buffering.visibility = VISIBLE
                binding.videoOption.isEnabled = false
                stopAutoplayTimer()
                showControls()
            }
            Player.STATE_ENDED -> {
                //                binding.preview.setImageResource(android.R.color.black);
                binding.play.setImageResource(drawable.ic_player_replay)
                binding.buffering.visibility = GONE
                binding.play.visibility = VISIBLE
                if (mPlayListListener?.isAutoplayEnabled() == true &&
                    mPlayListListener?.hasNext() == true
                ) {
                    binding.autoplayProgress.visibility = VISIBLE
                    startAutoPlayTimer()
                }
                else {
                    binding.autoplayProgress.visibility = GONE
                }
                if (mPlayListListener?.hasNext() == false) {
                    binding.playNext.visibility = INVISIBLE
                }
                else {
                    binding.playNext.visibility = VISIBLE
                }
                if (mPlayListListener?.hasPrevious() == false) {
                    binding.playPrev.visibility = INVISIBLE
                }
                else {
                    binding.playPrev.visibility = VISIBLE
                }
                showControls()
            }
            Player.STATE_IDLE -> {
                binding.preview.setImageResource(color.black)
                binding.play.setImageResource(drawable.ic_player_play)
                binding.buffering.visibility = GONE
                binding.play.visibility = VISIBLE
                nextButtonVisibility(false)
                prevButtonVisibility(false)
                binding.autoplayProgress.visibility = GONE
                stopAutoplayTimer()
                showControls()
            }
            Player.STATE_READY -> {
                if (binding.textureView.isAvailable) {
                    binding.textureView.visibility = VISIBLE
                }
                stopAutoplayTimer()
                binding.videoOption.isEnabled = true
                binding.preview.setImageResource(0)
                binding.share.isEnabled = true
                binding.autoplayProgress.visibility = GONE
                if (playWhenReady) {
                    binding.play.setImageResource(drawable.ic_player_pause)
                    binding.buffering.visibility = GONE
                    binding.play.visibility = VISIBLE
                    if (simpleExoPlayer?.isCurrentWindowLive == true) {
                        nextButtonVisibility(false)
                        prevButtonVisibility(false)
                    }
                    else {
                        nextButtonVisibility(true)
                        prevButtonVisibility(true)
                    }
                    showControls() //it is necessary since we don't have preparing state of player
                    hideControls(3000)
                }
                else {
                    binding.play.setImageResource(drawable.ic_player_play)
                    binding.buffering.visibility = GONE
                    binding.play.visibility = VISIBLE
                    if (simpleExoPlayer?.isCurrentWindowLive == true) {
                        nextButtonVisibility(false)
                        prevButtonVisibility(false)
                    }
                    else {
                        nextButtonVisibility(true)
                        prevButtonVisibility(true)
                    }
                    showControls()
                }
            }
            else -> {
            }
        }
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        videoWidth = -1
        videoHeight = -1
        val channelInfo = mediaItem?.getChannelMetadata(simpleExoPlayer)
        if(channelInfo is ChannelInfo) {
            val prevState = isVideoPortrait
            isVideoPortrait = channelInfo.isHorizontal != 1
            channelType = channelInfo.type

            if((prevState && !isVideoPortrait) || (!prevState && isVideoPortrait)) isFullScreen = false
            resizeView(UtilsKt.getRealScreenSize(context))

            binding.rotation.visibility = if(isVideoPortrait || !UtilsKt.isSystemRotationOn(context)) View.GONE else View.VISIBLE
            binding.share.visibility = if(channelInfo.isApproved == 1) View.VISIBLE else View.GONE
        }
        onPlayerControllerChangedListeners.forEach {
            it.onMediaItemChanged()
        }
    }

    private var timer: CountDownTimer? = null
    
    private fun startAutoPlayTimer() {
        timer?.cancel()
        binding.autoplayProgress.progress = 0f
        binding.autoplayProgress.setProgressWithAnimation(1000f, AUTOPLAY_INTERVAL)
        timer = object : CountDownTimer(AUTOPLAY_INTERVAL, AUTOPLAY_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) { }

            override fun onFinish() {
                if (mPlayListListener?.hasNext() == true) {
                    mPlayListListener?.playNext()
                }
            }
        }
        timer?.start()
    }

    private fun stopAutoplayTimer() {
        timer?.cancel()
    }

    fun resizeView(size: Point, forceFullScreen: Boolean = false) {
        val playerWidth: Int = size.x
        val playerHeight: Int = if (size.x > size.y) { //landscape
            size.y
        } else {
            if(!forceFullScreen) maxBound else size.y
        }

        isFullScreen = forceFullScreen || size.x > size.y

        if(!isFullScreen && playerWidth == layoutParams.width) {
            setHeightWithAnim(playerHeight)
        } else {
            layoutParams = layoutParams.apply {
                width = playerWidth
                height = playerHeight
            }
        }
        binding.playerContainer.setResizeMode(scaleType)
    }

    fun isClamped(): Boolean {
        return layoutParams.height <= minBound //|| layoutParams.height >= maxBound
    }

    fun isFullHeight(): Boolean {
        return layoutParams.height >= if(isVideoPortrait) maxBound else minBound
    }

    fun isFullScreenPortrait() = isVideoPortrait && layoutParams.height >= UtilsKt.getScreenHeight()

    private val scaleType: Int
        get() {
            if(!mPref.keepAspectRatio/* && maxBound == minBound*/) {
                if(videoWidth > 0 && videoHeight > 0) {
                    if(isFullScreen && videoWidth > videoHeight) {
                        return AspectRatioFrameLayout.RESIZE_MODE_FILL
                    }
                } else if(isFullScreen && !isVideoPortrait) {
                    return AspectRatioFrameLayout.RESIZE_MODE_FILL
                }
//                if(videoWidth > 0 && videoHeight > 0 && videoWidth > videoHeight
//                    && isFullScreen) { // Horizontal
//                    return AspectRatioFrameLayout.RESIZE_MODE_FILL
//                }
            }
            return if (isFullScreen)
                AspectRatioFrameLayout.RESIZE_MODE_FIT
            else
                AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
        }

    private val minVideoHeight: Int = screenWidth * 9 / 16
    private val maxVideoHeight: Int = screenHeight * 2 / 3

    val minBound = minVideoHeight
    val maxBound: Int
        get() {
            if(videoWidth > 0 && videoHeight > 0) {
                return min(max(minVideoHeight, ((videoHeight / videoWidth.toFloat()) * screenWidth).toInt()), maxVideoHeight)
            }
            return if(isVideoPortrait) {
                maxVideoHeight
            } else {
                minBound
            }
        }

    private var mActivePointerId = MotionEvent.INVALID_POINTER_ID
    private var mLastTouchX: Float = 0f
    private var mLastTouchY: Float = 0f
    private var mPosX: Float = 0f
    private var mPosY: Float = 0f
    private var startX: Float = 0f
    private var startY: Float = 0f

    fun clampOrFullHeight() {
        if(layoutParams.height <= minBound || layoutParams.height >= maxBound) return
        val isInTop = layoutParams.height in minBound .. (minBound + ((maxBound - minBound) / 2))
        setHeightWithAnim(if(isInTop) minBound else maxBound)
    }

    fun setHeightWithAnim(height: Int, animDuration: Long = 100L) {
        if(height == layoutParams.height) return
        heightAnim?.cancel()
        heightAnim = ValueAnimator.ofInt(layoutParams.height, height)
        heightAnim?.duration = animDuration
        heightAnim?.addUpdateListener {
            if(isAttachedToWindow) setLayoutHeight(it.animatedValue as Int)
        }
        heightAnim?.start()
    }

    private var heightAnim: ValueAnimator? = null

    fun handleTouchDown2(ev: MotionEvent) {
        heightAnim?.cancel()
        heightAnim = null
        ev.actionIndex.also { pointerIndex ->
            // Remember where we started (for dragging)
            mLastTouchX = ev.getX(pointerIndex)
            mLastTouchY = ev.getY(pointerIndex)
        }
        startX = mLastTouchX
        startY = mLastTouchY
        // Save the ID of this pointer (for dragging)
        mActivePointerId = ev.getPointerId(0)
    }

    fun handleTouchEvent(ev: MotionEvent): Boolean {

        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                handleTouchDown2(ev)
            }

            MotionEvent.ACTION_MOVE -> {
                // Find the index of the active pointer and fetch its position
                val (x: Float, y: Float) = ev.findPointerIndex(mActivePointerId).let { pointerIndex ->
                        // Calculate the distance moved
                    ev.getX(pointerIndex) to
                            ev.getY(pointerIndex)
                    }

                mPosX += x - mLastTouchX
                mPosY += y - mLastTouchY
                val distanceY = y - mLastTouchY

                setLayoutHeight(min(max(height + distanceY.toInt(), minBound), maxBound))

                invalidate()

                // Remember this touch position for the next move event
                mLastTouchX = x
                mLastTouchY = y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mActivePointerId = MotionEvent.INVALID_POINTER_ID
                val changeY = abs(ev.getY(ev.actionIndex) - startY)
                clampOrFullHeight()
//                if(changeY < 5) {
//                    return false
//                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                ev.actionIndex.also { pointerIndex ->
                    ev.getPointerId(pointerIndex)
                        .takeIf { it == mActivePointerId }
                        ?.run {
                            // This was our active pointer going up. Choose a new
                            // active pointer and adjust accordingly.
                            val newPointerIndex = if (pointerIndex == 0) 1 else 0
                            mLastTouchX = ev.getX(pointerIndex)
                            mLastTouchY = ev.getY(pointerIndex)
                            mActivePointerId = ev.getPointerId(newPointerIndex)
                        }
                }
            }
        }

        return true
    }

    fun setLayoutHeight(h: Int) {
        layoutParams = layoutParams.also {
            it.height = h
        }
    }

    override fun onVideoSizeChanged(
        width: Int,
        height: Int,
        unappliedRotationDegrees: Int,
        pixelWidthHeightRatio: Float
    ) {
        super.onVideoSizeChanged(width, height, unappliedRotationDegrees, pixelWidthHeightRatio)
        videoWidth = (width * pixelWidthHeightRatio).toInt()
        videoHeight = height

        Log.e("CONTROL_T", "Video resolution -> $videoWidth x $videoHeight, ratio -> $pixelWidthHeightRatio, min -> $minBound, max -> $maxBound")

//        isVideoPortrait = videoWidth <= videoHeight
        isVideoScalable = minBound != maxBound

        if(!isFullScreen) setHeightWithAnim(maxBound)

        binding.playerContainer.setResizeMode(scaleType)
        binding.playerContainer.setAspectRatio(videoWidth/videoHeight.toFloat())
        Log.e("CONTROL_T", "Aspect ratio -> ${binding.playerContainer.getAspectRatio()}")
    }

    companion object {
        private const val UPDATE_PROGRESS = 21
        private const val FORWARD_BACKWARD_DURATION_IN_MILLIS = 10000
        private const val AUTOPLAY_INTERVAL = 5000L
    }
}