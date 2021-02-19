package com.banglalink.toffee.usecase

import android.content.Context
import android.util.Log
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.network.retrofit.DbApi
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

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
                    if (file != null) {
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
        val fileName = "temp_${System.currentTimeMillis()}"
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