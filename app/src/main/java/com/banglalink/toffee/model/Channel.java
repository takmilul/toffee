package com.banglalink.toffee.model;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.banglalink.toffee.data.storage.Preference;
import com.banglalink.toffee.ui.player.Samples;
import com.banglalink.toffee.util.Utils;

import java.net.MalformedURLException;
import java.net.URL;

import static com.google.android.exoplayer2.C.TYPE_HLS;

/**
 * Created by shantanu on 8/31/16.
 */

public class Channel extends Samples.Sample {

    public Channel(String name, String uri, int type, String imageUrl) {
        super(name, uri, type);
        this.imageUrl = imageUrl;
    }

    public Channel(String name, String uri) {
        super(name, uri, TYPE_HLS);
        this.imageUrl = "";
    }

    public static Channel createChannel(ChannelInfo channelInfo){
        return new Channel(channelInfo.program_name,channelInfo.hlsLinks.get(0).hls_url_mobile);
    }

    public Channel(String name, String contentId, String provider, String uri, int type, String imageUrl) {
        super(name,contentId,provider,uri,type);
        this.imageUrl = imageUrl;
    }

    public final String imageUrl;

    public Bundle getBundle(){
        Bundle bundle = new Bundle();
        bundle.putString("name",name);
        bundle.putString("contentid",contentId);
        bundle.putString("provider",provider);
        bundle.putString("uri",uri);
        bundle.putInt("type",type);
        bundle.putString("imageurl",imageUrl);
        return bundle;
    }

    public static Channel create(Bundle bundle){
        Channel channel = new Channel(bundle.getString("name"),
                bundle.getString("contentid"),
                bundle.getString("provider"),
                bundle.getString("uri"),
                bundle.getInt("type"),
                bundle.getString("imageurl"));
        return channel;
    }

    public String getContentUri(Context context){
        Uri contentUri = null;
        boolean isWifiConnected = Utils.checkWifiOnAndConnected(context);
        if(!isWifiConnected && Preference.Companion.getInstance().watchOnlyWifi()){
            return null;
        }
        String text = "";
        if (isWifiConnected) {
            if (Preference.Companion.getInstance().getWifiProfileStatus() == 6) {
                text = "/auto";
            } else {
                text = "/" + Preference.Companion.getInstance().getWifiProfileStatus();
            }
        } else {
            text = "/" + Preference.Companion.getInstance().getCellularProfileStatus();
        }

        if(Preference.Companion.getInstance().shouldOverrideHlsUrl()){
            try {
                String path = new URL(uri).getPath();
                uri = Preference.Companion.getInstance().getHlsOverrideUrl()+path;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        if(uri.endsWith("/")){
            contentUri = Uri.parse(uri + Preference.Companion.getInstance().getSessionToken()+ text);
        }
        else {
            contentUri = Uri.parse(uri + "/" + Preference.Companion.getInstance().getSessionToken() + text);
        }
        return contentUri.toString();
    }
}
