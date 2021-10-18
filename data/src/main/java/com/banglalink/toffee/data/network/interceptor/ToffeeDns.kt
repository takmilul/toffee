package com.banglalink.toffee.data.network.interceptor

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
            Dns.SYSTEM.lookup(hostname)
        } catch (ex: UnknownHostException) {
            try {
                httpsDns.lookup(hostname).also {
                    ToffeeAnalytics.logException(ToffeeDnsException("Resolved by HttpsDns ${hostname}, Response size -> ${it.size}"))
                }
            } catch (ex2: UnknownHostException) {
                ex2.printStackTrace()
                throw ex
            }
        }
    }
}