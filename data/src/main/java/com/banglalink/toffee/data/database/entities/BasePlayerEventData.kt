package com.banglalink.toffee.data.database.entities

import android.os.Build
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.currentDateTime
import com.banglalink.toffee.util.currentDateTimeMillis
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
abstract class BasePlayerEventData {
    
    @SerialName("appVersion")
    var appVersion: String = CommonPreference.getInstance().appVersionName
    
    @SerialName("osName")
    var osName: String = Utils.getOsName()
    
    @SerialName("userId")
    var userId: Int = SessionPreference.getInstance().customerId
    
    @SerialName("deviceId")
    var deviceId: String = CommonPreference.getInstance().deviceId
    
    @SerialName("deviceManufacturer")
    var deviceManufacturer: String = Build.MANUFACTURER
    
    @SerialName("deviceModel")
    var deviceModel: String = Build.MODEL
    
    @SerialName("msisdn")
    var msisdn: String = SessionPreference.getInstance().phoneNumber
    
    @SerialName("osVersion")
    var osVersion: String = Build.VERSION.SDK_INT.toString()
    
    @SerialName("city")
    var city: String = SessionPreference.getInstance().geoCity
    
    @SerialName("region")
    var region: String = SessionPreference.getInstance().geoRegion
    
    @SerialName("country")
    var country: String = SessionPreference.getInstance().geoLocation
    
    @SerialName("lat")
    var lat: String = SessionPreference.getInstance().latitude
    
    @SerialName("lon")
    var lon: String = SessionPreference.getInstance().longitude
    
    @SerialName("clientIp")
    var clientIp: String = SessionPreference.getInstance().userIp
    
    @SerialName("deviceType")
    var deviceType: String = if(CommonPreference.getInstance().isTablet) "Tablet" else "Mobile Phone"
    
    @SerialName("applicationType")
    var applicationType: String = "Android"
    
    @SerialName("statusCode")
    var statusCode: Int = 200
    
    @SerialName("dateTime")
    var dateTime: String = currentDateTimeMillis
    
    @SerialName("reportingTime")
    var reportingTime: String = currentDateTime
}