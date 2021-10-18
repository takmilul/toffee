package com.banglalink.toffee.usecase

import android.content.Context
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.database.entities.ViewCount
import com.banglalink.toffee.data.network.retrofit.DbApi
import com.banglalink.toffee.data.repository.ViewCountRepository
import com.google.common.io.Files
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.util.zip.CRC32

const val TAG = "File"

class DownloadViewCountDb(
    private val dbApi: DbApi,
    private val viewCountRepository: ViewCountRepository
)
{
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
                ToffeeAnalytics.logApiError("", e.message)
            }
        }
    }

    private fun processFile(file: File?):Boolean {
        if(file == null || !file.exists()){
            return false
        }
        ToffeeAnalytics.logBreadCrumb("Processing view count file")
        val fileBytes = Files.toByteArray(file)
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
        ToffeeAnalytics.logBreadCrumb("Updating view count db")
        viewCountRepository.insertAll(*viewCountList.toTypedArray())
        ToffeeAnalytics.logBreadCrumb("View count db updated")
    }
}

