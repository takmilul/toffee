package com.banglalink.toffee.ui.common

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Resource
import kotlinx.coroutines.launch

abstract class SingleListViewModel<T: Any>: ViewModel() {
    protected abstract var repo: SingleListRepository<T>

    private val _listData = MutableLiveData<Resource<List<T>>>()
    var listData = _listData.toLiveData()

    private val _showProgress = MutableLiveData<Boolean>()
    val showProgress = _showProgress.toLiveData()

    open var enableToolbar = true

    init {
        _showProgress.value = false
    }

    fun loadData() {
        _showProgress.value = true
        viewModelScope.launch {
            _listData.value = resultFromResponse { repo.execute() }
            _showProgress.value = false
        }
    }

    open fun onItemClicked(item: T) {}
}
