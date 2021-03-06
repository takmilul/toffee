package com.banglalink.toffee.ui.mychannel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.banglalink.toffee.apiservice.MyChannelVideoDeleteService
import com.banglalink.toffee.apiservice.MyChannelVideosRequestParams
import com.banglalink.toffee.apiservice.MyChannelVideosService
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BaseNetworkPagingSource
import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.data.repository.ContentViewPorgressRepsitory
import com.banglalink.toffee.data.repository.ContinueWatchingRepository
import com.banglalink.toffee.data.repository.UserActivitiesRepository
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MyChannelDeleteVideoBean
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyChannelVideosViewModel @Inject constructor(
    private val mPref: Preference,
    private val reactionDao: ReactionDao,
    private val activitiesRepo: UserActivitiesRepository,
    private val viewProgressRepo: ContentViewPorgressRepsitory,
    private val continueWatchingRepo: ContinueWatchingRepository,
    private val apiService: MyChannelVideosService.AssistedFactory,
    private val myChannelVideoDeleteApiService: MyChannelVideoDeleteService,
) : BaseViewModel() {

    private val _data = MutableLiveData<Resource<MyChannelDeleteVideoBean>>()
    val deleteVideoLiveData = _data.toLiveData()

    fun getMyChannelVideos(channelOwnerId: Int): Flow<PagingData<ChannelInfo>> {
        return BaseListRepositoryImpl({
            BaseNetworkPagingSource(
                apiService.create(
                    MyChannelVideosRequestParams("VOD", channelOwnerId, 0, 0))
            )
        }).getList().cachedIn(viewModelScope)
    }

    fun deleteVideo(contentId: Int) {
        viewModelScope.launch {
            val response = resultFromResponse { myChannelVideoDeleteApiService.invoke(contentId) }
            _data.postValue(response)
            if (response is Success) {
                reactionDao.deleteByContentId(mPref.customerId, contentId.toLong())
                activitiesRepo.deleteByContentId(mPref.customerId, contentId.toLong())
                continueWatchingRepo.deleteByContentId(mPref.customerId, contentId.toLong())
                viewProgressRepo.deleteByContentId(mPref.customerId, contentId.toLong())
            }
        }
    }
}