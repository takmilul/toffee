package com.banglalink.toffee.usecase

import android.content.Context
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.database.entities.SubscriptionCount
import com.banglalink.toffee.data.network.retrofit.DbApi
import com.banglalink.toffee.data.repository.SubscriptionCountRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.nio.file.Files
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
                ToffeeAnalytics.logApiError("", e.message)
            }
        }
    }

    private fun processFile(file: File?): Boolean {
        if (file == null || !file.exists()) {
            return false
        }
        ToffeeAnalytics.logBreadCrumb("Processing subscription count file")
        val fileBytes = Files.readAllBytes(file.toPath())
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
        ToffeeAnalytics.logBreadCrumb("Updating reaction status db")
        subscriptionCountRepository.insertAll(*subscriptionCountList.toTypedArray())
        ToffeeAnalytics.logBreadCrumb("Reaction status db updated")
    }
}