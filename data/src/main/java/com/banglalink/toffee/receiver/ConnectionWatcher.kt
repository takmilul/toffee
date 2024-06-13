package com.banglalink.toffee.receiver

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.telephony.TelephonyManager.NETWORK_TYPE_1xRTT
import android.telephony.TelephonyManager.NETWORK_TYPE_CDMA
import android.telephony.TelephonyManager.NETWORK_TYPE_EDGE
import android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_0
import android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_A
import android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_B
import android.telephony.TelephonyManager.NETWORK_TYPE_GPRS
import android.telephony.TelephonyManager.NETWORK_TYPE_HSDPA
import android.telephony.TelephonyManager.NETWORK_TYPE_HSPA
import android.telephony.TelephonyManager.NETWORK_TYPE_HSPAP
import android.telephony.TelephonyManager.NETWORK_TYPE_IDEN
import android.telephony.TelephonyManager.NETWORK_TYPE_LTE
import android.telephony.TelephonyManager.NETWORK_TYPE_NR
import android.telephony.TelephonyManager.NETWORK_TYPE_UMTS
import androidx.annotation.IntRange
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine

//https://stackoverflow.com/questions/53532406/activenetworkinfo-type-is-deprecated-in-api-level-28

class ConnectionWatcher constructor(
    private val application: Application
) {
    
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
    
    var netType = ""
        get() {
            updateFields()
            return field
        }
    
    @Suppress("DEPRECATION")
    private fun updateFields() {
        try {
            var isWifi = false
            var isCellular = false
            var isEthernet = false
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val networkAvailability = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                
                if (networkAvailability != null &&
                    networkAvailability.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    networkAvailability.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                ) {
                    //has network
                    isOnline = true
                    
                    // wifi
                    isWifi = networkAvailability.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    isOverWifi = isWifi
                    
                    // cellular
                    isCellular = networkAvailability.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    isOverCellular = isCellular
                    
                    // ethernet
                    isEthernet = networkAvailability.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                    isOverEthernet = isEthernet
                } else {
                    networkFailed()
                }
            } else {
                val info = connectivityManager.activeNetworkInfo
                if (info != null && info.isConnected) {
                    isOnline = true
                    
                    val wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                    isWifi = wifi != null && wifi.isConnected
                    isOverWifi = isWifi
                    
                    val cellular = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                    isCellular = cellular != null && cellular.isConnected
                    isOverCellular = isCellular
                    
                    val ethernet = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET)
                    isEthernet = ethernet != null && ethernet.isConnected
                    isOverEthernet = isEthernet
                } else {
                    networkFailed()
                }
            }
            netType = when {
                isWifi -> "WiFi"
                isCellular -> updateCellularNetworkType()
                isEthernet -> "Ethernet"
                else -> "Offline"
            }
        } catch (e: Exception) {
            networkFailed()
            Log.e("CONN_", "Watcher - Connectivity registration failed: ${e.message}")
        }
    }
    
    @Suppress("DEPRECATION")
    private fun updateCellularNetworkType(): String {
        return try {
            val networkType = connectivityManager.activeNetworkInfo?.subtype ?: -1
            val is2G = networkType == NETWORK_TYPE_EDGE
                || networkType == NETWORK_TYPE_GPRS
                || networkType == NETWORK_TYPE_CDMA
                || networkType == NETWORK_TYPE_IDEN
                || networkType == NETWORK_TYPE_1xRTT
            
            val is3G = networkType == NETWORK_TYPE_UMTS
                || networkType == NETWORK_TYPE_HSDPA
                || networkType == NETWORK_TYPE_HSPA
                || networkType == NETWORK_TYPE_HSPAP
                || networkType == NETWORK_TYPE_EVDO_0
                || networkType == NETWORK_TYPE_EVDO_A
                || networkType == NETWORK_TYPE_EVDO_B
            
            val is4G = networkType == NETWORK_TYPE_LTE
            
            var is5G = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                is5G = networkType == NETWORK_TYPE_NR
            }
            
            isOver2G = is2G
            isOver3G = is3G
            isOver4G = is4G
            isOver5G = is5G
            
            when {
                is2G -> "2G"
                is3G -> "3G"
                is4G -> "4G"
                is5G -> "5G"
                else -> "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
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
        netType = "Unknown"
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