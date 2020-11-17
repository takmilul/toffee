package com.banglalink.toffee.ui.useractivities

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.MyChannelGetDetailService
import com.banglalink.toffee.common.paging.BaseListRepository
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.data.database.entities.UserActivities
import com.banglalink.toffee.data.repository.UserActivitiesRepository
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.MyChannelDetailBean
import kotlinx.coroutines.launch

class UserActivitiesListViewModel @ViewModelInject constructor(
    private val channelInfoApi: MyChannelGetDetailService,
    private val activitiesRepo: UserActivitiesRepository,
    private val preference: Preference
): BasePagingViewModel<UserActivities>() {
    
    val myChannelDetail = MutableLiveData<MyChannelDetailBean>()
    
    override val repo: BaseListRepository<UserActivities> by lazy {
        BaseListRepositoryImpl({activitiesRepo.getAllItems(preference.customerId)})
    }

    fun removeItem(item: UserActivities) {
        viewModelScope.launch {
            activitiesRepo.delete(item)
        }
    }


    fun getChannelInfo(isOwner: Int, isPublic: Int, channelId: Long, channelOwnerId: Int) {
        viewModelScope.launch {
            try {
                myChannelDetail.value = channelInfoApi.execute(isOwner, isPublic, channelId.toInt(), channelOwnerId)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

}