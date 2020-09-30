package com.banglalink.toffee.ui.challenge

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.ChallengeDetail
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.usecase.GetChallengeDetail
import com.banglalink.toffee.util.unsafeLazy
import kotlinx.coroutines.launch

class ChallengeDetailViewModel: ViewModel() {
    var data: ChallengeDetail? = null
        private set
    val repo by unsafeLazy { GetChallengeDetail() }
    private val _liveData = MutableLiveData<Resource<ChallengeDetail>>()
    val liveData = _liveData.toLiveData()

    fun loadData(){
        viewModelScope.launch {
            val response = resultFromResponse { repo.execute() }
            _liveData.postValue(response)

            if (response is Success) {
                data = response.data
            }
        }
    }
}