package com.banglalink.toffee.usecase

import android.content.Context
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.data.database.entities.ReactionInfo
import com.banglalink.toffee.data.network.retrofit.DbApi
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.util.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.util.zip.CRC32


class DownloadReactionDb(
    private val dbApi: DbApi,
    private val reactionDao: ReactionDao,
    private val mPref: SessionPreference
) {
    private val reactionList = mutableListOf<ReactionInfo>()

    companion object {
        const val TAG = "Reaction_db"
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
        ToffeeAnalytics.logBreadCrumb("Processing user reaction file")
        val fileBytes = Utils.readFileToBytes(file)
        val byteBuffer = ByteBuffer.wrap(fileBytes)
        val checksum = CRC32()
        checksum.update(fileBytes, 0, fileBytes.size)

        reactionList.clear()
        while (byteBuffer.remaining() > 0) {
            val customerId = byteBuffer.int
            val contentId = byteBuffer.long
            val reaction = byteBuffer.int
            val reactionTime = System.nanoTime()
            reactionList.add(ReactionInfo(null, customerId, contentId, reaction, reactionTime))
        }
        return true
    }

    private suspend fun updateDb() {
        ToffeeAnalytics.logBreadCrumb("Updating user reaction db")
        reactionDao.insertAll(*reactionList.toTypedArray())
        mPref.hasReactionDb = true
        ToffeeAnalytics.logBreadCrumb("User Reaction db updated")
    }
}