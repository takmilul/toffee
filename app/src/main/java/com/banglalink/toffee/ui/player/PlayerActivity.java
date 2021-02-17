package com.banglalink.toffee.ui.player;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.banglalink.toffee.BuildConfig;
import com.banglalink.toffee.R;
import com.banglalink.toffee.analytics.HeartBeatManager;
import com.banglalink.toffee.analytics.ToffeeAnalytics;
import com.banglalink.toffee.listeners.OnPlayerControllerChangedListener;
import com.banglalink.toffee.listeners.PlaylistListener;
import com.banglalink.toffee.model.AppSettingsKt;
import com.banglalink.toffee.model.ChannelInfo;
import com.banglalink.toffee.ui.common.BaseAppCompatActivity;
import com.banglalink.toffee.ui.mychannel.MyChannelPlaylistVideosFragment;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.MediaItem;
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
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.Util;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public abstract class PlayerActivity extends BaseAppCompatActivity implements OnPlayerControllerChangedListener, Player.EventListener, PlaylistListener {
    protected Handler handler;

    @Nullable
    protected SimpleExoPlayer player;
    private DefaultTrackSelector defaultTrackSelector;

    private DefaultTrackSelector.Parameters trackSelectorParameters;
    private TrackGroupArray lastSeenTrackGroupArray;

    protected PlaylistManager playlistManager = new PlaylistManager();
    @Inject HeartBeatManager heartBeatManager;
    private boolean startAutoPlay;
    private int startWindow;
    private long startPosition;

    private PlayerEventListener playerEventListener = new PlayerEventListener();

//    @Nullable
//    protected ChannelInfo channelInfo;

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
        heartBeatManager.getHeartBeatEventLiveData().observe(this, aBoolean -> {//In each heartbeat we are checking channel's expire date. Seriously??
            ChannelInfo cinfo = playlistManager.getCurrentChannel();
            if(cinfo != null && cinfo.isExpired(mPref.getSystemTime())){
                if(player!=null){
                    player.stop(true);
                }
                onContentExpired();//content is expired. Notify the subclass
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

            player =
                    new SimpleExoPlayer.Builder(this)
                            .setTrackSelector(defaultTrackSelector)
                            .build();
            player.addListener(playerEventListener);
            player.setPlayWhenReady(false);
            if(BuildConfig.DEBUG){
                player.addAnalyticsListener(new EventLogger(defaultTrackSelector));
            }
        }
        //we are checking whether there is already channelInfo exist. If not null then play it
        if(playlistManager.getCurrentChannel() != null){
            playChannel(false);
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
                Util.getUserAgent(this, getString(R.string.app_name)));
        HlsMediaSource.Factory hlsDataSourceFactory = new HlsMediaSource.Factory(dataSourceFactory);
        hlsDataSourceFactory.setAllowChunklessPreparation(true);

        return
                new HlsMediaSource.Factory(
                        dataType -> {
                            HttpDataSource dataSource =
                                    new DefaultHttpDataSource(AppSettingsKt.getTOFFEE_HEADER());
                            dataSource.setRequestProperty("TOFFEE-SESSION-TOKEN", mPref.getHeaderSessionToken());
                            return dataSource;
                        })
                        .createMediaSource(MediaItem.fromUri(uri));
    }

    protected void setPlayList(AddToPlaylistData data) {
        Log.e("PLAYLIST", "SetPlaylist - " + data.getItems().size());
        playlistManager.setPlayList(data);
    }

    @Override
    public boolean hasPrevious() {
        return playlistManager.hasPrevious();
    }

    @Override
    public boolean hasNext() {
        return playlistManager.hasNext();
    }

    public void playIndex(int index) {
        playlistManager.setIndex(index);
        playChannel(false);
    }

    @Override
    public boolean isAutoplayEnabled() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.details_viewer);
        if(fragment instanceof MyChannelPlaylistVideosFragment) {
            return ((MyChannelPlaylistVideosFragment) fragment).isAutoPlayEnabled();
        }
        return false;
    }

    @Override
    public void playNext() {
        playlistManager.nextChannel();
        playChannel(false);
    }

    @Override
    public void playPrevious() {
        playlistManager.previousChannel();
        playChannel(false);
    }

    protected void addChannelToPlayList(@NonNull ChannelInfo info) {
        ChannelInfo cinfo = playlistManager.getCurrentChannel();
        boolean isReload = false;
        if(cinfo != null && cinfo.getId().equalsIgnoreCase(info.getId())) {
            isReload = true;
        } else {
            playlistManager.setPlaylist(info);
        }
        playChannel(isReload);
    }

    protected void playChannel(boolean isReload) {
        ChannelInfo channelInfo = playlistManager.getCurrentChannel();
        String uri = null;//Channel.createChannel(channelInfo).getContentUri(this, mPref);
        if(uri == null){//in this case settings does not allow us to play content. So stop player and trigger event viewing stop
            if(player!=null)
                player.stop(true);
            channelCannotBePlayedDueToSettings();//notify hook/subclass
            heartBeatManager.triggerEventViewingContentStop();
            return;
        }
        //Checking whether we need to reload or not. Reload happens because of network switch or re-initialization of player
//        boolean isReload = false;
//        ChannelInfo oldChannelInfo = this.channelInfo;
//        this.channelInfo = channelInfo;
//        if(oldChannelInfo != null && oldChannelInfo.getId().equalsIgnoreCase(this.channelInfo.getId())){
//            isReload = true;//that means we have reload situation. We need to start where we left for VODs
//        }

        if(player!=null){
            heartBeatManager.triggerEventViewingContentStart(Integer.parseInt(channelInfo.getId()),channelInfo.getType());
            player.setPlayWhenReady(!isReload || player.getPlayWhenReady());
            MediaSource mediaSource = prepareMedia(Uri.parse(uri));
            if(isReload){//We need to start where we left off for VODs
                boolean haveStartPosition = startWindow != C.INDEX_UNSET;
                if (haveStartPosition && !channelInfo.isLive()) {
                    player.setMediaSource(mediaSource, false);
                    player.prepare();
//                    player.prepare(mediaSource, false, false);
                    player.seekTo(startWindow, startPosition);//we seek to where we left for VODs
                    return;
                }
            }
            player.setMediaSource(mediaSource);
            player.prepare();
//            player.prepare(mediaSource);//Non reload event or reload for live. Just prepare the media and play it
        }
    }

    //This will be called due to session token change while playing content or after init of player
    protected void reloadChannel(){
        ChannelInfo cinfo = playlistManager.getCurrentChannel();
        if(cinfo != null && cinfo.isExpired(mPref.getSystemTime())){
            //channel is expired. Stop the player and notify hook/subclass
            if(player!=null){
                player.stop(true);
            }
            onContentExpired();
            return;
        }
        if(cinfo != null){
            playChannel(true);
        }
    }

    protected void channelCannotBePlayedDueToSettings(){
        //subclass will hook into it
    }

    protected void clearChannel(){//set channelInfo = null
        playlistManager.clearPlaylist();
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
        ChannelInfo info = playlistManager.getCurrentChannel();
        if(info != null) {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/html");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, info.getVideo_share_url());
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
            return true;
        }
        return false;
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
