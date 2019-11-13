package com.banglalink.toffee.ui.channels;

import com.banglalink.toffee.model.ChannelInfo;

import java.util.List;

public class StickyHeaderInfo {
    public String header;
    public List<ChannelInfo> channelInfoList;

    public StickyHeaderInfo(String header, List<ChannelInfo> channelInfoList) {
        this.header = header;
        this.channelInfoList = channelInfoList;
    }
}