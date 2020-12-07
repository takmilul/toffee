package com.banglalink.toffee.ui.upload

enum class UploadStatus(val value: Int) {
    ADDED(0),
    STARTED(1),
    SUCCESS(2),
    ERROR(3),
    CANCELED(4),
    SUBMITTED(5),
    SUBMITTED_ERROR(6),
    RETRY_SUCCESS(7),
    RETRY_FAILED(8),
    CLEARED(9),
}