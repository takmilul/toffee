package com.banglalink.toffee.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.apiservice.GetContentCategoriesService
import com.banglalink.toffee.apiservice.GetProfileService
import com.banglalink.toffee.apiservice.MyChannelEditDetailService
import com.banglalink.toffee.apiservice.TermsConditionService
import com.banglalink.toffee.data.network.request.MyChannelEditRequest
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.EditProfileForm
import com.banglalink.toffee.model.MyChannelEditBean
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.TermsAndCondition
import com.banglalink.toffee.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewProfileViewModel @Inject constructor(
    private val profileApi: GetProfileService,
    private val categoryApi: GetContentCategoriesService,
    private val termsConditionService: TermsConditionService,
    private val myChannelDetailApiService: MyChannelEditDetailService,
) : ViewModel() {
    
    private val _data = MutableLiveData<Resource<MyChannelEditBean?>>()
    private val termsAndConditionResult = MutableLiveData<Resource<TermsAndCondition?>>()
    val editChannelResult = _data.toLiveData()
    val profileForm = MutableLiveData<EditProfileForm>()
    val title = MutableLiveData<String>()
    val description = MutableLiveData<String>()
    val categories = MutableLiveData<List<Category>>()
    val ageGroup = MutableLiveData<List<String>>()
    val ageGroupPosition = MutableLiveData<Int>()
    val exitFragment = SingleLiveEvent<Boolean>()
    
    init {
        viewModelScope.launch {
            categories.value = try {
                categoryApi.loadData(0, 0)
            } catch (ex: Exception) {
                ex.printStackTrace()
                emptyList()
            }
            if (categories.value.isNullOrEmpty()) {
                exitFragment.value = true
            }
        }
        ageGroup.value = listOf("For All", "3+", "9+", "13+")
        ageGroupPosition.value = 0
    }
    
    fun loadCustomerProfile(): LiveData<Resource<EditProfileForm?>> {
        return resultLiveData {
            profileApi()?.profile?.toProfileForm()
        }
    }
    
    fun editChannel(myChannelEditRequest: MyChannelEditRequest) {
        viewModelScope.launch {
            val response = resultFromResponse { myChannelDetailApiService.execute(myChannelEditRequest) }
            _data.postValue(response)
        }
    }
    
    fun terms() {
        viewModelScope.launch {
            val response = resultFromResponse { termsConditionService.execute() }
            termsAndConditionResult.postValue(response)
        }
    }
}