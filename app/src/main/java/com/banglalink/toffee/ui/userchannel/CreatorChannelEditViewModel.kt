package com.banglalink.toffee.ui.userchannel

import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.GetUgcCategories
import com.banglalink.toffee.data.network.request.UgcEditMyChannelRequest
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.UgcCategory
import com.banglalink.toffee.model.UgcEditMyChannelBean
import com.banglalink.toffee.model.UgcMyChannelDetail
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.usecase.EditMyChannel
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.launch

class CreatorChannelEditViewModel @AssistedInject constructor(private val apiService: EditMyChannel, private val categoryApiService: GetUgcCategories, @Assisted val ugcMyChannelDetail: UgcMyChannelDetail?) :
    BaseViewModel() {

    private val _data = MutableLiveData<Resource<UgcEditMyChannelBean>>()
    val liveData = _data.toLiveData()
    var categoryList = listOf<UgcCategory>()
    private var _categories = MutableLiveData<List<String>>()
    val categories = _categories.toLiveData()
    var selectedItem: UgcCategory? = null

    init {
        viewModelScope.launch {
            categoryList = categoryApiService.loadData(0, 0)
            selectedItem = categoryList.find { it.id == ugcMyChannelDetail?.categoryId }
            _categories.postValue(categoryList.map { it.categoryName })
        }
    }

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(ugcMyChannelDetail: UgcMyChannelDetail?): CreatorChannelEditViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: AssistedFactory,
            ugcMyChannelDetail: UgcMyChannelDetail?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(ugcMyChannelDetail) as T
            }
        }
    }

    fun editChannel(ugcEditMyChannelRequest: UgcEditMyChannelRequest) {
        viewModelScope.launch {
            _data.postValue(resultFromResponse { apiService.execute(ugcEditMyChannelRequest) })
        }
    }

    fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        selectedItem = categoryList.find { it.categoryName == parent?.adapter?.getItem(pos) }
    }
}