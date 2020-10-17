package com.banglalink.toffee.ui.channels

import androidx.hilt.lifecycle.ViewModelInject
import com.banglalink.toffee.apiservice.GetChannelWithCategory
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.util.getError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AllChannelsViewModel @ViewModelInject constructor(
    private val allChannelService: GetChannelWithCategory
): BaseViewModel() {

    operator fun invoke(subcategoryId: Int): Flow<Resource<List<StickyHeaderInfo>>> {
        return flow<Resource<List<StickyHeaderInfo>>> {
            try{
                val response = allChannelService(subcategoryId).map {
                    StickyHeaderInfo(it.categoryName, it.channels)
                }
                emit(Resource.Success(response))

            }catch (e: Exception){
                emit(Resource.Failure(getError(e)))
            }
        }
    }
}
