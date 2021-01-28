package com.banglalink.toffee.usecase

import android.content.Context
import android.os.Environment
import android.util.Log
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.network.retrofit.DbApi
import com.banglalink.toffee.data.storage.ViewCountDAO
import com.banglalink.toffee.data.storage.ViewCountDataModel
import com.google.common.io.Files
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.zip.CRC32

const val TAG = "File"

class DownloadViewCountDb(
    private val dbApi: DbApi,
    private val viewCountDAO: ViewCountDAO
)
{
    private val viewCountList = mutableListOf<ViewCountDataModel>()

    suspend fun execute(context: Context, url: String) =
        withContext(Dispatchers.IO) {
            var file: File? = null
            try {
                val response = dbApi.downloadFile(url).execute()
                if(response.isSuccessful){
                    Log.i(TAG, "File downloaded")
                    val buffer = response.body()?.byteStream()
                    if (buffer != null) {
                        file = createFile(context, "viewcount")
                        ToffeeAnalytics.logBreadCrumb("File created")
                        if (file != null) {
                            copyStreamToFile(buffer, file)
                        }
                    }
                    if(processFile(file)){
                        updateDb()
                    }
                }
            }catch (e:Exception){
                ToffeeAnalytics.logException(e)
            }
        }

    private fun processFile(file: File?):Boolean {
        if(file == null){
            return false
        }
        ToffeeAnalytics.logBreadCrumb("Processing file")
        val filebytes = Files.toByteArray(file)
        val byteBuffer = ByteBuffer.wrap(filebytes)
        val checksum = CRC32()
        checksum.update(filebytes, 0, filebytes.size)

        viewCountList.clear()
        while (byteBuffer.remaining() > 0) {
            val contentId = byteBuffer.int
            val viewCount = byteBuffer.long
            Log.i(TAG, "content id $contentId and viewcount $viewCount remaining ${byteBuffer.remaining()}")
            viewCountList.add(ViewCountDataModel().apply {
                this.channelId = contentId.toLong()
                this.viewCount = viewCount
            })
        }
        return true
    }

    private fun updateDb() {
        ToffeeAnalytics.logBreadCrumb("Updating view count db")
        viewCountDAO.insertAll(*viewCountList.toTypedArray())
        ToffeeAnalytics.logBreadCrumb("View count db updated")
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

