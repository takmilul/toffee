package com.banglalink.toffee.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.GetContentCategories
import com.banglalink.toffee.apiservice.GetProfile
import com.banglalink.toffee.apiservice.MyChannelEditDetailService
import com.banglalink.toffee.apiservice.TermsConditionService
import com.banglalink.toffee.data.network.request.MyChannelEditRequest
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewProfileViewModel @Inject constructor(
    private val myChannelDetailApiService: MyChannelEditDetailService,
    private val categoryApi: GetContentCategories,
    private val termsConditionService: TermsConditionService,
    private val profileApi: GetProfile,
) : ViewModel() {

    private val _data = MutableLiveData<Resource<MyChannelEditBean>>()
    val termsAndConditionResult = MutableLiveData<Resource<TermsAndCondition>>()
    val editChannelResult = _data.toLiveData()

    val categories = MutableLiveData<List<Category>>()

    init {
        viewModelScope.launch {
            categories.value = try {
                categoryApi.loadData(0, 0)
            } catch (ex: Exception) {
                ex.printStackTrace()
                emptyList()
            }
        }
    }

    fun loadCustomerProfile(): LiveData<Resource<EditProfileForm>> {
        return resultLiveData {
            profileApi().profile.toProfileForm()
        }
    }

    fun editChannel(myChannelEditRequest: MyChannelEditRequest) {
        viewModelScope.launch {
            _data.postValue(resultFromResponse { myChannelDetailApiService.execute(myChannelEditRequest) })
        }
    }

    fun terms() {
        viewModelScope.launch {
            termsAndConditionResult.postValue(resultFromResponse { termsConditionService.execute() })
        }
    }
}