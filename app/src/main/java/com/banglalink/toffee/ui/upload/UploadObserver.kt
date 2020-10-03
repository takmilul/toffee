package com.banglalink.toffee.ui.upload

import android.app.Application
import android.content.Context
import com.banglalink.toffee.data.repository.UploadInfoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.gotev.uploadservice.data.UploadInfo
import net.gotev.uploadservice.network.ServerResponse
import net.gotev.uploadservice.observer.request.GlobalRequestObserver
import net.gotev.uploadservice.observer.request.RequestObserverDelegate

fun UploadInfo.stringToUploadId(): Long {
    return uploadId.filter { it.isDigit() }.toLong()
}

class UploadObserver(private val app: Application,
                     private val uploadRepo: UploadInfoRepository) {

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
                    val item = uploadRepo.getUploadById(uploadInfo.stringToUploadId()) ?: return@launch

                    uploadRepo.updateUploadInfo(item.apply {
                        statusMessage = exception.message
                        status = UploadStatus.ERROR.value
                    })
                }
            }

            override fun onProgress(context: Context,
                                    uploadInfo: UploadInfo) {
                coroutineScope.launch {
                    uploadRepo.updateProgressById(uploadInfo.stringToUploadId(),
                        uploadInfo.uploadedBytes,
                        uploadInfo.progressPercent)
                }
            }

            override fun onSuccess(
                context: Context,
                uploadInfo: UploadInfo,
                serverResponse: ServerResponse
            ) {
                coroutineScope.launch {
                    val item = uploadRepo.getUploadById(uploadInfo.stringToUploadId()) ?: return@launch

                    uploadRepo.updateUploadInfo(item.apply {
                        statusMessage = serverResponse.bodyString
                        status = UploadStatus.SUCCESS.value
                        completedPercent = 100
                        completedSize = fileSize
                    })
                }
            }
        })
    }
}