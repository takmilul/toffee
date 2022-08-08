package com.banglalink.toffee.ui.useractivities

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.common.paging.BaseListRepository
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.data.database.entities.UserActivities
import com.banglalink.toffee.data.repository.UserActivitiesRepository
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.MyChannelDetailBean
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserActivitiesListViewModel @Inject constructor(
    private val activitiesRepo: UserActivitiesRepository,
    private val preference: SessionPreference,
) : BasePagingViewModel<UserActivities>() {
    
    val myChannelDetail = MutableLiveData<MyChannelDetailBean>()
    
    override fun repo(): BaseListRepository<UserActivities> {
        return BaseListRepositoryImpl({ activitiesRepo.getAllItems(preference.customerId) })
    }
    
    fun removeItem(item: UserActivities) {
        viewModelScope.launch {
            activitiesRepo.delete(item)
        }
    }
}