package com.banglalink.toffee.usecase

import android.content.Context
import android.util.*
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.database.entities.ReactionStatusItem
import com.banglalink.toffee.data.network.retrofit.DbApi
import com.banglalink.toffee.data.repository.ReactionCountRepository
import com.banglalink.toffee.util.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.util.zip.*

class DownloadReactionStatusDb(
    private val dbApi: DbApi,
    private val reactionCountRepository: ReactionCountRepository
) {
    private val reactionList = mutableListOf<ReactionStatusItem>()
    
    companion object {
        const val TAG = "Reaction_Status_db"
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
        Log.i(DownloadService.TAG, "Processing reaction count file")
        ToffeeAnalytics.logBreadCrumb("Processing reaction status file")
        val fileBytes = Utils.readFileToBytes(file)
        val byteBuffer = ByteBuffer.wrap(fileBytes)
        val checksum = CRC32()
        checksum.update(fileBytes, 0, fileBytes.size)
        
        reactionList.clear()
        while (byteBuffer.remaining() > 0) {
            val contentId = byteBuffer.int
            val reactionType = byteBuffer.int
            val reactionCount = byteBuffer.long
            reactionList.add(ReactionStatusItem(contentId, reactionType, reactionCount))
        }
        return true
    }
    
    private suspend fun updateDb() {
        Log.i(DownloadService.TAG, "Updating reaction count db")
        ToffeeAnalytics.logBreadCrumb("Updating reaction status db")
        reactionCountRepository.insertAll(*reactionList.toTypedArray())
        Log.i(DownloadService.TAG, "Reaction count db updated")
        ToffeeAnalytics.logBreadCrumb("Reaction status db updated")
    }
}