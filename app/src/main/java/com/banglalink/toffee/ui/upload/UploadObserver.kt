package com.banglalink.toffee.ui.upload

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.apiservice.ApiRoutes
import com.banglalink.toffee.data.network.retrofit.CacheManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.gotev.uploadservice.UploadServiceConfig
import net.gotev.uploadservice.data.RetryPolicyConfig
import net.gotev.uploadservice.data.UploadInfo
import net.gotev.uploadservice.network.ServerResponse
import net.gotev.uploadservice.observer.request.GlobalRequestObserver
import net.gotev.uploadservice.observer.request.RequestObserverDelegate
import net.gotev.uploadservice.okhttp.OkHttpStack
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class UploadObserver(
    private val app: Application,
    private val uploadManager: UploadStateManager,
    private val cacheManager: CacheManager
) {
    val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        initUploader()
    }

    companion object {
        const val notificationChannelID = "Toffee Upload"
    }

    private fun initUploader() {
        createNotificationChannel()

        UploadServiceConfig.initialize(app, notificationChannelID, BuildConfig.DEBUG)
        UploadServiceConfig.httpStack = OkHttpStack()
        UploadServiceConfig.retryPolicy = RetryPolicyConfig(
            initialWaitTimeSeconds = 5,
            maxWaitTimeSeconds = 30,
            multiplier = 1,
            defaultMaxRetries = 6
        )

        UploadServiceConfig.threadPool = ThreadPoolExecutor(
            1, // Initial pool size
            1, // Max pool size
            5.toLong(), // Keep Alive Time
            TimeUnit.SECONDS,
            LinkedBlockingQueue()
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                notificationChannelID,
                "Toffee Upload Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun start() {
        GlobalRequestObserver(app, object: RequestObserverDelegate {
            override fun onCompleted(context: Context, uploadInfo: UploadInfo) {

            }

            override fun onCompletedWhileNotObserving() {

            }

            override fun onError(context: Context, uploadInfo: UploadInfo, exception: Throwable) {
                coroutineScope.launch {
                    uploadManager.handleError(uploadInfo.uploadId, exception)
                }
            }

            override fun onProgress(context: Context, uploadInfo: UploadInfo) {
                coroutineScope.launch {
                    val tusUploadUri = if(uploadInfo.files.isNotEmpty()) uploadInfo.files.first().properties[TusUploadTaskParameters.TUS_UPLOAD_URL] else null
                    uploadManager.handleProgress(
                        uploadInfo.uploadId,
                        uploadInfo.totalBytes,
                        uploadInfo.uploadedBytes,
                        uploadInfo.progressPercent,
                        tusUploadUri
                    )
                }
            }

            override fun onSuccess(context: Context, uploadInfo: UploadInfo, serverResponse: ServerResponse) {
                coroutineScope.launch {
                    cacheManager.clearCacheByUrl(ApiRoutes.GET_MY_CHANNEL_VIDEOS)
                    uploadManager.handleSuccess(uploadInfo.uploadId, serverResponse)
                }
            }
        })
    }
}