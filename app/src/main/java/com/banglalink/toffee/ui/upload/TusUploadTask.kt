package com.banglalink.toffee.ui.upload

import android.util.Log
import net.gotev.uploadservice.UploadServiceConfig
import net.gotev.uploadservice.UploadTask
import net.gotev.uploadservice.data.NameValue
import net.gotev.uploadservice.logger.UploadServiceLogger
import net.gotev.uploadservice.network.BodyWriter
import net.gotev.uploadservice.network.HttpRequest
import net.gotev.uploadservice.network.HttpStack
import net.gotev.uploadservice.network.ServerResponse
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.IOException
import java.io.InputStream
import java.net.ProtocolException
import kotlin.math.max
import kotlin.math.min

class TusUploadTask: UploadTask(), HttpRequest.RequestBodyDelegate,
    BodyWriter.OnStreamWriteListener {

    private val tusVersion = "1.0.0"
    private val tusResumeableHeader = "Tus-Resumable"
    private val tusUploadLengthHeader = "Upload-Length"
    private val tusUploadOffsetHeader = "Upload-Offset"
    private val tusUploadMetadataHeader = "Upload-Metadata"
    private val uploadChunkSize: Long = max(min(UploadServiceConfig.bufferSizeBytes * 1024L, 4 * 1024 * 1024L), 1024 * 1024L)
    private var uploadCompleted = false

    private val tusParams: TusUploadTaskParameters
        get() = params.additionalParameters as TusUploadTaskParameters


    private val file by lazy { params.files.first().handler }

    private val bodyLength: Long
        get() = file.size(context)

    private var resumeOffset: Long = -1L

    @Throws(Exception::class)
    private fun createUpload(httpStack: HttpStack): String {
        UploadServiceLogger.error(javaClass.simpleName, params.id) { "Trying to create Tus Upload with metadata ->>> ${tusParams.metadata}" }
        val createHeaders =
            listOf(
                NameValue(tusResumeableHeader, tusVersion),
                NameValue(tusUploadLengthHeader, bodyLength.toString()),
                NameValue(tusUploadMetadataHeader, tusParams.metadata!!),
            )

        val resp = httpStack
            .newRequest(params.id, "POST", params.serverUrl)
            .setHeaders(createHeaders)
            .setTotalBodyBytes(0, false)
            .getResponse(this, this)

        Log.e("UPLOAD", "${resp.code}, ${resp.bodyString}")

        if(resp.code !in 200..299) {
            throw ProtocolException("Unexpected status code ${resp.code} while creating upload")
        }

        val uploadUrl = resp.headers["Location"]
        if(uploadUrl.isNullOrEmpty()) {
            throw ProtocolException("missing upload URL in response for creating upload")
        }

        UploadServiceLogger.error(javaClass.simpleName, params.id) { "Tus upload created with url - $uploadUrl" }

        return params.serverUrl.toHttpUrl().resolve(uploadUrl).toString().also {
            tusParams.uploadUrl = it
            params.files.first().properties[TusUploadTaskParameters.FINGERPRINT] = tusParams.fingerprint
            params.files.first().properties[TusUploadTaskParameters.TUS_UPLOAD_URL] = it
        }
    }

    @Throws(Exception::class)
    private fun getResumeOffset(httpStack: HttpStack, uploadUrl: String): Long {
        UploadServiceLogger.error(javaClass.simpleName, params.id) { "Trying to get resume offset from api" }

        val offsetRequestHeaders = listOf(
            NameValue(tusResumeableHeader, tusVersion),
        )

        val resp = httpStack
            .newRequest(params.id, "HEAD", uploadUrl)
            .setHeaders(offsetRequestHeaders)
            .setTotalBodyBytes(0, false)
            .getResponse(this, this)

        if(resp.code !in 200..299) {
            throw ProtocolException("Unexpected status code ${resp.code} while resuming upload")
        }

        val uploadOffset = resp.headers[tusUploadOffsetHeader]
        if(uploadOffset.isNullOrEmpty()) {
            throw ProtocolException("missing upload offset in response for resuming upload")
        }

        UploadServiceLogger.error(javaClass.simpleName, params.id) { "Got resume offset from api - $uploadOffset" }

        return uploadOffset.toLong()
    }

    @Throws(Exception::class)
    private fun resumeUpload(httpStack: HttpStack, uploadUrl: String, offset: Long = -1L): ServerResponse {
        UploadServiceLogger.error(javaClass.simpleName, params.id) { "Starting upload task with offset ->> $offset" }

        setAllFilesHaveBeenSuccessfullyUploaded(false)
        resumeOffset = if(offset >= 0) offset else getResumeOffset(httpStack, uploadUrl)
//        resetUploadedBytes()

        if(resumeOffset < 0) throw IllegalStateException("Resume offset should be positive")

        Log.e("Upload", "Resume offset ->>> $resumeOffset")
        totalBytes = bodyLength// - resumeOffset
        onProgress(resumeOffset)

        shouldWriteBody = true

        val resumeHeaders =
            listOf(
                NameValue(tusResumeableHeader, tusVersion),
                NameValue(tusUploadOffsetHeader, resumeOffset.toString()),
                NameValue("Content-Type", "application/offset+octet-stream"),
                NameValue("Expect", "100-continue"),
            )

        val response = httpStack.newRequest(params.id, "PATCH", uploadUrl)
            .setHeaders(resumeHeaders)
            .setTotalBodyBytes(totalBytes - resumeOffset, false)
            .getResponse(this, this)

        UploadServiceLogger.error(javaClass.simpleName, params.id) {
            "Server response: code ${response.code}, header - ${response.headers}, body ${response.bodyString}"
        }

        val uploadedOffset = response.headers[tusUploadOffsetHeader]?.toLongOrNull()
            ?: throw ProtocolException("missing upload offset in response for resuming upload")

        if(uploadedOffset == bodyLength) {
            uploadCompleted = true
        }

        resumeOffset = uploadedOffset
        return response
    }

    @Throws(Exception::class)
    override fun upload(httpStack: HttpStack) {
        shouldWriteBody = false
        resumeOffset = -1L
        Log.e("UPload", "Resume Offset ->>> $resumeOffset")
        val uploadUrl = if(tusParams.uploadUrl != null) {
            tusParams.uploadUrl!!
        } else {
            resumeOffset = 0L
            createUpload(httpStack)
        }
        var serverResponse: ServerResponse? = null
        while(!uploadCompleted && shouldContinue) {
            resetUploadedBytes()
            serverResponse = resumeUpload(httpStack, uploadUrl, resumeOffset)
        }
        if (shouldContinue) {
            serverResponse?.let {
                if(it.isSuccessful) {
                    setAllFilesHaveBeenSuccessfullyUploaded()
                }
                onResponseReceived(it)
            }
        }
    }

    override fun onWriteRequestBody(bodyWriter: BodyWriter) {
        if(shouldWriteBody) {
            val body = file.stream(context)
            body.skip(resumeOffset)
            writeStream(body, bodyWriter)
        }
    }

    @Throws(IOException::class)
    fun writeStream(stream: InputStream, bodyWriter: BodyWriter) {
        val buffer = ByteArray(UploadServiceConfig.bufferSizeBytes)
        var currentBodyLength = 0L
        stream.use {
            while (shouldContinueWriting()) {
                val bytesRead = it.read(buffer, 0, buffer.size)
                currentBodyLength += bytesRead
                if (bytesRead <= 0 || currentBodyLength > uploadChunkSize) break

                bodyWriter.write(buffer, bytesRead)
            }
        }
    }

    private var shouldWriteBody = false

    override fun onBytesWritten(bytesWritten: Int) {
        onProgress(bytesWritten.toLong())
    }

    override fun shouldContinueWriting(): Boolean = shouldContinue
}
//
//@Throws(IOException::class)
//fun HurlStack.getResponse() {
//
//}