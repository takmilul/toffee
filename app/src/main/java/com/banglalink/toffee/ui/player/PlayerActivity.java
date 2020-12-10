package com.banglalink.toffee.ui.player;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.banglalink.toffee.BuildConfig;
import com.banglalink.toffee.analytics.HeartBeatManager;
import com.banglalink.toffee.analytics.ToffeeAnalytics;
import com.banglalink.toffee.data.storage.PlayerPreference;
import com.banglalink.toffee.data.storage.Preference;
import com.banglalink.toffee.listeners.OnPlayerControllerChangedListener;
import com.banglalink.toffee.model.AppSettingsKt;
import com.banglalink.toffee.model.Channel;
import com.banglalink.toffee.model.ChannelInfo;
import com.banglalink.toffee.ui.common.BaseAppCompatActivity;
import com.banglalink.toffee.usecase.ReportLastPlayerSession;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.Util;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.jetbrains.annotations.NotNull;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;


/**
 * Created by shantanu on 5/5/17.
 */

public abstract class PlayerActivity extends BaseAppCompatActivity implements OnPlayerControllerChangedListener, Player.EventListener, AnalyticsListener {
    protected Handler handler;

    @Nullable
    protected SimpleExoPlayer player;
    private DefaultTrackSelector defaultTrackSelector;

    private DefaultTrackSelector.Parameters trackSelectorParameters;
    private TrackGroupArray lastSeenTrackGroupArray;

    private PlayerViewModel playerViewModel;

    private boolean startAutoPlay;
    private int startWindow;
    private long startPosition;

    private final PlayerEventListener playerEventListener = new PlayerEventListener();
    private PlayerAnalyticsListener playerAnalyticsListener;

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
        playerViewModel = ViewModelProviders.of(this).get(PlayerViewModel.class);

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
        HeartBeatManager.INSTANCE.getHeartBeatEventLiveData().observe(this, aBoolean -> {//In each heartbeat we are checking channel's expire date. Seriously??
            if(channelInfo!=null && channelInfo.isExpired(Preference.Companion.getInstance().getSystemTime())){
                if(player!=null){
                    player.stop(true);
                }
                onContentExpired();//content is expired. Notify the subclass
            }
            if(playerAnalyticsListener!=null){
                //In every heartbeat event we are sending bandwitdh data to Pubsub
                Log.e("PLAYER BYTES","Flushing to pubsub");
                playerViewModel.reportBandWidthFromPlayerPref(playerAnalyticsListener.getDurationInSeconds(),playerAnalyticsListener.getTotalBytesInMB());
                playerAnalyticsListener.resetData();
            }
        });
    }

    protected void onContentExpired(){
        //hook for subclass
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
            AdaptiveTrackSelection.Factory adaptiveTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
            defaultTrackSelector = new DefaultTrackSelector(this, adaptiveTrackSelectionFactory);
            defaultTrackSelector.setParameters(trackSelectorParameters);
            lastSeenTrackGroupArray = null;
            playerAnalyticsListener = new PlayerAnalyticsListener();

            player =
                    new SimpleExoPlayer.Builder(this)
                            .setTrackSelector(defaultTrackSelector)
                            .build();
            player.addAnalyticsListener(playerAnalyticsListener);
            player.addListener(playerEventListener);
            player.setPlayWhenReady(false);
            if(BuildConfig.DEBUG){
                player.addAnalyticsListener(new EventLogger(defaultTrackSelector));
            }
        }
        //we are checking whether there is already channelInfo exist. If not null then play it
        if(channelInfo!=null){
            playChannel(channelInfo);
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
            if(playerAnalyticsListener!=null){
                PlayerPreference.Companion.getInstance().savePlayerSessionBandWidth(playerAnalyticsListener.getDurationInSeconds(),playerAnalyticsListener.getTotalBytesInMB());
            }
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
                                    new DefaultHttpDataSource(AppSettingsKt.getTOFFEE_HEADER());
                            dataSource.setRequestProperty("TOFFEE-SESSION-TOKEN", Preference.Companion.getInstance().getHeaderSessionToken());
                            return dataSource;
                        })
                        .createMediaSource(uri);
    }

    protected void playChannel(@NonNull  ChannelInfo channelInfo) {
        String uri = Channel.createChannel(channelInfo).getContentUri(this);
        if(uri == null){//in this case settings does not allow us to play content. So stop player and trigger event viewing stop
            if(player!=null)
                player.stop(true);
            channelCannotBePlayedDueToSettings();//notify hook/subclass
            HeartBeatManager.INSTANCE.triggerEventViewingContentStop();
            return;
        }
        //Checking whether we need to reload or not. Reload happens because of network switch or re-initialization of player
        boolean isReload = false;
        ChannelInfo oldChannelInfo = this.channelInfo;
        this.channelInfo = channelInfo;
        if(oldChannelInfo != null && oldChannelInfo.id.equalsIgnoreCase(this.channelInfo.id)){
            isReload = true;//that means we have reload situation. We need to start where we left for VODs
        }

        if(player!=null){
            HeartBeatManager.INSTANCE.triggerEventViewingContentStart(Integer.parseInt(channelInfo.id),channelInfo.type);
            player.setPlayWhenReady(!isReload || player.getPlayWhenReady());
            MediaSource mediaSource = prepareMedia(Uri.parse(uri));
            if(isReload){//We need to start where we left off for VODs
                boolean haveStartPosition = startWindow != C.INDEX_UNSET;
                if (haveStartPosition && !channelInfo.isLive()) {
                    player.prepare(mediaSource, false, false);
                    player.seekTo(startWindow, startPosition);//we seek to where we left for VODs
                    return;
                }
            }
            player.prepare(mediaSource);//Non reload event or reload for live. Just prepare the media and play it
        }
    }

    //This will be called due to session token change while playing content or after init of player
    protected void reloadChannel(){
        if(channelInfo!=null && channelInfo.isExpired(Preference.Companion.getInstance().getSystemTime())){
            //channel is expired. Stop the player and notify hook/subclass
            if(player!=null){
                player.stop(true);
            }
            onContentExpired();
            return;
        }
        if(channelInfo!=null){
            playChannel(this.channelInfo);
        }
    }

    protected void channelCannotBePlayedDueToSettings(){
        //subclass will hook into it
    }

    protected void clearChannel(){//set channelInfo = null
        channelInfo = null;
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
        if(defaultTrackSelector==null || defaultTrackSelector.getCurrentMappedTrackInfo() == null)
            return false;

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

    private static class PlayerAnalyticsListener implements AnalyticsListener{
        private long totalBytesInMB = 0;
        private long initialTimeStamp = 0;
        private long durationInMillis = 0;

        @Override
        public void onLoadCompleted(@NotNull EventTime eventTime, @NotNull MediaSourceEventListener.LoadEventInfo loadEventInfo, @NotNull MediaSourceEventListener.MediaLoadData mediaLoadData) {
            try{
                totalBytesInMB += (loadEventInfo.bytesLoaded);
                if(initialTimeStamp == 0){
                    initialTimeStamp = System.currentTimeMillis();
                }else{
                    durationInMillis = System.currentTimeMillis() - initialTimeStamp;
                }
                Log.e("PLAYER BYTES","Event time "+(durationInMillis /1000)+" Bytes "+(totalBytesInMB * 0.000001) +" MB");
            }catch (Exception e){
                ToffeeAnalytics.INSTANCE.logBreadCrumb("Exception in PlayerAnalyticsListener");
            }

        }

        double getTotalBytesInMB(){
            return (totalBytesInMB * 0.000001);
        }

        long getDurationInSeconds(){
            return durationInMillis/1000;
        }

        void resetData(){
            totalBytesInMB = 0;
            durationInMillis = 0;
            initialTimeStamp = 0;
        }


    }

    private class PlayerEventListener implements Player.EventListener {

        @Override
        public void onPlayerError(ExoPlaybackException e) {
            e.printStackTrace();
            ToffeeAnalytics.INSTANCE.logException(e);
            if (isBehindLiveWindow(e)) {
                clearStartPosition();
                reloadChannel();
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
