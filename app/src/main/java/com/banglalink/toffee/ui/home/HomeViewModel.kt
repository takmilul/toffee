package com.banglalink.toffee.ui.home

import android.content.Context
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.analytics.FirebaseParams
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.apiservice.*
import com.banglalink.toffee.data.ToffeeConfig
import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.data.database.entities.SubscriptionInfo
import com.banglalink.toffee.data.database.entities.TVChannelItem
import com.banglalink.toffee.data.network.response.MediaCdnSignUrl
import com.banglalink.toffee.data.network.response.MqttBean
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.data.network.retrofit.DbApi
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.data.network.util.resultLiveData
import com.banglalink.toffee.data.repository.*
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.di.AppCoroutineScope
import com.banglalink.toffee.di.SimpleHttpClient
import com.banglalink.toffee.extension.isTestEnvironment
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
    private var toffeeConfig: ToffeeConfig,
    private val logoutService: LogoutService,
    private val accountDeleteService: AccountDeleteService,
    private val vastTagServiceV3: VastTagServiceV3,
    private val updateFavorite: UpdateFavorite,
    private val sendOtpLogEvent: SendOTPLogEvent,
    private val credentialService: CredentialService,
    private val tvChannelRepo: TVChannelRepository,
    @ApplicationContext private val mContext: Context,
    private val sendSubscribeEvent: SendSubscribeEvent,
    private val viewCountRepository: ViewCountRepository,
    private val sendShareCountEvent: SendShareCountEvent,
    private val shareCountRepository: ShareCountRepository,
    private val sendViewContentEvent: SendViewContentEvent,
    @SimpleHttpClient private val httpClient: OkHttpClient,
    @AppCoroutineScope private val appScope: CoroutineScope,
    private val checkForUpdateService: CheckForUpdateService,
    private val mqttCredentialService: MqttCredentialService,
    private val sendUserInterestEvent: SendUserInterestEvent,
    private val sendContentReportEvent: SendContentReportEvent,
    private val reactionCountRepository: ReactionCountRepository,
    private val contentFromShareableUrl: GetContentFromShareableUrl,
    private val subscribeChannelApiService: SubscribeChannelService,
    private val myChannelDetailApiService: MyChannelGetDetailService,
    private val subscriptionCountRepository: SubscriptionCountRepository,
    private val episodeListApi: GetShareableDramaEpisodesBySeason.AssistedFactory,
    private val playlistShareableApiService: PlaylistShareableService.AssistedFactory,
    private val sendCategoryChannelShareCountEvent: SendCategoryChannelShareCountEvent,
    private val mediaCdnSignUrlService: MediaCdnSignUrlService,
) : ViewModel() {
    
    val fcmToken = MutableLiveData<String>()
    val isStingray = MutableLiveData<Boolean>()
    val playContentLiveData = SingleLiveEvent<Any>()
    private var _playlistManager = PlaylistManager()
    val shareUrlLiveData = SingleLiveEvent<String>()
    val vastTagLiveData = MutableLiveData<Boolean>()
    val isFireworkActive = MutableLiveData<Boolean>()
    val viewAllVideoLiveData = MutableLiveData<Boolean>()
    val shareContentLiveData = SingleLiveEvent<ChannelInfo>()
    val updateStatusLiveData = SingleLiveEvent<Resource<Any?>>()
    val logoutLiveData = MutableLiveData<Resource<LogoutBean>>()
    val accountDeleteLiveData = SingleLiveEvent<Resource<AccountDeleteBean>>()
    private val _channelDetail = MutableLiveData<MyChannelDetail>()
    val myChannelNavLiveData = SingleLiveEvent<MyChannelNavParams>()
    val mqttCredentialLiveData = SingleLiveEvent<Resource<MqttBean?>>()
    val addToPlayListMutableLiveData = MutableLiveData<AddToPlaylistData>()
    val myChannelDetailResponse = SingleLiveEvent<Resource<MyChannelDetailBean>>()
    val subscriptionLiveData = MutableLiveData<Resource<MyChannelSubscribeBean>>()
    val mediaCdnSignUrlData = SingleLiveEvent<Resource<MediaCdnSignUrl?>>()
    val myChannelDetailLiveData = _channelDetail.toLiveData()
    val webSeriesShareableLiveData = SingleLiveEvent<Resource<DramaSeriesContentBean>>()
    val playlistShareableLiveData = SingleLiveEvent<Resource<MyChannelPlaylistVideosBean>>()
    val isBottomChannelScrolling = SingleLiveEvent<Boolean>().apply { value = false }
    
    init {
        if (mPref.customerId != 0 && mPref.password.isNotBlank()) {
            getProfile()
        }
        FirebaseMessaging.getInstance().subscribeToTopic("buzz")
        if (toffeeConfig.toffeeBaseUrl.isTestEnvironment()) {
            FirebaseMessaging.getInstance().subscribeToTopic("test-fcm")
            FirebaseMessaging.getInstance().subscribeToTopic("test-fifa-score")
            FirebaseMessaging.getInstance().unsubscribeFromTopic("prod-fifa-score")
        } else {
            FirebaseMessaging.getInstance().subscribeToTopic("prod-fifa-score")
            FirebaseMessaging.getInstance().unsubscribeFromTopic("test-fifa-score")
        }
        // Disable this in production.
        if (mPref.betaVersionCodes?.split(",")?.contains(BuildConfig.VERSION_CODE.toString()) == true) {
            FirebaseMessaging.getInstance().subscribeToTopic("beta-user-detection")
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("beta-user-detection")
        }
        
        FirebaseMessaging.getInstance().subscribeToTopic("DRM-LICENSE-RELEASE")
        FirebaseMessaging.getInstance().subscribeToTopic("controls")
        FirebaseMessaging.getInstance().subscribeToTopic("cdn_control")
        FirebaseMessaging.getInstance().subscribeToTopic("clear_cache")
        FirebaseMessaging.getInstance().subscribeToTopic("content_refresh")
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                fcmToken.postValue(token)
                setFcmToken(token)
            }
        }
    }
    
    fun getPlaylistManager() = _playlistManager
    
    fun setFcmToken(token: String) {
        viewModelScope.launch {
            try {
                setFcmToken.execute(token)
            } catch (e: Exception) {
                val error = getError(e)
                ToffeeAnalytics.logEvent(
                    ToffeeEvents.EXCEPTION, bundleOf(
                        "api_name" to ApiNames.SET_FCM_TOKEN,
                        FirebaseParams.BROWSER_SCREEN to BrowsingScreens.HOME_PAGE,
                        "error_code" to error.code,
                        "error_description" to error.msg
                    )
                )
            }
        }
    }
    
    fun populateViewCountDb(url: String) {
        appScope.launch {
            DownloadViewCountDb(dbApi, viewCountRepository).execute(mContext, url)
        }
    }
    
    fun populateReactionStatusDb(url: String) {
        appScope.launch {
            DownloadReactionStatusDb(dbApi, reactionCountRepository).execute(mContext, url)
        }
    }
    
    fun populateSubscriptionCountDb(url: String) {
        appScope.launch {
            DownloadSubscriptionCountDb(dbApi, subscriptionCountRepository).execute(mContext, url)
        }
    }
    
    fun populateShareCountDb(url: String) {
        appScope.launch {
            DownloadShareCountDb(dbApi, shareCountRepository).execute(mContext, url)
        }
    }
    
    private fun getProfile() {
        viewModelScope.launch {
            val response = resultFromResponse {
                profileApi()
            }
            if (response is Resource.Failure) {
                ToffeeAnalytics.logEvent(
                    ToffeeEvents.EXCEPTION, bundleOf(
                        "api_name" to ApiNames.GET_USER_PROFILE,
                        FirebaseParams.BROWSER_SCREEN to "Profile Screen",
                        "error_code" to response.error.code,
                        "error_description" to response.error.msg
                    )
                )
            }
        }
    }
    
    suspend fun fetchRedirectedDeepLink(url: String?): String? {
        if (url == null) return url
        return withContext(Dispatchers.IO + Job()) {
            try {
                val resp = httpClient.newCall(Request.Builder().url(url).build()).execute()
                val redirUrl = resp.request.url
                if (redirUrl.host == "toffeelive.com") redirUrl.toString()
                else null
            } catch (ex: Exception) {
                ex.printStackTrace()
                null
            }
        }
    }
    
    fun checkForUpdateStatus() {
        viewModelScope.launch {
            val updateResponse = resultFromResponse { checkForUpdateService.execute(BuildConfig.VERSION_CODE.toString()) }
            updateStatusLiveData.value = updateResponse
        }
    }
    
    fun getShareableContent(shareUrl: String, type: String? = null): LiveData<Resource<ChannelInfo?>> {
        return resultLiveData {
            contentFromShareableUrl.execute(shareUrl, type)
        }
    }
    
    fun sendViewContentEvent(channelInfo: ChannelInfo) {
        if (channelInfo.isApproved == 1 && channelInfo.channel_owner_id != mPref.customerId) {
            viewModelScope.launch {
                try {
                    sendViewContentEvent.execute(channelInfo)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
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
                    it.view_count?.toLong() ?: 0L,
                    it.isStingray
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
                    mPref.customerName = it.name ?: ""
                    mPref.customerEmail = it.email ?: ""
                    mPref.customerAddress = it.address ?: ""
                    mPref.customerDOB = it.dateOfBirth ?: ""
                    mPref.customerNID = it.nationalIdNo ?: ""
                    mPref.channelLogo = it.profileUrl ?: ""
                    mPref.channelName = it.channelName ?: ""
                }
            }
            myChannelDetailResponse.value = result
        }
    }
    
    fun sendShareLog(channelInfo: ChannelInfo) {
        if (channelInfo.isApproved == 1 && channelInfo.channel_owner_id != mPref.customerId) {
            ToffeeAnalytics.logEvent(ToffeeEvents.SHARE_CLICK)
            viewModelScope.launch {
                sendShareCountEvent.execute(channelInfo)
            }
        }
    }
    
    fun sendCategoryChannelShareLog(contentType: String, contentId: Int, sharedUrl: String) {
        viewModelScope.launch {
            sendCategoryChannelShareCountEvent.execute(contentType, contentId, sharedUrl)
        }
    }
    
    fun sendSubscriptionStatus(subscriptionInfo: SubscriptionInfo, status: Int) {
        viewModelScope.launch {
            val response = resultFromResponse { subscribeChannelApiService.execute(subscriptionInfo, status) }
            if (response is Success) {
                sendSubscribeEvent.sendToPubSub(subscriptionInfo, status)
                cacheManager.clearCacheByUrl(ApiRoutes.GET_SUBSCRIBED_CHANNELS)
                ToffeeAnalytics.logEvent(ToffeeEvents.CHANNEL_SUBSCRIPTION, bundleOf("isSubscribed" to status))
            } else {
                val error = response as Resource.Failure
                ToffeeAnalytics.logEvent(
                    ToffeeEvents.EXCEPTION, bundleOf(
                        "api_name" to ApiNames.SUBSCRIBE_CHANNEL,
                        FirebaseParams.BROWSER_SCREEN to "Users Channels",
                        "error_code" to error.error.code,
                        "error_description" to error.error.msg
                    )
                )
            }
            subscriptionLiveData.value = response
        }
    }
    
    fun updateFavorite(channelInfo: ChannelInfo): LiveData<Resource<FavoriteBean>> {
        return resultLiveData {
            val favorite = channelInfo.favorite == null || channelInfo.favorite == "0"
            updateFavorite.execute(channelInfo, favorite)
        }
    }
    
    fun getMqttCredential() {
        viewModelScope.launch {
            val response = resultFromResponse { mqttCredentialService.execute() }
            
            if (response is Resource.Success) {
                mqttCredentialLiveData.postValue(response)
            } else {
                val error = response as Resource.Failure
                ToffeeAnalytics.logEvent(
                    ToffeeEvents.EXCEPTION, bundleOf(
                        "api_name" to ApiNames.GET_MQTT_CREDENTIAL,
                        FirebaseParams.BROWSER_SCREEN to BrowsingScreens.HOME_PAGE,
                        "error_code" to error.error.code,
                        "error_description" to error.error.msg
                    )
                )
            }
        }
    }
    
    fun sendReportData(reportInfo: ReportInfo) {
        viewModelScope.launch {
            sendContentReportEvent.execute(reportInfo)
        }
    }
    
    fun logoutUser() {
        viewModelScope.launch {
            val response = resultFromResponse { logoutService.execute() }
            logoutLiveData.postValue(response)
        }
    }

    fun accountDelete() {
        viewModelScope.launch {
            val response = resultFromResponse { accountDeleteService.execute() }
            accountDeleteLiveData.postValue(response)
        }
    }
    
    fun sendUserInterestData(interestList: Map<String, Int>) {
        viewModelScope.launch {
            sendUserInterestEvent.execute(interestList)
        }
    }
    
    fun sendOtpLogData(otpLogData: OTPLogData, phoneNumber: String) {
        viewModelScope.launch {
            sendOtpLogEvent.execute(otpLogData, phoneNumber)
        }
    }
    
    fun getPlaylistShareableVideos(shareableData: ShareableData) {
        viewModelScope.launch {
            val response = resultFromResponse { playlistShareableApiService.create(shareableData).loadData(0, 30) }
            playlistShareableLiveData.postValue(response)
        }
    }
    
    fun getShareableEpisodesBySeason(shareableData: ShareableData) {
        viewModelScope.launch {
            val response = resultFromResponse { episodeListApi.create(shareableData).loadData(0, 30) }
            webSeriesShareableLiveData.postValue(response)
        }
    }
    
    fun getMediaCdnSignUrl(contentId: String) {
        viewModelScope.launch {
            val response = resultFromResponse { mediaCdnSignUrlService.execute(contentId) }
            mediaCdnSignUrlData.postValue(response)
        }
    }
    
    fun getVastTagV3(shouldObserve: Boolean = true) {
        viewModelScope.launch {
            try {
                vastTagServiceV3.execute().response.let {
                    mPref.vastTagListV3LiveData.value = it?.vastTagV3
                    mPref.nativeAdSettings.value = it?.nativeAdSettings
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val error = getError(e)
                ToffeeAnalytics.logEvent(
                    ToffeeEvents.EXCEPTION, bundleOf(
                        "api_name" to ApiNames.GET_VAST_TAG_LIST_V3,
                        FirebaseParams.BROWSER_SCREEN to BrowsingScreens.HOME_PAGE,
                        "error_code" to error.code,
                        "error_description" to error.msg
                    )
                )
            }
            if (shouldObserve) vastTagLiveData.value = true
        }
    }
    
    fun getCredential() {
        viewModelScope.launch {
            credentialService.execute()
        }
    }
}