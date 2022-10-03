package com.banglalink.toffee

import com.banglalink.toffee.model.ChannelInfo

fun getDummyChannelList(): List<ChannelInfo> {
    val list = mutableListOf<ChannelInfo>()

    for(i in 0..150) {
        list.add(
            ChannelInfo(
            id = "$i",
            mainTableId = "$i",
            iptvProgramsId = "$i",
            program_name = "Channel $i",
            description = "Channel $i description",
            view_count = "$i",
            type = "VOD",
            duration = "${i + 1000}"
        ))
    }
    return list
}