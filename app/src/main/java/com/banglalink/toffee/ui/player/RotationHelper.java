package com.banglalink.toffee.ui.player;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.OrientationEventListener;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

public class RotationHelper implements LifecycleObserver {
    private final Activity activity;
    private final OrientationEventListener orientationEventListener;
    private boolean landscape;

    public RotationHelper(Activity activity){
        this.activity = activity;
        orientationEventListener = new  OrientationEventListener(activity){

            @Override
            public void onOrientationChanged(int orientation) {
                if(landscape){ //moving from
                    if((orientation > 80 && orientation < 135) || (orientation < 285 && orientation > 230 )){
                        resetRotation();
                    }

                }
                else{
                    if((orientation < 45 && orientation > 0) || (orientation > 320 && orientation < 365 )){
                        resetRotation();
                    }
                }
//                if(orientation < 10){
//                    resetRotation();
//                }
                Log.e("rotation","" + orientation);
            }
        };
    }

    private void resetRotation(){
        orientationEventListener.disable();
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
    }

    public void lockOrientation(boolean landscape){
        this.landscape = landscape;
        if(landscape)
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        else
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        if(orientationEventListener.canDetectOrientation()) orientationEventListener.enable();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    void onPause() {
        resetRotation();
    }

    public void registerObserver(Lifecycle lifecycle){
        lifecycle.addObserver(this);
    }

    public void unregisterObserver(Lifecycle lifecycle){
        lifecycle.removeObserver(this);
    }
}
