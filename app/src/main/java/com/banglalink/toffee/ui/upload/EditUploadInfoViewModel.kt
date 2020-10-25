package com.banglalink.toffee.ui.upload

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.GetUgcCategories
import com.banglalink.toffee.apiservice.UgcContentUpload
import com.banglalink.toffee.data.database.dao.UploadDao
import com.banglalink.toffee.data.database.entities.UploadInfo
import com.banglalink.toffee.data.repository.UploadInfoRepository
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.UgcCategory
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.UtilsKt
import kotlinx.coroutines.launch

class EditUploadInfoViewModel @ViewModelInject constructor(
    private val uploadRepo: UploadInfoRepository,
    private val contentUploadApi: UgcContentUpload,
    private val preference: Preference,
    private val categoryApi: GetUgcCategories
): ViewModel() {
    val progressDialog = MutableLiveData<Boolean>()

    val submitButtonStatus = MutableLiveData<Boolean>()

    val title = MutableLiveData<String>()
    val description = MutableLiveData<String>()

    val tags = MutableLiveData<String>()

    val uploadProgress = MutableLiveData<Int>()
    val uploadSize = MutableLiveData<String>()

    val categories = MutableLiveData<List<UgcCategory>>()
    val categoryPosition = MutableLiveData<Int>()

    val ageGroup = MutableLiveData<List<String>>()
    val ageGroupPosition = MutableLiveData<Int>()

//    val challengeSelectionList = MutableLiveData<List<String>>()
//    val challengeSelectionPosition = MutableLiveData<Int>()

    init {
        categoryPosition.value = 0

        load()

        ageGroup.value = listOf("Select", "Everyone", "3+", "9+", "12+", "18+")
        ageGroupPosition.value = 0

//        challengeSelectionList.value = listOf("Select", "Music", "Movie", "Games", "TV Series")
//        challengeSelectionPosition.value = 0
    }

    private fun load() {
        viewModelScope.launch {
            progressDialog.value = true

            categories.value = categoryApi.loadData(0, 0)

            val uploadId = preference.uploadId ?: ""
            val info = uploadRepo.getUploadById(UtilsKt.stringToUploadId(uploadId)) ?: run {
                progressDialog.value = false
                return@launch
            }

            initUploadInfo(info)

            progressDialog.value = false
        }
    }

    private fun initUploadInfo(uploadInfo: UploadInfo) {
        title.value = uploadInfo.title
        description.value = uploadInfo.description
        tags.value = uploadInfo.tags

        submitButtonStatus.value = uploadInfo.status == UploadStatus.SUCCESS.value

        categoryPosition.value = uploadInfo.categoryIndex
        ageGroupPosition.value = uploadInfo.ageGroupIndex
//        challengeSelectionPosition.value = uploadInfo.submitToChallengeIndex
    }

    fun updateProgress(progress: Int, size: Long) {
        uploadProgress.value = progress
        uploadSize.value = Utils.readableFileSize(size)
    }

    fun saveUploadInfo(fileName: String, tags: String?) {
        viewModelScope.launch {
            contentUploadApi(0, fileName, title.value, description.value, tags, ageGroup.value.toString(), 1)
        }
    }
}
