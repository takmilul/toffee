package com.banglalink.toffee.ui.upload

import android.app.Application
import android.content.Context
import com.banglalink.toffee.apiservice.GET_MY_CHANNEL_VIDEOS_URL
import com.banglalink.toffee.data.network.retrofit.CacheManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.gotev.uploadservice.data.UploadInfo
import net.gotev.uploadservice.network.ServerResponse
import net.gotev.uploadservice.observer.request.GlobalRequestObserver
import net.gotev.uploadservice.observer.request.RequestObserverDelegate

class UploadObserver(
    private val app: Application,
    private val uploadManager: UploadStateManager,
    private val cacheManager: CacheManager
) {
    val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun start() {
        GlobalRequestObserver(app, object: RequestObserverDelegate {
            override fun onCompleted(context: Context, uploadInfo: UploadInfo) {

            }

            override fun onCompletedWhileNotObserving() {

            }

            override fun onError(context: Context,
                                 uploadInfo: UploadInfo,
                                 exception: Throwable) {
                coroutineScope.launch {
                    uploadManager.handleError(uploadInfo.uploadId, exception)
                }
            }

            override fun onProgress(context: Context,
                                    uploadInfo: UploadInfo) {
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

            override fun onSuccess(
                context: Context,
                uploadInfo: UploadInfo,
                serverResponse: ServerResponse
            ) {
                coroutineScope.launch {
                    cacheManager.clearCacheByUrl(GET_MY_CHANNEL_VIDEOS_URL)
                    uploadManager.handleSuccess(uploadInfo.uploadId, serverResponse)
                }
            }
        })
    }
}