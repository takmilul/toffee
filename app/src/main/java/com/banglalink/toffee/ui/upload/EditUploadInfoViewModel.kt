package com.banglalink.toffee.ui.upload

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.banglalink.toffee.util.Utils

class EditUploadInfoViewModel: ViewModel() {
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
        title.value = "This is a title"
        categories.value = listOf("Select", "Hello", "World")
        categoryPosition.value = 2

        uploadProgress.value = 0

        ageGroup.value = listOf("Select", "3+", "9+", "12+", "18+")
        ageGroupPosition.value = 3

        challengeSelectionList.value = listOf("Select", "Music", "Movie", "Games", "TV Series")
        challengeSelectionPosition.value = 2
    }

    fun updateProgress(progress: Int, size: Long) {
        uploadProgress.value = progress
        uploadSize.value = Utils.readableFileSize(size)
    }
}
