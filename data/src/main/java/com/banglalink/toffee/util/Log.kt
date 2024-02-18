package com.banglalink.toffee.util

import timber.log.Timber

object Log {
    
    const val SHOULD_LOG = true
    private const val TAG = "TAG"
    private const val EMPTY_MSG = "Log message is empty."
    
    fun v(tag: String?, msg: String?, tr: Throwable? = null) {
        if (SHOULD_LOG) Timber.tag(tag ?: TAG).v(tr, msg ?: EMPTY_MSG)
    }
    
    fun d(tag: String?, msg: String?, tr: Throwable? = null) {
        if (SHOULD_LOG) Timber.tag(tag ?: TAG).d(tr, msg ?: EMPTY_MSG)
    }
    
    fun i(tag: String?, msg: String?, tr: Throwable? = null) {
        if (SHOULD_LOG) Timber.tag(tag ?: TAG).i(tr, msg ?: EMPTY_MSG)
    }
    
    fun w(tag: String?, msg: String?, tr: Throwable? = null) {
        if (SHOULD_LOG) Timber.tag(tag ?: TAG).w(tr, msg ?: EMPTY_MSG)
    }
    
    fun e(tag: String?, msg: String?, tr: Throwable? = null) {
        if (SHOULD_LOG) Timber.tag(tag ?: TAG).e(tr, msg ?: EMPTY_MSG)
    }
}