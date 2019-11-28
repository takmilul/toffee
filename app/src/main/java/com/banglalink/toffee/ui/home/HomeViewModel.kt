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
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.SingleLiveEvent
import com.banglalink.toffee.util.getError
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.iid.InstanceIdResult
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*
import com.banglalink.toffee.usecase.*
import com.banglalink.toffee.util.unsafeLazy
import com.google.firebase.iid.FirebaseInstanceId


class HomeViewModel(application: Application):BaseViewModel(application),OnCompleteListener<InstanceIdResult> {

    private var timer: Timer? = null
    private val TIMER_DELAY = 0
    private val TIMER_PERIOD = 30000

    private val categoryMutableLiveData = MutableLiveData<Resource<NavCategoryGroup>>()
    val categoryLiveData = categoryMutableLiveData.toLiveData()

    private val channelMutableLiveData = MutableLiveData<Resource<List<StickyHeaderInfo>>>()
    val channelLiveData = channelMutableLiveData.toLiveData()

    private val shareableContentMutableLiveData = SingleLiveEvent<Resource<ChannelInfo>>()
    val shareableLiveData = shareableContentMutableLiveData.toLiveData()

    //this will be updated by fragments which are hosted in HomeActivity to communicate with HomeActivity
    val fragmentDetailsMutableLiveData = MutableLiveData<ChannelInfo>()
    //this will be updated by fragments which are hosted in HomeActivity to communicate with HomeActivity
    val viewAllChannelLiveData = MutableLiveData<Boolean>()
    //this will be updated by fragments which are hosted in HomeActivity to communicate with HomeActivity
    val viewAllVideoLiveData = MutableLiveData<Boolean>()

    private val getCategory by lazy {
        GetCategory(RetrofitApiClient.toffeeApi)
    }

    private val getChannelWithCategory by unsafeLazy {
        GetChannelWithCategory(Preference.getInstance(),RetrofitApiClient.toffeeApi)
    }

    private val getContentFromShareableUrl by unsafeLazy{
        GetContentFromShareableUrl(Preference.getInstance(),RetrofitApiClient.toffeeApi)
    }

    private val setFcmToken by unsafeLazy {
        SetFcmToken(Preference.getInstance(),RetrofitApiClient.toffeeApi)
    }

    init {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener(this)
        getCategory()
        getChannelByCategory(0)
    }

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

    fun getShareableContent(shareUrl :String){
        viewModelScope.launch {
            try{
                val response = getContentFromShareableUrl.execute(shareUrl)
                response?.let {
                    shareableContentMutableLiveData.setSuccess(response)
                }

            }catch (e:Exception){
                 getError(e)
            }
        }
    }


    fun stopHeartBeatTimer() {
        timer?.cancel()
        timer?.purge()
        timer = null
    }

    fun startHeartBeatTimer() {
        timer = Timer()
        timer?.scheduleAtFixedRate(SendHeartBeat(viewModelScope,Preference.getInstance(),RetrofitApiClient.toffeeApi), TIMER_DELAY.toLong(), TIMER_PERIOD.toLong())
    }
}