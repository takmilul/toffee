package com.banglalink.toffee.ui.upload

import net.gotev.uploadservice.observer.task.UploadTaskObserver

interface TusUploadTaskObserver: UploadTaskObserver {
    fun onReceiveTusUploadUrl(fingerprint: String, uploadUrl: String)
}