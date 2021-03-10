package com.banglalink.toffee.usecase

import android.content.Context
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.database.entities.ReactionStatusItem
import com.banglalink.toffee.data.network.retrofit.DbApi
import com.banglalink.toffee.data.repository.ReactionStatusRepository
import com.google.common.io.Files
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.util.zip.CRC32


class DownloadReactionStatusDb(
    private val dbApi: DbApi,
    private val reactionStatusRepository: ReactionStatusRepository
) {
    private val reactionList = mutableListOf<ReactionStatusItem>()

    companion object {
        const val TAG = "Reaction_Status_db"
    }

    suspend fun execute(context: Context, url: String) {
        withContext(Dispatchers.IO) {
            val file = DownloaderGeneric(context, dbApi).downloadFile(url)
            if(processFile(file)) {
                updateDb()
            }
        }
    }

    private fun processFile(file: File?): Boolean {
        if (file == null) {
            return false
        }
        ToffeeAnalytics.logBreadCrumb("Processing reaction status file")
        val fileBytes = Files.toByteArray(file)
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
        ToffeeAnalytics.logBreadCrumb("Updating reaction status db")
        reactionStatusRepository.insert(*reactionList.toTypedArray())
        ToffeeAnalytics.logBreadCrumb("Reaction status db updated")
    }
}