package com.banglalink.toffee.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class PlayerEventData (
    
    @SerializedName("dateTime")
    var dateTime: String, // = System.currentTimeMillis().toFormattedBigDate()
    
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    val id: Long = 0,
    
    @SerializedName("sessionId")
    var sessionId: String? = null,
    
    @SerializedName("isInternetAvailable")
    var isInternetAvailable: Boolean? = null,                               //ping tool
    
    @SerializedName("networkType")
    var networkType: String? = null,                                        //ping tool
    
    @SerializedName("ispOrTelecomOperator")
    var ispOrTelecomOperator: String? = null,                               //ping tool
    
    @SerializedName("remoteHost")
    var remoteHost: String? = null,                                         //ping tool
    
    @SerializedName("remoteIp")
    var remoteIp: String? = null,                                           //ping tool
    
    @SerializedName("latencyClientToCdn")
    var latencyClientToCdn: String? = null,                                 //ping tool
    
    @SerializedName("playerEventId")
    var playerEventId: Int? = null,

    @SerializedName("playerEvent")
    var playerEvent: String? = null,

    @SerializedName("contentId")
    var contentId: String? = null,
    
    @SerializedName("contentTitle")
    var contentTitle: String? = null,
    
    @SerializedName("contentProviderId")
    var contentProviderId: String? = null,
    
    @SerializedName("contentProviderName")
    var contentProviderName: String? = null,
    
    @SerializedName("contentCategoryId")
    var contentCategoryId: Int? = 0,
    
    @SerializedName("contentCategoryName")
    var contentCategoryName: String? = null,
    
    @SerializedName("contentDuration")
    var contentDuration: String? = null,
    
    @SerializedName("seasonName")
    var seasonName: String? = null,
    
    @SerializedName("seasonNo")
    var seasonNo: Int? = null,
    
    @SerializedName("episodeName")
    var episodeName: String? = null,
    
    @SerializedName("episodeNo")
    var episodeNo: String? = null,
    
    @SerializedName("contentType")
    var contentType: String? = null,
    
    @SerializedName("isDrm")
    var isDrm: Boolean? = null,
    
    @SerializedName("playingUrl")
    var playingUrl: String? = null,
    
    @SerializedName("affiliate")
    var affiliate: String? = null,

    @SerializedName("agent")
    var agent: String? = null,
    
    @SerializedName("errorMessage")
    var errorMessage: String? = null,
    
    @SerializedName("errorCause")
    var errorCause: String? = null,
    
    @SerializedName("adId")
    var adId: String? = null,
    
    @SerializedName("adEvents")
    var adEvent: String? = null,
    
    @SerializedName("adPosition")
    var adPosition: String? = null,
    
    @SerializedName("adIsLive")
    var adIsLive: String? = null,
    
    @SerializedName("adCreativeId")
    var adCreativeId: String? = null,
    
    @SerializedName("adFirstCreativeId")
    var adFirstCreativeId: String? = null,
    
    @SerializedName("adFirstAdId")
    var adFirstAdId: String? = null,
    
    @SerializedName("adFirstAdSystem")
    var adFirstAdSystem: String? = null,
    
    @SerializedName("adSystem")
    var adSystem: String? = null,
    
    @SerializedName("adTechnology")
    var adTechnology: String? = null,
    
    @SerializedName("adIsSlate")
    var adIsSlate: String? = null,
    
    @SerializedName("adErrorMessage")
    var adErrorMessage: String? = null,

): BasePlayerEventData()
