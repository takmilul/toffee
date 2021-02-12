package com.banglalink.toffee.usecase

import android.content.Context
import android.os.Environment
import android.util.Log
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.data.database.entities.ReactionInfo
import com.banglalink.toffee.data.network.retrofit.DbApi
import com.google.common.io.Files
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.zip.CRC32


class DownloadReactionDb(
    private val dbApi: DbApi,
    private val reactionDao: ReactionDao
) {
    private val reactionList = mutableListOf<ReactionInfo>()

    companion object {
        const val TAG = "Reaction_db"
    }

    suspend fun execute(context: Context, url: String) =
        withContext(Dispatchers.IO) {
            var file: File? = null
            try {
                val response = dbApi.downloadFile(url).execute()
                if (response.isSuccessful) {
                    Log.i(TAG, "File downloaded")
                    val buffer = response.body()?.byteStream()
                    if (buffer != null) {
                        file = createFile(context, "reaction")
                        ToffeeAnalytics.logBreadCrumb("File created")
                        if (file != null) {
                            copyStreamToFile(buffer, file)
                        }
                    }
                    if (processFile(file)) {
                        updateDb()
                    }
                }
            }
            catch (e: Exception) {
                ToffeeAnalytics.logException(e)
            }
        }

    private fun processFile(file: File?): Boolean {
        if (file == null) {
            return false
        }
        ToffeeAnalytics.logBreadCrumb("Processing file")
        val fileBytes = Files.toByteArray(file)
        val byteBuffer = ByteBuffer.wrap(fileBytes)
        val checksum = CRC32()
        checksum.update(fileBytes, 0, fileBytes.size)

        reactionList.clear()
        while (byteBuffer.remaining() > 0) {
            val id = byteBuffer.long
            val customerId = byteBuffer.int
            val contentId = byteBuffer.long
            val reaction = byteBuffer.int
            val reactionTime = byteBuffer.long
            reactionList.add(ReactionInfo(id, customerId, contentId, reaction, reactionTime))
        }
        return true
    }

    private suspend fun updateDb() {
        ToffeeAnalytics.logBreadCrumb("Updating reaction db")
        reactionDao.insertAll(*reactionList.toTypedArray())
        ToffeeAnalytics.logBreadCrumb("Reaction db updated")
    }

    private fun createFile(context: Context, fileName: String): File? {
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        return File.createTempFile(fileName, ".db", storageDir)
    }

    private fun copyStreamToFile(inputStream: InputStream, outputFile: File) {
        ToffeeAnalytics.logBreadCrumb("File write started")
        inputStream.use { input ->
            val outputStream = FileOutputStream(outputFile)
            outputStream.use { output ->
                val buffer = ByteArray(4 * 1024)
                while (true) {
                    val byteCount = input.read(buffer)
                    if (byteCount < 0) break
                    output.write(buffer, 0, byteCount)
                }
                output.flush()
                ToffeeAnalytics.logBreadCrumb("File write finished")
            }
        }
    }
}

