package com.banglalink.toffee.ui.player;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.banglalink.toffee.BuildConfig;
import com.banglalink.toffee.listeners.OnPlayerControllerChangedListener;
import com.banglalink.toffee.model.Channel;
import com.banglalink.toffee.model.ChannelInfo;
import com.banglalink.toffee.ui.common.BaseAppCompatActivity;
import com.banglalink.toffee.util.Utils;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
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

    @Nullable
    protected SimpleExoPlayer player;
    private DefaultTrackSelector defaultTrackSelector;

    private MediaSource mediaSource;
    private DefaultTrackSelector.Parameters trackSelectorParameters;
    private TrackGroupArray lastSeenTrackGroupArray;


    private boolean startAutoPlay;
    private int startWindow;
    private long startPosition;

    private PlayerEventListener playerEventListener = new PlayerEventListener();

    protected ChannelInfo channelInfo;
    private boolean isShowingTrackSelectionDialog;

    private static final CookieManager DEFAULT_COOKIE_MANAGER;
    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private static final String KEY_TRACK_SELECTOR_PARAMETERS = "track_selector_parameters";
    private static final String KEY_WINDOW = "window";
    private static final String KEY_POSITION = "position";
    private static final String KEY_AUTO_PLAY = "auto_play";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }
        if (savedInstanceState != null) {
            trackSelectorParameters = savedInstanceState.getParcelable(KEY_TRACK_SELECTOR_PARAMETERS);
            startAutoPlay = savedInstanceState.getBoolean(KEY_AUTO_PLAY);
            startWindow = savedInstanceState.getInt(KEY_WINDOW);
            startPosition = savedInstanceState.getLong(KEY_POSITION);
        } else {
            DefaultTrackSelector.ParametersBuilder builder =
                    new DefaultTrackSelector.ParametersBuilder(/* context= */ this);
            if (Util.SDK_INT >= 21) {
                builder.setTunnelingAudioSessionId(C.generateAudioSessionIdV21(/* context= */ this));
            }
            trackSelectorParameters = builder.build();
            clearStartPosition();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        updateTrackSelectorParameters();
        updateStartPosition();
        outState.putParcelable(KEY_TRACK_SELECTOR_PARAMETERS, trackSelectorParameters);
        outState.putBoolean(KEY_AUTO_PLAY, startAutoPlay);
        outState.putInt(KEY_WINDOW, startWindow);
        outState.putLong(KEY_POSITION, startPosition);
    }


    private void initializePlayer() {
        if (player == null) {
            DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter.Builder(this).build();
            AdaptiveTrackSelection.Factory adaptiveTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
            defaultTrackSelector = new DefaultTrackSelector(this, adaptiveTrackSelectionFactory);
            defaultTrackSelector.setParameters(trackSelectorParameters);
            lastSeenTrackGroupArray = null;

            player =
                    new SimpleExoPlayer.Builder(/* context= */ this)
                            .setTrackSelector(defaultTrackSelector)
                            .setBandwidthMeter(defaultBandwidthMeter)
                            .build();
            player.addListener(playerEventListener);
            player.setPlayWhenReady(startAutoPlay);
            if(BuildConfig.DEBUG){
                player.addAnalyticsListener(new EventLogger(defaultTrackSelector));
            }
        }
        if(channelInfo!=null){
            mediaSource = prepareMedia(Uri.parse( Channel.createChannel(channelInfo).getContentUri(this)));
            player.setPlayWhenReady(false);
            boolean haveStartPosition = startWindow != C.INDEX_UNSET;
            if (haveStartPosition) {
                player.seekTo(startWindow, startPosition);
            }
            player.prepare(mediaSource, !haveStartPosition, false);
        }
    }

    private void releasePlayer() {
        if (player != null) {
            player.removeListener(playerEventListener);
            updateTrackSelectorParameters();
            updateStartPosition();
            player.release();
            player = null;
            mediaSource = null;
            defaultTrackSelector = null;
        }
    }

    private void updateTrackSelectorParameters() {
        if (defaultTrackSelector != null) {
            trackSelectorParameters = defaultTrackSelector.getParameters();
        }
    }

    private void updateStartPosition() {
        if (player != null) {
            startAutoPlay = player.getPlayWhenReady();
            startWindow = player.getCurrentWindowIndex();
            startPosition = Math.max(0, player.getContentPosition());
        }
    }

    private void clearStartPosition() {
        startAutoPlay = true;
        startWindow = C.INDEX_UNSET;
        startPosition = C.TIME_UNSET;
    }

    private MediaSource prepareMedia(Uri uri){
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "Exo2"));
        HlsMediaSource.Factory hlsDataSourceFactory = new HlsMediaSource.Factory(dataSourceFactory);
        hlsDataSourceFactory.setAllowChunklessPreparation(true);
        mediaSource = hlsDataSourceFactory.createMediaSource(uri);
        return  hlsDataSourceFactory.createMediaSource(uri);
    }

    protected void playChannel(ChannelInfo channelInfo) {
        this.channelInfo = channelInfo;
        if(player!=null){
            mediaSource = prepareMedia(Uri.parse( Channel.createChannel(channelInfo).getContentUri(this)));
            player.prepare(mediaSource);
            player.setPlayWhenReady(true);
            player.seekTo(0);
        }
    }

    //This will be called due to session token change while playing content
    protected void reloadChannel(){
        if(channelInfo!=null && player!=null && player.isPlaying()){
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


    private class PlayerEventListener implements Player.EventListener {

        @Override
        public void onPlayerError(ExoPlaybackException e) {
            e.printStackTrace();
            if (isBehindLiveWindow(e)) {
                clearStartPosition();
                reloadChannel();
                Toast.makeText(PlayerActivity.this,"Behind live window exception",Toast.LENGTH_LONG).show();
            }
            else{
                reloadChannel();
                Toast.makeText(PlayerActivity.this,e.getSourceException().getMessage(),Toast.LENGTH_LONG).show();
            }
        }

        @Override
        @SuppressWarnings("ReferenceEquality")
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            if (trackGroups != lastSeenTrackGroupArray) {
                lastSeenTrackGroupArray = trackGroups;
            }
        }

        private boolean isBehindLiveWindow(ExoPlaybackException e) {
            if (e.type != ExoPlaybackException.TYPE_SOURCE) {
                return false;
            }
            Throwable cause = e.getSourceException();
            while (cause != null) {
                if (cause instanceof BehindLiveWindowException) {
                    return true;
                }
                cause = cause.getCause();
            }
            return false;
        }
    }
}
