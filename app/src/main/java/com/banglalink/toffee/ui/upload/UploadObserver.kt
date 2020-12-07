package com.banglalink.toffee.ui.upload

import android.app.Application
import android.content.Context
import android.util.Log
import com.banglalink.toffee.apiservice.UgcUploadConfirmation
import com.banglalink.toffee.data.repository.UploadInfoRepository
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.ui.widget.VelBoxAlertDialogBuilder
import com.banglalink.toffee.util.UtilsKt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.gotev.uploadservice.data.UploadInfo
import net.gotev.uploadservice.exceptions.UserCancelledUploadException
import net.gotev.uploadservice.network.ServerResponse
import net.gotev.uploadservice.observer.request.GlobalRequestObserver
import net.gotev.uploadservice.observer.request.RequestObserverDelegate

class UploadObserver(private val app: Application,
                     private val mPref: Preference,
                     private val uploadConfirmApi: UgcUploadConfirmation,
                     private val uploadRepo: UploadInfoRepository) {

    val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        coroutineScope.launch {
//            val currentUploadId = mPref.uploadId
//            if(currentUploadId != null) {
//                val currentUpload = uploadRepo.getUploadById(UtilsKt.stringToUploadId(currentUploadId)) ?: kotlin.run {
//                    mPref.uploadId = null
//                    return@launch
//                }
//                if(currentUpload.status in listOf(0, 1) && UploadService.taskList.isEmpty()) { // Active upload
//                    currentUpload.status = UploadStatus.CANCELED.value
//                    mPref.uploadId = null
//                }
//            }
            val uploads = uploadRepo.getUploads()
            if(uploads.isNotEmpty()) {
                uploads.forEach {
                    when (it.status) {
                        UploadStatus.CLEARED.value,
                        UploadStatus.SUBMITTED.value,
                        UploadStatus.SUBMITTED_ERROR.value-> {
                            uploadRepo.deleteUploadInfo(it)
                        }
                        in listOf(
                            UploadStatus.SUCCESS.value,
                            UploadStatus.RETRY_SUCCESS.value
                        ) -> {
                            sendStatusToServer(it, true)
                        }
                        in listOf(
                            UploadStatus.ADDED.value,
                            UploadStatus.STARTED.value,
                            UploadStatus.CANCELED.value,
                            UploadStatus.ERROR.value,
                            UploadStatus.RETRY_FAILED.value,
                        ) -> {
                            sendStatusToServer(it, false)
                        }
                    }
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

                    sendStatusToServer(item, false)
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

                    sendStatusToServer(item, true)
                }
            }
        })
    }

    private suspend fun sendStatusToServer(item: com.banglalink.toffee.data.database.entities.UploadInfo, status: Boolean) {

        if(item.status == UploadStatus.ERROR.value) {
            VelBoxAlertDialogBuilder(app, "Can't uplaod video", "Upload error. Please try again later.").apply {
                setPositiveButtonListener("OK") {
                    it?.dismiss()
                }
            }
        }

        val newStatus: Int
        newStatus = try {
            uploadConfirmApi(item.serverContentId, status)
            if(status) {
                UploadStatus.SUBMITTED.value
            } else {
                UploadStatus.SUBMITTED_ERROR.value
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            if(status) {
                UploadStatus.RETRY_SUCCESS.value
            } else {
                UploadStatus.RETRY_FAILED.value
            }
        }
        uploadRepo.updateUploadInfo(item.apply {
            this.status = newStatus
        })
    }
}