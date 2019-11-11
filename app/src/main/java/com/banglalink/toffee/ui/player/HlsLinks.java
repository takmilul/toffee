package com.banglalink.toffee.ui.player;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by shantanu on 11/25/16.
 */

public class HlsLinks implements Parcelable {
    public String hls_url_mobile;
    public HlsLinks(){}
    public HlsLinks(String link){
        this.hls_url_mobile = link;
    }

    protected HlsLinks(Parcel in) {
        hls_url_mobile = in.readString();
    }

    public static final Creator<HlsLinks> CREATOR = new Creator<HlsLinks>() {
        @Override
        public HlsLinks createFromParcel(Parcel in) {
            return new HlsLinks(in);
        }

        @Override
        public HlsLinks[] newArray(int size) {
            return new HlsLinks[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(hls_url_mobile);
    }
}
