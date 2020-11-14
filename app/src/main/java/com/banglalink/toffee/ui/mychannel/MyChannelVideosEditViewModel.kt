package com.banglalink.toffee.ui.mychannel

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Base64
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.GetUgcCategories
import com.banglalink.toffee.apiservice.UgcContentUpload
import com.banglalink.toffee.data.database.entities.UploadInfo
import com.banglalink.toffee.data.repository.UploadInfoRepository
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.SubCategory
import com.banglalink.toffee.model.UgcCategory
import com.banglalink.toffee.util.UtilsKt
import com.banglalink.toffee.util.getError
import com.banglalink.toffee.util.imagePathToBase64
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class MyChannelVideosEditViewModel @ViewModelInject constructor(
    @ApplicationContext private val appContext: Context,
    private val uploadRepo: UploadInfoRepository,
    private val contentUploadApi: UgcContentUpload,
    private val preference: Preference,
    private val categoryApi: GetUgcCategories,
//    private val subCategoryApi: SubCategoryService
): ViewModel() {
//    val progressDialog = MutableLiveData<Boolean>()

//    val submitButtonStatus = MutableLiveData<Boolean>()
//    val resultLiveData = MutableLiveData<Resource<UgcResponseBean>>()

    val title = MutableLiveData<String>()
    val description = MutableLiveData<String>()

    val tags = MutableLiveData<String>()

//    val uploadProgress = MutableLiveData<Int>()
//    val uploadSize = MutableLiveData<String>()

    val categories = MutableLiveData<List<UgcCategory>>()
    val categoryPosition = MutableLiveData<Int>()
    
    val subCategories = MutableLiveData<List<SubCategory>>()
    val subCategoryPosition = MutableLiveData<Int>()

    val ageGroup = MutableLiveData<List<String>>()
    val ageGroupPosition = MutableLiveData<Int>()

    val thumbnailUrl = MutableLiveData<String?>()
    var bannerBase64: String? = null

//    val challengeSelectionList = MutableLiveData<List<String>>()
//    val challengeSelectionPosition = MutableLiveData<Int>()

    init {
        categoryPosition.value = 0

        load()

        ageGroup.value = listOf("For All", "3+", "9+", "13+", "18+")
        ageGroupPosition.value = 0

//        challengeSelectionList.value = listOf("Select", "Music", "Movie", "Games", "TV Series")
//        challengeSelectionPosition.value = 0
    }

    private fun load() {
        viewModelScope.launch {
//            progressDialog.value = true
            categories.value = categoryApi.loadData(0, 0)

//            subCategories.value = subCategoryApi.loadData(0,0)

//            val uploadId = preference.uploadId ?: ""
            /*val info = uploadRepo.getUploadById(UtilsKt.stringToUploadId(uploadId)) ?: run {
                progressDialog.value = false
                return@launch
            }*/

//            initUploadInfo(info)

//            progressDialog.value = false
        }
    }

    fun initUploadInfo(uploadInfo: UploadInfo) {
        title.value = uploadInfo.title
        description.value = uploadInfo.description
        tags.value = uploadInfo.tags
        thumbnailUrl.value = uploadInfo.thumbUri
        
        categoryPosition.value = categories.value?.find { it.categoryName == uploadInfo.category }?.id?.toInt()?:0
        categoryPosition.value = uploadInfo.categoryIndex
        ageGroupPosition.value = uploadInfo.ageGroupIndex

        /*if(uploadInfo.thumbUri == null) {
            viewModelScope.launch {
                withContext(Dispatchers.Default + Job()) {
                    generateThumbnail(uploadInfo)
                }?.let { saveThumbnailToDb(it) }
            }
        }*/
//        challengeSelectionPosition.value = uploadInfo.submitToChallengeIndex
    }

    /*fun updateProgress(progress: Int, size: Long) {
        uploadProgress.value = progress
        uploadSize.value = Utils.readableFileSize(size)
    }*/

    private fun generateThumbnail(uploadInfo: UploadInfo): String? {
        return try {
            val mmr = MediaMetadataRetriever()
            if(uploadInfo.fileUri.startsWith("content://")) {
                mmr.setDataSource(appContext, Uri.parse(uploadInfo.fileUri))
            } else {
                mmr.setDataSource(uploadInfo.fileUri)
            }
            val bmp = mmr.frameAtTime

            val scaledBmp = UtilsKt.resizeBitmap(bmp, 1280, 720)
            val byteArrayOutputStream = ByteArrayOutputStream()
            scaledBmp?.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    fun saveUploadInfo(fileName: String, tags: String?, categoryId: Long) {
        viewModelScope.launch {
//            progressDialog.value = true
            val ageGroupId = ageGroupPosition.value ?: -1
            val resp = try {
                Resource.Success(
                    contentUploadApi(
                        fileName,
                        title.value,
                        description.value,
                        tags,
                        ageGroupId.toString(),
                        categoryId,
                        thumbnailUrl.value
                    ))
            } catch (ex: Exception) {
                Resource.Failure(getError(ex))
            }
            //progressDialog.value = false
//            resultLiveData.postValue(resp)
        }
    }

    fun saveThumbnail(uri: String?) {
        if(uri == null) return
        viewModelScope.launch {
            val imageData = withContext(Dispatchers.Default + Job()) {
                imagePathToBase64(appContext, uri)
            }
            bannerBase64 = imageData
//            saveThumbnailToDb(imageData)
        }
    }

    /*private suspend fun saveThumbnailToDb(imageData: String) {
        val uploadId = preference.uploadId ?: ""
        uploadRepo.getUploadById(UtilsKt.stringToUploadId(uploadId))?.apply {
            thumbUri = imageData
        }?.also { info->
            uploadRepo.updateUploadInfo(info)
        }
        thumbnailData.value = imageData
    }*/
}
