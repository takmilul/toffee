package com.banglalink.toffee.ui.player;

import com.banglalink.toffee.listeners.MediaPlayerListener;
import com.google.android.exoplayer.MediaFormat;

/**
 * Created by shantanu on 5/6/17.
 */

public class LocalPlayer implements MediaPlayerListener {

    private final DemoPlayer mPlayer;

    public LocalPlayer(DemoPlayer mPlayer){
        this.mPlayer = mPlayer;
    }

    @Override
    public boolean isPlaying() {
        return mPlayer.getPlayerControl().isPlaying();
    }

    @Override
    public int getSelectedTrack(int type) {
        return mPlayer.getSelectedTrack(type);
    }

    @Override
    public int getTrackCount(int type) {
        return mPlayer.getTrackCount(type);
    }

    @Override
    public void setSelectedTrack(int type, int index) {
        mPlayer.setSelectedTrack(type,index);
    }

    @Override
    public MediaFormat getTrackFormat(int type, int index) {
        return mPlayer.getTrackFormat(type,index);
    }

    @Override
    public long getCurrentPosition() {
        return mPlayer.getCurrentPosition();
    }

    @Override
    public long getDuration() {
        return mPlayer.getDuration();
    }

    @Override
    public void seekTo(long position) {
        mPlayer.seekTo(position);
    }

    @Override
    public int getBufferPercentage() {
        return mPlayer.getBufferedPercentage();
    }

    @Override
    public void pause() {
        mPlayer.getPlayerControl().pause();
    }

    @Override
    public void start() {
        mPlayer.getPlayerControl().start();
    }
}
