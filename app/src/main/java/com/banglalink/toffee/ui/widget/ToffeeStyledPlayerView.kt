package com.banglalink.toffee.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.database.ContentObserver
import android.graphics.Point
import android.os.CountDownTimer
import android.os.Handler
import android.provider.Settings
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Space
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.mediarouter.app.MediaRouteButton
import com.banglalink.toffee.R
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.extension.getChannelMetadata
import com.banglalink.toffee.extension.px
import com.banglalink.toffee.listeners.OnPlayerControllerChangedListener
import com.banglalink.toffee.listeners.PlaylistListener
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.PlayerOverlayData
import com.banglalink.toffee.ui.player.PlayerOverlayView
import com.banglalink.toffee.ui.player.PlayerPreview
import com.banglalink.toffee.util.BindingUtil
import com.banglalink.toffee.util.UtilsKt
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.exoplayer2.ui.StyledPlayerControlView
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.video.VideoListener
import com.google.android.gms.cast.framework.CastButtonFactory
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@AndroidEntryPoint
open class ToffeeStyledPlayerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    :StyledPlayerView(context, attrs, defStyleAttr),
    View.OnClickListener,
    Player.EventListener,
    DraggerLayout.OnPositionChangedListener,
    VideoListener {

    private val onPlayerControllerChangedListeners = mutableListOf<OnPlayerControllerChangedListener>()
    private var mPlayListListener: PlaylistListener? = null

    private val screenWidth = UtilsKt.getScreenWidth()
    private val screenHeight = UtilsKt.getScreenHeight()
    private var videoWidth = -1
    private var videoHeight = -1
    private var isUgc = false
    protected var isMinimize = false
    private var channelType: String? = null
    private lateinit var drawerButton: ImageView
    private lateinit var videoOption: ImageView
    private lateinit var shareButton: ImageView
    private lateinit var autoplayProgress: CircularProgressBar
    private lateinit var rotateButton: ImageView
    private lateinit var minimizeButton: ImageView
    protected lateinit var playerOverlay: PlayerOverlayView
    private lateinit var castButton: MediaRouteButton
    private lateinit var textCasting: AppCompatTextView
    private lateinit var debugContainer: FrameLayout
    private lateinit var playerBottomSpace: Space
    protected lateinit var doubleTapInterceptor: PlayerPreview
    private lateinit var playerControlView: StyledPlayerControlView
    private lateinit var fullscreenButton: ImageView
    private lateinit var controllerBg: View
    private lateinit var playNext: ImageView
    private lateinit var playPrev: ImageView
    private lateinit var playPause: ImageView
    private lateinit var previewImage: ImageView

    private lateinit var exoPosition: TextView
    private lateinit var exoTimeSeperator: TextView
    private lateinit var exoDuration: TextView
    private lateinit var exoProgress: DefaultTimeBar

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var debugJob: Job? = null

    @Inject
    lateinit var bindingUtil: BindingUtil

    @Inject
    lateinit var mPref: SessionPreference

    init {
        initView()
    }

    private fun initView() {
        playerControlView = findViewById(R.id.exo_controller)
        drawerButton = findViewById(R.id.drawer)
        videoOption = findViewById(R.id.video_option)
        shareButton = findViewById(R.id.share)
        autoplayProgress = findViewById(R.id.autoplayProgress)
        rotateButton = findViewById(R.id.rotation)
        if (isAutoRotationEnabled) {
            rotateButton.setImageResource(R.drawable.ic_screen_rotate)
        } else {
            rotateButton.setImageResource(R.mipmap.rotation_off)
        }

        setShowNextButton(false)
        setShowPreviousButton(false)
        setShowFastForwardButton(false)
        setShowRewindButton(false)

        minimizeButton = findViewById(R.id.minimize)
        castButton = findViewById(R.id.cast_button)
        playerOverlay = findViewById(R.id.playerOverlay)
        textCasting = findViewById(R.id.text_casting)
        debugContainer = findViewById(R.id.debug_container)
        playerBottomSpace = findViewById(R.id.player_bottom_space)
        doubleTapInterceptor = findViewById(R.id.dtInterceptor)
        fullscreenButton = findViewById(R.id.fullscreen)
        controllerBg = findViewById(R.id.controller_bg)
        playPause = findViewById(R.id.exo_play_pause)
        previewImage = findViewById(R.id.exo_shutter)

        playNext = findViewById(R.id.play_next)
        playPrev = findViewById(R.id.play_prev)

        exoDuration = findViewById(R.id.exo_duration)
        exoTimeSeperator = findViewById(R.id.time_seperator)
        exoPosition = findViewById(R.id.exo_position)
        exoProgress = findViewById(R.id.exo_progress)

        drawerButton.setOnClickListener(this)
        videoOption.setOnClickListener(this)
        shareButton.setOnClickListener(this)
        rotateButton.setOnClickListener(this)
        minimizeButton.setOnClickListener(this)
        doubleTapInterceptor.setOnClickListener(this)
        fullscreenButton.setOnClickListener(this)

        playNext.setOnClickListener(this)
        playPrev.setOnClickListener(this)

        playerControlView.isAnimationEnabled = false

        controllerShowTimeoutMs = 3000

        setControllerVisibilityListener { ctrlVisibility->
            when(ctrlVisibility) {
                View.VISIBLE-> {
                    playerControlView.setShowMultiWindowTimeBar(player?.isCurrentWindowLive == false)
                    onPlayerControllerChangedListeners.forEach {
                        it.onControllerVisible()
                    }
                }
                View.GONE-> {
                    onPlayerControllerChangedListeners.forEach {
                        it.onControllerInVisible()
                    }
                }
            }
        }

        setupOverlay()
        setupCastButton()
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

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setupRotationObserver()
        updateRotationStatus(UtilsKt.isSystemRotationOn(context), false)
    }

    private val rotationObserver = object: ContentObserver(Handler()){
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            updateRotationStatus(UtilsKt.isSystemRotationOn(context))
        }
    }

    fun updateRotationStatus(status: Boolean, invokeListener: Boolean = true) {
        rotateButton.visibility = if(status && !isVideoPortrait) View.VISIBLE else View.GONE
        isAutoRotationEnabled = status
        rotateButton.setImageResource(if (!isAutoRotationEnabled) R.mipmap.rotation_off else R.drawable.ic_screen_rotate)
        if(invokeListener) {
            onPlayerControllerChangedListeners.forEach {
                it.onRotationLock(isAutoRotationEnabled)
            }
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.video_option-> {
                if(videoOption.isEnabled) {
                    onPlayerControllerChangedListeners.forEach {
                        it.onOptionMenuPressed()
                    }
                }
            }
            R.id.share-> {
                onPlayerControllerChangedListeners.forEach {
                    it.onShareButtonPressed()
                }
            }
            R.id.minimize-> {
                onPlayerControllerChangedListeners.forEach {
                    it.onMinimizeButtonPressed()
                }
            }
            R.id.play_next-> {
                mPlayListListener?.playNext()
            }
            R.id.play_prev-> {
                mPlayListListener?.playPrevious()
            }
            R.id.drawer-> {
                onPlayerControllerChangedListeners.forEach {
                    it.onDrawerButtonPressed()
                }
            }
            R.id.fullscreen-> {
                isFullScreen = isFullScreen.not()
                onPlayerControllerChangedListeners.forEach {
                    it.onFullScreenButtonPressed()
                }
            }
            R.id.dtInterceptor-> {
                if(!isControllerFullyVisible) {
                    showController()
                } else {
                    hideController()
                }
            }
            R.id.rotation-> {
                if (isAutoRotationEnabled) {
                    isAutoRotationEnabled = false
                    rotateButton.setImageResource(R.mipmap.rotation_off)
                }
                else {
                    isAutoRotationEnabled = true
                    rotateButton.setImageResource(R.drawable.ic_screen_rotate)
                }
                onPlayerControllerChangedListeners.forEach {
                    it.onRotationLock(isAutoRotationEnabled)
                }
            }
        }
    }

    private fun setupRotationObserver(){
        context.contentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION),
            true,
            rotationObserver
        )
    }

    override fun showController() {
        if(!isControllerFullyVisible && !isMinimize) {
            super.showController()
            controllerBg.visibility = View.VISIBLE
            updateControllerUI()
        }
    }

    private fun updateControllerUI() {
        val isChannelLive = player?.isCurrentWindowLive == true || channelType == "LIVE"
        changeTimerVisibility(isChannelLive)

        player?.let {
            if(it.duration > 0 && !isChannelLive) {
                nextButtonVisibility(player?.playbackState == Player.STATE_READY)
                prevButtonVisibility(player?.playbackState == Player.STATE_READY)
            } else {
                nextButtonVisibility(false)
                prevButtonVisibility(false)
            }
        }
    }

    private fun changeTimerVisibility(state: Boolean) {
        when(state) {
            false -> {
                exoPosition.visibility = View.VISIBLE
                exoDuration.visibility = View.VISIBLE
                exoTimeSeperator.visibility = View.VISIBLE
                exoProgress.visibility = View.VISIBLE
            }
            else -> {
                exoPosition.visibility = View.INVISIBLE
                exoDuration.visibility = View.INVISIBLE
                exoTimeSeperator.visibility = View.INVISIBLE
                exoProgress.visibility = View.GONE
            }
        }
    }

    override fun hideController() {
        super.hideController()
        if(textCasting.visibility != View.VISIBLE) {
            controllerBg.visibility = View.GONE
        }
    }

    fun showCastingText(show: Boolean, deviceName: String? = null) {
        if(show) {
            controllerBg.visibility = View.VISIBLE
            textCasting.visibility = View.VISIBLE
            textCasting.text = if(deviceName != null) "Playing on $deviceName" else "Casting..."
        } else {
            textCasting.visibility = View.GONE
            if(!isControllerFullyVisible) {
                controllerBg.visibility = View.GONE
            }
        }
    }

    fun showDebugOverlay(data: PlayerOverlayData, cid: String) {
        clearDebugWindow()
        if(!isMinimize) {
            debugContainer.addView(DebugOverlayView(context).apply {
                setPlayerOverlayData(data, cid)
            }, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))

            debugJob = coroutineScope.launch {
                delay(data.params.duration * 1000)
                clearDebugWindow()
            }
        }
    }

    fun getDebugOverLay(): View? {
        if(debugContainer.childCount > 0) return debugContainer.getChildAt(0)
        return null
    }

    fun clearDebugWindow() {
        if(debugContainer.childCount > 0) {
            debugContainer.removeAllViews()
        }
    }

    override fun onDetachedFromWindow() {
        debugJob?.cancel()
        clearDebugWindow()
        removeRotationObserver()
        stopAutoplayTimer()
        super.onDetachedFromWindow()
    }

    private fun removeRotationObserver() {
        context.contentResolver.unregisterContentObserver(rotationObserver)
    }

    private fun setupOverlay() {
        playerOverlay.performListener(object : PlayerOverlayView.PerformListener {
            override fun onAnimationStart() {
                // Do UI changes when circle scaling animation starts (e.g. hide controller views)
                playerOverlay.visibility = View.VISIBLE
            }

            override fun onAnimationEnd() {
                // Do UI changes when circle scaling animation starts (e.g. show controller views)
                playerOverlay.visibility = View.GONE
            }
        })
    }

    private fun setupCastButton() {
        if(mPref.isCastEnabled) {
            CastButtonFactory.setUpMediaRouteButton(context.applicationContext, castButton)
        } else {
            castButton.visibility = View.GONE
        }
    }

    private fun nextButtonVisibility(visible: Boolean) {
        playNext.visibility = if(!visible) View.INVISIBLE else {
            if(mPlayListListener?.hasNext() == true) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun prevButtonVisibility(visible: Boolean) {
        playPrev.visibility = if(!visible) View.INVISIBLE else {
            if(mPlayListListener?.hasPrevious() == true) View.VISIBLE else View.INVISIBLE
        }
    }

    private var timer: CountDownTimer? = null

    private fun startAutoPlayTimer() {
        timer?.cancel()
        autoplayProgress.progress = 0f
        autoplayProgress.setProgressWithAnimation(1000f, AUTOPLAY_INTERVAL)
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

    fun isClamped(): Boolean {
        return layoutParams.height <= minBound
    }

    fun isFullScreenPortrait() = isVideoPortrait && layoutParams.height >= UtilsKt.getScreenHeight()

    private val minVideoHeight: Int = screenWidth * 9 / 16
    private val maxVideoHeight: Int = screenHeight * 2 / 3

    val minBound = minVideoHeight
    val maxBound: Int
        get() {
            if(channelType == "LIVE" || !isUgc) return minBound
            if(videoWidth > 0 && videoHeight > 0) {
                return min(max(minVideoHeight, ((videoHeight / videoWidth.toFloat()) * screenWidth).toInt()), maxVideoHeight)
            }
            return if(isVideoPortrait) {
                maxVideoHeight
            } else {
                minBound
            }
        }

    fun clampOrFullHeight() {
        if(layoutParams.height <= minBound || layoutParams.height >= maxBound) return
        val isInTop = layoutParams.height in minBound .. (minBound + ((maxBound - minBound) / 2))
        setHeightWithAnim(if(isInTop) minBound else maxBound)
    }
    var isVideoScalable = false
    var isVideoPortrait = false

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

    fun setLayoutHeight(h: Int){
        layoutParams = layoutParams.also {
            it.height = h
        }
    }

    private var mActivePointerId = MotionEvent.INVALID_POINTER_ID
    private var mLastTouchX: Float = 0f
    private var mLastTouchY: Float = 0f
    private var mPosX: Float = 0f
    private var mPosY: Float = 0f
    private var startX: Float = 0f
    private var startY: Float = 0f

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

    override fun onViewMinimize() {
        isMinimize = true
        hideController()
    }

    override fun onViewMaximize() {
        isMinimize = false
//        binding.textureView.setOnClickListener(this)
        if (player?.isPlaying == true) {
//            hideControls(2000)
            hideController()
        }
        else {
            showController()
        }
    }

    override fun onViewDestroy() {
        if(player !is CastPlayer) {
            player?.stop()
        }
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
        resizeMode = scaleType
    }

    fun showWifiOnlyMessage() {
        bindingUtil.loadImageFromResource(previewImage, R.drawable.watch_wifi_only_msg)
        hideController()
        doubleTapInterceptor.setOnClickListener(null)
    }

    fun showContentExpiredMessage() {
        bindingUtil.loadImageFromResource(previewImage, R.drawable.content_expired)
        hideController()
        doubleTapInterceptor.setOnClickListener(null)
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when(playbackState) {
            Player.STATE_BUFFERING-> {
                previewImage.setImageResource(0)
                playPause.visibility = View.GONE
                doubleTapInterceptor.setOnClickListener(this)
                nextButtonVisibility(false)
                prevButtonVisibility(false)
                autoplayProgress.visibility = View.GONE
                stopAutoplayTimer()
            }
            Player.STATE_IDLE-> {
                previewImage.setImageResource(0)
                playPause.visibility = View.VISIBLE
                nextButtonVisibility(false)
                prevButtonVisibility(false)
                autoplayProgress.visibility = View.GONE
                stopAutoplayTimer()
            }
            Player.STATE_READY-> {
                previewImage.setImageResource(0)
                playPause.visibility = View.VISIBLE
                val isChannelLive = player?.isCurrentWindowLive == true || channelType == "LIVE"
                nextButtonVisibility(!isChannelLive)
                prevButtonVisibility(!isChannelLive)
                autoplayProgress.visibility = View.GONE
                stopAutoplayTimer()
            }
            Player.STATE_ENDED-> {
                playPause.visibility = View.VISIBLE
                playNext.visibility = if (mPlayListListener?.hasNext() == false) View.INVISIBLE else View.VISIBLE
                playPrev.visibility = if (mPlayListListener?.hasPrevious() == false) View.INVISIBLE else VISIBLE

                if (mPlayListListener?.isAutoplayEnabled() == true &&
                    mPlayListListener?.hasNext() == true
                ) {
                    autoplayProgress.visibility = VISIBLE
                    startAutoPlayTimer()
                }
                else {
                    autoplayProgress.visibility = GONE
                }
            }
        }
    }

    fun moveController(offset: Float) {
        val intOffset = if (offset < 0.0f) {
            (48.px * (1.0f + offset)).toInt()
        }
        else {
            ((104 - 48).px * offset).toInt() + 48.px
        }
        playerBottomSpace.minimumHeight = intOffset
    }

    fun onFullScreen(state: Boolean) {
        if (state) { //fullscreen
            minimizeButton.visibility = GONE
            drawerButton.visibility = INVISIBLE
            fullscreenButton.setImageResource(R.drawable.exo_styled_controls_fullscreen_exit)
        }
        else {
            minimizeButton.visibility = VISIBLE
            drawerButton.visibility = VISIBLE
            fullscreenButton.setImageResource(R.drawable.exo_styled_controls_fullscreen_enter)
        }
    }

    var isFullScreen = false
    var isAutoRotationEnabled = true

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

        isVideoScalable = minBound != maxBound

        if(!isFullScreen) setHeightWithAnim(maxBound)

        resizeMode = scaleType
//        binding.playerContainer.setAspectRatio(videoWidth/videoHeight.toFloat())
//        Log.e("CONTROL_T", "Aspect ratio -> ${binding.playerContainer.getAspectRatio()}")
    }

    private val scaleType: Int
        get() {
            if(channelType == "LIVE" || !isUgc) return AspectRatioFrameLayout.RESIZE_MODE_FILL
            if(!mPref.keepAspectRatio/* && maxBound == minBound*/) {
                if(videoWidth > 0 && videoHeight > 0) {
                    if(isFullScreen && videoWidth > videoHeight) {
                        return AspectRatioFrameLayout.RESIZE_MODE_FILL
                    }
                } else if(isFullScreen && !isVideoPortrait) {
                    return AspectRatioFrameLayout.RESIZE_MODE_FILL
                }
            }
            return if (isFullScreen)
                AspectRatioFrameLayout.RESIZE_MODE_FIT
            else
                AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
        }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        videoWidth = -1
        videoHeight = -1
        val channelInfo = mediaItem?.getChannelMetadata(player)
        if (channelInfo is ChannelInfo) {
            val prevState = isVideoPortrait
            isVideoPortrait = channelInfo.isHorizontal != 1
            channelType = channelInfo.type
            isUgc = channelInfo.is_ugc == 1

            if ((prevState && !isVideoPortrait) || (!prevState && isVideoPortrait)) isFullScreen =
                false
            resizeView(UtilsKt.getRealScreenSize(context))

            rotateButton.visibility =
                if (isVideoPortrait || !UtilsKt.isSystemRotationOn(context)) View.GONE else View.VISIBLE
            shareButton.visibility = if (channelInfo.isApproved == 1) View.VISIBLE else View.GONE
        }
        onPlayerControllerChangedListeners.forEach {
            it.onMediaItemChanged()
        }
    }

    override fun setPlayer(newPlayer: Player?) {
        super.setPlayer(newPlayer)
        playerOverlay.player(newPlayer)
        val oldPlayer = this.player //get reference of old player which attached previously
        if (oldPlayer != null) { //if old player not null then clear it
            oldPlayer.removeListener(this)
            if(oldPlayer is SimpleExoPlayer) {
                oldPlayer.removeVideoListener(this)
            }
        }
        if (this.player != null) {
            this.player?.addListener(this)
            this.player?.let {
                if(it is SimpleExoPlayer) it.addVideoListener(this)
            }
        }

        player?.currentMediaItem?.getChannelMetadata(player)?.let {
            isVideoPortrait = it.isHorizontal != 1
            rotateButton.visibility = if(isVideoPortrait || !UtilsKt.isSystemRotationOn(context)) View.GONE else View.VISIBLE
            shareButton.visibility = if(it.isApproved == 1) View.VISIBLE else View.GONE
        }
    }

    companion object {
        private const val UPDATE_PROGRESS = 21
        private const val FORWARD_BACKWARD_DURATION_IN_MILLIS = 10000
        private const val AUTOPLAY_INTERVAL = 5000L
    }
}
