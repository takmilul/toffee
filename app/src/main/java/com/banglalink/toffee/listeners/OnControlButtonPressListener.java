package com.banglalink.toffee.listeners;

import android.view.View;

/**
 * Created by shantanu on 5/5/16.
 */
public interface OnControlButtonPressListener {
    public void onPlayButtonPress(int state);
    public void onFullScreenButtonPress(boolean state);
    public void onAudioOptionPress(View v);
    public void onVideoOptionPress(View v);
    public void onShare();
    public void onChangeBrightness(int value);
    public void onMinimizeButtonPressed();
    public void onDrawerButtonPressed();
}
