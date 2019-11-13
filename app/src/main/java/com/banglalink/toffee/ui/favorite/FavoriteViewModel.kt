package com.banglalink.toffee.ui.favorite

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.extension.setError
import com.banglalink.toffee.extension.setSuccess
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.usecase.GetFavoriteContents
import com.banglalink.toffee.util.getError
import kotlinx.coroutines.launch

class FavoriteViewModel(application: Application):BaseViewModel(application) {
    private val favoriteMutableLiveData = MutableLiveData<Resource<List<ChannelInfo>>>()
    val favoriteListLiveData = favoriteMutableLiveData.toLiveData()

    private val getFavoriteContents by lazy {
        GetFavoriteContents(Preference.getInstance(),RetrofitApiClient.toffeeApi)
    }

    fun loadFavoriteContents(offset:Int){
        viewModelScope.launch {
           try{
                favoriteMutableLiveData.setSuccess(getFavoriteContents.execute(offset))
           }catch (e:Exception){
               favoriteMutableLiveData.setError(getError(e))
           }
        }
    }
}