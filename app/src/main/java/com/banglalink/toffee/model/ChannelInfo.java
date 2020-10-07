package com.banglalink.toffee.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.banglalink.toffee.ui.player.HlsLinks;
import com.banglalink.toffee.util.Utils;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by MD.TAUFIQUR RAHMAN on 11/22/2016.
 */

public class ChannelInfo implements Parcelable {

    public String id;
    public String program_name;
    public String video_share_url;
    public String video_trailer_url;
    public String description;
    public String water_mark_url;
    public String type;
    public String view_count;
    public String formatted_view_count;
    public String lcn;
    public String individual_price;
    public String video_tags;
    public String duration;
    public String formattedDuration;
    public String age_restriction;
    public String service_operator_id;
    public String logo_mobile_url;
    public String poster_url_mobile;
    public boolean subscription;
    public boolean individual_purchase;
    public String expireTime;
    public List<HlsLinks> hlsLinks;
    public String channel_logo;
    public String category;
    public String subCategory;
    public int subCategoryId;
    public String favorite;
    public String potrait_ratio_800_1200;
    public String landscape_ratio_1280_720;
    public String feature_image;
    public String content_provider_name;
    public String content_provider_id;
    @SerializedName("url_type")
    public int urlType;


    public ChannelInfo() {

    }

    public boolean isLive(){
        return "LIVE".equalsIgnoreCase(type);
    }

    public boolean isVOD(){
        return "VOD".equalsIgnoreCase(type);
    }

    public boolean isCatchup(){
        return "CATCHUP".equalsIgnoreCase(type);
    }

    public String getHlsLink(){
        return hlsLinks.get(0).hls_url_mobile;
    }

//    protected ChannelInfo(ChannelInfo channelInfo){
//        this.id = channelInfo.id;
//        this.program_name = channelInfo.program_name;
//        this.video_share_url = channelInfo.video_share_url;
//        this.video_trailer_url = channelInfo.video_trailer_url;
//        this.description = channelInfo.description;
//        this.water_mark_url = channelInfo.water_mark_url;
//        this.type = channelInfo.type;
//        this.view_count = channelInfo.view_count;
//        this.lcn = channelInfo.lcn;
//        this.individual_price = channelInfo.individual_price;
//        this.video_tags = channelInfo.video_tags;
//        this.duration = channelInfo.duration;
//        this.age_restriction = channelInfo.age_restriction;
//        this.service_operator_id = channelInfo.service_operator_id;
//        this.logo_mobile_url = channelInfo.logo_mobile_url;
//        this.poster_url_mobile = channelInfo.poster_url_mobile;
//        this.subscription = channelInfo.subscription;
//        this.expireTime = channelInfo.expireTime;
//        this.hlsLinks = channelInfo.hlsLinks;
//        this.channel_logo = channelInfo.channel_logo;
//    }

    protected ChannelInfo(Parcel in) {
        id = in.readString();
        program_name = in.readString();
        video_share_url = in.readString();
        video_trailer_url = in.readString();
        description = in.readString();
        water_mark_url = in.readString();
        type = in.readString();
        view_count = in.readString();
        formatted_view_count = in.readString();
        lcn = in.readString();
        individual_price = in.readString();
        video_tags = in.readString();
        duration = in.readString();
        formattedDuration = in.readString();
        age_restriction = in.readString();
        service_operator_id = in.readString();
        logo_mobile_url = in.readString();
        poster_url_mobile = in.readString();
        subscription = in.readByte() != 0;
        individual_purchase = in.readByte() != 0;
        expireTime = in.readString();
        hlsLinks = in.createTypedArrayList(HlsLinks.CREATOR);
        channel_logo = in.readString();
        category = in.readString();
        subCategory = in.readString();
        subCategoryId = in.readInt();
        favorite = in.readString();
        potrait_ratio_800_1200 = in.readString();
        landscape_ratio_1280_720 = in.readString();
        feature_image = in.readString();
        content_provider_name = in.readString();
        content_provider_id = in.readString();
    }

    public static final Creator<ChannelInfo> CREATOR = new Creator<ChannelInfo>() {
        @Override
        public ChannelInfo createFromParcel(Parcel in) {
            return new ChannelInfo(in);
        }

        @Override
        public ChannelInfo[] newArray(int size) {
            return new ChannelInfo[size];
        }
    };

    public static List<HlsLinks> getHlsArrayList(List<String> links){
        List<HlsLinks> hlsLinks = new ArrayList<>();
        if(links != null){
            for(String link : links){
                HlsLinks hlsLink = new HlsLinks();
                hlsLink.hls_url_mobile = link;
                hlsLinks.add(hlsLink);
            }
        }
        return hlsLinks;
    }

    public static List<String> getStringArrayList(List<HlsLinks> hlsLinks){
        List<String> list = new ArrayList<>();
        if(hlsLinks != null){
            for(HlsLinks hls : hlsLinks){
                list.add(hls.hls_url_mobile);
            }
        }
        return list;
    }

    public String getCategory(String category, String subCategory){
        String itemCategory = "Channels>" + category;
        if (subCategory.length() > 0) {
            itemCategory += ">" + subCategory;
        }
        return itemCategory;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(program_name);
        dest.writeString(video_share_url);
        dest.writeString(video_trailer_url);
        dest.writeString(description);
        dest.writeString(water_mark_url);
        dest.writeString(type);
        dest.writeString(view_count);
        dest.writeString(formatted_view_count);
        dest.writeString(lcn);
        dest.writeString(individual_price);
        dest.writeString(video_tags);
        dest.writeString(duration);
        dest.writeString(formattedDuration);
        dest.writeString(age_restriction);
        dest.writeString(service_operator_id);
        dest.writeString(logo_mobile_url);
        dest.writeString(poster_url_mobile);
        dest.writeByte((byte) (subscription ? 1 : 0));
        dest.writeByte((byte) (individual_purchase ? 1 : 0));
        dest.writeString(expireTime);
        dest.writeTypedList(hlsLinks);
        dest.writeString(channel_logo);
        dest.writeString(category);
        dest.writeString(subCategory);
        dest.writeInt(subCategoryId);
        dest.writeString(favorite);
        dest.writeString(potrait_ratio_800_1200);
        dest.writeString(landscape_ratio_1280_720);
        dest.writeString(feature_image);
        dest.writeString(content_provider_name);
        dest.writeString(content_provider_id);
    }

    public boolean isPurchased() {
        return Integer.parseInt(individual_price) > 0 && individual_purchase;
    }

    public boolean isSubscribed(){
        return Integer.parseInt(individual_price
        ) == 0 && subscription;
    }

    public boolean isExpired(Date serverDate){
        try{
            return serverDate.after(Utils.getDate(expireTime));
        }catch (NullPointerException ne){
            return true;
        }
    }
}
