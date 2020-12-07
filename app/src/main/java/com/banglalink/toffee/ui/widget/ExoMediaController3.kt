package com.banglalink.toffee.ui.widget

import android.R.color
import android.content.Context
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.os.CountDownTimer
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.databinding.DataBindingUtil
import com.banglalink.toffee.R.*
import com.banglalink.toffee.databinding.MediaControlLayout3Binding
import com.banglalink.toffee.listeners.OnPlayerControllerChangedListener
import com.banglalink.toffee.listeners.PlaylistListener
import com.banglalink.toffee.ui.widget.DraggerLayout.OnPositionChangedListener
import com.banglalink.toffee.util.Utils
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.EventListener
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * Created by shantanu on 5/4/16.
 */
class ExoMediaController3 : FrameLayout, OnClickListener, OnSeekBarChangeListener, EventListener, OnPositionChangedListener, SurfaceTextureListener {
    private var inflater: LayoutInflater
    private var handler: MessageHandler
    private val onPlayerControllerChangedListeners: MutableList<OnPlayerControllerChangedListener> = ArrayList()
    private var simpleExoPlayer: Player? = null
    private var mFormatBuilder: StringBuilder? = null
    private var mFormatter: Formatter? = null
    private var isMinimize = false
    var lastPlayerPosition: Long = 0
        private set
    var isAutoRotationEnabled = true
        private set
    private var mPlayListListener: PlaylistListener? = null
    private val videoWidth = 1920
    private val videoHeight = 1080
    private lateinit var binding: MediaControlLayout3Binding

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        handler = MessageHandler()
        initView()
    }

    constructor(context: Context) : super(context) {
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        handler = MessageHandler()
        initView()
    }

    fun addPlayerControllerChangeListener(onPlayerControllerChangedListener: OnPlayerControllerChangedListener) {
        onPlayerControllerChangedListeners.add(onPlayerControllerChangedListener)
    }

    fun setPlaylistListener(listener: PlaylistListener?) {
        mPlayListListener = listener
    }

    fun clearListeners() {
        onPlayerControllerChangedListeners.clear()
    }

    private fun initView() {
        binding = DataBindingUtil.inflate(inflater, layout.media_control_layout3, this, true)
        binding.minimize.setOnClickListener(this)
        binding.play.setOnClickListener(this)
        binding.forward.setOnClickListener(this)
        binding.backward.setOnClickListener(this)
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
        binding.preview.setOnClickListener(this)
        binding.share.setOnClickListener(this)
        mFormatBuilder = StringBuilder()
        mFormatter = Formatter(mFormatBuilder, Locale.getDefault())
    }

    //Use this method to set and unset the player
    fun setPlayer(simpleExoPlayer: Player?) {
        if (this.simpleExoPlayer === simpleExoPlayer) {
            return
        }
        binding.textureView.surfaceTextureListener = this
        if (binding.textureView.isAvailable) {
            binding.preview.setImageBitmap(binding.textureView.bitmap)
        }
        val oldPlayer = this.simpleExoPlayer //get reference of old player which attached previously
        if (oldPlayer != null) { //if old player not null then clear it
            oldPlayer.removeListener(this)
            if (oldPlayer.videoComponent != null) {
                oldPlayer.videoComponent!!.clearVideoTextureView(binding.textureView)
            }
        }
        this.simpleExoPlayer = simpleExoPlayer
        if (this.simpleExoPlayer != null) {
            this.simpleExoPlayer!!.addListener(this)
            if (binding.textureView.isAvailable && this.simpleExoPlayer!!.videoComponent != null) {
                this.simpleExoPlayer!!.videoComponent!!.setVideoTextureView(binding.textureView)
            }
        }
    }

    private fun forward() {
        if (simpleExoPlayer != null) simpleExoPlayer!!.seekTo(min(simpleExoPlayer!!.currentPosition + FORWARD_BACKWARD_DURATION_IN_MILLIS, simpleExoPlayer!!.duration))
    }

    private fun backward() {
        if (simpleExoPlayer != null) simpleExoPlayer!!.seekTo(max(0, simpleExoPlayer!!.currentPosition - FORWARD_BACKWARD_DURATION_IN_MILLIS))
    }

    fun showWifiOnlyMessage() {
        binding.preview.setImageResource(mipmap.watch_wifi_only_msg)
        hideControls(0)
        binding.preview.setOnClickListener(null)
    }

    val isControllerHidden: Boolean
        get() = binding.controller.visibility != VISIBLE

    fun moveController(offset: Float) {
        val intOffset = if (offset < 0.0f) {
            (Utils.dpToPx(48) * (1.0f + offset)).toInt()
        }
        else {
            (Utils.dpToPx(104 - 48) * offset).toInt() + Utils.dpToPx(48)
        }
        binding.playerBottomSpace.minimumHeight = intOffset
    }

    private fun showControls(): Boolean {
        var status = false
        handler.removeCallbacks(hideRunnable)
        if (binding.controller.visibility != VISIBLE && !isMinimize) {
            binding.controller.visibility = VISIBLE
            for (onPlayerControllerChangedListener in onPlayerControllerChangedListeners) {
                onPlayerControllerChangedListener.onControllerVisible()
            }
            status = true
        }
        updateSeekBar()
        return status
    }

    private var hideRunnable = Runnable {
        if (binding.controller.visibility != GONE) {
            binding.controller.visibility = GONE
            for (onPlayerControllerChangedListener in onPlayerControllerChangedListeners) {
                onPlayerControllerChangedListener.onControllerInVisible()
            }
        }
    }

    private fun updateSeekBar() {
        if (simpleExoPlayer == null) {
            return
        }
        lastPlayerPosition = simpleExoPlayer!!.currentPosition
        val duration = simpleExoPlayer!!.duration
        if (duration > 0 && !simpleExoPlayer!!.isCurrentWindowLive) {
            // use long to avoid overflow
            val pos = 1000L * lastPlayerPosition / duration
            binding.progress.isEnabled = true
            binding.progress.visibility = VISIBLE
            binding.progress.progress = pos.toInt()
            binding.duration.visibility = VISIBLE
            binding.timeSeperator.visibility = VISIBLE
            binding.currentTime.visibility = VISIBLE
        }
        else {
            binding.progress.isEnabled = false
            binding.progress.visibility = GONE
            binding.duration.visibility = INVISIBLE
            binding.timeSeperator.visibility = INVISIBLE
            binding.currentTime.visibility = INVISIBLE
        }
        val percent = simpleExoPlayer!!.bufferedPercentage
        binding.progress.secondaryProgress = percent * 10
        binding.duration.text = stringForTime(duration)
        binding.currentTime.text = stringForTime(lastPlayerPosition)
        if (visibility == VISIBLE && simpleExoPlayer!!.isPlaying) {
            val msg = Message()
            msg.what = UPDATE_PROGRESS
            handler.sendMessageDelayed(msg, 1000)
        }
    }

    private fun stringForTime(timeMs: Long): String {
        val totalSeconds = timeMs / 1000
        val seconds = (totalSeconds % 60).toInt()
        val minutes = (totalSeconds / 60 % 60).toInt()
        val hours = (totalSeconds / 3600).toInt()
        mFormatBuilder!!.setLength(0)
        return if (hours > 0) {
            mFormatter!!.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        }
        else {
            mFormatter!!.format("%02d:%02d", minutes, seconds).toString()
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
        if (seekBar === binding.progress && simpleExoPlayer != null) {
            val duration = simpleExoPlayer!!.duration
            val newPosition = duration * progress / 1000L
            simpleExoPlayer!!.seekTo(newPosition)
            binding.currentTime.text = stringForTime(newPosition)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}
    override fun onStopTrackingTouch(seekBar: SeekBar) {
        updateSeekBar()
    }

    override fun onViewMinimize() {
        binding.root.keepScreenOn = true
        isMinimize = true
        binding.textureView.setOnClickListener(null)
        hideControls(0)
    }

    override fun onViewMaximize() {
        binding.root.keepScreenOn = true
        isMinimize = false
        binding.textureView.setOnClickListener(this)
        if (simpleExoPlayer != null && simpleExoPlayer!!.isPlaying) {
            hideControls(2000)
        }
        else {
            showControls()
        }
    }

    override fun onViewDestroy() {
        binding.root.keepScreenOn = false
        if (simpleExoPlayer != null) {
            simpleExoPlayer!!.stop()
        }
    }

    override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, i: Int, i1: Int) {
        if (simpleExoPlayer != null && simpleExoPlayer!!.videoComponent != null) {
            simpleExoPlayer!!.videoComponent!!.setVideoTextureView(binding.textureView)
        }
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
        binding.preview.setImageResource(drawable.content_expired)
        hideControls(0)
        binding.preview.setOnClickListener(null)
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

    override fun onClick(v: View) {
        if (v === binding.play && simpleExoPlayer != null) {
            if (simpleExoPlayer!!.isPlaying) {
                simpleExoPlayer!!.playWhenReady = false
                showControls()
            }
            else {
                if (simpleExoPlayer!!.playbackState == Player.STATE_ENDED) {
                    simpleExoPlayer!!.seekTo(0)
                }
                simpleExoPlayer!!.playWhenReady = true
                hideControls(3000)
                if (simpleExoPlayer!!.playWhenReady && simpleExoPlayer!!.playbackState == Player.STATE_IDLE) {
                    for (onPlayerControllerChangedListener in onPlayerControllerChangedListeners) {
                        onPlayerControllerChangedListener.onPlayerIdleDueToError()
                    }
                }
            }
            updateSeekBar()
            for (OnPlayerControllerChangedListener in onPlayerControllerChangedListeners) {
                OnPlayerControllerChangedListener.onPlayButtonPressed(simpleExoPlayer!!.playbackState)
            }
        }
        else if (v === binding.videoOption && binding.videoOption.isEnabled) {
            for (OnPlayerControllerChangedListener in onPlayerControllerChangedListeners) {
                OnPlayerControllerChangedListener.onOptionMenuPressed()
            }
        }
        else if (v === binding.fullscreen) {
            for (onPlayerControllerChangedListener in onPlayerControllerChangedListeners) {
                onPlayerControllerChangedListener.onFullScreenButtonPressed()
            }
        }
        else if (v === binding.share) {
            for (onPlayerControllerChangedListener in onPlayerControllerChangedListeners) {
                onPlayerControllerChangedListener.onShareButtonPressed()
            }
        }
        else if (v === binding.minimize) {
            for (onPlayerControllerChangedListener in onPlayerControllerChangedListeners) {
                onPlayerControllerChangedListener.onMinimizeButtonPressed()
            }
        }
        else if (v === binding.drawer) {
            for (onPlayerControllerChangedListener in onPlayerControllerChangedListeners) {
                onPlayerControllerChangedListener.onDrawerButtonPressed()
            }
        }
        else if (v === binding.preview) {
            if (showControls()) {
                if (simpleExoPlayer != null && simpleExoPlayer!!.isPlaying) {
                    hideControls(3000)
                }
            }
            else {
                hideControls(0)
            }
        }
        else if (v === binding.forward) {
            forward()
        }
        else if (v === binding.backward) {
            backward()
        }
        else if (v === binding.playPrev) {
            if (mPlayListListener != null) mPlayListListener!!.playPrevious()
        }
        else if (v === binding.playNext) {
            if (mPlayListListener != null) mPlayListListener!!.playNext()
        }
        else if (v === binding.rotation) {
            if (isAutoRotationEnabled) {
                isAutoRotationEnabled = false
                binding.rotation.setImageResource(mipmap.rotation_off)
            }
            else {
                isAutoRotationEnabled = true
                binding.rotation.setImageResource(drawable.ic_screen_rotate)
            }
            for (OnPlayerControllerChangedListener in onPlayerControllerChangedListeners) {
                OnPlayerControllerChangedListener.onRotationLock(isAutoRotationEnabled)
            }
        }
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
            Player.STATE_BUFFERING -> {
                binding.preview.setOnClickListener(this)
                binding.preview.setImageResource(color.black)
                binding.play.visibility = GONE
                binding.forward.visibility = INVISIBLE
                binding.backward.visibility = INVISIBLE
                binding.playPrev.visibility = GONE
                binding.playNext.visibility = GONE
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
                binding.forward.visibility = INVISIBLE
                binding.backward.visibility = INVISIBLE
                if (mPlayListListener != null &&
                    mPlayListListener!!.isAutoplayEnabled() &&
                    mPlayListListener!!.hasNext()
                ) {
                    binding.autoplayProgress.visibility = VISIBLE
                    startAutoPlayTimer()
                }
                else {
                    binding.autoplayProgress.visibility = GONE
                }
                if (mPlayListListener != null && !mPlayListListener!!.hasNext()) {
                    binding.playNext.visibility = GONE
                }
                else {
                    binding.playNext.visibility = VISIBLE
                }
                if (mPlayListListener != null && !mPlayListListener!!.hasPrevious()) {
                    binding.playPrev.visibility = GONE
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
                binding.forward.visibility = INVISIBLE
                binding.backward.visibility = INVISIBLE
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
                binding.playNext.visibility = GONE
                binding.playPrev.visibility = GONE
                if (playWhenReady) {
                    binding.play.setImageResource(drawable.ic_player_pause)
                    binding.buffering.visibility = GONE
                    binding.play.visibility = VISIBLE
                    if (simpleExoPlayer != null && simpleExoPlayer!!.isCurrentWindowLive) {
                        binding.forward.visibility = INVISIBLE
                        binding.backward.visibility = INVISIBLE
                    }
                    else {
                        binding.forward.visibility = VISIBLE
                        binding.backward.visibility = VISIBLE
                    }
                    showControls() //it is necessary since we don't have preparing state of player
                    hideControls(3000)
                }
                else {
                    binding.play.setImageResource(drawable.ic_player_play)
                    binding.buffering.visibility = GONE
                    binding.play.visibility = VISIBLE
                    if (simpleExoPlayer != null && simpleExoPlayer!!.isCurrentWindowLive) {
                        binding.forward.visibility = INVISIBLE
                        binding.backward.visibility = INVISIBLE
                    }
                    else {
                        binding.forward.visibility = VISIBLE
                        binding.backward.visibility = VISIBLE
                    }
                    showControls()
                }
            }
            else -> {
            }
        }
    }

    private val AUTOPLAY_INTERVAL = 5000L
    private var timer: CountDownTimer? = null
    private fun startAutoPlayTimer() {
        if (timer != null) {
            timer!!.cancel()
        }
        binding.autoplayProgress.progress = 0f
        binding.autoplayProgress.setProgressWithAnimation(1000f, AUTOPLAY_INTERVAL)
        timer = object : CountDownTimer(AUTOPLAY_INTERVAL, AUTOPLAY_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {
//                int progress = (int)(5000 - millisUntilFinished) / 5;
            }

            override fun onFinish() {
                if (mPlayListListener != null && mPlayListListener!!.hasNext()) {
                    mPlayListListener!!.playNext()
                }
            }
        }
        timer?.start()
    }

    private fun stopAutoplayTimer() {
        if (timer != null) {
            timer!!.cancel()
        }
    }

    fun resizeView(size: Point) {
        val playerWidth: Int
        val playerHeight: Int
        val controllerWidth: Int
        val controllerHeight: Int
        playerWidth = size.x
        if (size.x > size.y) { //landscape
            playerHeight = size.y
            controllerWidth = size.x
            controllerHeight = size.y
        }
        else {
            playerHeight = playerWidth * 9 / 16
            controllerWidth = playerWidth
            controllerHeight = playerHeight
        }
        var params: ViewGroup.LayoutParams = layoutParams
        params.width = controllerWidth
        params.height = controllerHeight
        layoutParams = params
        params = binding.playerContainer.layoutParams
        params.width = playerWidth
        params.height = playerHeight
        binding.playerContainer.layoutParams = params
    }

    companion object {
        private const val UPDATE_PROGRESS = 21
        private const val FORWARD_BACKWARD_DURATION_IN_MILLIS = 10000
    }
}