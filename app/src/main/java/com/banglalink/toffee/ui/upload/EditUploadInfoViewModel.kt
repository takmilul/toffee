package com.banglalink.toffee.ui.upload

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.GetUgcCategories
import com.banglalink.toffee.apiservice.UgcContentUpload
import com.banglalink.toffee.data.database.entities.UploadInfo
import com.banglalink.toffee.data.repository.UploadInfoRepository
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.exception.Error
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.TUS_UPLOAD_SERVER_URL
import com.banglalink.toffee.model.UgcCategory
import com.banglalink.toffee.model.UgcSubCategory
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.UtilsKt
import com.banglalink.toffee.util.getError
import com.banglalink.toffee.util.imagePathToBase64
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.android.synthetic.main.upload_method_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.gotev.uploadservice.protocols.binary.BinaryUploadRequest
import java.lang.Exception
import java.util.*

class EditUploadInfoViewModel @AssistedInject constructor(
    @ApplicationContext private val appContext: Context,
    private val uploadRepo: UploadInfoRepository,
    private val contentUploadApi: UgcContentUpload,
    private val preference: Preference,
    private val categoryApi: GetUgcCategories,
    @Assisted private val uploadFileUri: String,
//    private val subCategoryApi: SubCategoryService
): ViewModel() {
    val progressDialog = MutableLiveData<Boolean>()

    val submitButtonStatus = MutableLiveData<Boolean>()
    val resultLiveData = MutableLiveData<Resource<Pair<String, Long>>>()

    val title = MutableLiveData<String>()
    val description = MutableLiveData<String>()

    val tags = MutableLiveData<String>()

    val uploadProgress = MutableLiveData<Int>()
    val uploadSize = MutableLiveData<String>()

    val categories = MutableLiveData<List<UgcCategory>>()
    val categoryPosition = MutableLiveData<Int>()
    
    val subCategories = MutableLiveData<List<UgcSubCategory>>()
    val subCategoryPosition = MutableLiveData<Int>()

    val ageGroup = MutableLiveData<List<String>>()
    val ageGroupPosition = MutableLiveData<Int>()

    val thumbnailData = MutableLiveData<String?>()

    val uploadStatusText = MutableLiveData<String>()

    val durationData = MutableLiveData<Long>()
    val orientationData = MutableLiveData<Int>()

    private var fileName: String = ""
    private var actualFileName: String? = null

//    val challengeSelectionList = MutableLiveData<List<String>>()
//    val challengeSelectionPosition = MutableLiveData<Int>()

    init {
        categoryPosition.value = 0
        durationData.value = 0
        load()

        ageGroup.value = listOf("For All", "3+", "9+", "13+", "18+")
        ageGroupPosition.value = 0

//        challengeSelectionList.value = listOf("Select", "Music", "Movie", "Games", "TV Series")
//        challengeSelectionPosition.value = 0
    }

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(uploadFileUri: String): EditUploadInfoViewModel
    }

    companion object {
        fun provideFactory(assistedFactory: AssistedFactory, uploadFileUri: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return assistedFactory.create(uploadFileUri) as T
                }
            }
    }

    private fun load() {
        viewModelScope.launch {
            progressDialog.value = true

            categories.value = categoryApi.loadData(0, 0)
//            subCategories.value = subCategoryApi.loadData(0,0)

//            val uploadId = preference.uploadId ?: ""
//            val info = uploadRepo.getUploadById(UtilsKt.stringToUploadId(uploadId)) ?: run {
//                progressDialog.value = false
//                return@launch
//            }
            initUpload()
//            initUploadInfo(info)
            loadThumbnail()
            loadVideoDuration()
            progressDialog.value = false
        }
    }

    private fun loadThumbnail() {
        viewModelScope.launch {
            withContext(Dispatchers.Default + Job()) {
                UtilsKt.generateThumbnail(appContext, uploadFileUri)
            }?.let {
                it.first?.let { thumb->
                    saveThumbnailToDb(thumb)
                }
                orientationData.value = it.second
            }
        }
    }

    private fun loadVideoDuration() {
        viewModelScope.launch {
            withContext(Dispatchers.Default + Job()) {
                UtilsKt.getVideoDuration(appContext, uploadFileUri)
            }.let {
                durationData.value = it
            }
        }
    }

    private suspend fun initUpload() {
//        val accessToken = withContext(Dispatchers.IO) {
//            val credential = GoogleCredential.fromStream(
//                appContext.assets.open("toffee-261507-60ca3e5405df.json")
//            ).createScoped(listOf("https://www.googleapis.com/auth/devstorage.read_write"))
//            credential.refreshToken()
//            credential.accessToken
//        }

//        if (accessToken.isNullOrEmpty()) {
//            open_gallery_button.snack("Error uploading file. Please try again later.") {}
//            return@launch
//        }

        actualFileName = withContext(Dispatchers.IO + Job()) {
            UtilsKt.fileNameFromContentUri(appContext, Uri.parse(uploadFileUri))
        }

        val fileSize = withContext(Dispatchers.IO + Job()) {
            UtilsKt.fileSizeFromContentUri(appContext, Uri.parse(uploadFileUri))
        }

        uploadStatusText.value = "$actualFileName \u2022 ${Utils.readableFileSize(fileSize)}"

        val idx = actualFileName?.lastIndexOf(".") ?: -1
        val ext = if (idx >= 0) {
            actualFileName?.substring(idx) ?: ""
        }
        else ""
//
        fileName = preference.customerId.toString() + "_" + UUID.randomUUID().toString() + ext
//        val upInfo = UploadInfo(fileUri = uploadFileUri, fileName = fileName)
//
//        val contentType = withContext(Dispatchers.IO + Job()) {
//            UtilsKt.contentTypeFromContentUri(appContext, Uri.parse(uploadFileUri))
//        }
    }

//    private fun initUploadInfo(uploadInfo: UploadInfo) {
//        title.value = uploadInfo.title
//        description.value = uploadInfo.description
//        tags.value = uploadInfo.tags
//
//        submitButtonStatus.value = uploadInfo.status == UploadStatus.SUCCESS.value
//        thumbnailData.value = uploadInfo.thumbUri
//        categoryPosition.value = uploadInfo.categoryIndex
//        ageGroupPosition.value = uploadInfo.ageGroupIndex


//        challengeSelectionPosition.value = uploadInfo.submitToChallengeIndex
//    }

    fun categoryIndexChanged(idx: Int) {
        categories.value?.getOrNull(idx)?.let {
            subCategories.value = it.subcategories
            subCategoryPosition.value = 0
        }
    }

    fun updateProgress(progress: Int, size: Long) {
        uploadProgress.value = progress
        uploadSize.value = Utils.readableFileSize(size)
    }

    suspend fun saveUploadInfo(tags: String?, categoryId: Long, subcategoryId: Long, duration: Long, isHorizontal: Int) {
        progressDialog.value = true
        val ageGroupId = ageGroupPosition.value ?: -1

        try {
            val resp = contentUploadApi(
                fileName,
                title.value,
                description.value,
                tags,
                ageGroupId.toString(),
                categoryId,
                subcategoryId,
                thumbnailData.value,
                (duration / 1000).toString(),
                isHorizontal
            )
            Log.e("RESP", resp.toString())
            if (resp.contentId > 0L) {
                val uploadId = startUpload(resp.contentId)
                resultLiveData.value = Resource.Success(Pair(uploadId, resp.contentId))
                progressDialog.value = false
                return
            }
            resultLiveData.value = Resource.Failure(Error(-1, "Unknown error occured"))
        } catch (ex: Exception) {
            resultLiveData.value = Resource.Failure(getError(ex))
        }
        progressDialog.value = false
    }

    fun saveThumbnail(uri: String?) {
        if(uri == null) return
        viewModelScope.launch {
            val imageData = withContext(Dispatchers.Default + Job()) {
                imagePathToBase64(appContext, uri)
            }
            saveThumbnailToDb(imageData)
        }
    }

    private fun saveThumbnailToDb(imageData: String) {
//        val uploadId = preference.uploadId ?: ""
//        uploadRepo.getUploadById(UtilsKt.stringToUploadId(uploadId))?.apply {
//            thumbUri = imageData
//        }?.also { info->
//            uploadRepo.updateUploadInfo(info)
//        }
        Log.e("Thumbnail", imageData)
        thumbnailData.value = imageData
    }

    private suspend fun startUpload(serverContentId: Long): String {
        var upInfo = UploadInfo(
            serverContentId = serverContentId,
            fileUri = uploadFileUri,
            fileName = fileName,
        )
        val upId = uploadRepo.insertUploadInfo(upInfo)
        upInfo = upInfo.copy(uploadId = upId)

        return withContext(Dispatchers.IO + Job()) {
            TusUploadRequest(
                appContext,
                TUS_UPLOAD_SERVER_URL,
            )
                .setResumeInfo(upInfo.getFingerprint()!!, null)
                .setMetadata(upInfo.getFileNameMetadata())
                .setUploadID(upInfo.getUploadIdStr()!!)
                .setFileToUpload(uploadFileUri)
                .startUpload()
        }
    }

    private suspend fun startUpload2(serverContentId: Long): String {
        val accessToken = withContext(Dispatchers.IO) {
            val credential = GoogleCredential.fromStream(
                appContext.assets.open("toffee-261507-60ca3e5405df.json")
            ).createScoped(listOf("https://www.googleapis.com/auth/devstorage.read_write"))
            credential.refreshToken()
            credential.accessToken
        }

        if (accessToken.isNullOrEmpty()) {
            throw RuntimeException("Error uploading file. Please try again later.")
        }

//        val fn = withContext(Dispatchers.IO + Job()) {
//            UtilsKt.fileNameFromContentUri(appContext, Uri.parse(uri))
//        }
        val idx = actualFileName?.lastIndexOf(".") ?: -1
        val ext = if (idx >= 0) {
            actualFileName!!.substring(idx)
        }
        else ""

//        val fileName = preference.customerId.toString() + "_" + UUID.randomUUID().toString() + ext
        val upInfo = UploadInfo(serverContentId = serverContentId, fileUri = uploadFileUri, fileName = fileName)

        val contentType = withContext(Dispatchers.IO + Job()) {
            UtilsKt.contentTypeFromContentUri(appContext, Uri.parse(uploadFileUri))
        }

        Log.e("UPLOAD", "$fileName, $contentType")

        val upId = uploadRepo.insertUploadInfo(upInfo)
        return withContext(Dispatchers.IO + Job()) {
                BinaryUploadRequest(
                    appContext,
                    "https://storage.googleapis.com/upload/storage/v1/b/ugc-content-storage/o?uploadType=media&name=${fileName}"
                )
                    .setUploadID(UtilsKt.uploadIdToString(upId))
                    .setMethod("POST")
                    .addHeader("Content-Type", contentType)
                    .setFileToUpload(uploadFileUri)
                    .setBearerAuth(accessToken)
                    .startUpload()
            }
    }
}
