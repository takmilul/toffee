package com.banglalink.toffee.ui.upload

import android.app.Application
import android.content.Context
import android.util.Log
import com.banglalink.toffee.apiservice.UgcUploadConfirmation
import com.banglalink.toffee.data.repository.UploadInfoRepository
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.TUS_UPLOAD_SERVER_URL
import com.banglalink.toffee.ui.widget.VelBoxAlertDialogBuilder
import com.banglalink.toffee.util.UtilsKt
import kotlinx.coroutines.*
import net.gotev.uploadservice.data.UploadInfo
import net.gotev.uploadservice.exceptions.UserCancelledUploadException
import net.gotev.uploadservice.network.ServerResponse
import net.gotev.uploadservice.observer.request.GlobalRequestObserver
import net.gotev.uploadservice.observer.request.RequestObserverDelegate

class UploadObserver(
    private val app: Application,
    private val uploadManager: UploadStateManager
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
                    uploadManager.handleSuccess(uploadInfo.uploadId, serverResponse)
                }
            }
        })
    }
}