package com.banglalink.toffee.util;

import android.content.Context;

import com.conviva.sdk.ConvivaAdAnalytics;
import com.conviva.sdk.ConvivaAnalytics;
import com.conviva.sdk.ConvivaVideoAnalytics;

public class ConvivaFactory {
    private ConvivaVideoAnalytics videoAnalytics = null;
    private ConvivaAdAnalytics adAnalytics = null;
    private static final ConvivaFactory instance = new ConvivaFactory();
    
    private ConvivaFactory() {}
    
    public static void init(Context context, boolean isActive) {
        if (isActive) {
            instance.videoAnalytics = ConvivaAnalytics.buildVideoAnalytics(context);
            instance.adAnalytics = ConvivaAnalytics.buildAdAnalytics(context, instance.videoAnalytics);
        }
    }
    
    public static ConvivaVideoAnalytics getConvivaVideoAnalytics() {
        return instance.videoAnalytics;
    }
    
    public static ConvivaAdAnalytics getConvivaAdAnalytics() {
        return instance.adAnalytics;
    }
}
