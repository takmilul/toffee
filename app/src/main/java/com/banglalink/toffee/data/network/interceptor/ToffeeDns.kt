package com.banglalink.toffee.data.network.interceptor

import android.util.Log
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.exception.ToffeeDnsException
import okhttp3.Dns
import okhttp3.dnsoverhttps.DnsOverHttps
import java.net.InetAddress
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToffeeDns @Inject constructor(private val httpsDns: DnsOverHttps): Dns {
    override fun lookup(hostname: String): List<InetAddress> {
        return try {
//            if(hostname.contains("streamer")) throw UnknownHostException(hostname)
            Dns.SYSTEM.lookup(hostname)
//                .apply {
//                Log.e("DNS_T", "Resolved by system DNS -> $hostname")
//            }
        } catch (ex: UnknownHostException) {
            httpsDns.lookup(hostname).also {
//                Log.e("DNS_T", "Resolved by HTTPS DNS -> $hostname")
//                Log.e("DNS_T", "Resolved IPs -> $it")
                if(it.isNotEmpty()) {
                    ToffeeAnalytics.logException(ToffeeDnsException("Resolved by HttpsDns ${hostname}, Response entry -> ${it.size}"))
                }
            }
        }
    }
}