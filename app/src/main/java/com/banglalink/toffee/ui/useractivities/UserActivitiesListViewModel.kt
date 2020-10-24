package com.banglalink.toffee.ui.useractivities

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.common.paging.BaseListRepository
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.common.paging.BasePagingViewModel
import com.banglalink.toffee.data.database.entities.UserActivities
import com.banglalink.toffee.data.repository.UserActivitiesRepository
import kotlinx.coroutines.launch

class UserActivitiesListViewModel @ViewModelInject constructor(
    private val activitiesRepo: UserActivitiesRepository
): BasePagingViewModel<UserActivities>() {

    override val repo: BaseListRepository<UserActivities> by lazy {
        BaseListRepositoryImpl({activitiesRepo.getAllItems()})
    }

    fun removeItem(item: UserActivities) {
        viewModelScope.launch {
            activitiesRepo.delete(item)
        }
    }
}