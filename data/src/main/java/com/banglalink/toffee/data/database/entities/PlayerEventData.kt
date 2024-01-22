package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class PlayerEventData(
    @PrimaryKey(autoGenerate = true)
    @SerialName("id")
    val id: Long = 0,
    @SerialName("sessionId")
    var sessionId: String? = null,
    @SerialName("contentPlayingSessionId")
    var contentPlayingSessionId: String? = null,
    @SerialName("contentPlayingSessionSequenceId")
    var contentPlayingSessionSequenceId: String? = null,
    @SerialName("appLifeCycleId")
    var appLifeCycleId: String? = null,
    @SerialName("isForeground")
    var isForeground: String? = null,
    @SerialName("isInternetAvailable")
    var isInternetAvailable: Boolean? = null,                               //ping tool
    @SerialName("networkType")
    var networkType: String? = null,                                        //ping tool
    @SerialName("ispOrTelecomOperator")
    var ispOrTelecomOperator: String? = null,                               //ping tool
    @SerialName("remoteHost")
    var remoteHost: String? = null,                                         //ping tool
    @SerialName("remoteIp")
    var remoteIp: String? = null,                                           //ping tool
    @SerialName("latencyClientToCdn")
    var latencyClientToCdn: String? = null,                                 //ping tool
    @SerialName("playerEventId")
    var playerEventId: Int? = null,
    @SerialName("playerEvent")
    var playerEvent: String? = null,
    @SerialName("contentId")
    var contentId: String? = null,
    @SerialName("contentTitle")
    var contentTitle: String? = null,
    @SerialName("contentProviderId")
    var contentProviderId: String? = null,
    @SerialName("contentProviderName")
    var contentProviderName: String? = null,
    @SerialName("contentCategoryId")
    var contentCategoryId: Int? = 0,
    @SerialName("contentCategoryName")
    var contentCategoryName: String? = null,
    @SerialName("contentDuration")
    var contentDuration: String? = null,
    @SerialName("seasonName")
    var seasonName: String? = null,
    @SerialName("seasonNo")
    var seasonNo: Int? = null,
    @SerialName("episodeName")
    var episodeName: String? = null,
    @SerialName("episodeNo")
    var episodeNo: String? = null,
    @SerialName("contentType")
    var contentType: String? = null,
    @SerialName("isDrm")
    var isDrm: Boolean? = null,
    @SerialName("playingUrl")
    var playingUrl: String? = null,
    @SerialName("affiliate")
    var affiliate: String? = null,
    @SerialName("agent")
    var agent: String? = null,
    @SerialName("errorMessage")
    var errorMessage: String? = null,
    @SerialName("errorCause")
    var errorCause: String? = null,
    @SerialName("adId")
    var adId: String? = null,
    @SerialName("adEvents")
    var adEvent: String? = null,
    @SerialName("adPosition")
    var adPosition: String? = null,
    @SerialName("adIsLive")
    var adIsLive: String? = null,
    @SerialName("adCreativeId")
    var adCreativeId: String? = null,
    @SerialName("adFirstCreativeId")
    var adFirstCreativeId: String? = null,
    @SerialName("adFirstAdId")
    var adFirstAdId: String? = null,
    @SerialName("adFirstAdSystem")
    var adFirstAdSystem: String? = null,
    @SerialName("adSystem")
    var adSystem: String? = null,
    @SerialName("adTechnology")
    var adTechnology: String? = null,
    @SerialName("adIsSlate")
    var adIsSlate: String? = null,
    @SerialName("adErrorMessage")
    var adErrorMessage: String? = null,
) : BasePlayerEventData()
