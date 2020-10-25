package com.banglalink.toffee.data.network.request

data class ContentUploadRequest(
    val customerId: Int,
    val password: String,
    val channelId: Long,
    val bucket_content_name: String,
    val program_name: String? = null,
    val content_dir: String? = null,
    val description: String? = null,
    val channel_logo: String? = null,
    val logo_web_url: String? = null,
    val logo_stb_url: String? = null,
    val logo_mobile_url: String? = null,
    val poster_url_mobile: String? = null,
    val poster_url_web: String? = null,
    val poster_url_stb: String? = null,
    val player_poster_web: String? = null,
    val player_poster_mobile: String? = null,
    val potrait_ratio_800_1200: String? = null,
    val landscape_ratio_1280_720: String? = null,
    val feature_image: String? = null,
    val age_restriction: String? = null,
    val video_tags: String? = null,
    val keywords: String? = null
):BaseRequest("ugcContentUpload")