package com.banglalink.toffee.usecase

import android.content.Context
import android.util.Log
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.database.entities.SubscriptionCount
import com.banglalink.toffee.data.network.retrofit.DbApi
import com.banglalink.toffee.data.repository.SubscriptionCountRepository
import com.banglalink.toffee.util.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.util.zip.CRC32

class DownloadSubscriptionCountDb(
    private val dbApi: DbApi,
    private val subscriptionCountRepository: SubscriptionCountRepository
) {
    private val subscriptionCountList = mutableListOf<SubscriptionCount>()
    
    companion object {
        const val TAG = "Subscription_count_db"
    }
    
    suspend fun execute(context: Context, url: String) {
        withContext(Dispatchers.IO) {
            try {
                val file = DownloaderGeneric(context, dbApi).downloadFile(url)
                if(processFile(file)) {
                    updateDb()
                }
            }
            catch (e: Exception) {
                Log.i(DownloadService.TAG, e.message.toString())
                ToffeeAnalytics.logApiError("", e.message)
            }
        }
    }
    
    private fun processFile(file: File?): Boolean {
        if (file == null || !file.exists()) {
            return false
        }
        Log.i(DownloadService.TAG, "Processing subscription count file")
        ToffeeAnalytics.logBreadCrumb("Processing subscription count file")
        val fileBytes = Utils.readFileToBytes(file)
        val byteBuffer = ByteBuffer.wrap(fileBytes)
        val checksum = CRC32()
        checksum.update(fileBytes, 0, fileBytes.size)
        
        subscriptionCountList.clear()
        while (byteBuffer.remaining() > 0) {
            val channelId = byteBuffer.int
            val count = byteBuffer.long
            subscriptionCountList.add(SubscriptionCount(channelId, count))
        }
        return true
    }
    
    private suspend fun updateDb() {
        Log.i(DownloadService.TAG, "Updating subscription count db")
        ToffeeAnalytics.logBreadCrumb("Updating subscription status db")
        subscriptionCountRepository.insertAll(*subscriptionCountList.toTypedArray())
        Log.i(DownloadService.TAG, "Subscription count db updated")
        ToffeeAnalytics.logBreadCrumb("Subscription status db updated")
    }
}