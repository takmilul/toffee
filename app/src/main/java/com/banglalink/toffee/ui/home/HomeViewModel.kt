package com.banglalink.toffee.ui.home

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.extension.setError
import com.banglalink.toffee.extension.setSuccess
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.NavCategoryGroup
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.channels.StickyHeaderInfo
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.ui.player.ChannelInfo
import com.banglalink.toffee.usecase.GetCategory
import com.banglalink.toffee.usecase.GetChannelWithCategory
import com.banglalink.toffee.usecase.UpdateFavorite
import com.banglalink.toffee.util.getError
import kotlinx.coroutines.launch
import java.lang.Exception

class HomeViewModel(application: Application):BaseViewModel(application) {
    private val categoryMutableLiveData = MutableLiveData<Resource<NavCategoryGroup>>()
    val categoryLiveData = categoryMutableLiveData.toLiveData()

    private val channelMutableLiveData = MutableLiveData<Resource<List<StickyHeaderInfo>>>()
    val channelLiveData = channelMutableLiveData.toLiveData()

    //this will be updated by fragments which are hosted in HomeActivity to communicate with HomeActivity
    val fragmentDetailsMutableLiveData = MutableLiveData<ChannelInfo>()
    //this will be updated by fragments which are hosted in HomeActivity to communicate with HomeActivity
    val viewAllChannelLiveData = MutableLiveData<Boolean>()

    private val getCategory by lazy {
        GetCategory(RetrofitApiClient.toffeeApi)
    }

    private val getChannelWithCategory by lazy {
        GetChannelWithCategory(Preference.getInstance(),RetrofitApiClient.toffeeApi)
    }

    init {
        getCategory()
        getChannelByCategory(0)
    }

    private fun getCategory(){
        viewModelScope.launch {
            try{
                categoryMutableLiveData.setSuccess(getCategory.execute())
            }catch (e:Exception){
                categoryMutableLiveData.setError(getError(e))
            }
        }
    }

    fun getChannelByCategory(subcategoryId:Int){
        viewModelScope.launch {
            try{
                val response = getChannelWithCategory.execute(subcategoryId).map {
                    StickyHeaderInfo(it.categoryName,it.channels)
                }
                channelMutableLiveData.setSuccess(response)

            }catch (e:Exception){
                channelMutableLiveData.setError(getError(e))
            }
        }
    }
}