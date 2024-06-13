package com.banglalink.toffee.data.network.interceptor

import android.util.Log
import com.google.gson.JsonSyntaxException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Inject

class ApiLogger @Inject constructor(
    private val json: Json
): HttpLoggingInterceptor.Logger {
    override fun log(message: String) {
        val logName = "API_LOG"
        if (message.startsWith("{") || message.startsWith("[")) {
            try {
                val prettyPrintJson = json.encodeToString(json.parseToJsonElement(message))
                Log.d(logName, prettyPrintJson)
            } catch (m: JsonSyntaxException) {
                Log.d(logName, message)
            }
        } else {
            Log.d(logName, message)
            return
        }
    }
}