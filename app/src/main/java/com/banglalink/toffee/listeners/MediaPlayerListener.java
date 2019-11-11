package com.banglalink.toffee.listeners;

import com.google.android.exoplayer.MediaFormat;

/**
 * Created by shantanu on 5/6/17.
 */

public interface MediaPlayerListener {
    boolean isPlaying();
    int getSelectedTrack(int type);
    int getTrackCount(int type);
    void setSelectedTrack(int type, int index);
    MediaFormat getTrackFormat(int type, int index);
    long getCurrentPosition();
    long getDuration();
    void seekTo(long position);

    int getBufferPercentage();
    void pause();
    void start();
}
