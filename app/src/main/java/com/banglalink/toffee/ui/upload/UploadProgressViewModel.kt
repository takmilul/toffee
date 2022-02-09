package com.banglalink.toffee.ui.upload

import androidx.lifecycle.ViewModel
import com.banglalink.toffee.data.database.entities.UploadInfo
import com.banglalink.toffee.data.repository.UploadInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class UploadProgressViewModel @Inject constructor(
    private val repo: UploadInfoRepository,
) : ViewModel() {

    fun getActiveUploadList(): Flow<List<UploadInfo>> {
        return repo.getActiveUploads()
    }
}
