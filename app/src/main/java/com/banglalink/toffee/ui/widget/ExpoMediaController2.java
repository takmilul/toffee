package com.banglalink.toffee.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import androidx.databinding.DataBindingUtil;

import com.banglalink.toffee.R;
import com.banglalink.toffee.databinding.MediaControlLayout2Binding;
import com.banglalink.toffee.listeners.OnPlayerControllerChangedListener;
import com.google.android.exoplayer2.Player;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import static com.google.android.exoplayer2.Player.STATE_BUFFERING;
import static com.google.android.exoplayer2.Player.STATE_ENDED;
import static com.google.android.exoplayer2.Player.STATE_IDLE;
import static com.google.android.exoplayer2.Player.STATE_READY;

/**
 * Created by shantanu on 5/4/16.
 */
public class ExpoMediaController2 extends FrameLayout implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, Player.EventListener, DraggerLayout.OnPositionChangedListener, TextureView.SurfaceTextureListener {
    private static final int UPDATE_PROGRESS = 21;
    private LayoutInflater inflater;
    private MessageHandler handler;
    private List<OnPlayerControllerChangedListener> onPlayerControllerChangedListeners = new ArrayList<>();
    private @Nullable
    Player simpleExoPlayer;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private boolean isMinimize;
    private long lastPlayerPosition = 0;


    private int videoWidth = 1920;
    private int videoHeight = 1080;

    private MediaControlLayout2Binding binding;

    public ExpoMediaController2(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        handler = new MessageHandler();
        initView();
    }


    public ExpoMediaController2(Context context) {
        super(context);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        handler = new MessageHandler();
        initView();
    }

    public void addPlayerControllerChangeListener(OnPlayerControllerChangedListener onPlayerControllerChangedListener) {
        onPlayerControllerChangedListeners.add(onPlayerControllerChangedListener);
    }

    public void clearListeners() {
        onPlayerControllerChangedListeners.clear();
    }

    private void initView() {
        binding = DataBindingUtil.inflate(inflater, R.layout.media_control_layout2, this, true);
        binding.minimize.setOnClickListener(this);
        binding.play.setOnClickListener(this);
        binding.drawer.setOnClickListener(this);


        binding.progress.setMax(1000);
        binding.progress.setOnSeekBarChangeListener(this);
        binding.videoOption.setOnClickListener(this);
        binding.fullscreen.setOnClickListener(this);
        binding.preview.setOnClickListener(this);
        binding.share.setOnClickListener(this);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
    }


    //Use this method to set and unset the player
    public void setPlayer(@Nullable Player simpleExoPlayer) {
        if (this.simpleExoPlayer == simpleExoPlayer) {
            return;
        }
        binding.textureView.setSurfaceTextureListener(this);
        if(binding.textureView.isAvailable()){
            binding.preview.setImageBitmap(binding.textureView.getBitmap());
        }
        Player oldPlayer = this.simpleExoPlayer;//get reference of old player which attached previously
        if (oldPlayer != null) {//if old player not null then clear it
            oldPlayer.removeListener(this);
            if (oldPlayer.getVideoComponent() != null) {
                oldPlayer.getVideoComponent().clearVideoTextureView(binding.textureView);
            }
        }
        this.simpleExoPlayer = simpleExoPlayer;
        if (simpleExoPlayer != null) {
            this.simpleExoPlayer.addListener(this);
        }
    }


    public void showWifiOnlyMessage() {
        binding.preview.setImageResource(R.mipmap.watch_wifi_only_msg);
        hideControls(0);
        binding.preview.setOnClickListener(null);
    }

    public boolean showControls() {
        boolean status = false;
        handler.removeCallbacks(hideRunnable);
        Log.e("controls", "visibility " + getVisibility() + " minimize " + isMinimize);
        if (binding.controller.getVisibility() != VISIBLE && !isMinimize) {
            binding.controller.setVisibility(VISIBLE);
            status = true;
        }
        updateSeekBar();
        return status;
    }


    Runnable hideRunnable = new Runnable() {
        public void run() {
            if (binding.controller.getVisibility() != INVISIBLE) {
                binding.controller.setVisibility(INVISIBLE);
            }
        }
    };

    public long getLastPlayerPosition() {
        return lastPlayerPosition;
    }

    private void updateSeekBar() {
        Log.e("controls", "updating seekbar ");
        if (simpleExoPlayer == null) {
            return;
        }
        lastPlayerPosition = simpleExoPlayer.getCurrentPosition();
        long duration = simpleExoPlayer.getDuration();
        if (duration> 0 && !simpleExoPlayer.isCurrentWindowLive()) {
            // use long to avoid overflow
            long pos = 1000L * lastPlayerPosition / duration;
            binding.progress.setEnabled(true);
            binding.progress.setProgress((int) pos);
            binding.duration.setVisibility(VISIBLE);
            binding.currentTime.setVisibility(VISIBLE);
        } else {
            binding.progress.setEnabled(false);
            binding.duration.setVisibility(INVISIBLE);
            binding.currentTime.setVisibility(INVISIBLE);
            Log.e("seek bar: ", "seek bar is disable");
        }
        int percent = simpleExoPlayer.getBufferedPercentage();
        binding.progress.setSecondaryProgress(percent * 10);
        binding.duration.setText(stringForTime(duration));
        binding.currentTime.setText(stringForTime(lastPlayerPosition));
        Log.e("lastPlayerPosition: ", "" + lastPlayerPosition);
        Log.e("duration: ", "" + duration);
        Log.e("percent: ", "" + percent);
        if (getVisibility() == VISIBLE && simpleExoPlayer.isPlaying()) {
            Message msg = new Message();
            msg.what = UPDATE_PROGRESS;
            handler.sendMessageDelayed(msg, 1000);
        }
    }


    private String stringForTime(long timeMs) {
        long totalSeconds = timeMs / 1000;

        int seconds = (int) (totalSeconds % 60);
        int minutes = (int) ((totalSeconds / 60) % 60);
        int hours = (int) (totalSeconds / 3600);

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    public void hideControls(long delay) {
        handler.removeCallbacks(hideRunnable);
        handler.postDelayed(hideRunnable, delay);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!fromUser && seekBar == this.binding.progress) {
            return;
        }
        if (seekBar == this.binding.progress && simpleExoPlayer != null) {
            long duration = simpleExoPlayer.getDuration();
            long newPosition = (duration * progress) / 1000L;
            simpleExoPlayer.seekTo((int) newPosition);
            if (binding.currentTime != null) {
                binding.currentTime.setText(stringForTime((int) newPosition));
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        updateSeekBar();
    }

    @Override
    public void onViewMinimize() {
        binding.root.setKeepScreenOn(true);
        isMinimize = true;
        binding.textureView.setOnClickListener(null);
        hideControls(0);
    }

    @Override
    public void onViewMaximize() {
        binding.root.setKeepScreenOn(true);
        isMinimize = false;
        binding.textureView.setOnClickListener(this);
        if (simpleExoPlayer != null && simpleExoPlayer.isPlaying()) {
            hideControls(2000);
        } else {
            showControls();
        }
    }

    @Override
    public void onViewDestroy() {
        binding.root.setKeepScreenOn(false);
        if (simpleExoPlayer != null) {
            simpleExoPlayer.stop();
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        if (simpleExoPlayer != null && simpleExoPlayer.getVideoComponent() != null) {
            simpleExoPlayer.getVideoComponent().setVideoTextureView(binding.textureView);
        }

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }


    private class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_PROGRESS:
                    Log.e("update seek", "from timer");
                    updateSeekBar();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    public void onFullScreen(boolean state) {
        if (state) { //fullscreen
            binding.minimize.setVisibility(INVISIBLE);
            binding.drawer.setVisibility(INVISIBLE);
            binding.fullscreen.setImageResource(R.mipmap.ic_fullscreen_exit);
        } else {
            binding.minimize.setVisibility(VISIBLE);
            binding.drawer.setVisibility(VISIBLE);
            binding.fullscreen.setImageResource(R.mipmap.ic_media_fullscreen);
        }
    }


    @Override
    public void onClick(View v) {
        if (v == binding.play && simpleExoPlayer != null) {
            if (simpleExoPlayer.isPlaying()) {
                simpleExoPlayer.setPlayWhenReady(false);
                showControls();
            } else {
                if (simpleExoPlayer.getPlaybackState() == STATE_ENDED) {
                    simpleExoPlayer.seekTo(0);
                }
                simpleExoPlayer.setPlayWhenReady(true);
                hideControls(3000);
            }
            updateSeekBar();
        } else if (v == binding.videoOption && binding.videoOption.isEnabled()) {
            for (OnPlayerControllerChangedListener OnPlayerControllerChangedListener : onPlayerControllerChangedListeners) {
                OnPlayerControllerChangedListener.onOptionMenuPressed();
            }
        } else if (v == binding.fullscreen) {
            for (OnPlayerControllerChangedListener onPlayerControllerChangedListener : onPlayerControllerChangedListeners) {
                onPlayerControllerChangedListener.onFullScreenButtonPressed();
            }
        } else if (v == binding.share) {
            for (OnPlayerControllerChangedListener onPlayerControllerChangedListener : onPlayerControllerChangedListeners) {
                onPlayerControllerChangedListener.onShareButtonPressed();
            }
        } else if (v == binding.minimize) {
            for (OnPlayerControllerChangedListener onPlayerControllerChangedListener : onPlayerControllerChangedListeners) {
                onPlayerControllerChangedListener.onMinimizeButtonPressed();
            }

        } else if (v == binding.drawer) {
            for (OnPlayerControllerChangedListener onPlayerControllerChangedListener : onPlayerControllerChangedListeners) {
                onPlayerControllerChangedListener.onDrawerButtonPressed();
            }

        } else if (v == binding.preview) {
            if (showControls()) {
                if (simpleExoPlayer != null && simpleExoPlayer.isPlaying()) {
                    hideControls(3000);
                }
            } else {
                hideControls(0);
            }
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case STATE_BUFFERING:
                binding.preview.setOnClickListener(this);
                binding.preview.setImageResource(android.R.color.black);
                binding.play.setVisibility(GONE);
                binding.buffering.setVisibility(VISIBLE);
                showControls();
                break;
            case STATE_ENDED:
            case STATE_IDLE:
                binding.preview.setImageResource(android.R.color.black);
                binding.play.setImageResource(R.mipmap.ic_media_play);
                binding.buffering.setVisibility(GONE);
                binding.play.setVisibility(VISIBLE);
                showControls();
                break;
            case STATE_READY:
                if (playWhenReady) {
                    binding.preview.setImageResource(0);
                    binding.play.setImageResource(R.mipmap.ic_media_pause);
                    binding.buffering.setVisibility(GONE);
                    binding.play.setVisibility(VISIBLE);
                    showControls();//it is necessary since we don't have preparing state of player
                    hideControls(3000);
                } else {
                    binding.play.setImageResource(R.mipmap.ic_media_play);
                    binding.buffering.setVisibility(GONE);
                    binding.play.setVisibility(VISIBLE);
                    showControls();
                }
                break;
            default:
                break;
        }
    }


    public void resizeView(Point size) {
        int playerWidth;
        int playerHeight;
        int controlerWidth;
        int controlerHeight;

        playerWidth = size.x;
        if (size.x > size.y) { //landscape
            playerHeight = size.y;
            controlerWidth = size.x;
            controlerHeight = size.y;
        } else {
            Log.e("width: ", "" + playerWidth);
            playerHeight = (playerWidth * 9) / 16;
            Log.e("height: ", "" + playerHeight);
            controlerWidth = playerWidth;
            controlerHeight = playerHeight;
        }


        ViewGroup.LayoutParams params;
        params = getLayoutParams();
        params.width = controlerWidth;
        params.height = controlerHeight;
        setLayoutParams(params);

        params = binding.playerContainer.getLayoutParams();
        params.width = playerWidth;
        params.height = playerHeight;
        binding.playerContainer.setLayoutParams(params);

    }
}
