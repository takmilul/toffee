package com.banglalink.toffee.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import androidx.mediarouter.app.MediaRouteButton;

import com.banglalink.toffee.R;
import com.banglalink.toffee.listeners.MediaPlayerListener;
import com.banglalink.toffee.listeners.OnPlayerControllerChangedListener;
import com.banglalink.toffee.ui.player.DemoPlayer;
import com.banglalink.toffee.ui.player.PlayerFragment2;
import com.banglalink.toffee.ui.player.Quality;
import com.banglalink.toffee.ui.player.QualityListAdapter;
import com.banglalink.toffee.util.Utils;
import com.google.android.exoplayer.MediaFormat;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

/**
 * Created by shantanu on 5/4/16.
 */
public class ExpoMediaController2 extends FrameLayout implements  View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private static final int UPDATE_PROGRESS = 21;
    private final Context mContext;
    private LayoutInflater inflater;
    private ImageView playButton;
    private ProgressBar buffering;
    private ImageView audioButton;
    private ImageView videoButton;
    private ImageView fullScreenButton;
    private MessageHandler handler;
    private OnPlayerControllerChangedListener onPlayerControllerChangedListener;
    private SeekBar seekBar;
    private MediaPlayerListener mPlayer;
    private Chronometer mCurrentTime,mEndTime;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private ImageView shareButton;
    private VerticalSeekBar vbSeekBar;
    private ImageView volumeButton;
    private ImageView minimizeButton;
    public boolean isEnd = false;
    private ImageView drawerButton;
    public MediaRouteButton castButton;
    private Activity activity;
    public boolean isMinimize;
    private PlayerFragment2 fragment;
    private long lastPlayerPosition = 0;

    public ExpoMediaController2(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        handler = new MessageHandler();
        initView();
    }


    public ExpoMediaController2(Context context){
        super(context);
        this.mContext = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        handler = new MessageHandler();
        initView();
    }

    public void setOnPlayerControllerChangedListener(OnPlayerControllerChangedListener onPlayerControllerChangedListener){
        this.onPlayerControllerChangedListener = onPlayerControllerChangedListener;
    }

    private void initView(){
        View v = inflater.inflate(R.layout.media_control_layout2, this);
        shareButton = (ImageView) v.findViewById(R.id.share);
        volumeButton = (ImageView) v.findViewById(R.id.volume);
        volumeButton.setOnClickListener(this);
        minimizeButton = (ImageView)v.findViewById(R.id.minimize);
        minimizeButton.setOnClickListener(this);
        playButton = (ImageView) v.findViewById(R.id.play);
        playButton.setOnClickListener(this);
        buffering = (ProgressBar) v.findViewById(R.id.buffering);
        audioButton = (ImageView) v.findViewById(R.id.audio_option);
        videoButton = (ImageView) v.findViewById(R.id.video_option);
        fullScreenButton = (ImageView) v.findViewById(R.id.fullscreen);
        mCurrentTime = (Chronometer) v.findViewById(R.id.current_time);
        castButton = (MediaRouteButton) v.findViewById(R.id.cast);
        drawerButton = (ImageView)v.findViewById(R.id.drawer);
        drawerButton.setOnClickListener(this);

        mEndTime = (Chronometer) v.findViewById(R.id.duration);
        seekBar = (SeekBar) v.findViewById(R.id.progress);
        seekBar.setMax(1000);
        seekBar.setOnSeekBarChangeListener(this);
        vbSeekBar = (VerticalSeekBar) v.findViewById(R.id.bar);
        vbSeekBar.setOnSeekBarChangeListener(this);
        vbSeekBar.setMax(100);
        audioButton.setOnClickListener(this);
        videoButton.setOnClickListener(this);
        fullScreenButton.setOnClickListener(this);
        shareButton.setOnClickListener(this);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
    }

    public void setPlayer(MediaPlayerListener mediaPlayer){
        this.mPlayer = mediaPlayer;
    }

    public MediaPlayerListener getPlayer(){
        return this.mPlayer;
    }

    public boolean showControls(){
        boolean status = false;
        handler.removeCallbacks(hideRunnable);
        Log.e("controls","visibility " + getVisibility() + " minimize " + isMinimize);
        if(getVisibility() != VISIBLE && !isMinimize) {
            updateControllerState();
            setVisibility(VISIBLE);
            status = true;
        }
        updateSeekBar();
        return status;
    }

    private void updateControllerState() {
        if(mPlayer == null) return;
        if(mPlayer.isPlaying()){
            playButton.setImageResource(R.mipmap.ic_media_pause);
        }
        else {
            playButton.setImageResource(R.mipmap.ic_media_play);
        }
    }

    public static final int STATE_BUFFERING = 0;
    public static final int STATE_PLAYING = 1;
    public static final int STATE_PAUSE = 2;

    public void setPlayState(int state){
        if(state == STATE_BUFFERING){//buffering
            playButton.setVisibility(GONE);
            buffering.setVisibility(VISIBLE);
        }
        else if(state == STATE_PLAYING) { //playing
            playButton.setImageResource(R.mipmap.ic_media_pause);
            buffering.setVisibility(GONE);
            playButton.setVisibility(VISIBLE);
        }
        else if(state == STATE_PAUSE){ //pause or end
            playButton.setImageResource(R.mipmap.ic_media_play);
            buffering.setVisibility(GONE);
            playButton.setVisibility(VISIBLE);
        }
    }

    Runnable hideRunnable = new Runnable(){
        public void run(){
            if(getVisibility() != INVISIBLE) {
                setVisibility(INVISIBLE);
            }
        }
    };
    public long getLastPlayerPosition(){
        return lastPlayerPosition;
    }

    private void updateSeekBar(){
        Log.e("controls","updating seekbar ");
        if(mPlayer == null){
            return;
        }
        lastPlayerPosition = mPlayer.getCurrentPosition();
        long duration = mPlayer.getDuration();
        if (duration > 0) {
            // use long to avoid overflow
            long pos = 1000L * lastPlayerPosition / duration;
            seekBar.setEnabled(true);
            seekBar.setProgress((int) pos);
            mEndTime.setVisibility(VISIBLE);
        } else {
            seekBar.setEnabled(false);
            mEndTime.setVisibility(INVISIBLE);
            Log.e("seek bar: ", "seek bar is disable");
        }
        int percent = mPlayer.getBufferPercentage();
        seekBar.setSecondaryProgress(percent * 10);
        if (mEndTime != null)
            mEndTime.setText(stringForTime(duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime(lastPlayerPosition));
        Log.e("lastPlayerPosition: ", "" + lastPlayerPosition);
        Log.e("duration: ", "" + duration);
        Log.e("percent: ", "" + percent);
        if (getVisibility() == VISIBLE && mPlayer.isPlaying()) {
            Message msg = new Message();
            msg.what = UPDATE_PROGRESS;
            handler.sendMessageDelayed(msg, 1000);
        }
    }


    private String stringForTime(long timeMs) {
        long totalSeconds = timeMs / 1000;

        int seconds = (int) (totalSeconds % 60);
        int minutes = (int) ((totalSeconds / 60) % 60);
        int hours   = (int) (totalSeconds / 3600);

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    public void hideControls(long delay){
        handler.removeCallbacks(hideRunnable);
        handler.postDelayed(hideRunnable,delay);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!fromUser && seekBar == this.seekBar) {
            return;
        }
        if(seekBar == this.seekBar) {
            long duration = mPlayer.getDuration();
            long newPosition = (duration * progress) / 1000L;
            mPlayer.seekTo((int) newPosition);
            if (mCurrentTime != null) {
                mCurrentTime.setText(stringForTime((int) newPosition));
            }
            isEnd = false;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        updateSeekBar();
    }

    public void setActivity(PlayerFragment2 fragment) {
        this.activity = fragment.getActivity();
        this.fragment = fragment;
    }


    private class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_PROGRESS:
                    Log.e("update seek","from timer");
                    updateSeekBar();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    public void onFullScreen(boolean state) {
        if(state){ //fullscreen
            minimizeButton.setVisibility(INVISIBLE);
            drawerButton.setVisibility(INVISIBLE);
            fullScreenButton.setImageResource(R.mipmap.ic_fullscreen_exit);
        }
        else{
            minimizeButton.setVisibility(VISIBLE);
            drawerButton.setVisibility(VISIBLE);
            fullScreenButton.setImageResource(R.mipmap.ic_media_fullscreen);
        }
    }


    @Override
    public void onClick(View v) {
        if(v == playButton && mPlayer != null){
            if(onPlayerControllerChangedListener != null){
                if(onPlayerControllerChangedListener.onPlayButtonPressed(mPlayer.isPlaying()? STATE_PLAYING:STATE_PAUSE)){
                    return;
                }
            }
            if(mPlayer.isPlaying()){
                mPlayer.pause();
                if(playButton != null) {
                    playButton.setImageResource(R.mipmap.ic_media_play);
                }
                showControls();
            }
            else{
                if(isEnd){
                    mPlayer.seekTo(0);
                    isEnd = false;
                }
                mPlayer.start();
                if(playButton != null) {
                    playButton.setImageResource(R.mipmap.ic_media_pause);
                }
                hideControls(3000);
            }
            updateSeekBar();
        }
//        else if(v == audioButton && onControlButtonPressListener != null){
//            onControlButtonPressListener.onAudioOptionPress(popupView);
//        }
        else if(v == videoButton && videoButton.isEnabled()){
            if(onPlayerControllerChangedListener != null){
                if(onPlayerControllerChangedListener.onOptionMenuPressed()) return;
            }
            final QualityListAdapter mAdapter = new QualityListAdapter(getContext(),R.layout.list_item_quality);
            mAdapter.addAll(getVideoQualityList());
            ActionMenuList actionMenuList = new ActionMenuList(getContext(), mAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Quality quality = mAdapter.getItem(which);
                    if(mPlayer != null){
                        mPlayer.setSelectedTrack(quality.type,quality.index);
                    }
                }
            });
            actionMenuList.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    dialog.dismiss();
                    fragment.updateFullScreenState();
                }
            });
            if(mAdapter.getCount() > 0){
                actionMenuList.show();
            }
        }
        else if(v == fullScreenButton){
            final boolean currentState = Utils.isFullScreen(activity);
            if(onPlayerControllerChangedListener != null && onPlayerControllerChangedListener.onFullScreenButtonPressed(currentState)) return;
            fragment.onFullScreenButtonPressed(currentState);
        }
        else if(v == shareButton && fragment != null){
            fragment.onShare();
        }
        else if( v == minimizeButton && onPlayerControllerChangedListener != null){
            onPlayerControllerChangedListener.onMinimizeButtonPressed();
        }
        else if (v == drawerButton && onPlayerControllerChangedListener != null) {
            onPlayerControllerChangedListener.onDrawerButtonPressed();
        }
    }

    private List<Quality> getVideoQualityList(){
        List<Quality> videoQualityList = new ArrayList<>();
        int videoIndex = mPlayer.getSelectedTrack(DemoPlayer.TYPE_VIDEO);
        if(videoIndex >= 0){
            int count = mPlayer.getTrackCount(DemoPlayer.TYPE_VIDEO);
            for(int i=0;i<count;i++){
                boolean selected = (videoIndex == i);
                Quality quality = new Quality(i,
                        buildResolutionString(mPlayer.getTrackFormat(DemoPlayer.TYPE_VIDEO,i)),
                        Quality.TYPE_VIDEO,selected);
                videoQualityList.add(quality);
            }
        }
        return videoQualityList;
    }

    private static String buildResolutionString(MediaFormat format) {
        if(format.adaptive){
            return "Auto";
        }
        return format.width == MediaFormat.NO_VALUE || format.height == MediaFormat.NO_VALUE
                ? "" : format.width + "x" + format.height;
    }
}
