package com.banglalink.toffee.data.storage

import android.content.Context
import android.content.SharedPreferences
import com.banglalink.toffee.model.PlayerSessionDetails
import com.banglalink.toffee.util.Log
import com.banglalink.toffee.util.currentDateTime
import java.text.SimpleDateFormat
import java.util.*

class PlayerPreference private constructor(val context: Context) {
    
    private val pref: SharedPreferences = context.getSharedPreferences("PLAYER_PREF_V3", Context.MODE_PRIVATE)
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
    
    companion object {
        private var instance: PlayerPreference? = null
        
        fun init(context: Context) {
            if (instance == null) {
                instance = PlayerPreference(context.applicationContext)
            }
        }
        
        fun getInstance(): PlayerPreference {
            if (instance == null) {
                throw InstantiationException("Instance is null...call init() first")
            }
            return instance as PlayerPreference
        }
    }

    fun savePlayerSessionBandWidth(durationInMillis: Long, bandWidthInMB: Double) {
        pref.edit().putString(UUID.randomUUID().toString(), "$durationInMillis,$bandWidthInMB")
            .apply()
    }

    fun setInitialTime(){
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Dhaka"))
        val cal = Calendar.getInstance(TimeZone.getDefault())
        val dateGMT = cal.time

       pref.edit().putString("initial_Time",sdf.format(dateGMT)).apply()

    }

    fun getInitialTime():String{
        val timeString = pref.getString("initial_Time", currentDateTime) ?: currentDateTime
        pref.edit().remove("initial_Time").apply()
        return timeString
    }

    fun getPlayerSessionDetails(): List<PlayerSessionDetails> {
        return try {
            val keys: Map<String, *> = pref.all
            val sessionList = ArrayList<PlayerSessionDetails>()
            for ((key, value) in keys) {
                val durationBandWidthString = value.toString()
                val list = durationBandWidthString.split(",")
                
                if (list.size != 2) {
                    continue
                }
                val durationInSec = list[0].toLong()
                val totalBandWidthInMB = list[1].toDouble()
                
                if (durationInSec == 0L)
                    continue
                
                sessionList.add(PlayerSessionDetails(durationInSec, totalBandWidthInMB))
                Log.i("map values", "$key: ${value.toString()}")
            }
            pref.edit().clear().apply()
            sessionList
        } catch (e: Exception) {
            Log.e("Exception", e.message ?: "")
            emptyList()
        }
    }
}