package com.banglalink.toffee.receiver

import android.Manifest
import android.Manifest.permission
import android.app.Application
import android.content.Context
import android.content.Context.TELEPHONY_SERVICE
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.telephony.TelephonyManager
import android.telephony.TelephonyManager.*
import androidx.annotation.IntRange
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine

//https://stackoverflow.com/questions/53532406/activenetworkinfo-type-is-deprecated-in-api-level-28

class ConnectionWatcher
@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
constructor(
    private val application: Application
) {
    
    private val telephonyManager = application.applicationContext.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
    private val connectivityManager = application.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    // general availability of Internet over any type
    var isOnline = false
        get() {
            updateFields()
            return field
        }
    
    var isOverWifi = false
        get() {
            updateFields()
            return field
        }
    
    var isOverCellular = false
        get() {
            updateFields()
            return field
        }
    
    var isOverEthernet = false
        get() {
            updateFields()
            return field
        }
    
    var isOver2G = false
        get() {
            updateFields()
            return field
        }
    
    var isOver3G = false
        get() {
            updateFields()
            return field
        }
    
    var isOver4G = false
        get() {
            updateFields()
            return field
        }
    
    var isOver5G = false
        get() {
            updateFields()
            return field
        }
    
    @Suppress("DEPRECATION")
    private fun updateFields() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val networkAvailability = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                
                if (networkAvailability != null &&
                    networkAvailability.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    networkAvailability.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                ) {
                    //has network
                    isOnline = true
                    
                    // wifi
                    isOverWifi = networkAvailability.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    
                    // cellular
                    isOverCellular = networkAvailability.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    
                    // ethernet
                    isOverEthernet = networkAvailability.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                    
                    if (networkAvailability.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        updateCellularNetworkType()
                    }
                } else {
                    networkFailed()
                }
            } else {
                val info = connectivityManager.activeNetworkInfo
                if (info != null && info.isConnected) {
                    isOnline = true
                    
                    val wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                    isOverWifi = wifi != null && wifi.isConnected
                    
                    val cellular = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                    isOverCellular = cellular != null && cellular.isConnected
                    
                    val ethernet = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET)
                    isOverEthernet = ethernet != null && ethernet.isConnected
                    
                    if (cellular != null && cellular.isConnected) {
                        updateCellularNetworkType()
                    }
                } else {
                    networkFailed()
                }
            }
        } catch (e: Exception) {
            networkFailed()
            Log.e("CONN_", "Watcher - Connectivity registration failed: ${e.message}")
        }
    }
    
    @Suppress("DEPRECATION")
    private fun updateCellularNetworkType() {
        if (ActivityCompat.checkSelfPermission(application.applicationContext, permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            isOver2G = telephonyManager.networkType == NETWORK_TYPE_EDGE
                || telephonyManager.networkType == NETWORK_TYPE_GPRS
                || telephonyManager.networkType == NETWORK_TYPE_CDMA
                || telephonyManager.networkType == NETWORK_TYPE_IDEN
                || telephonyManager.networkType == NETWORK_TYPE_1xRTT
            
            isOver3G = telephonyManager.networkType == NETWORK_TYPE_UMTS
                || telephonyManager.networkType == NETWORK_TYPE_HSDPA
                || telephonyManager.networkType == NETWORK_TYPE_HSPA
                || telephonyManager.networkType == NETWORK_TYPE_HSPAP
                || telephonyManager.networkType == NETWORK_TYPE_EVDO_0
                || telephonyManager.networkType == NETWORK_TYPE_EVDO_A
                || telephonyManager.networkType == NETWORK_TYPE_EVDO_B
            
            isOver4G = telephonyManager.networkType == NETWORK_TYPE_LTE
            
            if (VERSION.SDK_INT >= VERSION_CODES.Q) {
                isOver5G = telephonyManager.networkType == NETWORK_TYPE_NR
            }
        }
    }
    
    private fun networkFailed() {
        isOnline = false
        isOver2G = false
        isOver3G = false
        isOver4G = false
        isOver5G = false
        isOverWifi = false
        isOverCellular = false
        isOverEthernet = false
    }
    
    fun watchNetwork(): Flow<Boolean> = watchWifi()
        .combine(watchCellular()) { wifi, cellular -> wifi || cellular }
        .combine(watchEthernet()) { wifiAndCellular, ethernet -> wifiAndCellular || ethernet }
    
    fun watchWifi(): Flow<Boolean> = callbackFlowForType(NetworkCapabilities.TRANSPORT_WIFI)
    
    fun watchCellular(): Flow<Boolean> = callbackFlowForType(NetworkCapabilities.TRANSPORT_CELLULAR)
    
    fun watchEthernet(): Flow<Boolean> = callbackFlowForType(NetworkCapabilities.TRANSPORT_ETHERNET)
    
    private fun callbackFlowForType(@IntRange(from = 0, to = 7) type: Int) = callbackFlow {
        
        trySend(false)
        
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(type)
            .build()
        
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onLost(network: Network) {
                trySend(false)
            }
            
            override fun onUnavailable() {
                trySend(false)
            }
            
            override fun onLosing(network: Network, maxMsToLive: Int) {
                // do nothing
            }
            
            override fun onAvailable(network: Network) {
                trySend(true)
            }
        }
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (application.applicationContext.checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED) {
                    connectivityManager.registerNetworkCallback(networkRequest, callback)
                } else {
                    Log.e("CONN_", "Watcher - Connectivity registration failed: network permission denied")
                }
            } else {
                connectivityManager.registerNetworkCallback(networkRequest, callback)
            }
        } catch (e: Exception) {
            Log.e("CONN_", "Watcher - Connectivity registration failed: ${e.message}")
        }
        
        awaitClose {
            try {
                connectivityManager.unregisterNetworkCallback(callback)
            } catch (e: Exception) {
                ToffeeAnalytics.logBreadCrumb("connectivity manager unregister error -> ${e.message}")
            }
        }
    }
}