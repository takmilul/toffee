package com.banglalink.toffee.usecase

import android.content.Context
import android.util.Log
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.database.entities.ViewCount
import com.banglalink.toffee.data.network.retrofit.DbApi
import com.banglalink.toffee.data.repository.ViewCountRepository
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.util.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.util.zip.CRC32

const val TAG = "File"

class DownloadViewCountDb(
    private val dbApi: DbApi,
    private val mPref: SessionPreference,
    private val viewCountRepository: ViewCountRepository
) {
    private val viewCountList = mutableListOf<ViewCount>()
    
    suspend fun execute(context: Context, url: String){
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
    
    private fun processFile(file: File?):Boolean {
        if(file == null || !file.exists()){
            return false
        }
        Log.i(DownloadService.TAG, "Processing view count file")
        ToffeeAnalytics.logBreadCrumb("Processing view count file")
        val fileBytes = Utils.readFileToBytes(file)
        val byteBuffer = ByteBuffer.wrap(fileBytes)
        val checksum = CRC32()
        checksum.update(fileBytes, 0, fileBytes.size)
        
        viewCountList.clear()
        while (byteBuffer.remaining() > 0) {
            val contentId = byteBuffer.int
            val viewCount = byteBuffer.long
//            Log.i(TAG, "content id $contentId and viewcount $viewCount remaining ${byteBuffer.remaining()}")
            viewCountList.add(ViewCount().apply {
                this.channelId = contentId.toLong()
                this.viewCount = viewCount
            })
        }
        return true
    }
    
    private suspend fun updateDb() {
        Log.i(DownloadService.TAG, "Updating view count db")
        ToffeeAnalytics.logBreadCrumb("Updating view count db")
        viewCountRepository.insertAll(*viewCountList.toTypedArray())
        mPref.isViewCountDbUpdatedLiveData.postValue(true)
        Log.i(DownloadService.TAG, "View count db updated")
        ToffeeAnalytics.logBreadCrumb("View count db updated")
    }
}

