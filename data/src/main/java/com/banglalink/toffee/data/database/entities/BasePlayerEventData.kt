package com.banglalink.toffee.data.database.entities

import android.os.Build
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.currentDateTimeMillis
import com.google.gson.annotations.SerializedName

abstract class BasePlayerEventData {
    
    @SerializedName("appVersion")
    var appVersion: String = CommonPreference.getInstance().appVersionName
    
    @SerializedName("osName")
    var osName: String = Utils.getOsName()
    
    @SerializedName("userId")
    var userId: Int = SessionPreference.getInstance().customerId
    
    @SerializedName("deviceId")
    var deviceId: String = CommonPreference.getInstance().deviceId
    
    @SerializedName("deviceManufacturer")
    var deviceManufacturer: String = Build.MANUFACTURER
    
    @SerializedName("deviceModel")
    var deviceModel: String = Build.MODEL
    
    @SerializedName("msisdn")
    var msisdn: String = SessionPreference.getInstance().phoneNumber
    
    @SerializedName("osVersion")
    var osVersion: String = Build.VERSION.SDK_INT.toString()
    
    @SerializedName("city")
    var city: String = SessionPreference.getInstance().geoCity
    
    @SerializedName("region")
    var region: String = SessionPreference.getInstance().geoRegion
    
    @SerializedName("country")
    var country: String = SessionPreference.getInstance().geoLocation
    
    @SerializedName("lat")
    var lat: String = SessionPreference.getInstance().latitude
    
    @SerializedName("lon")
    var lon: String = SessionPreference.getInstance().longitude
    
    @SerializedName("clientIp")
    var clientIp: String = SessionPreference.getInstance().userIp
    
    @SerializedName("deviceType")
    var deviceType: String = if(CommonPreference.getInstance().isTablet) "Tablet" else "Mobile Phone"
    
    @SerializedName("applicationType")
    var applicationType: String = "Android"
    
    @SerializedName("statusCode")
    var statusCode: Int = 200
    
    @SerializedName("dateTime")
    var dateTime: String = currentDateTimeMillis
    
}