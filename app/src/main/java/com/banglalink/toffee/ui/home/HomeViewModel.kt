package com.banglalink.toffee.ui.home

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.apiservice.*
import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.data.database.entities.SubscriptionInfo
import com.banglalink.toffee.data.database.entities.TVChannelItem
import com.banglalink.toffee.data.network.response.MqttBean
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.data.network.retrofit.DbApi
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.repository.*
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.di.AppCoroutineScope
import com.banglalink.toffee.di.SimpleHttpClient
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.*
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.player.AddToPlaylistData
import com.banglalink.toffee.ui.player.PlaylistManager
import com.banglalink.toffee.usecase.*
import com.banglalink.toffee.util.SingleLiveEvent
import com.banglalink.toffee.util.getError
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dbApi: DbApi,
    private val profileApi: GetProfile,
    private val mPref: SessionPreference,
    private val setFcmToken: SetFcmToken,
    private val reactionDao: ReactionDao,
    private val cacheManager: CacheManager,
    private val logoutService: LogoutService,
    private val updateFavorite: UpdateFavorite,
    private val tvChannelRepo: TVChannelRepository,
    @ApplicationContext private val mContext: Context,
    private val sendSubscribeEvent: SendSubscribeEvent,
    private val viewCountRepository: ViewCountRepository,
    private val sendShareCountEvent: SendShareCountEvent,
    private val shareCountRepository: ShareCountRepository,
    private val sendViewContentEvent: SendViewContentEvent,
    @SimpleHttpClient private val httpClient: OkHttpClient,
    @AppCoroutineScope private val appScope: CoroutineScope,
    private val mqttCredentialService: MqttCredentialService,
    private val sendUserInterestEvent: SendUserInterestEvent,
    private val sendContentReportEvent: SendContentReportEvent,
    private val reactionStatusRepository: ReactionStatusRepository,
    private val contentFromShareableUrl: GetContentFromShareableUrl,
    private val myChannelDetailApiService: MyChannelGetDetailService,
    private val subscriptionCountRepository: SubscriptionCountRepository,
) : ViewModel() {

    //this will be updated by fragments which are hosted in HomeActivity to communicate with HomeActivity
    val fragmentDetailsMutableLiveData = SingleLiveEvent<Any>()
    val addToPlayListMutableLiveData = MutableLiveData<AddToPlaylistData>()
    val shareContentLiveData = SingleLiveEvent<ChannelInfo>()
    //this will be updated by fragments which are hosted in HomeActivity to communicate with HomeActivity
    val viewAllVideoLiveData = MutableLiveData<Boolean>()
//    val viewAllCategories = MutableLiveData<Boolean>()
    val logoutLiveData = SingleLiveEvent<Resource<LogoutBean>>()
    val myChannelNavLiveData = SingleLiveEvent<MyChannelNavParams>()
    val notificationUrlLiveData = SingleLiveEvent<String>()
    val mqttCredentialLiveData = SingleLiveEvent<Resource<MqttBean?>>()
    private val _channelDetail = MutableLiveData<MyChannelDetail>()
    val myChannelDetailResponse = SingleLiveEvent<Resource<MyChannelDetailBean>>()
    private var _playlistManager = PlaylistManager()
    val subscriptionLiveData = SingleLiveEvent<Resource<MyChannelSubscribeBean>>()
    val myChannelDetailLiveData = _channelDetail.toLiveData()

    fun getPlaylistManager() = _playlistManager

    init {
        getProfile()
        FirebaseMessaging.getInstance().subscribeToTopic("buzz")
        FirebaseMessaging.getInstance().subscribeToTopic("controls")
        FirebaseMessaging.getInstance().subscribeToTopic("cdn_control")
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if(task.isSuccessful) {
                val token = task.result
                if(token != null) {
                    setFcmToken(token)
                }
            }
        }

    }
    private fun setFcmToken(token: String) {
        viewModelScope.launch {
            try {
                setFcmToken.execute(token)
            } catch (e: Exception) {
                getError(e)
            }
        }
    }

    fun populateViewCountDb(url: String) {
        appScope.launch {
            DownloadViewCountDb(dbApi, viewCountRepository)
                .execute(mContext, url)
        }
    }

    fun populateReactionDb(url: String) {
        appScope.launch {
            DownloadReactionDb(dbApi, reactionDao, mPref)
                .execute(mContext, url)
        }
    }

    fun populateReactionStatusDb(url: String) {
        appScope.launch {
            DownloadReactionStatusDb(dbApi, reactionStatusRepository)
                .execute(mContext, url)
        }
    }

    fun populateSubscriptionCountDb(url: String) {
        appScope.launch {
            DownloadSubscriptionCountDb(dbApi, subscriptionCountRepository)
                .execute(mContext, url)
        }
    }

    fun populateShareCountDb(url: String) {
        appScope.launch {
            DownloadShareCountDb(dbApi, shareCountRepository)
                .execute(mContext, url)
        }
    }

    private fun getProfile() {
        viewModelScope.launch {
            try {
                profileApi()
            } catch (e: Exception) {
                ToffeeAnalytics.logException(e)
            }
        }
    }

    suspend fun fetchRedirectedDeepLink(url: String?): String? {
        if(url == null) return url
        return withContext(Dispatchers.IO + Job()) {
            try {
                val resp = httpClient.newCall(Request.Builder().url(url).build()).execute()
                val redirUrl = resp.request.url
                if (redirUrl.host == "toffeelive.com") redirUrl.toString()
                else null
            }
            catch (ex: Exception) {
                ex.printStackTrace()
                null
            }
        }
    }

    fun getShareableContent(shareUrl: String): LiveData<Resource<ChannelInfo?>> {
        return resultLiveData {
            contentFromShareableUrl.execute(shareUrl)
        }
    }

    fun sendViewContentEvent(channelInfo: ChannelInfo) {
        viewModelScope.launch {
            try {
                sendViewContentEvent.execute(channelInfo)
            } catch (e: Exception) {
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

    fun getChannelDetail(channelOwnerId: Int) {
        viewModelScope.launch {
            val result = resultFromResponse { myChannelDetailApiService.execute(channelOwnerId) }

            if (result is Success) {
                val myChannelDetail = result.data.myChannelDetail
                myChannelDetail?.let {
                    _channelDetail.value = it
                    mPref.isChannelDetailChecked = true
                    mPref.channelId = it.id.toInt()
                    if (!it.profileUrl.isNullOrBlank()) {
                        mPref.channelLogo = it.profileUrl
                    }
                    if (!it.channelName.isNullOrBlank()) {
                        mPref.channelName = it.channelName
                    }
                    if (!it.name.isNullOrBlank()) {
                        mPref.customerName = it.name !!
                    }
                    if (!it.email.isNullOrBlank()) {
                        mPref.customerEmail = it.email !!
                    }
                    if (!it.address.isNullOrBlank()) {
                        mPref.customerAddress = it.address !!
                    }
                    if (!it.dateOfBirth.isNullOrBlank()) {
                        mPref.customerDOB = it.dateOfBirth
                    }
                    if (!it.nationalIdNo.isNullOrBlank()) {
                        mPref.customerNID = it.nationalIdNo
                    }
                }
            }
            myChannelDetailResponse.value = result
        }
    }

    fun sendShareLog(channelInfo: ChannelInfo) {
        viewModelScope.launch {
            sendShareCountEvent.execute(channelInfo)
        }
    }

    fun sendSubscriptionStatus(subscriptionInfo: SubscriptionInfo, status: Int) {
        viewModelScope.launch {
            val response = resultFromResponse { sendSubscribeEvent.execute(subscriptionInfo, status, true) }
            if (response is Success) {
                cacheManager.clearCacheByUrl(GET_SUBSCRIBED_CHANNELS)
            }
            subscriptionLiveData.value = response
        }
    }

    fun updateSubscriptionCountTable(subscriptionInfo: SubscriptionInfo, status: Int) {
        viewModelScope.launch {
            sendSubscribeEvent.updateSubscriptionCountDb(subscriptionInfo, status)
        }
    }
    
    fun updateFavorite(channelInfo: ChannelInfo): LiveData<Resource<ChannelInfo>> {
        return resultLiveData {
            val favorite = channelInfo.favorite == null || channelInfo.favorite == "0"
            updateFavorite.execute(channelInfo, favorite)
        }
    }
    
    fun getMqttCredential() {
        viewModelScope.launch { 
            mqttCredentialLiveData.postValue(resultFromResponse { mqttCredentialService.execute() }!!)
        }
    }
    
    fun sendReportData(reportInfo: ReportInfo) {
        viewModelScope.launch { 
            sendContentReportEvent.execute(reportInfo)
        }
    }
    
    fun logoutUser() {
        viewModelScope.launch { 
            logoutLiveData.postValue(resultFromResponse { logoutService.execute() }!!)
        }
    }
    
    fun sendUserInterestData(interestList: Map<String, Int>) {
        viewModelScope.launch {
            sendUserInterestEvent.execute(interestList)
        }
    }
}