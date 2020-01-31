package com.banglalink.toffee.ui.player;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;

import com.banglalink.toffee.BuildConfig;
import com.banglalink.toffee.listeners.OnPlayerControllerChangedListener;
import com.banglalink.toffee.model.Channel;
import com.banglalink.toffee.model.ChannelInfo;
import com.banglalink.toffee.ui.common.BaseAppCompatActivity;
import com.banglalink.toffee.util.Utils;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.Util;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;


/**
 * Created by shantanu on 5/5/17.
 */

public abstract class PlayerActivity extends BaseAppCompatActivity implements OnPlayerControllerChangedListener, Player.EventListener {
    protected Handler handler;

    protected SimpleExoPlayer player;
    private DefaultTrackSelector defaultTrackSelector;
    private HlsMediaSource.Factory hlsDataSourceFactory;
    private DefaultDataSourceFactory defaultDataSourceFactory;


    protected ChannelInfo channelInfo;
    private boolean isShowingTrackSelectionDialog;

    private static final CookieManager DEFAULT_COOKIE_MANAGER;
    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }
        defaultDataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "Exo2"));
        initPlayer();
    }


    private void initPlayer() {
        hlsDataSourceFactory = new HlsMediaSource.Factory(defaultDataSourceFactory);

        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter.Builder(this).build();
        AdaptiveTrackSelection.Factory adaptiveTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
        defaultTrackSelector = new DefaultTrackSelector(this, adaptiveTrackSelectionFactory);

        player = new SimpleExoPlayer.Builder(/* context= */ this)
                .setBandwidthMeter(defaultBandwidthMeter)
                .setTrackSelector(defaultTrackSelector)
                .build();
        if(BuildConfig.DEBUG){
            player.addAnalyticsListener(new EventLogger(defaultTrackSelector));
        }
//        player.addListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        player.setPlayWhenReady(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    protected void playChannel(ChannelInfo channelInfo) {
        this.channelInfo = channelInfo;
        Uri uri = Uri.parse( Channel.createChannel(channelInfo).getContentUri(this));
        MediaSource mediaSource = hlsDataSourceFactory.createMediaSource(uri);
        player.seekTo(0);
        player.prepare(mediaSource);
        player.setPlayWhenReady(true);
    }

    //This will be called due to session token change while playing content
    protected void reloadChannel(){
        if(channelInfo!=null && player.isPlaying()){
            playChannel(channelInfo);
        }
    }


    @Override
    public boolean onPlayButtonPressed(int currentState) {
        return false;
    }

    @Override
    public boolean onFullScreenButtonPressed() {
        boolean landscape = Utils.isFullScreen(this);
        if(!landscape)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        else
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        return true;
    }

    @Override
    public boolean onDrawerButtonPressed() {
        return false;
    }

    @Override
    public boolean onMinimizeButtonPressed() {
        return false;
    }

    @Override
    public boolean onOptionMenuPressed() {
                TrackSelectionDialog trackSelectionDialog =
                TrackSelectionDialog.createForTrackSelector(
                        defaultTrackSelector,
                        /* onDismissListener= */ dismissedDialog -> {
                            isShowingTrackSelectionDialog = false;
                            onTrackerDialogDismissed();
                        });
        trackSelectionDialog.show(this.getSupportFragmentManager(), /* tag= */ null);
        return true;
    }

    protected void onTrackerDialogDismissed(){
        //hook for subclass to listen the dismiss event
    }
    @Override
    public boolean onSeekPosition(int position) {
        return false;
    }

    @Override
    public boolean onShareButtonPressed() {
        if(channelInfo != null) {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/html");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, channelInfo.video_share_url);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
            return true;
        }
        return false;
    }
}
