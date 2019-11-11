package com.banglalink.toffee.ui.player;

import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;

/**
 * Created by shantanu on 5/6/17.
 */

public class RemotePlayer extends LocalPlayer{

    private CastSession mCastSession;

    public RemotePlayer(DemoPlayer mPlayer, CastSession mCastSession) {
        super(mPlayer);
        this.mCastSession = mCastSession;
    }

    public void setCastSession(CastSession mCastSession){
        this.mCastSession = mCastSession;
    }

    @Override
    public boolean isPlaying() {
        if(mCastSession == null || mCastSession.getRemoteMediaClient() == null){
            return  false;
        }
        RemoteMediaClient rmClient = mCastSession.getRemoteMediaClient();
        if(rmClient.hasMediaSession()){
            return rmClient.isPlaying();
        }
        return false;
    }

    @Override
    public long getCurrentPosition() {
        if(mCastSession == null || mCastSession.getRemoteMediaClient() == null){
            return  -1;
        }
        RemoteMediaClient rmClient = mCastSession.getRemoteMediaClient();
        if(rmClient.hasMediaSession()){
            return rmClient.getApproximateStreamPosition();
        }
        return -1;
    }

    @Override
    public long getDuration() {
        if(mCastSession == null || mCastSession.getRemoteMediaClient() == null){
            return  -1;
        }
        RemoteMediaClient rmClient = mCastSession.getRemoteMediaClient();
        if(rmClient.hasMediaSession()){
            return rmClient.getStreamDuration();
        }
        return -1;
    }

    @Override
    public void seekTo(long position) {
        if(mCastSession == null || mCastSession.getRemoteMediaClient() == null){
            return ;
        }
        RemoteMediaClient rmClient = mCastSession.getRemoteMediaClient();
        if(rmClient.hasMediaSession()){
            rmClient.seek(position);
        }
    }


    @Override
    public void pause() {
        if(mCastSession == null || mCastSession.getRemoteMediaClient() == null){
            return;
        }
        RemoteMediaClient rmClient = mCastSession.getRemoteMediaClient();
        if(rmClient.hasMediaSession()){
            rmClient.pause();
        }
    }

    @Override
    public void start() {
        if(mCastSession == null || mCastSession.getRemoteMediaClient() == null){
            return;
        }
        RemoteMediaClient rmClient = mCastSession.getRemoteMediaClient();
        if(rmClient.hasMediaSession()){
            rmClient.play();
        }
    }
}
