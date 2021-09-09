package com.banglalink.toffee.ui.upload

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.banglalink.toffee.apiservice.ContentUpload
import com.banglalink.toffee.apiservice.GetContentCategories
import com.banglalink.toffee.apiservice.UploadSignedUrlService
import com.banglalink.toffee.data.database.entities.UploadInfo
import com.banglalink.toffee.data.repository.UploadInfoRepository
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.exception.Error
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.SubCategory
import com.banglalink.toffee.util.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.gotev.uploadservice.protocols.binary.BinaryUploadRequest
import java.util.*

class EditUploadInfoViewModel @AssistedInject constructor(
    @ApplicationContext private val appContext: Context,
    private val uploadRepo: UploadInfoRepository,
    private val contentUploadApi: ContentUpload,
    private val preference: SessionPreference,
    private val categoryApi: GetContentCategories,
    private val uploadSignedUrlService: UploadSignedUrlService,
    @Assisted private val uploadFileUri: String
) : ViewModel() {

    val progressDialog = MutableLiveData<Boolean>()

    val submitButtonStatus = MutableLiveData<Boolean>()
    val resultLiveData = MutableLiveData<Resource<Pair<String, Long>>>()

    val title = MutableLiveData<String>()
    val description = MutableLiveData<String>()

    val tags = MutableLiveData<String>()

    val uploadProgress = MutableLiveData<Int>()
    val uploadSize = MutableLiveData<String>()

    val categories = MutableLiveData<List<Category>>()
    val categoryPosition = MutableLiveData<Int>()

    val subCategories = MutableLiveData<List<SubCategory>>()
    val subCategoryPosition = MutableLiveData<Int>()

    val ageGroup = MutableLiveData<List<String>>()
    val ageGroupPosition = MutableLiveData<Int>()

    val thumbnailData = MutableLiveData<String?>()

    val uploadStatusText = MutableLiveData<String>()
    val copyrightFileName = MutableLiveData<String>()

    val durationData = MutableLiveData<Long>()
    val orientationData = MutableLiveData<Int>()

    val exitFragment = SingleLiveEvent<Boolean>()

    private var fileName: String = ""
    private var actualFileName: String? = null

    var copyrightDocUri: String? = null
    var copyrightDocExt: String? = null

//    private val workerContext

//    val challengeSelectionList = MutableLiveData<List<String>>()
//    val challengeSelectionPosition = MutableLiveData<Int>()

    init {
        categoryPosition.value = 0
        durationData.value = 0
        load()

        ageGroup.value = listOf("For All", "3+", "9+", "13+")
        ageGroupPosition.value = 0

//        challengeSelectionList.value = listOf("Select", "Music", "Movie", "Games", "TV Series")
//        challengeSelectionPosition.value = 0
    }

    @dagger.assisted.AssistedFactory
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

            categories.value = try {
                categoryApi.loadData(0, 0)
            } catch (ex: Exception) {
                ex.printStackTrace()
                null
            }
            if (categories.value.isNullOrEmpty()) {
                progressDialog.value = false
                exitFragment.value = true
                return@launch
            }
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
                it.first?.let { thumb ->
                    thumbnailData.value = thumb
                }
                orientationData.value = it.second ?: 1
            }
        }
    }

    private fun loadVideoDuration() {
        viewModelScope.launch {
            durationData.value = UtilsKt.getVideoDuration(appContext, uploadFileUri)
        }
    }

    private suspend fun initUpload() {
        actualFileName = UtilsKt.fileNameFromContentUri(appContext, Uri.parse(uploadFileUri))
        val fileSize = UtilsKt.fileSizeFromContentUri(appContext, Uri.parse(uploadFileUri))

        uploadStatusText.value = "$actualFileName \u2022 ${Utils.readableFileSize(fileSize)}"

        val idx = actualFileName?.lastIndexOf(".") ?: -1
        val ext = if (idx >= 0) {
            actualFileName?.substring(idx) ?: ".mp4"
        } else ".mp4"
//
        fileName = preference.customerId.toString() + "_" + UUID.randomUUID().toString() + if(ext.isNotBlank()) ext else ".mp4"

//        val upInfo = UploadInfo(fileUri = uploadFileUri, fileName = fileName)
//
//        val contentType = withContext(Dispatchers.IO + Job()) {
//            UtilsKt.contentTypeFromContentUri(appContext, Uri.parse(uploadFileUri))
//        }
    }

    fun categoryIndexChanged(idx: Int) {
        categories.value?.getOrNull(idx)?.let {
            subCategories.value = it.subcategories ?: emptyList()
//            subCategoryPosition.value = 1
        }
    }

    fun updateProgress(progress: Int, size: Long) {
        uploadProgress.value = progress
        uploadSize.value = Utils.readableFileSize(size)
    }

    suspend fun loadCopyrightFileName(fileUri: Uri) {
        copyrightDocUri = fileUri.toString()
        val fileSize = UtilsKt.fileSizeFromContentUri(appContext, fileUri)
        val actualFileSize = Utils.readableFileSize(fileSize)
        val contentFileName = UtilsKt.fileNameFromContentUri(appContext, Uri.parse(fileUri.toString()))
        copyrightDocExt = contentFileName.substringAfterLast(".")
        val docFileName = "$contentFileName ($actualFileSize)"
        copyrightFileName.value = docFileName
    }

    suspend fun saveUploadInfo(tags: String?, categoryId: Long, subcategoryId: Long, duration: Long, isHorizontal: Int, isUploadCopyrightFile: Boolean) {
        progressDialog.value = true
        val ageGroupId = ageGroupPosition.value ?: -1

        val copyrightDir = fileName.substringBeforeLast(".", fileName)
        val copyrightFileName = "${System.currentTimeMillis()}.${copyrightDocExt}"

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
                isHorizontal,
                if (isUploadCopyrightFile) "${copyrightDir}/${copyrightFileName}" else ""
            )
            Log.e("RESP", resp.toString())
            if (resp.contentId > 0L) {
                val uploadId = startUpload(resp.contentId, resp.uploadVODSignedUrl, resp.uploadCopyrightSignedUrl, isUploadCopyrightFile)
                Log.e("uploadId", uploadId)
                if(uploadId != null) {
                    resultLiveData.value = Resource.Success(Pair(uploadId, resp.contentId))
                } else {
                    resultLiveData.value = Resource.Failure(Error(-1, "Unknown error occured"))
                }
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
        if (uri == null) return
        viewModelScope.launch {
            val imageData = withContext(Dispatchers.Default + Job()) {
                imagePathToBase64(appContext, uri)
            }
            thumbnailData.value = imageData
        }
    }

    private suspend fun startUpload(serverContentId: Long, vodSignedUrl: String, copyrightSignedUrl: String? = null, isUploadCopyrightFile: Boolean): String {
        var upInfo = UploadInfo(
            serverContentId = serverContentId,
            fileUri = uploadFileUri,
            fileName = fileName,
        )
        val upId = uploadRepo.insertUploadInfo(upInfo)
        upInfo = upInfo.copy(uploadId = upId)
        return withContext(Dispatchers.IO + Job()) {
            BinaryUploadRequest(
                appContext,
                vodSignedUrl
            )
                .setUploadID(upInfo.getUploadIdStr()!!)
                .setMethod("PUT")
                .addHeader("Content-Type", "application/octet-stream")
                .setFileToUpload(uploadFileUri)
                .startUpload().also {
                    if(isUploadCopyrightFile && !copyrightSignedUrl.isNullOrBlank()) {
                        BinaryUploadRequest(appContext, copyrightSignedUrl)
                            .setMethod("PUT")
                            .addHeader("Content-Type", "application/octet-stream")
                            .setFileToUpload(copyrightDocUri!!)
                            .startUpload()
                    }
                }
        }
    }
}
