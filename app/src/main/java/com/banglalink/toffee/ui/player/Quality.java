package com.banglalink.toffee.ui.player;

/**
 * Created by shantanu on 9/1/16.
 */

public class Quality {
    public static final int TYPE_VIDEO = DemoPlayer.TYPE_VIDEO;
    public static final int TYPE_AUDIO = DemoPlayer.TYPE_AUDIO;

    public int type;
    public int index;
    public String format;
    public boolean selected;
    public Quality(int index, String format, int type, boolean selected){
        this.type = type;
        this.format = format;
        this.index = index;
        this.selected = selected;
    }
}
