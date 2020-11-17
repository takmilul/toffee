package com.banglalink.toffee.ui.home

import android.content.Context
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.apiservice.GetProfile
import com.banglalink.toffee.data.database.entities.TVChannelItem
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.repository.TVChannelRepository
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.data.storage.ViewCountDAO
import com.banglalink.toffee.di.AppCoroutineScope
import com.banglalink.toffee.model.*
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.util.getError
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.iid.InstanceIdResult
import kotlinx.coroutines.launch
import java.lang.Exception
import com.banglalink.toffee.usecase.*
import com.banglalink.toffee.util.unsafeLazy
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


class HomeViewModel @ViewModelInject constructor(
    @AppCoroutineScope private val appScope: CoroutineScope,
    private val profileApi: GetProfile,
    private val viewCountDAO: ViewCountDAO,
    private val sendViewContentEvent: SendViewContentEvent,
    @ApplicationContext private val mContext: Context,
    private val tvChannelRepo: TVChannelRepository
):BaseViewModel(),OnCompleteListener<InstanceIdResult> {

    //this will be updated by fragments which are hosted in HomeActivity to communicate with HomeActivity
    val fragmentDetailsMutableLiveData = MutableLiveData<ChannelInfo>()
    val shareContentLiveData = MutableLiveData<ChannelInfo>()
    val userChannelMutableLiveData = MutableLiveData<ChannelInfo>()
    //this will be updated by fragments which are hosted in HomeActivity to communicate with HomeActivity
    val viewAllChannelLiveData = MutableLiveData<Boolean>()
    //this will be updated by fragments which are hosted in HomeActivity to communicate with HomeActivity
    val viewAllVideoLiveData = MutableLiveData<Boolean>()
    val viewAllCategories = MutableLiveData<Boolean>()

    private val getCategory by lazy {
        GetCategory(Preference.getInstance(),RetrofitApiClient.toffeeApi)
    }

    private val getContentFromShareableUrl by unsafeLazy{
        GetContentFromShareableUrl(Preference.getInstance(),RetrofitApiClient.toffeeApi)
    }

    private val setFcmToken by unsafeLazy {
        SetFcmToken(Preference.getInstance(),RetrofitApiClient.toffeeApi)
    }

    init {
        getCategory()
        getProfile()
        FirebaseMessaging.getInstance().subscribeToTopic("buzz")
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener(this)
    }

    //overridden function for firebase token
    override fun onComplete(task: Task<InstanceIdResult>) {
        if (task.isSuccessful) {
            val token = task.result?.token
            if(token!=null){
                setFcmToken(token)
            }
        }

    }

    private fun setFcmToken(token:String){
        viewModelScope.launch {
            try{
                setFcmToken.execute(token)
            }
            catch (e:Exception){
                getError(e)
            }
        }
    }

    fun populateViewCountDb(url:String){
        appScope.launch {
            DownloadViewCountDb(RetrofitApiClient.dbApi, viewCountDAO)
                .execute(mContext, url)
        }
    }

    fun getCategory():LiveData<Resource<NavCategoryGroup>>{
        return resultLiveData { getCategory.execute() }
    }

    private fun getProfile(){
        viewModelScope.launch {
            try{
                profileApi()
            }catch (e:Exception){
                ToffeeAnalytics.logException(e)
            }
        }
    }

    fun getShareableContent(shareUrl :String):LiveData<Resource<ChannelInfo?>>{
       return resultLiveData{
           getContentFromShareableUrl.execute(shareUrl)
       }
    }

    fun sendViewContentEvent(channelInfo: ChannelInfo){
        viewModelScope.launch {
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    sendViewContentEvent.execute(channelInfo)
                }

            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

    fun addTvChannelToRecent(it: ChannelInfo) {
        viewModelScope.launch {
            tvChannelRepo.insertRecentItems(
                TVChannelItem(
                    it.id.toLong(),
                    it.type ?: "LIVE",
                    0,
                    "Recent",
                    Gson().toJson(it)
                )
            )
        }
    }
}