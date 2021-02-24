package com.banglalink.toffee.ui.home

import android.content.Context
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.apiservice.GetProfile
import com.banglalink.toffee.apiservice.MyChannelGetDetailService
import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.data.database.entities.TVChannelItem
import com.banglalink.toffee.data.network.retrofit.DbApi
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.repository.TVChannelRepository
import com.banglalink.toffee.data.repository.ViewCountRepository
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.di.AppCoroutineScope
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MyChannelDetailBean
import com.banglalink.toffee.model.MyChannelNavParams
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.common.BaseViewModel
import com.banglalink.toffee.ui.player.AddToPlaylistData
import com.banglalink.toffee.ui.player.PlaylistManager
import com.banglalink.toffee.usecase.*
import com.banglalink.toffee.util.SingleLiveEvent
import com.banglalink.toffee.util.getError
import com.banglalink.toffee.util.unsafeLazy
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class HomeViewModel @ViewModelInject constructor(
    @AppCoroutineScope private val appScope: CoroutineScope,
    private val profileApi: GetProfile,
    private val reactionDao: ReactionDao,
    private val myChannelDetailApiService: MyChannelGetDetailService,
    private val sendShareCountEvent: SendShareCountEvent,
    private val sendViewContentEvent: SendViewContentEvent,
    @ApplicationContext private val mContext: Context,
    private val tvChannelRepo: TVChannelRepository,
    private val viewCountRepository: ViewCountRepository,
    private val mPref: Preference,
    private val toffeeApi: ToffeeApi,
    private val dbApi: DbApi
):BaseViewModel(),OnCompleteListener<InstanceIdResult> {

    //this will be updated by fragments which are hosted in HomeActivity to communicate with HomeActivity
    val fragmentDetailsMutableLiveData = SingleLiveEvent<Any>()
    val addToPlayListMutableLiveData = MutableLiveData<AddToPlaylistData>()
    val shareContentLiveData = SingleLiveEvent<ChannelInfo>()
    val userChannelMutableLiveData = MutableLiveData<ChannelInfo>()
    //this will be updated by fragments which are hosted in HomeActivity to communicate with HomeActivity
    val switchBottomTab = SingleLiveEvent<Int>()
    //this will be updated by fragments which are hosted in HomeActivity to communicate with HomeActivity
    val viewAllVideoLiveData = MutableLiveData<Boolean>()
    val viewAllCategories = MutableLiveData<Boolean>()
    val myChannelNavLiveData = SingleLiveEvent<MyChannelNavParams>()

    private val _channelDetail = MutableLiveData<Resource<MyChannelDetailBean?>>()
    val channelDetail = _channelDetail.toLiveData()
    private var _playlistManager = PlaylistManager()

    fun getPlaylistManager() = _playlistManager

    private val getContentFromShareableUrl by unsafeLazy{
        GetContentFromShareableUrl(Preference.getInstance(), toffeeApi)
    }

    private val setFcmToken by unsafeLazy {
        SetFcmToken(Preference.getInstance(), toffeeApi)
    }

    init {
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
            DownloadViewCountDb(dbApi,viewCountRepository)
                .execute(mContext, url)
        }
    }

    fun populateReactionDb(url:String){
        appScope.launch {
            DownloadReactionDb(dbApi, reactionDao, mPref)
                .execute(mContext, url)
        }
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
                sendViewContentEvent.execute(channelInfo)
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
                    Gson().toJson(it),
                    it.view_count?.toLong() ?: 0L
                )
            )
        }
    }

    fun getChannelDetail(isOwner: Int, isPublic:Int, channelId: Int, channelOwnerId: Int) {
        viewModelScope.launch {
            val result = resultFromResponse { myChannelDetailApiService.execute(isOwner, isPublic, channelId, channelOwnerId) }

            if (result is Success) {
                val myChannelDetail = result.data.myChannelDetail
                myChannelDetail?.let {
                    mPref.isChannelDetailChecked = true
                    mPref.channelId = it.id.toInt()
                    if(!it.profileUrl.isNullOrBlank()){
                        mPref.channelLogo = it.profileUrl
                    }
                    if (!it.channelName.isNullOrBlank()) {
                        mPref.channelName = it.channelName
                    }
                }
            }
        }
    }
    
    fun sendShareLog(channelInfo: ChannelInfo){
        viewModelScope.launch { 
            sendShareCountEvent.execute(channelInfo)
        }
    }

    private val updateFavorite by unsafeLazy {
        UpdateFavorite(Preference.getInstance(), toffeeApi)
    }

    fun updateFavorite(channelInfo: ChannelInfo):LiveData<Resource<ChannelInfo>>{
        return resultLiveData {
            val favorite= channelInfo.favorite == null || channelInfo.favorite == "0"
            updateFavorite.execute(channelInfo,favorite)
        }
    }
}