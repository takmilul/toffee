package com.banglalink.toffee.ui.earnings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.banglalink.toffee.data.network.util.setAndResultLiveData
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.usecase.GetNewPaymentMethodInfo
import com.banglalink.toffee.util.unsafeLazy


class AddNewPaymentMethodViewModel: ViewModel() {
    private val addPaymentRepo by unsafeLazy { GetNewPaymentMethodInfo() }
    
    private val _accountTypes = MutableLiveData<List<String>>()
    val accountTypes = _accountTypes.toLiveData()
    private val _bankNames = MutableLiveData<List<String>>()
    val bankNames = _bankNames.toLiveData()
    private val _districtNames = MutableLiveData<List<String>>()
    val districtNames = _districtNames.toLiveData()
    private val _branchNames = MutableLiveData<List<String>>()
    val branchNames = _branchNames.toLiveData()
    
    fun getAccountTypeList(): LiveData<Resource<List<String>>> {
        return setAndResultLiveData (_accountTypes)
        {
            addPaymentRepo.getAccountTypes()
        }
    }

    fun getBankNameList(): LiveData<Resource<List<String>>>  {
        return setAndResultLiveData (_bankNames)
        {
            addPaymentRepo.getBankNames()
        }
    }

    fun getDistrictNameList(): LiveData<Resource<List<String>>>  {
        return setAndResultLiveData (_districtNames)
        {
            addPaymentRepo.getDistrictNames()
        }
    }

    fun getBranchNameList(): LiveData<Resource<List<String>>>  {
        return setAndResultLiveData (_branchNames)
        {
            addPaymentRepo.getBranchNames()
        }
    }
}