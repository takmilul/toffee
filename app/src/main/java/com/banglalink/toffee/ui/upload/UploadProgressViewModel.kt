package com.banglalink.toffee.ui.upload

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.banglalink.toffee.data.database.entities.UploadInfo
import com.banglalink.toffee.data.repository.UploadInfoRepository
import kotlinx.coroutines.flow.Flow

class UploadProgressViewModel @ViewModelInject constructor(
    private val repo: UploadInfoRepository
): ViewModel() {
    fun getActiveUploadList(): Flow<List<UploadInfo>> {
        return repo.getActiveUploads()
    }
}
