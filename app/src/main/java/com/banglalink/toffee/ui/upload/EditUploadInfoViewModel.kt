package com.banglalink.toffee.ui.upload

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.banglalink.toffee.data.database.dao.UploadDao
import com.banglalink.toffee.data.database.entities.UploadInfo
import com.banglalink.toffee.data.repository.UploadInfoRepository
import com.banglalink.toffee.util.Utils

class EditUploadInfoViewModel @ViewModelInject constructor(private val uploadRepo: UploadInfoRepository): ViewModel() {
    val title = MutableLiveData<String>()
    val description = MutableLiveData<String>()

    val uploadProgress = MutableLiveData<Int>()
    val uploadSize = MutableLiveData<String>()

    val categories = MutableLiveData<List<String>>()
    val categoryPosition = MutableLiveData<Int>()

    val ageGroup = MutableLiveData<List<String>>()
    val ageGroupPosition = MutableLiveData<Int>()

    val challengeSelectionList = MutableLiveData<List<String>>()
    val challengeSelectionPosition = MutableLiveData<Int>()

    init {
        title.value = ""
        categories.value = listOf("Select", "Movie", "Natok", "Music video", "Games")
        categoryPosition.value = 0

        uploadProgress.value = 0

        ageGroup.value = listOf("Select", "Everyone", "3+", "9+", "12+", "18+")
        ageGroupPosition.value = 0

        challengeSelectionList.value = listOf("Select", "Music", "Movie", "Games", "TV Series")
        challengeSelectionPosition.value = 0
    }

    fun initUploadInfo(uploadInfo: UploadInfo) {
        title.value = uploadInfo.title
        description.value = uploadInfo.description

        categoryPosition.value = uploadInfo.categoryIndex
        ageGroupPosition.value = uploadInfo.ageGroupIndex
        challengeSelectionPosition.value = uploadInfo.submitToChallengeIndex
    }

    fun updateProgress(progress: Int, size: Long) {
        uploadProgress.value = progress
        uploadSize.value = Utils.readableFileSize(size)
    }
}
