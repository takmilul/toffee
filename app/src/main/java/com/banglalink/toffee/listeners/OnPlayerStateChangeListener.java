package com.banglalink.toffee.listeners;

/**
 * Created by shantanu on 5/4/16.
 */
public interface OnPlayerStateChangeListener {
    public void onReady();
    public void onBuffering();
    public void onEnd();
    public void onPreparing();
    public void onIdle();
    public void onFullScreen(boolean state);
}
