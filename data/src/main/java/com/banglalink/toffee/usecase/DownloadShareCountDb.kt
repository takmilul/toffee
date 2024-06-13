package com.banglalink.toffee.usecase

import android.content.Context
import android.util.Log
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.database.entities.ShareCount
import com.banglalink.toffee.data.network.retrofit.DbApi
import com.banglalink.toffee.data.repository.ShareCountRepository
import com.banglalink.toffee.util.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.util.zip.CRC32

class DownloadShareCountDb(
    private val dbApi: DbApi,
    private val shareCountRepository: ShareCountRepository
) {
    private val shareCountList = mutableListOf<ShareCount>()
    
    companion object {
        const val TAG = "Share_Count_db"
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
        Log.i(DownloadService.TAG, "Processing share count file")
        ToffeeAnalytics.logBreadCrumb("Processing share count file")
        val fileBytes = Utils.readFileToBytes(file)
        val byteBuffer = ByteBuffer.wrap(fileBytes)
        val checksum = CRC32()
        checksum.update(fileBytes, 0, fileBytes.size)
        
        shareCountList.clear()
        while (byteBuffer.remaining() > 0) {
            val contentId = byteBuffer.int
            val count = byteBuffer.long
            shareCountList.add(ShareCount(contentId, count))
        }
        return true
    }
    
    private suspend fun updateDb() {
        Log.i(DownloadService.TAG, "Updating share count db")
        ToffeeAnalytics.logBreadCrumb("Updating share count db")
        shareCountRepository.insertAll(*shareCountList.toTypedArray())
        Log.i(DownloadService.TAG, "Share count db updated")
        ToffeeAnalytics.logBreadCrumb("Share count db updated")
    }
}