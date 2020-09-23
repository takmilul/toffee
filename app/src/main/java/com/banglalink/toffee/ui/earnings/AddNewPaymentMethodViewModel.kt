package com.banglalink.toffee.ui.earnings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.usecase.GetNewPaymentMethodInfo
import com.banglalink.toffee.util.unsafeLazy
import kotlinx.coroutines.launch


class AddNewPaymentMethodViewModel : ViewModel() {
    private val addPaymentRepo by unsafeLazy { GetNewPaymentMethodInfo() }

    private val _accountTypes = MutableLiveData<Resource<List<String>>>()
    val accountTypesLiveData = _accountTypes.toLiveData()
    var accountTypes: List<String> = listOf()
        private set
    
    private val _bankNames = MutableLiveData<Resource<List<String>>>()
    val bankNamesLiveData = _bankNames.toLiveData()
    var bankNames: List<String> = listOf() 
        private set
    
    private val _districtNames = MutableLiveData<Resource<List<String>>>()
    val districtNamesLiveData = _districtNames.toLiveData()
    var districtNames: List<String> = listOf()
        private set
    
    private val _branchNames = MutableLiveData<Resource<List<String>>>()
    val branchNamesLiveData = _branchNames.toLiveData()
    var branchNames: List<String> = listOf()
        private set

    fun getAccountTypeList() {
        viewModelScope.launch {
            val response = resultFromResponse { addPaymentRepo.getAccountTypes() }
            _accountTypes.postValue(response)
            if (response is Success) {
                accountTypes = response.data
            }
        }
    }

    fun getBankNameList() {
        viewModelScope.launch { 
            val response = resultFromResponse { addPaymentRepo.getBankNames() }
            _bankNames.postValue(response)
            if (response is Success){
                bankNames = response.data
            }
        }
    }

    fun getDistrictNameList() {
        viewModelScope.launch {
            val response = resultFromResponse { addPaymentRepo.getDistrictNames() }
            _districtNames.postValue(response)
            if (response is Success){
                districtNames = response.data
            }
        }
    }

    fun getBranchNameList() {
        viewModelScope.launch {
            val response = resultFromResponse { addPaymentRepo.getBranchNames() }
            _branchNames.postValue(response)
            if (response is Success){
                branchNames = response.data
            }
        }
    }
}