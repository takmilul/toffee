package com.banglalink.toffee.ui.points

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.banglalink.toffee.apiservice.GetAboutPoints
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.model.AboutPointsBean
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.util.unsafeLazy

class AboutPointsViewModel : ViewModel() {
    
    private val aboutPoints by unsafeLazy { GetAboutPoints() }
    
    fun setAboutPoints(): LiveData<Resource<AboutPointsBean>> {
        return resultLiveData { 
            aboutPoints.execute()
        }
    }
}