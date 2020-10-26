package com.banglalink.toffee.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File

object UtilsKt {
    fun uploadIdToString(id: Long) = "Toffee_Upload_$id"
    fun stringToUploadId(uploadId: String) = uploadId.filter { it.isDigit() }.toLong()

    fun contentTypeFromContentUri(context: Context, uri: Uri): String {
        val type = context.contentResolver.getType(uri)

        return if (type.isNullOrBlank()) {
            "application/octet-stream"
        } else {
            type
        }
    }

    fun fileNameFromContentUri(context: Context, uri: Uri): String {
        return context.contentResolver.query(uri, null, null, null, null)?.use {
            if (it.moveToFirst()) {
                it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            } else {
                null
            }
        } ?: uri.toString().split(File.separator).last()
    }
}