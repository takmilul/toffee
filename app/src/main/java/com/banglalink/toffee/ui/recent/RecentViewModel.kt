package com.banglalink.toffee.ui.recent

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.storage.AppDatabase
import com.banglalink.toffee.data.storage.ChannelDataModel
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.usecase.GetHistory
import com.banglalink.toffee.usecase.GetViewCount
import com.banglalink.toffee.util.unsafeLazy
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecentViewModel(application: Application):BaseViewModel(application) {
    private val channelDAO by lazy {
        AppDatabase.getDatabase().channelDAO()
    }
    private val getHistory by unsafeLazy {
        GetHistory(channelDAO, GetViewCount(AppDatabase.getDatabase().viewCountDAO()))
    }

    private val gson = Gson()

    fun loadRecentItems(): LiveData<Resource<List<ChannelInfo>>> {
        return resultLiveData {
            getHistory.execute()
        }
    }

    fun updateRecentItem(channelInfo: ChannelInfo){
        viewModelScope.launch {
            CoroutineScope(Dispatchers.IO).launch {
                val channelDataModel = ChannelDataModel().apply {
                    payLoad =  gson.toJson(channelInfo)
                    channelId = channelInfo.id.toInt()
                    type = channelInfo.type
                    category = "history"
                }

                channelDAO.update(channelDataModel)
            }
        }

    }

    fun deleteRecentItem(channelInfo: ChannelInfo) {
        viewModelScope.launch {
            CoroutineScope(Dispatchers.IO).launch {
                channelDAO.deleteById(channelInfo.id.toInt())
            }
        }
    }


}