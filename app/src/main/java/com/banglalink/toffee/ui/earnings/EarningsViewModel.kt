package com.banglalink.toffee.ui.earnings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.banglalink.toffee.data.network.util.setAndResultLiveData
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Earning
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.usecase.GetEarningInfo
import com.banglalink.toffee.util.unsafeLazy

class EarningsViewModel : ViewModel() {
    private val earningRepo by unsafeLazy { GetEarningInfo() }
    private val _earning: MutableLiveData<Earning> = MutableLiveData()
    val data = _earning.toLiveData()

    fun getEarningInfo(): LiveData<Resource<Earning>> {
        return setAndResultLiveData (_earning)
        { 
            earningRepo.execute() 
        }
    }
    
}