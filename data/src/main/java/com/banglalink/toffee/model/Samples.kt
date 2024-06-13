/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName
import java.util.*

object Samples {
    const val TYPE_DASH = 0
    const val TYPE_SS = 1
    const val TYPE_HLS = 2
    const val TYPE_RTSP = 3
    const val TYPE_OTHER = 4
    
    val YOUTUBE_DASH_MP4 = arrayOf(
        Sample(
            "Google Glass (MP4,H264)",
            "https://www.youtube.com/api/manifest/dash/id/bf5bb2419360daf1/source/youtube?" + "as=fmp4_audio_clear,fmp4_sd_hd_clear&sparams=ip,ipbits,expire,source,id,as&ip=0.0.0.0&" + "ipbits=0&expire=19000000000&signature=51AF5F39AB0CEC3E5497CD9C900EBFEAECCCB5C7." + "8506521BFC350652163895D4C26DEE124209AA9E&key=ik0",
            TYPE_DASH
        ), Sample(
            "Google Play (MP4,H264)",
            "https://www.youtube.com/api/manifest/dash/id/3aa39fa2cc27967f/source/youtube?" + "as=fmp4_audio_clear,fmp4_sd_hd_clear&sparams=ip,ipbits,expire,source,id,as&ip=0.0.0.0&" + "ipbits=0&expire=19000000000&signature=A2716F75795F5D2AF0E88962FFCD10DB79384F29." + "84308FF04844498CE6FBCE4731507882B8307798&key=ik0",
            TYPE_DASH
        )
    )
    val YOUTUBE_DASH_WEBM = arrayOf(
        Sample(
            "Google Glass (WebM,VP9)",
            "https://www.youtube.com/api/manifest/dash/id/bf5bb2419360daf1/source/youtube?" + "as=fmp4_audio_clear,webm2_sd_hd_clear&sparams=ip,ipbits,expire,source,id,as&ip=0.0.0.0&" + "ipbits=0&expire=19000000000&signature=249B04F79E984D7F86B4D8DB48AE6FAF41C17AB3." + "7B9F0EC0505E1566E59B8E488E9419F253DDF413&key=ik0",
            TYPE_DASH
        ), Sample(
            "Google Play (WebM,VP9)",
            "https://www.youtube.com/api/manifest/dash/id/3aa39fa2cc27967f/source/youtube?" + "as=fmp4_audio_clear,webm2_sd_hd_clear&sparams=ip,ipbits,expire,source,id,as&ip=0.0.0.0&" + "ipbits=0&expire=19000000000&signature=B1C2A74783AC1CC4865EB312D7DD2D48230CC9FD." + "BD153B9882175F1F94BFE5141A5482313EA38E8D&key=ik0",
            TYPE_DASH
        )
    )
    val SMOOTHSTREAMING = arrayOf(
        Sample(
            "Super speed", "https://playready.directtaps.net/smoothstreaming/SSWSS720H264/SuperSpeedway_720.ism", TYPE_SS
        ), Sample(
            "Super speed (PlayReady)", "https://playready.directtaps.net/smoothstreaming/SSWSS720H264PR/SuperSpeedway_720.ism", TYPE_SS
        )
    )
    private const val WIDEVINE_GTS_MPD = "https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd"
    val WIDEVINE_GTS = arrayOf(
        Sample(
            "WV: HDCP not specified", "d286538032258a1c", "widevine_test", WIDEVINE_GTS_MPD, TYPE_DASH
        ), Sample(
            "WV: HDCP not required", "48fcc369939ac96c", "widevine_test", WIDEVINE_GTS_MPD, TYPE_DASH
        ), Sample(
            "WV: HDCP required", "e06c39f1151da3df", "widevine_test", WIDEVINE_GTS_MPD, TYPE_DASH
        ), Sample(
            "WV: Secure video path required (MP4,H264)", "0894c7c8719b28a0", "widevine_test", WIDEVINE_GTS_MPD, TYPE_DASH
        ), Sample(
            "WV: Secure video path required (WebM,VP9)",
            "0894c7c8719b28a0",
            "widevine_test",
            "https://storage.googleapis.com/wvmedia/cenc/vp9/tears/tears.mpd",
            TYPE_DASH
        ), Sample(
            "WV: Secure video path required (MP4,H265)",
            "0894c7c8719b28a0",
            "widevine_test",
            "https://storage.googleapis.com/wvmedia/cenc/hevc/tears/tears.mpd",
            TYPE_DASH
        ), Sample(
            "WV: HDCP + secure video path required", "efd045b1eb61888a", "widevine_test", WIDEVINE_GTS_MPD, TYPE_DASH
        ), Sample(
            "WV: 30s license duration (fails at ~30s)", "f9a34cab7b05881a", "widevine_test", WIDEVINE_GTS_MPD, TYPE_DASH
        )
    )
    val WIDEVINE_HDCP = arrayOf(
        Sample(
            "WV: HDCP: None (not required)", "HDCP_None", "widevine_test", WIDEVINE_GTS_MPD, TYPE_DASH
        ), Sample(
            "WV: HDCP: 1.0 required", "HDCP_V1", "widevine_test", WIDEVINE_GTS_MPD, TYPE_DASH
        ), Sample(
            "WV: HDCP: 2.0 required", "HDCP_V2", "widevine_test", WIDEVINE_GTS_MPD, TYPE_DASH
        ), Sample(
            "WV: HDCP: 2.1 required", "HDCP_V2_1", "widevine_test", WIDEVINE_GTS_MPD, TYPE_DASH
        ), Sample(
            "WV: HDCP: 2.2 required", "HDCP_V2_2", "widevine_test", WIDEVINE_GTS_MPD, TYPE_DASH
        ), Sample(
            "WV: HDCP: No digital output", "HDCP_NO_DIGTAL_OUTPUT", "widevine_test", WIDEVINE_GTS_MPD, TYPE_DASH
        )
    )
    val WIDEVINE_H264_MP4_CLEAR = arrayOf(
        Sample(
            "WV: Clear SD & HD (MP4,H264)", "https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd", TYPE_DASH
        ), Sample(
            "WV: Clear SD (MP4,H264)", "https://storage.googleapis.com/wvmedia/clear/h264/tears/tears_sd.mpd", TYPE_DASH
        ), Sample(
            "WV: Clear HD (MP4,H264)", "https://storage.googleapis.com/wvmedia/clear/h264/tears/tears_hd.mpd", TYPE_DASH
        ), Sample(
            "WV: Clear UHD (MP4,H264)", "https://storage.googleapis.com/wvmedia/clear/h264/tears/tears_uhd.mpd", TYPE_DASH
        )
    )
    val WIDEVINE_H264_MP4_SECURE = arrayOf(
        Sample(
            "WV: Secure SD & HD (MP4,H264)", "", "widevine_test", "https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd", TYPE_DASH
        ), Sample(
            "WV: Secure SD (MP4,H264)", "", "widevine_test", "https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears_sd.mpd", TYPE_DASH
        ), Sample(
            "WV: Secure HD (MP4,H264)", "", "widevine_test", "https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears_hd.mpd", TYPE_DASH
        ), Sample(
            "WV: Secure UHD (MP4,H264)", "", "widevine_test", "https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears_uhd.mpd", TYPE_DASH
        )
    )
    val WIDEVINE_VP9_WEBM_CLEAR = arrayOf(
        Sample(
            "WV: Clear SD & HD (WebM,VP9)", "https://storage.googleapis.com/wvmedia/clear/vp9/tears/tears.mpd", TYPE_DASH
        ), Sample(
            "WV: Clear SD (WebM,VP9)", "https://storage.googleapis.com/wvmedia/clear/vp9/tears/tears_sd.mpd", TYPE_DASH
        ), Sample(
            "WV: Clear HD (WebM,VP9)", "https://storage.googleapis.com/wvmedia/clear/vp9/tears/tears_hd.mpd", TYPE_DASH
        ), Sample(
            "WV: Clear UHD (WebM,VP9)", "https://storage.googleapis.com/wvmedia/clear/vp9/tears/tears_uhd.mpd", TYPE_DASH
        )
    )
    val WIDEVINE_VP9_WEBM_SECURE = arrayOf(
        Sample(
            "WV: Secure SD & HD (WebM,VP9)", "", "widevine_test", "https://storage.googleapis.com/wvmedia/cenc/vp9/tears/tears.mpd", TYPE_DASH
        ), Sample(
            "WV: Secure SD (WebM,VP9)", "", "widevine_test", "https://storage.googleapis.com/wvmedia/cenc/vp9/tears/tears_sd.mpd", TYPE_DASH
        ), Sample(
            "WV: Secure HD (WebM,VP9)", "", "widevine_test", "https://storage.googleapis.com/wvmedia/cenc/vp9/tears/tears_hd.mpd", TYPE_DASH
        ), Sample(
            "WV: Secure UHD (WebM,VP9)", "", "widevine_test", "https://storage.googleapis.com/wvmedia/cenc/vp9/tears/tears_uhd.mpd", TYPE_DASH
        )
    )
    val WIDEVINE_H265_MP4_CLEAR = arrayOf(
        Sample(
            "WV: Clear SD & HD (MP4,H265)", "https://storage.googleapis.com/wvmedia/clear/hevc/tears/tears.mpd", TYPE_DASH
        ), Sample(
            "WV: Clear SD (MP4,H265)", "https://storage.googleapis.com/wvmedia/clear/hevc/tears/tears_sd.mpd", TYPE_DASH
        ), Sample(
            "WV: Clear HD (MP4,H265)", "https://storage.googleapis.com/wvmedia/clear/hevc/tears/tears_hd.mpd", TYPE_DASH
        ), Sample(
            "WV: Clear UHD (MP4,H265)", "https://storage.googleapis.com/wvmedia/clear/hevc/tears/tears_uhd.mpd", TYPE_DASH
        )
    )
    val WIDEVINE_H265_MP4_SECURE = arrayOf(
        Sample(
            "WV: Secure SD & HD (MP4,H265)", "", "widevine_test", "https://storage.googleapis.com/wvmedia/cenc/hevc/tears/tears.mpd", TYPE_DASH
        ), Sample(
            "WV: Secure SD (MP4,H265)", "", "widevine_test", "https://storage.googleapis.com/wvmedia/cenc/hevc/tears/tears_sd.mpd", TYPE_DASH
        ), Sample(
            "WV: Secure HD (MP4,H265)", "", "widevine_test", "https://storage.googleapis.com/wvmedia/cenc/hevc/tears/tears_hd.mpd", TYPE_DASH
        ), Sample(
            "WV: Secure UHD (MP4,H265)", "", "widevine_test", "https://storage.googleapis.com/wvmedia/cenc/hevc/tears/tears_uhd.mpd", TYPE_DASH
        )
    )
    val HLS = arrayOf(
        Sample(
            "Apple master playlist", "https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/" + "bipbop_4x3_variant.m3u8", TYPE_HLS
        ), Sample(
            "Apple master playlist advanced",
            "https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/" + "bipbop_16x9_variant.m3u8",
            TYPE_HLS
        ), Sample(
            "Apple TS media playlist", "https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear1/" + "prog_index.m3u8", TYPE_HLS
        ), Sample(
            "Apple AAC media playlist", "https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear0/" + "prog_index.m3u8", TYPE_HLS
        ), Sample(
            "Apple ID3 metadata",
            "https://27.131.14.202:1935/live/smil:Test_Stream.smil/playlist.m3u8",  // "https://27.131.14.202:1935/live/myStream/playlist.m3u8",//"https://118.179.35.11/hls/Desh_TVYCjrz/index.m3u8", //"https://devimages.apple.com/samplecode/adDemo/ad.m3u8",
            TYPE_HLS
        )
    )
    val MISC = arrayOf(
        Sample("Dizzy", "https://html5demos.com/assets/dizzy.mp4", TYPE_OTHER), Sample(
            "Apple AAC 10s", "https://devimages.apple.com.edgekey.net/" + "streaming/examples/bipbop_4x3/gear0/fileSequence0.aac", TYPE_OTHER
        ), Sample(
            "Apple TS 10s", "https://devimages.apple.com.edgekey.net/streaming/examples/" + "bipbop_4x3/gear1/fileSequence0.ts", TYPE_OTHER
        ), Sample(
            "Android screens (Matroska)",
            "https://storage.googleapis.com/exoplayer-test-media-1/" + "mkv/android-screens-lavf-56.36.100-aac-avc-main-1280x720.mkv",
            TYPE_OTHER
        ), Sample(
            "Big Buck Bunny (MP4 Video)",
            "https://redirector.c.youtube.com/videoplayback?id=604ed5ce52eda7ee&itag=22&source=youtube&" + "sparams=ip,ipbits,expire,source,id&ip=0.0.0.0&ipbits=0&expire=19000000000&signature=" + "513F28C7FDCBEC60A66C86C9A393556C99DC47FB.04C88036EEE12565A1ED864A875A58F15D8B5300" + "&key=ik0",
            TYPE_OTHER
        ), Sample(
            "Google Play (MP3 Audio)", "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3", TYPE_OTHER
        ), Sample(
            "Google Play (Ogg/Vorbis Audio)", "https://storage.googleapis.com/exoplayer-test-media-1/ogg/play.ogg", TYPE_OTHER
        ), Sample(
            "Google Glass (WebM Video with Vorbis Audio)", "https://demos.webmproject.org/exoplayer/glass_vp9_vorbis.webm", TYPE_OTHER
        ), Sample(
            "Big Buck Bunny (FLV Video)", "https://vod.leasewebcdn.com/bbb.flv?ri=1024&rs=150&start=0", TYPE_OTHER
        )
    )
    
    open class Sample(
        @field:SerializedName("name") val name: String,
        @field:SerializedName("contentId") val contentId: String,
        @field:SerializedName("provider") val provider: String,
        @field:SerializedName("uri") var uri: String?,
        @field:SerializedName("type") val type: Int
    ) {
        constructor(
            name: String, uri: String?, type: Int
        ) : this(name, name.lowercase(Locale.ENGLISH).replace("\\s".toRegex(), ""), "", uri, type) {}
    }
}