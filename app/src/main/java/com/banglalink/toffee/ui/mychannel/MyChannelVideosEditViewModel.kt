package com.banglalink.toffee.ui.mychannel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.ContentEdit
import com.banglalink.toffee.apiservice.GetCategories
import com.banglalink.toffee.data.network.response.ResponseBean
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.SubCategory
import com.banglalink.toffee.util.getError
import com.banglalink.toffee.util.imagePathToBase64
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MyChannelVideosEditViewModel @Inject constructor(
    private val categoryApi: GetCategories,
    private val contentEditApi: ContentEdit,
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

    init {
        viewModelScope.launch {
            categories.value = categoryApi.loadData(0, 0)
        }
        ageGroup.value = listOf("For All", "3+", "9+", "13+", "18+")
        ageGroupPosition.value = 0
    }

    fun saveUploadInfo(contentId: Int, fileName: String, tags: String?, categoryId: Long, subCategoryId: Int) {
        viewModelScope.launch {
            val ageGroupId = ageGroupPosition.value ?: -1
            responseLiveData.value = try {
                Resource.Success(
                    contentEditApi(
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
                    ))
            } catch (ex: Exception) {
                Resource.Failure(getError(ex))
            }
        }
    }

    fun saveThumbnail(uri: String?) {
        if (uri == null) return
        viewModelScope.launch {
            val imageData = withContext(Dispatchers.Default + Job()) {
                imagePathToBase64(appContext, uri)
            }
            bannerBase64 = imageData
        }
    }

    fun categoryIndexChanged(idx: Int) {
        categories.value?.getOrNull(idx)?.let {
            subCategories.value = it.subcategories
        }
    }
}
