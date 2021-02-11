package com.banglalink.toffee.ui.mychannel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.GetUgcCategories
import com.banglalink.toffee.apiservice.MyChannelEditDetailService
import com.banglalink.toffee.data.network.request.MyChannelEditRequest
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.MyChannelDetail
import com.banglalink.toffee.model.MyChannelEditBean
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.UgcCategory
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.util.SingleLiveEvent
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.launch

class MyChannelEditDetailViewModel @AssistedInject constructor(private val myChannelDetailApiService: MyChannelEditDetailService, private val categoryApiService: GetUgcCategories, @Assisted val myChannelDetail: MyChannelDetail?) :
    BaseViewModel() {

    private val _data = MutableLiveData<Resource<MyChannelEditBean>>()
    val editDetailLiveData = _data.toLiveData()
    var categoryList = MutableLiveData<List<UgcCategory>>()
    private var _categories = MutableLiveData<List<String>>()
//    val categories = _categories.toLiveData()
    var selectedCategory: UgcCategory? = null
    val selectedCategoryPosition = MutableLiveData<Int>()
    val exitFragment = SingleLiveEvent<Boolean>()

    init {
        viewModelScope.launch {
            categoryList.value = try {
                categoryApiService.loadData(0, 0)
            } catch (ex: Exception) {
                ex.printStackTrace()
                emptyList()
            }

            if(categoryList.value.isNullOrEmpty()) {
                exitFragment.value = true
            }
        }
    }

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(myChannelDetail: MyChannelDetail?): MyChannelEditDetailViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: AssistedFactory,
            myChannelDetail: MyChannelDetail?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(myChannelDetail) as T
            }
        }
    }

    fun editChannel(myChannelEditRequest: MyChannelEditRequest) {
        viewModelScope.launch {
            _data.postValue(resultFromResponse { myChannelDetailApiService.execute(myChannelEditRequest) })
        }
    }

    /*fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        selectedCategory = categoryList.find { it.categoryName == parent?.adapter?.getItem(pos) }
    }*/
}