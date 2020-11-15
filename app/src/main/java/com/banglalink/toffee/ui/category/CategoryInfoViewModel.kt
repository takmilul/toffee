package com.banglalink.toffee.ui.category

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.ApiCategoryRequestParams
import com.banglalink.toffee.apiservice.GetCategoryFeatureContents
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.UgcSubCategory
import kotlinx.coroutines.launch

class CategoryInfoViewModel @ViewModelInject constructor(
//    private val followCategory: UgcFollowCategory,
    private val featureContentFactory: GetCategoryFeatureContents.AssistedFactory
): ViewModel() {
    val featuredList = MutableLiveData<List<ChannelInfo>>()
    val subcategoryList = MutableLiveData<List<UgcSubCategory>>()
//    val followerCount = MutableLiveData<Long>()
//    val isCategoryFollowing = MutableLiveData<Int>()
//    val followCategoryResponse = MutableLiveData<Resource<FollowCategoryBean>>()

    fun requestList(categoryId: Long) {
        viewModelScope.launch {
            val featureContentApi = featureContentFactory.create(
                ApiCategoryRequestParams(
                    "VOD", 1, categoryId.toInt()
                )
            )

            try {
                val resp = featureContentApi()
                featuredList.value = resp.channels
                subcategoryList.value = resp.subcategories
//                followerCount.value = resp.followers
//                isCategoryFollowing.value = resp.isFollowed
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    /*fun updateFollow(categoryId: Int) {
        viewModelScope.launch {
            followCategoryResponse.value = try {
                val followStatus: Int = isCategoryFollowing.value?.toInt() ?: return@launch
                val newFollowStatus = 1 - followStatus
                val resp = followCategory(categoryId, newFollowStatus)
                isCategoryFollowing.value = 1 - resp.isFollowed
                Resource.Success(resp)
            } catch (ex: Exception) {
                ex.printStackTrace()
                Resource.Failure(getError(ex))
            }
        }
    }*/
}