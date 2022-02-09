package com.banglalink.toffee.usecase

import android.content.Context
import com.banglalink.toffee.util.Log
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.network.retrofit.DbApi
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*

class DownloaderGeneric(private val context: Context, private val dbApi: DbApi) {

    fun downloadFile(url: String): File? {
        var file: File? = null
        try {
            val response = dbApi.downloadFile(url).execute()
            if(response.isSuccessful){
                Log.i(TAG, "File downloaded")
                val buffer = response.body()?.byteStream()
                if (buffer != null) {
                    file = createFile(context)
                    ToffeeAnalytics.logBreadCrumb("File created")
                    if (file != null && file.exists()) {
                        copyStreamToFile(buffer, file)
                    }
                }
                return file
//                    if(processFile(file)){
//                        updateDb()
//                    }
            }
        }catch (e:Exception){
            ToffeeAnalytics.logException(e)
        }
        return null
    }

    private fun createFile(context: Context): File? {
        val storageDir = context.cacheDir
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        val fileName = "temp_${UUID.randomUUID()}"
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