package com.banglalink.toffee.listeners;


import com.google.android.exoplayer2.Format;

/**
 * Created by shantanu on 5/6/17.
 */

public interface MediaPlayerListener {
    boolean isPlaying();
    int getSelectedTrack(int type);
    int getTrackCount(int type);
    void setSelectedTrack(int type, int index);
    Format getTrackFormat(int type, int index);
    long getCurrentPosition();
    long getDuration();
    void seekTo(long position);
    boolean isLiveStreaming();

    int getBufferPercentage();
    void pause();
    void start();
}
