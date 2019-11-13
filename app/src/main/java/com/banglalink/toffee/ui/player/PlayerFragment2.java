package com.banglalink.toffee.ui.player;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.banglalink.toffee.R;
import com.banglalink.toffee.listeners.OnPlayerControllerChangedListener;
import com.banglalink.toffee.ui.widget.ExpoMediaController2;
import com.banglalink.toffee.util.Utils;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import com.google.android.exoplayer.metadata.id3.GeobFrame;
import com.google.android.exoplayer.metadata.id3.Id3Frame;
import com.google.android.exoplayer.metadata.id3.PrivFrame;
import com.google.android.exoplayer.metadata.id3.TxxxFrame;
import com.google.android.exoplayer.util.Util;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;
import com.google.android.gms.cast.*;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.List;

/**
 * Created by shantanu on 5/5/17.
 */

public class PlayerFragment2 extends Fragment implements TextureView.SurfaceTextureListener, DemoPlayer.Listener, DemoPlayer.Id3MetadataListener, View.OnClickListener {
    private static final String TAG = "MEDIA_PLAYER";
    private TextureView textureView;
    private AudioCapabilitiesReceiver audioCapabilitiesReceiver;
    private static final CookieManager defaultCookieManager;
    private static final String DATA = "uri-data";
    private Channel channel;

    static {
        defaultCookieManager = new CookieManager();
        defaultCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private DemoPlayer player;
    private EventLogger eventLogger;
    private int playerWidth;
    private int playerHeight;
    private ExpoMediaController2 mediaController;
    private Handler handler;
    private ChannelInfo channelInfo;
    private CastSession mCastSession;
    private RemoteMediaClient remoteMediaClient;
    private FrameLayout rootView;
    private ImageView preview;
    private Bitmap previewImage;
    private RotationHelper rotationHelper;

    private PlayerFragmentViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        rotationHelper = new RotationHelper(getActivity());
        viewModel = ViewModelProviders.of(this).get(PlayerFragmentViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_player2,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootView = (FrameLayout) view.findViewById(R.id.root);
        preview = (ImageView) view.findViewById(R.id.preview);
        textureView = (TextureView) view.findViewById(R.id.texture_view);
        textureView.setSurfaceTextureListener(this);
        CookieHandler currentHandler = CookieHandler.getDefault();
        if (currentHandler != defaultCookieManager) {
            CookieHandler.setDefault(defaultCookieManager);
        }
        mCastSession = ((PlayerActivity)getActivity()).getCastSession();
        mediaController = (ExpoMediaController2) view.findViewById(R.id.media_controller_view);
        if(mediaController != null){
            mediaController.setOnPlayerControllerChangedListener((OnPlayerControllerChangedListener) getActivity());
            mediaController.setActivity(this);
            mediaController.castButton.setRemoteIndicatorDrawable(getActivity().getResources().getDrawable(R.drawable.mr_button_dark));
            CastButtonFactory.setUpMediaRouteButton(getActivity(),mediaController.castButton);
        }
        preview.setOnClickListener(this);
    }

    private DemoPlayer.RendererBuilder getRendererBuilder() {
        String userAgent = Util.getUserAgent(getActivity(), "ExoPlayer");
        return new HlsRendererBuilder(getActivity(), userAgent, channel.getContentUri(getActivity()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
        releaseScreen();
    }

    public void load(ChannelInfo channelInfo){
        previewImage = null;
        preview.setImageResource(android.R.color.black);
        this.channelInfo = channelInfo;
        this.channel = Channel.createChannel(channelInfo);
        releasePlayer();
        if(channel.getContentUri(getActivity()) == null){
            preview.setImageResource(R.mipmap.watch_wifi_only_msg);
            mediaController.hideControls(0);
        }
        else {
            preparePlayer(true);
        }

        viewModel.sendViewContentEvent(channelInfo);

    }

    private void preparePlayer(boolean playWhenReady){
        player = new DemoPlayer(getActivity(),getRendererBuilder());
        player.addListener(this);
        player.setMetadataListener(this);
        player.seekTo(0);
        player.prepare();
        player.setPlayWhenReady(playWhenReady);
        if(textureView.getSurfaceTexture() != null) {
            player.setSurface(new Surface(textureView.getSurfaceTexture()));
        }
        eventLogger = new EventLogger();
        eventLogger.startSession();
        player.addListener(eventLogger);
        player.setInfoListener(eventLogger);
        player.setInternalErrorListener(eventLogger);
        mediaController.setPlayer(new LocalPlayer(player));
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        rotationHelper.registerObserver(getActivity().getLifecycle());
        resizeView();
        if(mCastSession == null || !mCastSession.isConnected()){
            if(player != null) {
                player.addListener(this);
                mediaController.setPlayer(new LocalPlayer(player));
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        rotationHelper.unregisterObserver(getActivity().getLifecycle());
        if(player != null){
            player.getPlayerControl().pause();
        }
        if(textureView != null && preview != null){
            if(previewImage == null && textureView.getBitmap() != null){
                previewImage = textureView.getBitmap();
                Log.e("preview","set");
            }
            preview.setImageBitmap(previewImage);

        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateFullScreenState();
    }

    public void updateFullScreenState(){
        boolean state = (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
        Utils.setFullScreen(getActivity(),state);
        resizeView();
        if(mediaController != null){
            mediaController.onFullScreen(state);
        }
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
//            }
//        },2000);
    }

    public void onMinimizePlayer(){
        mediaController.isMinimize = true;
        textureView.setOnClickListener(null);
        mediaController.hideControls(0);
    }

    public void onMaximizePlayer(){
        mediaController.isMinimize = false;
        textureView.setOnClickListener(this);
        if(mediaController.getPlayer() != null && mediaController.getPlayer().isPlaying()){
            mediaController.hideControls(2000);
        }
        else {
            mediaController.showControls();
        }
    }

    public void onShare() {
        if(channelInfo != null) {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/html");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, channelInfo.video_share_url);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
        }
    }

    public void resizeView(){
        calculateScreenWidth();
        ViewGroup.LayoutParams params = getView().getLayoutParams();
        params.width = playerWidth;
        params.height = playerHeight;
        getView().setLayoutParams(params);
    }

    private void calculateScreenWidth(){
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        playerWidth = size.x;
        if(size.x > size.y){ //landscape
            playerHeight = size.y;
        }
        else{
            Log.e("width: ","" + playerWidth);
            playerHeight = (playerWidth * 9) / 16;
            Log.e("height: ", "" + playerHeight);
        }
    }

    //surface changed listener
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if(textureView.getSurfaceTexture() != surface) {
            textureView.setSurfaceTexture(surface);
        }
        if (player != null && player.getSurface() == null) {
            player.setSurface(new Surface(textureView.getSurfaceTexture()));
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (player != null) {
            player.blockingClearSurface();
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private void keepScreenOn(){
        if(rootView != null){
            rootView.setKeepScreenOn(true);
        }
    }

    private void releaseScreen(){
        if(rootView != null){
            rootView.setKeepScreenOn(false);
        }
    }

    //media player listener
    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_ENDED) {
            mediaController.showControls();
            player.getPlayerControl().pause();
        }
        switch(playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                if(mediaController != null){
                    mediaController.setPlayState(ExpoMediaController2.STATE_BUFFERING);
                    mediaController.showControls();
                }
                keepScreenOn();
                break;
            case ExoPlayer.STATE_ENDED:
                if(mediaController != null){
                    mediaController.setPlayState(ExpoMediaController2.STATE_PAUSE);
                    mediaController.showControls();
                    mediaController.isEnd = true;
                }
                releaseScreen();
                break;
            case ExoPlayer.STATE_IDLE:
                if(mediaController != null){
                    mediaController.setPlayState(ExpoMediaController2.STATE_PAUSE);
                    mediaController.showControls();
                }
                releaseScreen();
                break;
            case ExoPlayer.STATE_PREPARING:
                mediaController.setPlayState(ExpoMediaController2.STATE_BUFFERING);
                mediaController.showControls();
                break;
            case ExoPlayer.STATE_READY:
                if(mediaController != null && player != null){
                    if(player.getPlayerControl().isPlaying()) {
                        previewImage = null;
                        preview.setImageResource(0);
                        keepScreenOn();
                        Log.e("preview","null");
                        mediaController.setPlayState(ExpoMediaController2.STATE_PLAYING);
                        mediaController.hideControls(3000);
                        onCastSessionStarted(mCastSession);
                    }
                    else{
                        releaseScreen();
                        mediaController.setPlayState(ExpoMediaController2.STATE_PAUSE);
                        mediaController.showControls();
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {

    }

    @Override
    public void onId3Metadata(List<Id3Frame> id3Frames) {
        for (Id3Frame id3Frame : id3Frames) {
            if (id3Frame instanceof TxxxFrame) {
                TxxxFrame txxxFrame = (TxxxFrame) id3Frame;
                Log.i(TAG, String.format("ID3 TimedMetadata %s: description=%s, value=%s", txxxFrame.id,
                        txxxFrame.description, txxxFrame.value));
            } else if (id3Frame instanceof PrivFrame) {
                PrivFrame privFrame = (PrivFrame) id3Frame;
                Log.i(TAG, String.format("ID3 TimedMetadata %s: owner=%s", privFrame.id, privFrame.owner));
            } else if (id3Frame instanceof GeobFrame) {
                GeobFrame geobFrame = (GeobFrame) id3Frame;
                Log.i(TAG, String.format("ID3 TimedMetadata %s: mimeType=%s, filename=%s, description=%s",
                        geobFrame.id, geobFrame.mimeType, geobFrame.filename, geobFrame.description));
            } else {
                Log.i(TAG, String.format("ID3 TimedMetadata %s", id3Frame.id));
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(v == preview){
            if(mediaController.showControls()) {
                if(mediaController.getPlayer() != null && mediaController.getPlayer().isPlaying()) {
                    mediaController.hideControls(3000);
                }
            }
            else{
                mediaController.hideControls(0);
            }
        }
    }

    private MediaInfo getMediaInfo(){
        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE );
        Log.e("video url", channel.getContentUri(getActivity()));
        mediaMetadata.putString(MediaMetadata.KEY_TITLE,channelInfo.program_name);
        mediaMetadata.addImage(new WebImage(Uri.parse(channelInfo.logo_mobile_url)));

        if (channelInfo.isLive()) {
            return  new MediaInfo.Builder(channel.getContentUri(getContext()))
                    .setContentType("application/x-mpegurl")
                    .setStreamType( MediaInfo.STREAM_TYPE_BUFFERED )
                    .setMetadata( mediaMetadata )
//                    .setStreamDuration(0) // 0 for Infinity
                    .build();
        } else {
            return  new MediaInfo.Builder(channel.getContentUri(getContext()))
                    .setContentType("application/x-mpegurl")
                    .setStreamType( MediaInfo.STREAM_TYPE_BUFFERED )
                    .setMetadata( mediaMetadata )
//                    .setStreamDuration(MediaInfo.STREAM_TYPE_LIVE)
                    .build();
        }

    }

    public void onCastSessionEnd(CastSession session) {
        if(player != null) {
            player.addListener(this);
            mediaController.setPlayer(new LocalPlayer(player));
            if(!player.getPlayerControl().isPlaying()){
                if(mediaController.getLastPlayerPosition() > 0) {
                    player.seekTo(mediaController.getLastPlayerPosition());
                }
                player.getPlayerControl().start();
            }
            remoteMediaClient = null;
        }


    }

    public void onCastSessionStarting(CastSession session) {
    }

    //cast
    public void onCastSessionStarted(CastSession castSession){
        this.mCastSession = castSession;
        if(mCastSession != null && mCastSession.isConnected()){
            mediaController.setPlayer(new RemotePlayer(player,mCastSession));
            remoteMediaClient = castSession.getRemoteMediaClient();
            if (remoteMediaClient == null) {
                return;
            }
            remoteMediaClient.addListener(new RemoteMediaClient.Listener() {
                @Override
                public void onStatusUpdated() {
                    if(remoteMediaClient.getMediaStatus() != null) {
                        int state = remoteMediaClient.getMediaStatus().getPlayerState();
                        if(mediaController != null){
                            if(state == MediaStatus.PLAYER_STATE_BUFFERING) {
                                mediaController.setPlayState(ExpoMediaController2.STATE_BUFFERING);
                                mediaController.showControls();
                            }
                            else if(state == MediaStatus.PLAYER_STATE_PLAYING) {
                                mediaController.setPlayState(ExpoMediaController2.STATE_PLAYING);
                                mediaController.showControls();
                                mediaController.hideControls(3000);
                            }
                            else if(state == MediaStatus.PLAYER_STATE_PAUSED || state == MediaStatus.PLAYER_STATE_IDLE) {
                                mediaController.setPlayState(ExpoMediaController2.STATE_PAUSE);
                                mediaController.showControls();
                            }
                        }
                    }
//                    remoteMediaClient.removeListener(this);
                }

                @Override
                public void onMetadataUpdated() {
                }

                @Override
                public void onQueueStatusUpdated() {
                }

                @Override
                public void onPreloadStatusUpdated() {
                }

                @Override
                public void onSendingRemoteMediaRequest() {
                }

                @Override
                public void onAdBreakStatusUpdated() {

                }
            });
            if(player != null) {
                remoteMediaClient.load(getMediaInfo(), true,player.getCurrentPosition());
                mediaController.setPlayState(ExpoMediaController2.STATE_BUFFERING);
                mediaController.showControls();
            }
            if(player != null) {
                player.addListener(new DemoPlayer.Listener() {
                    @Override
                    public void onStateChanged(boolean playWhenReady, int playbackState) {

                    }

                    @Override
                    public void onError(Exception e) {

                    }

                    @Override
                    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {

                    }
                });
                player.getPlayerControl().pause();
            }
        }
        else{
            if(player != null) {
                player.addListener(this);
                mediaController.setPlayer(new LocalPlayer(player));
//                if (!player.getPlayerControl().isPlaying()) {
//                    player.getPlayerControl().start();
//                }
            }
        }
    }

    public void setCastSession(CastSession castSession) {
        this.mCastSession = castSession;
    }

    public ChannelInfo getChannelInfo() {
        return channelInfo;
    }

    public void setChannelInfo(ChannelInfo channelInfo) {
        this.channelInfo = channelInfo;
    }

    public void onFullScreenButtonPressed(boolean currentState) {
        rotationHelper.lockOrientation(!currentState);
//        if(!currentState) {
//            rotationHelper.lockOrientation(true);
//        }
//        else{
//            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        }
    }
}
