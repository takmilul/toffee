package com.banglalink.toffee.ui.upload

import android.app.Application
import android.content.Context
import android.util.Log
import com.banglalink.toffee.data.repository.UploadInfoRepository
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.util.UtilsKt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.gotev.uploadservice.UploadService
import net.gotev.uploadservice.data.UploadInfo
import net.gotev.uploadservice.exceptions.UserCancelledUploadException
import net.gotev.uploadservice.network.ServerResponse
import net.gotev.uploadservice.observer.request.GlobalRequestObserver
import net.gotev.uploadservice.observer.request.RequestObserverDelegate

class UploadObserver(private val app: Application,
                     private val mPref: Preference,
                     private val uploadRepo: UploadInfoRepository) {

    val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        coroutineScope.launch {
            val currentUploadId = mPref.uploadId
            if(currentUploadId != null) {
                val currentUpload = uploadRepo.getUploadById(UtilsKt.stringToUploadId(currentUploadId)) ?: kotlin.run {
                    mPref.uploadId = null
                    return@launch
                }
                if(currentUpload.status in listOf(0, 1) && UploadService.taskList.isEmpty()) { // Active upload
                    currentUpload.status = UploadStatus.CANCELED.value
                    mPref.uploadId = null
                }
            }
        }
    }

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
                    val item = uploadRepo.getUploadById(UtilsKt.stringToUploadId(uploadInfo.uploadId)) ?: return@launch

                    uploadRepo.updateUploadInfo(item.apply {
                        statusMessage = exception.message
                        status = if(exception is UserCancelledUploadException)
                                    UploadStatus.CANCELED.value
                                 else
                                    UploadStatus.ERROR.value
                    })
                }
            }

            override fun onProgress(context: Context,
                                    uploadInfo: UploadInfo) {
                Log.e("UPLOAD", "Uploading -===>>> ${uploadInfo.progressPercent}")
                coroutineScope.launch {
                    uploadRepo.updateProgressById(UtilsKt.stringToUploadId(uploadInfo.uploadId),
                        uploadInfo.uploadedBytes,
                        uploadInfo.progressPercent,
                        uploadInfo.totalBytes)
                }
            }

            override fun onSuccess(
                context: Context,
                uploadInfo: UploadInfo,
                serverResponse: ServerResponse
            ) {
                coroutineScope.launch {
                    val item = uploadRepo.getUploadById(UtilsKt.stringToUploadId(uploadInfo.uploadId)) ?: return@launch

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