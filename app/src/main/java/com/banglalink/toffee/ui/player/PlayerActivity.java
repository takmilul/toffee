package com.banglalink.toffee.ui.player;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.banglalink.toffee.BuildConfig;
import com.banglalink.toffee.analytics.ToffeeAnalytics;
import com.banglalink.toffee.data.storage.Preference;
import com.banglalink.toffee.listeners.OnPlayerControllerChangedListener;
import com.banglalink.toffee.model.Channel;
import com.banglalink.toffee.model.ChannelInfo;
import com.banglalink.toffee.ui.common.BaseAppCompatActivity;
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
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.Util;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

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

    private DefaultTrackSelector.Parameters trackSelectorParameters;
    private TrackGroupArray lastSeenTrackGroupArray;


    private boolean startAutoPlay;
    private int startWindow;
    private long startPosition;

    private PlayerEventListener playerEventListener = new PlayerEventListener();

    @Nullable
    private ChannelInfo channelInfo;

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
            player.setPlayWhenReady(false);
            if(BuildConfig.DEBUG){
                player.addAnalyticsListener(new EventLogger(defaultTrackSelector));
            }
        }
        if(channelInfo!=null){
            reloadChannel();
        }
    }

    private void releasePlayer() {
        if (player != null) {
            player.removeListener(playerEventListener);
            updateTrackSelectorParameters();
            updateStartPosition();
            player.release();
            player = null;
            defaultTrackSelector = null;
        }
    }

    private void updateTrackSelectorParameters() {
        if (defaultTrackSelector != null) {
            trackSelectorParameters = defaultTrackSelector.getParameters();
        }
    }

    protected void updateStartPosition() {
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
        DataSource.Factory dataSourceFactory = new DefaultHttpDataSourceFactory(
                Util.getUserAgent(this, "Toffee"));
        HlsMediaSource.Factory hlsDataSourceFactory = new HlsMediaSource.Factory(dataSourceFactory);
        hlsDataSourceFactory.setAllowChunklessPreparation(true);

        return
                new HlsMediaSource.Factory(
                        dataType -> {
                            HttpDataSource dataSource =
                                    new DefaultHttpDataSource(Util.getUserAgent(this, "Toffee"));
                            dataSource.setRequestProperty("TOFFEE-SESSION-TOKEN", Preference.Companion.getInstance().getHeaderSessionToken());
                            return dataSource;
                        })
                        .createMediaSource(uri);
    }

    protected void playChannel(ChannelInfo channelInfo) {
        this.channelInfo = channelInfo;
        if(player!=null){
            player.setPlayWhenReady(true);
            MediaSource mediaSource = prepareMedia(Uri.parse( Channel.createChannel(channelInfo).getContentUri(this)));
            player.prepare(mediaSource);
            if(!channelInfo.isLive()){
                player.seekTo(0);
            }

        }
    }

    protected void clearChannel(){//set channelInfo = null
        channelInfo = null;
    }

    //This will be called due to session token change while playing content
    protected void reloadChannel(){
        if(channelInfo!=null && player!=null){
            MediaSource mediaSource = prepareMedia(Uri.parse( Channel.createChannel(channelInfo).getContentUri(this)));
            player.setPlayWhenReady(player.getPlayWhenReady());
            boolean haveStartPosition = startWindow != C.INDEX_UNSET;
            player.prepare(mediaSource, !haveStartPosition, false);
            if (haveStartPosition && !channelInfo.isLive()) {
                player.seekTo(startWindow, startPosition);
            }
        }
    }


    @Override
    public boolean onPlayButtonPressed(int currentState) {
        return false;
    }

    @Override
    public boolean onFullScreenButtonPressed() {
        boolean isPortrait = getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        if(isPortrait)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        else
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        return true;
    }

    @Override
    public void onPlayerIdleDueToError() {
        if(player!=null && player.getPlayWhenReady()){
            ToffeeAnalytics.INSTANCE.logForcePlay();
            reloadChannel();
        }
    }

    @Override
    public boolean onOptionMenuPressed() {
        TrackSelectionDialog bottomSheetDialog = new TrackSelectionDialog(this);
        bottomSheetDialog.init(defaultTrackSelector);
        getLifecycle().addObserver(bottomSheetDialog);

        bottomSheetDialog.setOnDismissListener(dialogInterface -> {
            getLifecycle().removeObserver(bottomSheetDialog);
            onTrackerDialogDismissed();
        });
        bottomSheetDialog.setOnCancelListener(dialogInterface -> {
            getLifecycle().removeObserver(bottomSheetDialog);
            onTrackerDialogDismissed();
        });
        bottomSheetDialog.show();
        bottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);

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
            FirebaseCrashlytics.getInstance().recordException(e);
            if (isBehindLiveWindow(e)) {
                clearStartPosition();
                reloadChannel();
                if(channelInfo!=null)
                    ToffeeAnalytics.INSTANCE.playerError(channelInfo,"Behind live window");//log error
            }
            else{
                if(channelInfo!=null && e.getSourceException().getMessage()!=null)
                    ToffeeAnalytics.INSTANCE.playerError(channelInfo,e.getSourceException().getMessage());//trying to log error with proper message.
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
