package com.banglalink.toffee.ui.upload

import android.util.Log
import net.gotev.uploadservice.UploadTask
import net.gotev.uploadservice.data.NameValue
import net.gotev.uploadservice.logger.UploadServiceLogger
import net.gotev.uploadservice.network.BodyWriter
import net.gotev.uploadservice.network.HttpRequest
import net.gotev.uploadservice.network.HttpStack
import net.gotev.uploadservice.network.hurl.HurlStack
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.IOException
import java.net.ProtocolException
import java.net.URL

class TusUploadTask: UploadTask(), HttpRequest.RequestBodyDelegate,
    BodyWriter.OnStreamWriteListener {

    private val tusVersion = "1.0.0"
    private val tusResumeableHeader = "Tus-Resumable"
    private val tusUploadLengthHeader = "Upload-Length"
    private val tusUploadOffsetHeader = "Upload-Offset"
    private val tusUploadMetadataHeader = "Upload-Metadata"

    private val tusParams: TusUploadTaskParameters
        get() = params.additionalParameters as TusUploadTaskParameters


    private val file by lazy { params.files.first().handler }

    private val bodyLength: Long
        get() = file.size(context)

    private var resumeOffset: Long = 0L

    @Throws(Exception::class)
    private fun createUpload(httpStack: HttpStack): String {
        UploadServiceLogger.debug(javaClass.simpleName, params.id) { "Trying to create Tus Upload with metadata ->>> ${tusParams.metadata}" }
        val createHeaders =
            listOf(
                NameValue(tusResumeableHeader, tusVersion),
                NameValue(tusUploadLengthHeader, bodyLength.toString()),
                NameValue(tusUploadMetadataHeader, tusParams.metadata!!),
            )

        val resp = httpStack
            .newRequest(params.id, "POST", params.serverUrl)
            .setHeaders(createHeaders)
            .setTotalBodyBytes(0, true)
            .getResponse(this, this)

        Log.e("UPLOAD", "${resp.code}, ${resp.bodyString}")

        if(resp.code !in 200..299) {
            throw ProtocolException("Unexpected status code ${resp.code} while creating upload")
        }

        val uploadUrl = resp.headers["Location"]
        if(uploadUrl.isNullOrEmpty()) {
            throw ProtocolException("missing upload URL in response for creating upload")
        }

        UploadServiceLogger.debug(javaClass.simpleName, params.id) { "Tus upload created with url - $uploadUrl" }

        // Received upload url from server //TODO: Save url and fingerprint to localstore

        return params.serverUrl.toHttpUrl().resolve(uploadUrl).toString().also {
            tusParams.uploadUrl = uploadUrl
            params.files.first().properties[TusUploadTaskParameters.FINGERPRINT] = tusParams.fingerprint
            params.files.first().properties[TusUploadTaskParameters.TUS_UPLOAD_URL] = it
        }
    }

    @Throws(Exception::class)
    private fun getResumeOffset(httpStack: HttpStack, uploadUrl: String): Long {
        UploadServiceLogger.debug(javaClass.simpleName, params.id) { "Trying to get resume offset" }

        val offsetRequestHeaders = listOf(
            NameValue(tusResumeableHeader, tusVersion),
        )

        val resp = httpStack
            .newRequest(params.id, "HEAD", uploadUrl)
            .setHeaders(offsetRequestHeaders)
            .setTotalBodyBytes(0, true)
            .getResponse(this, this)

        if(resp.code !in 200..299) {
            throw ProtocolException("Unexpected status code ${resp.code} while resuming upload")
        }

        val uploadOffset = resp.headers["Upload-Offset"]
        if(uploadOffset.isNullOrEmpty()) {
            throw ProtocolException("missing upload offset in response for resuming upload")
        }

        UploadServiceLogger.debug(javaClass.simpleName, params.id) { "Got resume offset - $uploadOffset" }

        return uploadOffset.toLong()
    }

    @Throws(Exception::class)
    private fun resumeUpload(httpStack: HttpStack, uploadUrl: String) {
        UploadServiceLogger.debug(javaClass.simpleName, params.id) { "Starting upload task" }

        setAllFilesHaveBeenSuccessfullyUploaded(false)
        resumeOffset = getResumeOffset(httpStack, uploadUrl)
        totalBytes = bodyLength - resumeOffset

        shouldWriteBody = true

        val resumeHeaders =
            listOf(
                NameValue(tusResumeableHeader, tusVersion),
                NameValue(tusUploadOffsetHeader, resumeOffset.toString()),
                NameValue("Content-Type", "application/offset+octet-stream"),
            )

        val response = httpStack.newRequest(params.id, "PATCH", uploadUrl)
            .setHeaders(resumeHeaders)
            .setTotalBodyBytes(totalBytes, true)
            .getResponse(this, this)

        UploadServiceLogger.debug(javaClass.simpleName, params.id) {
            "Server response: code ${response.code}, header - ${response.headers}, body ${response.bodyString}"
        }

        if (shouldContinue) {
            if (response.isSuccessful) {
                setAllFilesHaveBeenSuccessfullyUploaded()
            }
            onResponseReceived(response)
        }
    }

    @Throws(Exception::class)
    override fun upload(httpStack: HttpStack) {
        val uploadUrl = if(tusParams.uploadUrl != null) {
            tusParams.uploadUrl!!
        } else createUpload(httpStack)
        resumeUpload(httpStack, uploadUrl)
    }

    override fun onWriteRequestBody(bodyWriter: BodyWriter) {
        if(shouldWriteBody) {
            val body = file.stream(context)
            body.skip(resumeOffset)
            bodyWriter.writeStream(body)
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