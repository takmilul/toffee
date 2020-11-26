package com.banglalink.toffee.listeners;

/**
 * Created by shantanu on 5/5/17.
 */

public interface OnPlayerControllerChangedListener {
    boolean onPlayButtonPressed(int currentState);
    boolean onFullScreenButtonPressed();
    boolean onDrawerButtonPressed();
    boolean onMinimizeButtonPressed();
    boolean onOptionMenuPressed();
    boolean onShareButtonPressed();
    void  onPlayerIdleDueToError();
    void onRotationLock(boolean isAutoRotationEnabled);
    boolean onSeekPosition(int position);
    void onControllerVisible();
    void onControllerInVisible();
}
