package com.banglalink.toffee.util

object UtilsKt {
    fun uploadIdToString(id: Long) = "Toffee_Upload_$id"
    fun stringToUploadId(uploadId: String) = uploadId.filter { it.isDigit() }.toLong()
}