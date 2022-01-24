package com.banglalink.toffee.util

import android.util.Log

object Log {
    
    const val SHOULD_LOG = true
    private const val TAG = "TAG"
    private const val EMPTY_MSG = "Log message is empty."
    
    fun v(tag: String?, msg: String?, tr: Throwable? = null) {
        if (SHOULD_LOG) Log.v(tag ?: TAG, msg ?: EMPTY_MSG, tr)
    }
    
    fun d(tag: String?, msg: String?, tr: Throwable? = null) {
        if (SHOULD_LOG) Log.d(tag ?: TAG, msg ?: EMPTY_MSG, tr)
    }
    
    fun i(tag: String?, msg: String?, tr: Throwable? = null) {
        if (SHOULD_LOG) Log.i(tag ?: TAG, msg ?: EMPTY_MSG, tr)
    }
    
    fun w(tag: String?, msg: String?, tr: Throwable? = null) {
        if (SHOULD_LOG) Log.w(tag ?: TAG, msg ?: EMPTY_MSG, tr)
    }
    
    fun e(tag: String?, msg: String?, tr: Throwable? = null) {
        if (SHOULD_LOG) Log.e(tag ?: TAG, msg ?: EMPTY_MSG, tr)
    }
}