package com.banglalink.toffee.util

import com.banglalink.toffee.receiver.ConnectionWatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.net.InetAddress
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.measureTimeMillis

@Serializable
data class PingData(
    val isOnline: Boolean = false,
    val networkType: String? = null,
    val ispOrTelecom: String? = null,
    val host: String? = null,
    val ip: String? = null,
    val latency: String? = null
)

@Singleton
class PingTool @Inject constructor(
    private var connectionWatcher: ConnectionWatcher
) {
    
    private val coroutineScope = CoroutineScope(IO + Job())
    
    suspend fun ping(hostUrl: String): PingData? {
        var pingData: PingData? = null
        coroutineScope.launch(IO) {
            try {
                val host = hostUrl.removePrefix("https://www.").removePrefix("http://www.").replaceAfter(".com", "")
                val inetAddress = InetAddress.getByName(host)
                val ip = inetAddress.hostAddress
                val runtime = Runtime.getRuntime()
//                var isExist = false
                val isOnline = connectionWatcher.isOnline
                val netType = connectionWatcher.netType
                val ispOrTelecom = if (connectionWatcher.isOverCellular) "Telecom" else "ISP"
                val latency = measureTimeMillis {
                    try {
                        val ipProcess = runtime.exec("/system/bin/ping -c 1 $host")
                        val exitValue = ipProcess.waitFor()
//                        isExist = (exitValue == 0)
                    } catch (e: Exception) {
                        Log.i("HOS_", "isConnectedToThisServer: ${e.message}")
                        e.printStackTrace()
                    }
                }.toString().plus(" ms")
                pingData = PingData(isOnline, netType, ispOrTelecom, host, ip, latency)
            } catch (e: Exception) {
                pingData = PingData(false)
            }
        }.join()
        return pingData
    }
}