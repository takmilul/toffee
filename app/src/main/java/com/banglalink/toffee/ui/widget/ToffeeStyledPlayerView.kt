package com.banglalink.toffee.ui.widget

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Space
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.media3.cast.CastPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView
import androidx.media3.ui.PlayerView.ControllerVisibilityListener
import androidx.mediarouter.app.MediaRouteButton
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.extension.getChannelMetadata
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.ifNotNullOrBlank
import com.banglalink.toffee.extension.invisible
import com.banglalink.toffee.extension.px
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.listeners.OnPlayerControllerChangedListener
import com.banglalink.toffee.listeners.PlaylistListener
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.PlayerOverlayData
import com.banglalink.toffee.ui.player.PlayerOverlayView
import com.banglalink.toffee.ui.player.PlayerPreview
import com.banglalink.toffee.ui.player.ToffeePlayerEventHelper
import com.banglalink.toffee.util.BindingUtil
import com.banglalink.toffee.util.ConvivaHelper
import com.banglalink.toffee.util.Utils
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.material.slider.Slider
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

@AndroidEntryPoint
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
open class ToffeeStyledPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : 
    View.OnClickListener,
    Player.Listener,
    DraggerLayout.OnPositionChangedListener,
    PlayerView(context, attrs, defStyleAttr) 
{
    val activity = context as Activity
    
    var isVideoScalable = false
    var isVideoPortrait = false
    
    private var isUgc = false
    private var videoWidth = -1
    private var videoHeight = -1
    private var tickTime: Long = 0
    protected var isMinimize = false
    private var isStateEnded = false
    private var debugJob: Job? = null
    private lateinit var controllerBg: View
    private lateinit var playNext: ImageView
    private lateinit var playPrev: ImageView
    private lateinit var buffering: ImageView
    private var timer: CountDownTimer? = null
    private lateinit var playPause: ImageView
    private lateinit var exoPosition: TextView
    private lateinit var exoDuration: TextView
    private lateinit var videoOption: ImageView
    private lateinit var shareButton: ImageView
    private var isLinearChannel: Boolean = false
    @Inject lateinit var cPref: CommonPreference
    private lateinit var previewImage: ImageView
    private lateinit var audioBookImageView: RatioImageView
    private lateinit var rotateButton: ImageView
    private lateinit var drawerButton: ImageView
    @Inject lateinit var mPref: SessionPreference
    @Inject lateinit var bindingUtil: BindingUtil
    private lateinit var playerBottomSpace: Space
    private lateinit var minimizeButton: ImageView
    private lateinit var closeIcon: ImageView
    private lateinit var exoTimeSeparator: TextView
    private lateinit var exoProgress: DefaultTimeBar
    private lateinit var fullscreenButton: ImageView
    private lateinit var debugContainer: FrameLayout
    private lateinit var castButton: MediaRouteButton
    private lateinit var brightnessControllBar: Slider
    private lateinit var brightnessIcon: ImageView
    private val screenWidth = Utils.getScreenWidth()
    private val screenHeight = Utils.getScreenHeight()
    private lateinit var textCasting: AppCompatTextView
    private var mPlayListListener: PlaylistListener? = null
    protected lateinit var playerOverlay: PlayerOverlayView
    private lateinit var errorMessageView: AppCompatTextView
    private lateinit var autoplayProgress: CircularProgressBar
    protected lateinit var doubleTapInterceptor: PlayerPreview
    private lateinit var errorMessageContainer: ConstraintLayout
    @Inject lateinit var playerEventHelper: ToffeePlayerEventHelper
    private lateinit var playerControlView: PlayerControlView
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val onPlayerControllerChangedListeners = mutableListOf<OnPlayerControllerChangedListener>()
    private var brightness: Float = 0f
    private var trackingBrightness: Boolean = false
    
    companion object {
        private const val UPDATE_PROGRESS = 21
        private const val FORWARD_BACKWARD_DURATION_IN_MILLIS = 10000
        private const val AUTOPLAY_PROGRESS_TIME = 1000F
        private const val AUTOPLAY_INTERVAL = 5000L
    }
    
    init {
        initView()
    }
    
    private fun initView() {
        playerControlView = findViewById(androidx.media3.ui.R.id.exo_controller)
        drawerButton = findViewById(R.id.drawer)
        videoOption = findViewById(R.id.video_option)
        shareButton = findViewById(R.id.share)
        autoplayProgress = findViewById(R.id.autoplayProgress)
        rotateButton = findViewById(R.id.rotation)
        
        closeIcon = findViewById(R.id.closeIcon)
        minimizeButton = findViewById(R.id.minimize)
        castButton = findViewById(R.id.cast_button)
        playerBottomSpace = findViewById(R.id.player_bottom_space)
        fullscreenButton = findViewById(R.id.fullscreen)
        playNext = findViewById(R.id.play_next)
        playPrev = findViewById(R.id.play_prev)
        playPause = findViewById(androidx.media3.ui.R.id.exo_play_pause)
        exoDuration = findViewById(androidx.media3.ui.R.id.exo_duration)
        exoTimeSeparator = findViewById(R.id.time_seperator)
        exoPosition = findViewById(androidx.media3.ui.R.id.exo_position)
        exoProgress = findViewById(androidx.media3.ui.R.id.exo_progress)
        
        playerOverlay = findViewById(R.id.playerOverlay)
        textCasting = findViewById(R.id.text_casting)
        debugContainer = findViewById(R.id.debug_container)
        errorMessageView = findViewById(R.id.error_message_view)
        errorMessageContainer = findViewById(R.id.error_message_container)
        doubleTapInterceptor = findViewById(R.id.dtInterceptor)
        controllerBg = findViewById(R.id.controller_bg)
        previewImage = findViewById(R.id.exo_shutter)
        audioBookImageView = findViewById(R.id.audio_book_image_view)
        buffering = findViewById(R.id.exo_buffering)

        brightnessControllBar = findViewById(R.id.brightnessControlBar)
        brightnessIcon = findViewById(R.id.brightnessIcon)

        brightnessControllBar.addOnChangeListener { rangeSlider, value, fromUser ->
            /** Responds when slider's value is changed
             * Sending instructions to control player brightness
             */
            setScreenBrightness(value)
        }

        brightnessControllBar.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                trackingBrightness = true
            }

            override fun onStopTrackingTouch(slider: Slider) {
                /** Responds when slider's touch event is being stopped
                 * Storing players brightness value to use it as default brightness in next sessions
                 */
                trackingBrightness = false
                if (isFullScreen && !controllerBg.isVisible){
                    coroutineScope.launch {
                        delay(1000)
                        if (!controllerBg.isVisible){
                            brightnessControllBar.hide()
                            brightnessIcon.hide()
                        }
                    }
                }
                mPref.playerScreenBrightness = brightness
            }
        })

        if (isAutoRotationEnabled) {
            rotateButton.setImageResource(R.drawable.ic_screen_rotate)
        } else {
            rotateButton.setImageResource(R.drawable.rotation_off)
        }
        
        setShowNextButton(false)
        setShowPreviousButton(false)
        setShowFastForwardButton(false)
        setShowRewindButton(false)
        
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
        
        setControllerVisibilityListener(ControllerVisibilityListener { ctrlVisibility ->
            when (ctrlVisibility) {
                View.VISIBLE -> {
                    val isLive = player?.isCurrentMediaItemLive == true || isLinearChannel
                    changeTimerVisibility(isLive)
//                    playerControlView.setShowMultiWindowTimeBar(player?.isCurrentWindowLive == false)
                    if (isFullScreen){
                        brightnessIcon.show()
                        brightnessControllBar.show()
                    }
                    onPlayerControllerChangedListeners.forEach {
                        it.onControllerVisible()
                    }
                }
                View.GONE -> {
                    if (textCasting.visibility != View.VISIBLE) {
                        controllerBg.visibility = View.GONE
                    }
                    if (isFullScreen && !trackingBrightness){
                        brightnessIcon.hide()
                        brightnessControllBar.hide()
                    }
                    onPlayerControllerChangedListeners.forEach {
                        it.onControllerInVisible()
                    }
                }
            }
        })
        
        setupOverlay()
        setupCastButton()
        
        videoSurfaceView?.let {
            if (it is SurfaceView) {
//                it.holder.setFixedSize()
                val isDisableScreenshot = (
                    mPref.screenCaptureEnabledUsers.contains(cPref.deviceId)
                    || mPref.screenCaptureEnabledUsers.contains(mPref.customerId.toString())
                    || mPref.screenCaptureEnabledUsers.contains(mPref.phoneNumber)
                ).not()
                
                //disable screen capture
                if (isDisableScreenshot) {
                    it.setSecure(true)
                }
            }
        }
    }
    
    private fun setScreenBrightness(value: Float, isTransformedValue: Boolean? = false) {
        /** This method works for value from -1.0F to 1.0F
         * Value 0.0F to 1.0F to increase or decrese brightness
         * Value -1.0F to set the brightness as device defaults
         */
        brightness = if (value < 0 || isTransformedValue == true){ value } else {
            // Adjusting the rate of change for screen brightness,
            // This can help in achieving a more perceptually uniform brightness control
            val transformedValue = transformSliderValue(value)
            transformedValue
        }
        val layoutParams = activity.window.attributes
        layoutParams.screenBrightness = brightness
        activity.window.attributes = layoutParams
    }

    private fun transformSliderValue(value: Float): Float {
        val exponent = 2.0
        return value.toDouble().pow(exponent).toFloat()
    }

    private fun reverseTransformValue(transformedValue: Float): Float {
        val exponent = 2.0
        return transformedValue.toDouble().pow(1.0 / exponent).toFloat()
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
//        setupRotationObserver()
        updateRotationStatus(status = true, false)
    }

//    private val rotationObserver = object: ContentObserver(Handler(Looper.getMainLooper())){
//        override fun onChange(selfChange: Boolean) {
//            super.onChange(selfChange)
//            updateRotationStatus(UtilsKt.isSystemRotationOn(context))
//        }
//    }
    
    private fun updateRotationStatus(status: Boolean, invokeListener: Boolean = true) {
//        rotateButton.visibility = if (status && !isVideoPortrait) View.VISIBLE else View.GONE
        isAutoRotationEnabled = status
        rotateButton.setImageResource(if (!isAutoRotationEnabled) R.drawable.rotation_off else R.drawable.ic_screen_rotate)
        if (invokeListener) {
            onPlayerControllerChangedListeners.forEach {
                it.onRotationLock(isAutoRotationEnabled)
            }
        }
    }
    
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.video_option -> {
                if (videoOption.isEnabled) {
                    onPlayerControllerChangedListeners.forEach {
                        it.onOptionMenuPressed()
                    }
                }
            }
            R.id.share -> {
                onPlayerControllerChangedListeners.forEach {
                    it.onShareButtonPressed()
                }
            }
            R.id.minimize -> {
                onPlayerControllerChangedListeners.forEach {
                    it.onMinimizeButtonPressed()
                }
            }
            R.id.play_next -> {
                mPlayListListener?.playNext()
            }
            R.id.play_prev -> {
                mPlayListListener?.playPrevious()
            }
            R.id.drawer -> {
                onPlayerControllerChangedListeners.forEach {
                    ToffeeAnalytics.toffeeLogEvent(
                        ToffeeEvents.MENU_OPEN,
                        bundleOf(
                            "screen" to "Player"
                        )
                    )
                    it.onDrawerButtonPressed()
                }
            }
            R.id.fullscreen -> {
                isFullScreen = isFullScreen.not()
                onPlayerControllerChangedListeners.forEach {
                    it.onFullScreenButtonPressed()
                }
            }
            R.id.dtInterceptor -> {
                if (!isControllerFullyVisible) {
                    showController()
                } else {
                    hideController()
                }
            }
            R.id.rotation -> {
                if (isAutoRotationEnabled) {
                    isAutoRotationEnabled = false
                    rotateButton.setImageResource(R.drawable.rotation_off)
                } else {
                    isAutoRotationEnabled = true
                    rotateButton.setImageResource(R.drawable.ic_screen_rotate)
                }
                onPlayerControllerChangedListeners.forEach {
                    it.onRotationLock(isAutoRotationEnabled)
                }
            }
        }
    }
    
    fun toggleFullScreenStatus(state: Boolean) {
        isFullScreen = state
    }
    
    fun onPip(enabled: Boolean = false) {
        useController = !enabled
        setShowBuffering(/*if(enabled) SHOW_BUFFERING_NEVER else*/ SHOW_BUFFERING_ALWAYS)
        setScreenBrightness(-1.0f) // Set default brightness
    }
    
    fun isControllerVisible(): Boolean {
        return isControllerFullyVisible
    }
    
    override fun showController() {
        if (!isControllerFullyVisible && !isMinimize) {
            super.showController()
            controllerBg.visibility = View.VISIBLE
            updateControllerUI()
        }
    }
    
    private fun updateControllerUI() {
        val isChannelLive = player?.isCurrentMediaItemLive == true || isLinearChannel
        changeTimerVisibility(isChannelLive)
        
        player?.let {
            if (it.duration > 0 && !isChannelLive) {
                nextButtonVisibility(player?.playbackState == Player.STATE_READY)
                prevButtonVisibility(player?.playbackState == Player.STATE_READY)
            } else {
                nextButtonVisibility(false)
                prevButtonVisibility(false)
            }
        }
    }
    
    private fun nextButtonVisibility(visible: Boolean) {
        playNext.visibility = if (!visible) View.INVISIBLE else {
            if (mPlayListListener?.hasNext() == true) View.VISIBLE else View.INVISIBLE
        }
    }
    
    private fun prevButtonVisibility(visible: Boolean) {
        playPrev.visibility = if (!visible) View.INVISIBLE else {
            if (mPlayListListener?.hasPrevious() == true) View.VISIBLE else View.INVISIBLE
        }
    }
    
    private fun changeTimerVisibility(state: Boolean) {
        when (state) {
            false -> {
                exoPosition.visibility = View.VISIBLE
                exoDuration.visibility = View.VISIBLE
                exoTimeSeparator.visibility = View.VISIBLE
                exoProgress.visibility = View.VISIBLE
            }
            else -> {
                exoPosition.visibility = View.INVISIBLE
                exoDuration.visibility = View.INVISIBLE
                exoTimeSeparator.visibility = View.INVISIBLE
                exoProgress.visibility = View.GONE
            }
        }
    }
    
    fun showCastingText(show: Boolean, deviceName: String? = null) {
        if (show) {
            controllerBg.visibility = View.VISIBLE
            textCasting.visibility = View.VISIBLE
            textCasting.text = if (deviceName != null) "Playing on $deviceName" else "Casting..."
        } else {
            textCasting.visibility = View.GONE
            if (!isControllerFullyVisible) {
                controllerBg.visibility = View.GONE
            }
        }
    }
    
    fun showDebugOverlay(data: PlayerOverlayData, cid: String) {
        clearDebugWindow()
        if (!isMinimize) {
            debugContainer.addView(DebugOverlayView(context).apply {
                setPlayerOverlayData(data, cid)
            }, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
            
            debugJob = coroutineScope.launch {
                delay((data.params?.duration ?: 0) * 1000)
                clearDebugWindow()
            }
        }
    }
    
    fun getDebugOverLay(): View? {
        if (debugContainer.childCount > 0) return debugContainer.getChildAt(0)
        return null
    }
    
    fun clearDebugWindow() {
        if (debugContainer.childCount > 0) {
            debugContainer.removeAllViews()
        }
    }
    
    override fun onDetachedFromWindow() {
        debugJob?.cancel()
        clearDebugWindow()
//        removeRotationObserver()
        stopAutoplayTimer()
        super.onDetachedFromWindow()
    }

//    private fun removeRotationObserver() {
//        context.contentResolver.unregisterContentObserver(rotationObserver)
//    }
    
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
        if (mPref.isCastEnabled) {
            CastButtonFactory.setUpMediaRouteButton(context.applicationContext, castButton)
        } else {
            castButton.visibility = View.GONE
        }
    }
    
    private fun startAutoPlayTimer() {
        timer?.cancel()
        autoplayProgress.progress = 0f
        autoplayProgress.setProgressWithAnimation(AUTOPLAY_PROGRESS_TIME, AUTOPLAY_INTERVAL)
        timer = object : CountDownTimer(AUTOPLAY_INTERVAL, AUTOPLAY_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {
                tickTime = millisUntilFinished
            }
            
            override fun onFinish() {
                if (mPlayListListener?.hasNext() == true) {
                    tickTime = 0
                    mPlayListListener?.playNext()
                }
            }
        }
        timer?.start()
    }
    
    private fun stopAutoplayTimer() {
        tickTime = 0
        timer?.cancel()
    }
    
    fun isClamped(): Boolean {
        return layoutParams.height <= minBound
    }
    
    fun isFullScreenPortrait() = isVideoPortrait && layoutParams.height >= Utils.getScreenHeight()
    
    private val minVideoHeight: Int = screenWidth * 9 / 16
    private val maxVideoHeight: Int = screenHeight * 2 / 3
    
    val minBound = minVideoHeight
    val maxBound: Int
        get() {
            if (isLinearChannel || !isUgc) return minBound
            if (videoWidth > 0 && videoHeight > 0) {
                return min(max(minVideoHeight, ((videoHeight / videoWidth.toFloat()) * screenWidth).toInt()), maxVideoHeight)
            }
            return if (isVideoPortrait) {
                maxVideoHeight
            } else {
                minBound
            }
        }
    
    fun clampOrFullHeight() {
        if (layoutParams.height <= minBound || layoutParams.height >= maxBound) return
        val isInTop = layoutParams.height in minBound..(minBound + ((maxBound - minBound) / 2))
        setHeightWithAnim(if (isInTop) minBound else maxBound)
    }
    
    fun setHeightWithAnim(height: Int, animDuration: Long = 100L) {
        if (height == layoutParams.height) return
        heightAnim?.cancel()
        heightAnim = ValueAnimator.ofInt(layoutParams.height, height)
        heightAnim?.duration = animDuration
        heightAnim?.addUpdateListener {
            if (isAttachedToWindow) setLayoutHeight(it.animatedValue as Int)
        }
        heightAnim?.start()
    }
    
    private var heightAnim: ValueAnimator? = null
    
    fun setLayoutHeight(h: Int) {
        layoutParams = layoutParams.also {
            it.height = h
        }
    }
    
    private var mPosX: Float = 0f
    private var mPosY: Float = 0f
    private var startX: Float = 0f
    private var startY: Float = 0f
    private var mLastTouchX: Float = 0f
    private var mLastTouchY: Float = 0f
    private var mActivePointerId = MotionEvent.INVALID_POINTER_ID
    
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
//                val changeY = abs(ev.getY(ev.actionIndex) - startY)
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
    
    fun togglePlayerCloseIconVisibility(isVisible: Boolean) {
        closeIcon.isVisible = isVisible
    }
    
    fun getPlayerCloseIconArea(): IntArray {
        val viewLocation = IntArray(2)
        closeIcon.getLocationOnScreen(viewLocation)
        return viewLocation
    }
    
    override fun onPlayerMinimize() {
        isMinimize = true
        hideController()
    }
    
    override fun onPlayerMaximize() {
        isMinimize = false
        togglePlayerCloseIconVisibility(false)
//        binding.textureView.setOnClickListener(this)
        if (player?.isPlaying == true) {
//            hideControls(2000)
            hideController()
        }
//        else {
//            showController()
//        }
    }
    
    override fun onPlayerDestroy() {
//        MedalliaDigital.enableIntercept()
        if (player !is CastPlayer) {
            player?.stop()
        }
    }
    
    fun resizeView(size: Point, forceFullScreen: Boolean = false) {
        val playerWidth: Int = size.x
        val playerHeight: Int = if (size.x > size.y) { //landscape
            size.y
        } else {
            if (!forceFullScreen) maxBound else size.y
        }
        
        isFullScreen = forceFullScreen || size.x > size.y
        
        if (!isFullScreen && playerWidth == layoutParams.width) {
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
        previewImage.setBackgroundColor(Color.BLACK)
        previewImage.setPadding(24.px, 0, 24.px, 0)
        bindingUtil.loadImageFromResource(previewImage, R.drawable.ic_content_expired)
        hideController()
        doubleTapInterceptor.setOnClickListener(null)
    }
    
    fun showCustomErrorMessage(errorMessage: String? = null) {
        errorMessage?.ifNotNullOrBlank {
            errorMessageContainer.show()
            previewImage.setImageResource(0)
            audioBookImageView.setImageResource(0)
            errorMessageView.text = it
        }
    }
    
    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_BUFFERING -> {
                coroutineScope.launch { 
                    delay(500)
                    buffering.load(R.drawable.player_loader)
                }
                errorMessageContainer.hide()
                previewImage.setImageResource(0)
                audioBookImageView.setImageResource(0)
                playPause.visibility = View.GONE
                doubleTapInterceptor.setOnClickListener(this)
                nextButtonVisibility(false)
                prevButtonVisibility(false)
                autoplayProgress.visibility = View.GONE
                val progressTime = tickTime
                stopAutoplayTimer()
                if((mPlayListListener?.isAutoplayEnabled() == false && isStateEnded) || progressTime in 1 until AUTOPLAY_INTERVAL) {
                    ConvivaHelper.endPlayerSession(true)
                    getCurrentChannelInfo()?.let {
                        playerEventHelper.startContentPlayingSession(it.id)
                        ConvivaHelper.setConvivaVideoMetadata(it, mPref.customerId)
                    }
                }
                isStateEnded = false
                playerEventHelper.setPlayerEvent("Showing loader in player")
            }
            Player.STATE_IDLE -> {
                previewImage.setImageResource(0)
                audioBookImageView.setImageResource(0)
                playPause.visibility = View.VISIBLE
                nextButtonVisibility(false)
                prevButtonVisibility(false)
                autoplayProgress.visibility = View.GONE
                stopAutoplayTimer()
                playerEventHelper.setPlayerEvent("Player is idle")
            }
            Player.STATE_READY -> {
                errorMessageContainer.hide()
                setPlayerImage(getCurrentChannelInfo())
                playPause.visibility = View.VISIBLE
                val isChannelLive = player?.isCurrentMediaItemLive == true || isLinearChannel
                nextButtonVisibility(!isChannelLive)
                prevButtonVisibility(!isChannelLive)
                autoplayProgress.visibility = View.GONE
                stopAutoplayTimer()
                playerEventHelper.setPlayerEvent("Ready to play")
            }
            Player.STATE_ENDED -> {
                playPause.visibility = View.VISIBLE
                playNext.visibility = if (mPlayListListener?.hasNext() == false) View.INVISIBLE else View.VISIBLE
                playPrev.visibility = if (mPlayListListener?.hasPrevious() == false) View.INVISIBLE else VISIBLE
                
                if (mPlayListListener?.isAutoplayEnabled() == true && mPlayListListener?.hasNext() == true) {
                    autoplayProgress.visibility = VISIBLE
                    startAutoPlayTimer()
                } else {
                    autoplayProgress.visibility = GONE
                    ConvivaHelper.endPlayerSession(true)
                    isStateEnded = true
                }
                playerEventHelper.setPlayerEvent("Playing ended")
            }
        }
    }
    
    fun moveController(offset: Float) {
        val intOffset = if (offset < 0.0f) {
            (48.px * (1.0f + offset)).toInt()
        } else {
            ((104 - 48).px * offset).toInt() + 48.px
        }
        playerBottomSpace.minimumHeight = intOffset
    }
    
    fun onFullScreen(state: Boolean) {
        if (state) { //fullscreen
            minimizeButton.visibility = GONE
            drawerButton.visibility = INVISIBLE
            fullscreenButton.setImageResource(R.drawable.exo_styled_controls_fullscreen_exit)

            // set brightness controller
            if (isControllerVisible()){
                brightnessIcon.show()
                brightnessControllBar.show()
            }
            setScreenBrightness(value = mPref.playerScreenBrightness, isTransformedValue = true)
            brightnessControllBar.value = reverseTransformValue(mPref.playerScreenBrightness)
        } else {
            minimizeButton.visibility = VISIBLE
            drawerButton.visibility = VISIBLE
            fullscreenButton.setImageResource(R.drawable.exo_styled_controls_fullscreen_enter)

            // hide brightness controller
            brightnessIcon.hide()
            brightnessControllBar.hide()
            setScreenBrightness(-1.0f)
        }
    }
    
    var isFullScreen = false
    var isAutoRotationEnabled = true
    
    override fun onVideoSizeChanged(videoSize: VideoSize) {
        super.onVideoSizeChanged(videoSize)
        videoWidth = (width * videoSize.pixelWidthHeightRatio).toInt()
        videoHeight = height
        
//        Log.i("CONTROL_T", "Video resolution -> $videoWidth x $videoHeight, ratio -> $videoSize.pixelWidthHeightRatio, min -> $minBound, max -> $maxBound")
        
        isVideoScalable = minBound != maxBound
        
        if (!isFullScreen) setHeightWithAnim(maxBound)
        
        resizeMode = scaleType
//        binding.playerContainer.setAspectRatio(videoWidth/videoHeight.toFloat())
//        Log.e("CONTROL_T", "Aspect ratio -> ${binding.playerContainer.getAspectRatio()}")
    }
    
    private val scaleType: Int
        get() {
            return if (isLinearChannel || !isUgc)
                AspectRatioFrameLayout.RESIZE_MODE_FILL
            else if (isFullScreen)
                AspectRatioFrameLayout.RESIZE_MODE_FIT
            else
                AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
        }
    
    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        videoWidth = -1
        videoHeight = -1
        val channelInfo = getCurrentChannelInfo()
        if (channelInfo is ChannelInfo) {
            val prevState = isVideoPortrait
            isVideoPortrait = channelInfo.isHorizontal != 1
            isLinearChannel = channelInfo.isLinear
            changeTimerVisibility(isLinearChannel)
            isUgc = channelInfo.is_ugc == 1
            
            if ((prevState && !isVideoPortrait) || (!prevState && isVideoPortrait)) isFullScreen = false
            resizeView(Utils.getRealScreenSize(context))
            
//            rotateButton.visibility = if (isVideoPortrait/* || !UtilsKt.isSystemRotationOn(context)*/) View.GONE else View.VISIBLE
            shareButton.visibility = if (channelInfo.isApproved == 1 && !channelInfo.isAudioBook) View.VISIBLE else View.GONE
            
            toggleVideoProfileMenuFromPlayer(channelInfo.isFmRadio || channelInfo.isAudioBook)
        }
        onPlayerControllerChangedListeners.forEach {
            it.onMediaItemChanged()
        }
    }
    
    fun setPlayerImage(channelInfo: ChannelInfo?) {
        if (channelInfo != null && (channelInfo.isFmRadio || channelInfo.isAudioBook)) {
            channelInfo.ugcFeaturedImage?.let {
                if (channelInfo.isAudioBook) {
                    audioBookImageView.load(it)
                } else {
                    previewImage.load(it)
                }
            }
        } else {
            previewImage.setImageResource(0)
            audioBookImageView.setImageResource(0)
        }
    }
    
    private fun toggleVideoProfileMenuFromPlayer(isHide: Boolean = false) {
        if (isHide) {
            videoOption.invisible()
            videoOption.layoutParams.width = 1.px
        } else {
            videoOption.show()
            videoOption.layoutParams.width = ConstraintLayout.LayoutParams.WRAP_CONTENT
        }
    }
    
    override fun setPlayer(newPlayer: Player?) {
        val oldPlayer = this.player //get reference of old player which attached previously
        super.setPlayer(newPlayer)
        playerOverlay.player(newPlayer)
        oldPlayer?.removeListener(this)
        this.player?.addListener(this)
        
        getCurrentChannelInfo()?.let {
            isVideoPortrait = it.isHorizontal != 1
//            rotateButton.visibility = if (isVideoPortrait /*|| !UtilsKt.isSystemRotationOn(context)*/) View.GONE else View.VISIBLE
            shareButton.visibility = if (it.isApproved == 1) View.VISIBLE else View.GONE
        }
    }
    
    fun getCurrentChannelInfo() = player?.currentMediaItem?.getChannelMetadata(player)
}