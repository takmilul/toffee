package com.banglalink.toffee.ui.mychannel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.R
import com.banglalink.toffee.apiservice.ContentEditService
import com.banglalink.toffee.apiservice.GetContentCategoriesService
import com.banglalink.toffee.data.network.response.ResponseBean
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.SubCategory
import com.banglalink.toffee.util.SingleLiveEvent
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.getError
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MyChannelVideosEditViewModel @Inject constructor(
    private val categoryApi: GetContentCategoriesService,
    private val contentEditServiceApi: ContentEditService,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    val title = MutableLiveData<String>()
    val description = MutableLiveData<String>()
    val tags = MutableLiveData<String>()
    val categories = MutableLiveData<List<Category>>()
    val categoryPosition = MutableLiveData<Int>()
    val subCategories = MutableLiveData<List<SubCategory>>()
    val subCategoryPosition = MutableLiveData<Int>()
    val ageGroup = MutableLiveData<List<String>>()
    val ageGroupPosition = MutableLiveData<Int>()
    val thumbnailUrl = MutableLiveData<String?>()
    var bannerBase64: String? = "NULL"
    private val responseLiveData = MutableLiveData<Resource<ResponseBean>>()
    val editResponse = responseLiveData.toLiveData()
    val exitFragment = SingleLiveEvent<Boolean>()

    init {
        viewModelScope.launch {
            categories.value = try {
                categoryApi.loadData(0, 0)
            } catch (ex: Exception) {
                ex.printStackTrace()
                emptyList()
            }
            if (categories.value.isNullOrEmpty()) {
                exitFragment.value = true
            }
        }
        ageGroup.value = listOf("For All", "3+", "9+", "13+")
        ageGroupPosition.value = 0
    }

    fun saveUploadInfo(contentId: Int, fileName: String, tags: String?, categoryId: Long, subCategoryId: Int) {
        viewModelScope.launch {
            val ageGroupId = ageGroupPosition.value?: -1
            responseLiveData.value = try {
                val response = contentEditServiceApi(
                    contentId,
                    fileName,
                    title.value,
                    description.value,
                    tags,
                    ageGroupId.toString(),
                    categoryId,
                    subCategoryId,
                    thumbnailUrl.value,
                    bannerBase64
                )
                response?.let {
                    Resource.Success(it)
                } ?: Resource.Failure(getError(Exception(appContext.getString(R.string.try_again_message))))
            } catch (ex: Exception) {
                Resource.Failure(getError(ex))
            }
        }
    }

    fun saveThumbnail(uri: String?) {
        if (uri == null) return
        viewModelScope.launch {
            val imageData = withContext(Dispatchers.Default + Job()) {
                Utils.imagePathToBase64(appContext, uri)
            }
            bannerBase64 = imageData
        }
    }

    fun categoryIndexChanged(idx: Int) {
        categories.value?.getOrNull(idx)?.let {
            it.subcategories?.let {subCategoriesList->
                subCategories.value = subCategoriesList
            }

        }
    }
}
