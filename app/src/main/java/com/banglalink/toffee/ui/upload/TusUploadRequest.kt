package com.banglalink.toffee.ui.upload

import android.content.Context
import android.os.Parcelable
import net.gotev.uploadservice.HttpUploadRequest
import net.gotev.uploadservice.UploadTask
import net.gotev.uploadservice.data.UploadFile
import net.gotev.uploadservice.logger.UploadServiceLogger
import net.gotev.uploadservice.persistence.PersistableData
import net.gotev.uploadservice.protocols.binary.BinaryUploadRequest
import java.io.FileNotFoundException
import java.io.IOException

class TusUploadRequest(context: Context, serverUrl: String)
    :HttpUploadRequest<TusUploadRequest>(context, serverUrl) {
    override val taskClass: Class<out UploadTask>
        get() = TusUploadTask::class.java

    private lateinit var tusUploadParams: TusUploadTaskParameters

    /**
     * Sets the file used as raw body of the upload request.
     *
     * @param path path to the file that you want to upload
     * @throws FileNotFoundException if the file to upload does not exist
     * @return [BinaryUploadRequest]
     */
    @Throws(IOException::class)
    fun setFileToUpload(path: String): TusUploadRequest {
        files.clear()
        files.add(UploadFile(path))
        files.first().properties.apply {
            put(TusUploadTaskParameters.FINGERPRINT, tusUploadParams.fingerprint)
            if(tusUploadParams.uploadUrl != null) {
                put(TusUploadTaskParameters.TUS_UPLOAD_URL, tusUploadParams.uploadUrl!!)
            }
        }
        return this
    }

    fun setResumeInfo(fingerprint: String,
                      uploadUrl: String?): TusUploadRequest {
        tusUploadParams = TusUploadTaskParameters(fingerprint, uploadUrl)
        return this
    }

    fun setMetadata(metadata: String): TusUploadRequest {
        tusUploadParams.metadata = metadata
        return this
    }

    override fun addParameter(paramName: String, paramValue: String): TusUploadRequest {
        logDoesNotSupportParameters()
        return this
    }

    override fun addArrayParameter(paramName: String, vararg array: String): TusUploadRequest {
        logDoesNotSupportParameters()
        return this
    }

    override fun addArrayParameter(paramName: String, list: List<String>): TusUploadRequest {
        logDoesNotSupportParameters()
        return this
    }

    override fun startUpload(): String {
        require(files.isNotEmpty()) { "Set the file to be used in the request body first!" }
        return super.startUpload()
    }

    private fun logDoesNotSupportParameters() {
        UploadServiceLogger.error(javaClass.simpleName, "N/A") {
            "This upload method does not support adding parameters"
        }
    }

    override fun getAdditionalParameters(): PersistableData = tusUploadParams
}