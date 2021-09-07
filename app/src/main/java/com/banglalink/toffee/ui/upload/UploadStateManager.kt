package com.banglalink.toffee.ui.upload

import android.app.Application
import android.util.Log
import com.banglalink.toffee.apiservice.UploadConfirmation
import com.banglalink.toffee.data.database.entities.UploadInfo
import com.banglalink.toffee.data.repository.UploadInfoRepository
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.ui.widget.VelBoxAlertDialogBuilder
import com.banglalink.toffee.util.UtilsKt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import net.gotev.uploadservice.UploadService
import net.gotev.uploadservice.exceptions.UserCancelledUploadException
import net.gotev.uploadservice.network.ServerResponse

class UploadStateManager(
    private val app: Application,
    private val mPref: SessionPreference,
    private val uploadRepo: UploadInfoRepository,
    private val uploadConfirmApi: UploadConfirmation
) {
    
    private var retryUploadId: Long = -1L
    private var networkRetryCount: Int = MAX_RETRY_COUNT

    suspend fun checkUploadStatus(fromNetwork: Boolean = false) {
        val uploads = uploadRepo.getUploads()
        if(uploads.isNotEmpty()) {
            uploads.forEach {
                when (it.status) {
                    UploadStatus.CANCELED.value,
                    UploadStatus.ERROR_CONFIRMED.value,
                    UploadStatus.RETRY_FAILED.value -> {
                        sendStatusToServer(it, false, copyrightStatus = false)
                    }
                    UploadStatus.CLEARED.value,
                    UploadStatus.SUBMITTED.value,
                    UploadStatus.SUBMITTED_ERROR.value-> {
                        uploadRepo.deleteUploadInfo(it)
                    }
                    in listOf(
                        UploadStatus.SUCCESS.value,
                        UploadStatus.RETRY_SUCCESS.value
                    ) -> {
                        sendStatusToServer(it, true, copyrightStatus = false)
                    }
                    in listOf(
                        UploadStatus.ADDED.value,
                        UploadStatus.STARTED.value,
                        UploadStatus.ERROR.value,
                    ) -> {
//                        if(fromNetwork || !it.fileUri.startsWith("content://")) {
//                            restartUploadTask(it)
//                        } else {
                        sendStatusToServer(it, false, copyrightStatus = false)
//                        }
                    }
                }
            }
        }
    }

    private suspend fun sendStatusToServer(item: UploadInfo, status: Boolean, copyrightStatus: Boolean) {

        if(item.status == UploadStatus.ERROR_CONFIRMED.value) {
            VelBoxAlertDialogBuilder(app, "Can't uplaod video", "Upload error. Please try again later.").apply {
                setPositiveButtonListener("OK") {
                    it?.dismiss()
                }
            }
        }

        val newStatus = try {
            uploadConfirmApi(item.serverContentId, status, copyrightStatus)
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

    suspend fun restartUploadTask(info: UploadInfo) {
        if(UploadService.taskList.isNotEmpty()
            && UploadService.taskList[0] == UtilsKt.uploadIdToString(info.uploadId!!)) {
            return
        }
        if(retryUploadId == info.uploadId) {
            networkRetryCount--
        } else {
            networkRetryCount = MAX_RETRY_COUNT
            retryUploadId = info.uploadId ?: -1L
        }
        withContext(Dispatchers.IO + Job()) {
            TusUploadRequest(
                app,
                mPref.tusUploadServerUrl,
            )
                .setResumeInfo(info.getFingerprint()!!, info.tusUploadUri)
                .setMetadata(info.getFileNameMetadata())
                .setUploadID(info.getUploadIdStr()!!)
                .setFileToUpload(info.fileUri)
                .startUpload()
        }
    }

    suspend fun handleSuccess(uploadId: String, serverResponse: ServerResponse) {
        if(UtilsKt.isCopyrightUploadId(uploadId)) {
            handleCopyrightUploadSuccess(uploadId, serverResponse)
            return
        }
        val item = uploadRepo.getUploadById(UtilsKt.stringToUploadId(uploadId)) ?: return

        uploadRepo.updateUploadInfo(item.apply {
            statusMessage = serverResponse.bodyString
            status = UploadStatus.SUCCESS.value
            completedPercent = 100
            completedSize = fileSize
        })

//        sendStatusToServer(item, true)
    }

    private suspend fun handleCopyrightUploadSuccess(uploadId: String, serverResponse: ServerResponse) {
        val item = uploadRepo.getUploadById(UtilsKt.stringToUploadId(uploadId)) ?: return
        sendStatusToServer(item, true, copyrightStatus = true)
    }

    private suspend fun handleCopyrightUploadError(uploadId: String, exception: Throwable) {
        val item = uploadRepo.getUploadById(UtilsKt.stringToUploadId(uploadId)) ?: return
        sendStatusToServer(item, true, copyrightStatus = false)
    }

    suspend fun handleError(uploadId: String, exception: Throwable) {
        if(UtilsKt.isCopyrightUploadId(uploadId)) {
            handleCopyrightUploadError(uploadId, exception)
            return
        }

        val item = uploadRepo.getUploadById(UtilsKt.stringToUploadId(uploadId)) ?: return

        val newStatus = if(exception is UserCancelledUploadException)
            UploadStatus.CANCELED.value
        else {
//            if(networkRetryCount <= 0 && retryUploadId == item.uploadId) {
//                networkRetryCount = MAX_RETRY_COUNT
//                retryUploadId = -1L
                UploadStatus.ERROR_CONFIRMED.value
//            } else {
//                UploadStatus.ERROR.value
//            }
        }

        uploadRepo.updateUploadInfo(item.apply {
            statusMessage = exception.message
            status = newStatus
        })

        // Video upload failed. Stop copyright uploading.
        UploadService.stopAllUploads()

//        if(newStatus != UploadStatus.ERROR.value) {
//            sendStatusToServer(item, false, copyrightStatus = false)
//        }
    }

    suspend fun handleProgress(
        uploadId: String,
        totalBytes: Long,
        uploadedBytes: Long,
        progressPercent: Int,
        uploadUri: String?
    ) {
        if(UtilsKt.isCopyrightUploadId(uploadId)) return
        uploadRepo.updateProgressById(UtilsKt.stringToUploadId(uploadId),
            uploadedBytes,
            progressPercent,
            totalBytes,
            uploadUri
        )
    }

    companion object {
        const val MAX_RETRY_COUNT = 3
    }
}