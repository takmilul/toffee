package com.banglalink.toffee.ui.favorite

import android.app.Application
import androidx.lifecycle.LiveData
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.usecase.GetFavoriteContents
import com.banglalink.toffee.util.unsafeLazy

class FavoriteViewModel(application: Application) : BaseViewModel(application) {

    private val getFavoriteContents by unsafeLazy {
        GetFavoriteContents(Preference.getInstance(), RetrofitApiClient.toffeeApi)
    }

    fun loadFavoriteContents(): LiveData<Resource<List<ChannelInfo>>> {
        return resultLiveData {
            getFavoriteContents.execute()
        }
    }
}