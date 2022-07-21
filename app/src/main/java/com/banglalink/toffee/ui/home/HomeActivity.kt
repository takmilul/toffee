package com.banglalink.toffee.ui.home

import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PictureInPictureParams
import android.app.SearchManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Path
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Rational
import android.util.Xml
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.OnBackStackChangedListener
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.FirebaseParams
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.apiservice.ApiRoutes
import com.banglalink.toffee.apiservice.BrowsingScreens
import com.banglalink.toffee.data.database.dao.FavoriteItemDao
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.data.repository.NotificationInfoRepository
import com.banglalink.toffee.data.repository.UploadInfoRepository
import com.banglalink.toffee.databinding.ActivityHomeBinding
import com.banglalink.toffee.di.AppCoroutineScope
import com.banglalink.toffee.enums.CategoryType
import com.banglalink.toffee.enums.SharingType
import com.banglalink.toffee.enums.UploadStatus
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.*
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.mqttservice.ToffeeMqttService
import com.banglalink.toffee.notification.PUBSUBMessageStatus
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.notification.ToffeeMessagingService.Companion.ACTION_NAME
import com.banglalink.toffee.notification.ToffeeMessagingService.Companion.CONTENT_VIEW
import com.banglalink.toffee.notification.ToffeeMessagingService.Companion.DISMISS
import com.banglalink.toffee.notification.ToffeeMessagingService.Companion.NOTIFICATION_ID
import com.banglalink.toffee.notification.ToffeeMessagingService.Companion.PUB_SUB_ID
import com.banglalink.toffee.notification.ToffeeMessagingService.Companion.ROW_ID
import com.banglalink.toffee.notification.ToffeeMessagingService.Companion.WATCH_NOW
import com.banglalink.toffee.ui.category.music.stingray.StingrayChannelFragmentNew
import com.banglalink.toffee.ui.category.webseries.EpisodeListFragment
import com.banglalink.toffee.ui.channels.AllChannelsViewModel
import com.banglalink.toffee.ui.channels.ChannelFragmentNew
import com.banglalink.toffee.ui.common.Html5PlayerViewActivity
import com.banglalink.toffee.ui.mychannel.MyChannelPlaylistVideosFragment
import com.banglalink.toffee.ui.player.AddToPlaylistData
import com.banglalink.toffee.ui.player.PlayerPageActivity
import com.banglalink.toffee.ui.player.PlaylistItem
import com.banglalink.toffee.ui.player.PlaylistManager
import com.banglalink.toffee.ui.profile.ViewProfileViewModel
import com.banglalink.toffee.ui.search.SearchFragment
import com.banglalink.toffee.ui.splash.SplashScreenActivity
import com.banglalink.toffee.ui.upload.UploadProgressViewModel
import com.banglalink.toffee.ui.upload.UploadStateManager
import com.banglalink.toffee.ui.userplaylist.UserPlaylistVideosFragment
import com.banglalink.toffee.ui.widget.DraggerLayout
import com.banglalink.toffee.ui.widget.ToffeeAlertDialogBuilder
import com.banglalink.toffee.ui.widget.showDisplayMessageDialog
import com.banglalink.toffee.util.*
import com.conviva.sdk.ConvivaAnalytics
import com.conviva.sdk.ConvivaSdkConstants
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.inappmessaging.FirebaseInAppMessaging
import com.google.gson.Gson
import com.medallia.digital.mobilesdk.MedalliaDigital
import com.suke.widget.SwitchButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import net.gotev.uploadservice.UploadService
import org.xmlpull.v1.XmlPullParser
import java.net.URLDecoder
import javax.inject.Inject

const val PAYMENT = 1
const val NON_PAYMENT = 0
const val PLAY_IN_WEB_VIEW = 1
const val STINGRAY_CONTENT = 10
const val PLAY_IN_NATIVE_PLAYER = 0
const val OPEN_IN_EXTERNAL_BROWSER = 2
const val PLAYER_EVENT_TAG = "PLAYER_EVENT"
const val IN_APP_UPDATE_REQUEST_CODE = 0x100

@AndroidEntryPoint
class HomeActivity :
    PlayerPageActivity(),
    SearchView.OnQueryTextListener,
    DraggerLayout.OnPositionChangedListener,
    OnBackStackChangedListener
{
    private val gson = Gson()
    private var channelOwnerId: Int = 0
    lateinit var binding: ActivityHomeBinding
    private var searchView: SearchView? = null
    private var notificationBadge: View? = null
    @Inject lateinit var bindingUtil: BindingUtil
    private lateinit var drawerHelper: DrawerHelper
    @Inject lateinit var cacheManager: CacheManager
    private var playlistShareableUrl: String? = null
    private var shareableData: ShareableData? = null
    private var webSeriesShareableUrl: String? = null
    private lateinit var navController: NavController
    @Inject lateinit var favoriteDao: FavoriteItemDao
    @Inject lateinit var mqttService: ToffeeMqttService
    private val viewModel: HomeViewModel by viewModels()
    @Inject lateinit var uploadRepo: UploadInfoRepository
    private lateinit var appbarConfig: AppBarConfiguration
    @Inject lateinit var uploadManager: UploadStateManager
    private lateinit var appUpdateManager: AppUpdateManager
    @Inject lateinit var inAppMessageParser: InAppMessageParser
    @Inject @AppCoroutineScope lateinit var appScope: CoroutineScope
    @Inject lateinit var notificationRepo: NotificationInfoRepository
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private val profileViewModel by viewModels<ViewProfileViewModel>()
    private val allChannelViewModel by viewModels<AllChannelsViewModel>()
    private val uploadViewModel by viewModels<UploadProgressViewModel>()
    
    companion object {
        const val INTENT_REFERRAL_REDEEM_MSG = "REFERRAL_REDEEM_MSG"
        const val INTENT_PACKAGE_SUBSCRIBED = "PACKAGE_SUBSCRIBED"
    }
    
    override val playlistManager: PlaylistManager
        get() = viewModel.getPlaylistManager()
    
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        FirebaseInAppMessaging.getInstance().setMessagesSuppressed(false)
        
        val isDisableScreenshot = (
            mPref.screenCaptureEnabledUsers.contains(cPref.deviceId)
            || mPref.screenCaptureEnabledUsers.contains(mPref.customerId.toString())
            || mPref.screenCaptureEnabledUsers.contains(mPref.phoneNumber)
        ).not()
        
        //disable screen capture
        if (isDisableScreenshot) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE
            )
        }
        cPref.isAlreadyForceLoggedOut = false
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.tbar.toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        
        setupNavController()
        initializeDraggableView()
        initDrawer()
        initLandingPageFragmentAndListenBackStack()
        showRedeemMessageIfPossible()
        
        ToffeeAnalytics.logUserProperty(
            mapOf(
                "userId" to mPref.customerId.toString(),
                "user_type" to mPref.isBanglalinkNumber,
                "app_version" to BuildConfig.VERSION_CODE.toString()
            )
        )
        
        binding.uploadButton.setOnClickListener {
            ToffeeAnalytics.logEvent(ToffeeEvents.UPLOAD_CLICK)
            checkVerification {
                checkChannelDetailAndUpload()
            }
        }
        viewModel.getVastTag()
        val mqttClientId = try { EncryptionUtil.decryptResponse(mPref.mqttClientId) } catch (e: Exception) { "" }
        if (mqttClientId.isBlank() || mqttClientId.substringBefore("_") != mPref.phoneNumber) {
            mPref.mqttHost = ""
            mPref.mqttClientId = ""
            mPref.mqttUserName = ""
            mPref.mqttPassword = ""
        }
        observe(viewModel.playContentLiveData) {
//            resetPlayer()
            onDetailsFragmentLoad(it)
        }
        observe(mPref.sessionTokenLiveData) {
            if (binding.draggableView.visibility == View.VISIBLE) {
                updateStartPosition()//we are saving the player start position so that we can start where we left off for VOD.
                reloadChannel()
            }
        }
        observe(mPref.viewCountDbUrlLiveData) {
            if (it.isNotEmpty()) {
                viewModel.populateViewCountDb(it)
            }
        }
        observe(mPref.reactionStatusDbUrlLiveData) {
            if (it.isNotEmpty()) {
                viewModel.populateReactionStatusDb(it)
            }
        }
        observe(mPref.subscriberStatusDbUrlLiveData) {
            if (it.isNotEmpty()) {
                viewModel.populateSubscriptionCountDb(it)
            }
        }
        observe(mPref.shareCountDbUrlLiveData) {
            if (it.isNotEmpty()) {
                viewModel.populateShareCountDb(it)
            }
        }
        observe(mPref.forceLogoutUserLiveData) {
            if (it) {
                mPref.clear()
                UploadService.stopAllUploads()
                launchActivity<SplashScreenActivity>()
                finish()
            }
        }
        observe(viewModel.addToPlayListMutableLiveData) { item ->
            setPlayList(item)
        }
        observe(mPref.shareableUrlLiveData) {
            handleDeepLink(it)
        }
        observe(viewModel.shareContentLiveData) { channelInfo ->
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            sharingIntent.putExtra(
                Intent.EXTRA_TEXT, channelInfo.video_share_url
            )
            startActivity(Intent.createChooser(sharingIntent, "Share via"))
            viewModel.sendShareLog(channelInfo)
        }
        observe(viewModel.shareUrlLiveData) {
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            sharingIntent.putExtra(
                Intent.EXTRA_TEXT, it
            )
            startActivity(Intent.createChooser(sharingIntent, "Share via"))
        }
        if (!isChannelComplete() && mPref.isVerifiedUser) {
            viewModel.getChannelDetail(mPref.customerId)
            observe(profileViewModel.loadCustomerProfile()) {
                if (it is Success) {
                    profileViewModel.profileForm.value = it.data
                }
            }
        }
        if (intent.hasExtra(INTENT_PACKAGE_SUBSCRIBED)) {
            handlePackageSubscribe()
        }
        if (mPref.isVerifiedUser && mPref.mqttIsActive) {
            initMqtt()
        }
        observe(mPref.loginDialogLiveData) {
            if (it) {
                navController.navigate(R.id.loginDialog)
            }
        }
        observe(mPref.messageDialogLiveData) { message ->
            ToffeeAlertDialogBuilder(this, title = "Notice", text = message, positiveButtonListener = {
                it?.dismiss()
            }).create().show()
        }
        initSideNav()
        lifecycle.addObserver(heartBeatManager)
        observeInAppMessage()
        handleSharedUrl(intent)
        configureBottomSheet()
        observeUpload2()
        watchConnectionChange()
        observeMyChannelNavigation()
        inAppUpdate()
        customCrashReport()
        observe(mPref.shareableHashLiveData) { pair ->
            pair.first?.let { observeShareableContent(it, pair.second) }
        }
        if (mPref.isFireworkActive) {
            viewModel.isFireworkActive.postValue(true)
        }
        if (mPref.isMedalliaActive) {
            MedalliaDigital.enableIntercept()
        }
        if (mPref.isConvivaActive) {
            initConvivaSdk()
        }

        val isAnyNativeSectionActive= mPref.nativeAdSettings.value?.find {
           it.isActive
        }?.isActive ?: false
        
        if (isAnyNativeSectionActive && mPref.isNativeAdActive) {
//            val testDeviceIds = listOf("33D01C3F0C238BE4407EB453A72FA7E4", "09B67C1ED8519418B65ECA002058C882")
//            val configuration =
//                RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
//            MobileAds.setRequestConfiguration(configuration)
            MobileAds.initialize(this)
        }
//        showDeviceId()
    }
    
    private fun initConvivaSdk() {
        runCatching {
            if (BuildConfig.DEBUG) {
                val settings: Map<String, Any> = mutableMapOf(
                    ConvivaSdkConstants.GATEWAY_URL to getString(R.string.convivaGatewayUrl),
                    ConvivaSdkConstants.LOG_LEVEL to ConvivaSdkConstants.LogLevel.DEBUG
                )
                ConvivaAnalytics.init(applicationContext, getString(R.string.convivaCustomerKeyTest), settings)
            } else {
                ConvivaAnalytics.init(applicationContext, getString(R.string.convivaCustomerKeyProd))
            }
            ConvivaHelper.init(applicationContext, true)
        }
    }
    
    private fun showDeviceId() {
        ToffeeAlertDialogBuilder(this, title = "Device Id", text = cPref.deviceId, positiveButtonTitle = "copy", positiveButtonListener = {
            val clipboard: ClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText("DeviceId", cPref.deviceId)
            clipboard.setPrimaryClip(clip)
            showToast("copied to clipboard")
            it?.dismiss()
        }, negativeButtonTitle = "Close", negativeButtonListener = { it?.dismiss() }).create().show()
    }

    private fun isChannelComplete() = mPref.customerName.isNotBlank()
            && mPref.customerEmail.isNotBlank()
            && mPref.customerAddress.isNotBlank()
            && mPref.customerDOB.isNotBlank()
            && mPref.customerNID.isNotBlank()
            && mPref.channelName.isNotBlank()
            && mPref.channelLogo.isNotBlank()
            && mPref.isChannelDetailChecked
    
    private fun customCrashReport() {
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory()
        FirebaseCrashlytics.getInstance().setCustomKey("heap_size", "$maxMemory")
    }
    
    private fun initMqtt() {
        if (mPref.mqttHost.isBlank() || mPref.mqttClientId.isBlank() || mPref.mqttUserName.isBlank() || mPref.mqttPassword.isBlank()) {
            observe(viewModel.mqttCredentialLiveData) {
                when (it) {
                    is Success -> {
                        it.data?.let { data ->
                            mPref.mqttIsActive = data.mqttIsActive == 1
                            mPref.mqttHost = EncryptionUtil.encryptRequest(data.mqttUrl)
                            mPref.mqttClientId = EncryptionUtil.encryptRequest(data.mqttUserId)
                            mPref.mqttUserName = EncryptionUtil.encryptRequest(data.mqttUserId)
                            mPref.mqttPassword = EncryptionUtil.encryptRequest(data.mqttPassword)
                            
                            appScope.launch {
                                val mqttDir = withContext(Dispatchers.IO + Job()) {
                                    val mqttTag = "MqttConnection"
                                    var tempDir = getExternalFilesDir(mqttTag)
                                    if (tempDir == null) {
                                        tempDir = getDir(mqttTag, Context.MODE_PRIVATE)
                                    }
                                    tempDir
                                }
                                if (mPref.mqttIsActive && mqttDir != null) {
                                    mqttService.initialize()
                                }
                            }
                        }
                    }
                    is Failure -> {
                        Log.e("MQTT_", "onCreate: ${it.error.msg}")
                        ToffeeAnalytics.logEvent(
                            ToffeeEvents.EXCEPTION, bundleOf(
                                "api_name" to ApiNames.LOGIN_BY_PHONE_NO,
                                FirebaseParams.BROWSER_SCREEN to "Enter OTP",
                                "error_code" to it.error.code,
                                "error_description" to it.error.msg
                            )
                        )
                    }
                }
            }
            viewModel.getMqttCredential()
        } else {
            mqttService.initialize()
        }
    }
    
    private val appUpdateListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.INSTALLED) {
            showToast("Toffee updated successfully")
        }
    }
    
    private fun inAppUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask: Task<AppUpdateInfo> = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            val updateType = if (mPref.shouldForceUpdate(BuildConfig.VERSION_CODE)) AppUpdateType.IMMEDIATE else AppUpdateType.FLEXIBLE
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(updateType)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo, updateType, this, IN_APP_UPDATE_REQUEST_CODE
                    )
                } catch (e: SendIntentException) {
                    e.printStackTrace()
                    ToffeeAnalytics.logException(e)
                }
            }
        }
        appUpdateManager.registerListener(appUpdateListener)
    }
    
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IN_APP_UPDATE_REQUEST_CODE) {
            Log.i(TAG, "Start Download")
            if (resultCode != RESULT_OK) {
                Log.i(TAG, "Download Failed")
            }
        }
    }
    
    fun checkChannelDetailAndUpload() {
        if (!mPref.isChannelDetailChecked) {
            observe(viewModel.myChannelDetailResponse) {
                when (it) {
                    is Success -> showUploadDialog()
                    is Failure -> {
                        ToffeeAnalytics.logEvent(
                            ToffeeEvents.EXCEPTION, bundleOf(
                                "api_name" to ApiNames.GET_MY_CHANNEL_DETAILS,
                                FirebaseParams.BROWSER_SCREEN to "My Channel page",
                                "error_code" to it.error.code,
                                "error_description" to it.error.msg
                            )
                        )
                        showToast(getString(R.string.unable_to_load_data))
                    }
                }
            }
            viewModel.getChannelDetail(mPref.customerId)
        } else {
            showUploadDialog()
        }
    }
    
    private fun showUploadDialog(): Boolean {
        if (isChannelComplete()) {
            if (navController.currentDestination?.id == R.id.uploadMethodFragment) {
                navController.popBackStack()
                return true
            }
            lifecycleScope.launch {
                if (uploadRepo.getUnFinishedUploadsList().isNotEmpty()) {
                    return@launch
                }
                navController.navigate(R.id.uploadMethodFragment)
            }
        } else {
            if (navController.currentDestination?.id == R.id.bottomSheetUploadFragment) {
                navController.popBackStack()
                return true
            }
            lifecycleScope.launch {
                if (uploadRepo.getUnFinishedUploadsList().isNotEmpty()) {
                    return@launch
                }
                if (navController.currentDestination?.id == R.id.myChannelEditDetailFragment) {
                    navController.popBackStack()
                }
                navController.navigate(R.id.bottomSheetUploadFragment)
            }
        }
        return false
    }
    
    private fun watchConnectionChange() {
        lifecycleScope.launch {
            uploadManager.checkUploadStatus(false)
        }
        lifecycleScope.launch {
            connectionWatcher.watchNetwork().collect {
                if (it) {
                    uploadManager.checkUploadStatus(true)
                }
            }
        }
    }
    
    override fun getPlayerView(): StyledPlayerView = binding.playerView
    
    private fun configureBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.homeBottomSheet.bottomSheet)
        if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            binding.homeBottomSheet.bottomSheet.hide()
        } else {
            binding.homeBottomSheet.bottomSheet.show()
        }
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED && !binding.playerView.isControllerFullyVisible) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                }
            }
            
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.playerView.moveController(slideOffset)
            }
        })

//        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
//            if(insets.hasInsets()) {
//                Log.e("INSET_T", "Has inset")
//                val isFullScreen = requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
//                if(!isFullScreen) {
//                    updateFullScreenState()
//                }
//                WindowInsetsCompat.CONSUMED
//            } else {
//                insets
//            }
//        }
//        window.decorView.setOnSystemUiVisibilityChangeListener {
////            toggleNavigations(it and View.SYSTEM_UI_FLAG_FULLSCREEN == View.SYSTEM_UI_FLAG_FULLSCREEN)
//            val isFullScreen = it and View.SYSTEM_UI_FLAG_FULLSCREEN == View.SYSTEM_UI_FLAG_FULLSCREEN
//            if(!isFullScreen) {
//                updateFullScreenState()
//            }
//        }
    }
    
    private fun observeNotification() {
        lifecycleScope.launchWhenStarted {
            notificationRepo.getUnseenNotificationCount().collect {
                if (it > 0) {
                    notificationBadge?.visibility = View.VISIBLE
                } else {
                    notificationBadge?.visibility = View.GONE
                }
            }
        }
    }
    
    fun rotateFab(isRotate: Boolean) {
        ViewCompat.animate(binding.uploadButton).rotation(if (isRotate) 135.0F else 0.0F).withEndAction {
            if (isRotate) {
                val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.menuColorSecondaryDark))
                binding.uploadButton.backgroundTintList = colorStateList
                binding.uploadButton.imageTintList = colorStateList
            } else {
                val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorAccent2))
                binding.uploadButton.backgroundTintList = colorStateList
                binding.uploadButton.imageTintList = colorStateList
            }
        }.withLayer().setDuration(300L).setInterpolator(AccelerateInterpolator()).start()
    }
    
    fun getNavController() = navController
    
    fun getHomeViewModel() = viewModel
    
    private val destinationChangeListener = NavController.OnDestinationChangedListener { controller, _, _ ->
        if (binding.draggableView.isMaximized()) {
            minimizePlayer()
        }
        closeSearchBarIfOpen()
        
        // For firebase screenview logging
        if (controller.currentDestination is FragmentNavigator.Destination) {
            val currentFragmentClassName = (controller.currentDestination as FragmentNavigator.Destination).className.substringAfterLast(".")
            
            ToffeeAnalytics.logEvent(
                FirebaseAnalytics.Event.SCREEN_VIEW, bundleOf(
                    FirebaseAnalytics.Param.SCREEN_CLASS to currentFragmentClassName
                )
            )
        }
        
        binding.tbar.toolbar.setNavigationIcon(R.drawable.ic_toffee)
    }
    
    private fun setupNavController() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.home_nav_host) as NavHostFragment
        navController = navHostFragment.navController
        
        appbarConfig = AppBarConfiguration(
            setOf(
                R.id.menu_feed,
                R.id.menu_tv,
                R.id.menu_activities,
                R.id.menu_channel,
//                R.id.menu_all_tv_channel,
                R.id.menu_favorites,
                R.id.menu_settings,
                R.id.menu_subscriptions,
                R.id.menu_invite,
                R.id.menu_redeem,
                R.id.menu_creators_policy,
            ), binding.drawerLayout
        )
//        setupActionBarWithNavController(navController, appbarConfig)
//        NavigationUI.setupActionBarWithNavController(this, navController, appbarConfig)
        binding.tbar.toolbar.setupWithNavController(navController, appbarConfig)
        binding.tbar.toolbar.setNavigationIcon(R.drawable.ic_toffee)
        binding.sideNavigation.setupWithNavController(navController)
        binding.tabNavigator.setupWithNavController(navController)
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomAppBar) { _, _ ->
            WindowInsetsCompat.CONSUMED
        }
        
        binding.sideNavigation.setNavigationItemSelectedListener {
            drawerHelper.handleMenuItemById(it)
        }
        
        navController.addOnDestinationChangedListener(destinationChangeListener)
        binding.tabNavigator.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.menu_feed -> {
                    navController.popBackStack(R.id.menu_feed, true)
                    navController.navigate(R.id.menu_feed)
                }
                R.id.menu_tv -> navController.navigate(R.id.menu_tv) 
                R.id.menu_explore -> navController.navigate(R.id.menu_explore) 
                R.id.menu_channel -> navController.navigate(R.id.menu_channel)
            }
            return@setOnItemSelectedListener true
        }
//        binding.sideNavigation.setNavigationItemSelectedListener {
//            binding.drawerLayout.closeDrawers()
//            return@setNavigationItemSelectedListener false
//        }
    }
    
    override fun onResume() {
        super.onResume()
        binding.playerView.setPlaylistListener(this)
        binding.playerView.addPlayerControllerChangeListener(this)
        setPlayerInPlayerView()
        binding.playerView.resizeView(calculateScreenWidth())
        updateFullScreenState()
    }
    
    override fun setPlayerInPlayerView() {
        binding.playerView.player = player
        if (player is CastPlayer) {
            val deviceName = castContext?.sessionManager?.currentCastSession?.castDevice?.friendlyName
            binding.playerView.showCastingText(true, deviceName)
        } else {
            binding.playerView.showCastingText(false)
        }
    }
    
    override fun onPause() {
        super.onPause()
        binding.playerView.clearListeners()
        if (Util.SDK_INT <= 23) {
            binding.playerView.player = null
        }
    }
    
    override fun onStart() {
        super.onStart()
        playerEventHelper.appForegrounded("app foregrounded")
        if (playlistManager.getCurrentChannel() != null) {
            ConvivaAnalytics.reportAppForegrounded()
            maximizePlayer()
            loadDetailFragment(
                if (playlistManager.playlistId >= 0) {
                    PlaylistItem(playlistManager.playlistId, playlistManager.getCurrentChannel()!!)
                } else {
                    playlistManager.getCurrentChannel()!!
                }
            )
        }
    }
    
    override fun resumeCastSession(info: ChannelInfo) {
        maximizePlayer()
        loadDetailFragment(info)
    }
    
    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            binding.playerView.player = null
        }
        playerEventHelper.appBackgrounded("app backgrounded")
        ConvivaAnalytics.reportAppBackgrounded()
    }
    
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        /*
        viewDragHelper send onViewMinimize/Maximize event when start the transition
        it's not possible to get transition(animation) end listener
        If phone is already in landscape mode, it starts to move to full screen while drag transition is on going
        so player can't reset scale completely. Manually resetting player scale value
         */
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (playlistManager.getCurrentChannel()?.isLinear == true) {
                binding.homeBottomSheet.bottomSheet.visibility = View.VISIBLE
                if (binding.playerView.isControllerVisible()) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            } else {
                binding.homeBottomSheet.bottomSheet.visibility = View.GONE
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
            binding.playerView.isFullScreen = true
            binding.playerView.scaleX = 1f
            binding.playerView.scaleY = 1f
        } else {
            binding.playerView.isFullScreen = false
            binding.homeBottomSheet.bottomSheet.visibility = View.GONE
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            binding.playerView.moveController(-1.0f)
        }
        updateFullScreenState()
    }
    
    override fun channelCannotBePlayedDueToSettings() {
        binding.playerView.showWifiOnlyMessage()
    }
    
    override fun onContentExpired() {
        binding.playerView.showContentExpiredMessage()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appbarConfig) || super.onSupportNavigateUp()
    }
    
    private fun updateFullScreenState() {
        val state = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE || binding.playerView.isFullScreen
        binding.playerView.onFullScreen(state)
        binding.playerView.resizeView(calculateScreenWidth(), state)
        setFullScreen(state)
        toggleNavigation(state)
//        Utils.setFullScreen(this, state)// || binding.playerView.channelType != "LIVE")
    }
    
    private fun setFullScreen(visible: Boolean) {
        if (visible) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(window, window.decorView).let { controller ->
                controller.hide(
                    WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars() or WindowInsetsCompat.Type.displayCutout()
                )
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                window.attributes = window.attributes.apply {
//                    layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
//                }
//            }
        } else {
            WindowCompat.setDecorFitsSystemWindows(window, true)
            WindowInsetsControllerCompat(window, window.decorView).let { controller ->
                controller.show(
                    WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars() or WindowInsetsCompat.Type.displayCutout()
                )
            }
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                window.attributes = window.attributes.apply {
//                    layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
//                }
//            }
        }
    }
    
    private fun toggleNavigation(state: Boolean) {
        if (state) {
            supportActionBar?.hide()
            binding.bottomAppBar.hide()
            binding.uploadButton.hide()
            binding.mainUiFrame.visibility = View.GONE
        } else {
            binding.mainUiFrame.visibility = View.VISIBLE
            supportActionBar?.show()
            binding.bottomAppBar.show()
            binding.uploadButton.show()
        }
    }
    
    override fun onTrackerDialogDismissed() {
        updateFullScreenState()
    }
    
    private fun calculateScreenWidth(): Point {
        return Utils.getRealScreenSize(this)
    }
    
    private fun handleSharedUrl(intent: Intent) {
        lifecycleScope.launch {
            var appLinkUriStr: String? = null
            try {
                val appLinkUri = AppLinks.getTargetUrlFromInboundIntent(this@HomeActivity, intent)
                if (appLinkUri != null && appLinkUri.host != "toffeelive.com") {
                    appLinkUriStr = viewModel.fetchRedirectedDeepLink(appLinkUri.toString())
                }
            } catch (ex: Exception) {
                ToffeeAnalytics.logException(ex)
            }
            if (!appLinkUriStr.isNullOrEmpty()) {
                handleDeepLink(appLinkUriStr)
            } else {
                val uri = intent.data
                if (uri != null) {
                    val decodedUrl = runCatching {
                        URLDecoder.decode(uri.toString().trim(), "UTF-8")
                    }.getOrElse {
                        uri.toString().trim().replace("%3A", ":").replace("%2F", "/").replace("%23", "#")
                    }.replace(" ", "+")
                    handleDeepLink(decodedUrl)
                }
            }
            val id = intent.getLongExtra(ROW_ID, 0L)
            if (id != 0L) {
                notificationRepo.updateSeenStatus(id, true, System.currentTimeMillis())
            }
        }
    }
    
    private fun handleDeepLink(decodedUrl: String) {
        val url = prepareWebsiteDeepLink(decodedUrl)
        lifecycleScope.launch {
            try {
                if (!handleInAppDeepLink(url)) {
                    ToffeeAnalytics.logBreadCrumb("Trying to open individual item")
                    val hash = url.substringAfter("#video/")
                    var pair: Pair<String?, String?>? = null
                    if (hash.contains("data=", true)) {
                        val newHash = hash.substringAfter("data=").trim()
                        val encriptedurl =EncryptionUtil.decryptResponse(newHash).trimIndent()
                        shareableData = gson.fromJson(encriptedurl, ShareableData::class.java)
                        when(shareableData?.type) {
                            SharingType.STINGRAY.value -> {
                                if (!shareableData?.stingrayShareUrl.isNullOrBlank()) {
                                    pair = Pair(shareableData?.stingrayShareUrl, SharingType.STINGRAY.value)
                                }
                            }
                            SharingType.CATEGORY.value -> {
                                shareableData?.categoryId?.let {
                                    val categoryDeepLinkUrl = "https://toffeelive.com?routing=internal&page=categories&catid=$it"
                                    handleInAppDeepLink(categoryDeepLinkUrl)
                                    viewModel.sendCategoryChannelShareLog(shareableData!!.type!!, it, decodedUrl)
                                }
                            }
                            SharingType.CHANNEL.value -> {
                                shareableData?.channelId?.let {
                                    val channelDeepLinkUrl = "https://toffeelive.com?routing=internal&page=ugc_channel&owner_id=$it"
                                    handleInAppDeepLink(channelDeepLinkUrl)
                                    viewModel.sendCategoryChannelShareLog(shareableData!!.type!!, it, decodedUrl)
                                }
                            }
                            SharingType.PLAYLIST.value -> {
                                playlistShareableUrl = url
                                playPlaylistShareable()
                            }
                            SharingType.SERIES.value -> {
                                webSeriesShareableUrl = url
                                playShareableWebSeries()
                            }
                        }
                    } else {
                        pair = Pair(hash, null)
                    }
                    mPref.shareableHashLiveData.value = pair
                }
            } catch (e: Exception) {
                ToffeeAnalytics.logBreadCrumb("2. Failed to handle depplink $url")
                ToffeeAnalytics.logException(e)
            }
        }
    }
    
    private fun prepareWebsiteDeepLink(url: String): String {
        val categoryDeepLink = "https://toffeelive.com?routing=internal&page=categories&catid=categoryId"
        val ugcChannelDeepLink = "https://toffeelive.com?routing=internal&page=ugc_channel&owner_id=channelId"
        val commonDeepLink = "https://toffeelive.com?routing=internal&page=pagelink"
        return when {
            url.contains("channel/") -> {
                val ownerId = url.substringAfter("channel/").trim()
                ugcChannelDeepLink.replace("channelId", ownerId)
            }
            url.contains("category/") -> {
                val categoryId = url.substringAfter("category/").substringBefore("/").trim()
                categoryDeepLink.replace("categoryId", categoryId)
            }
            url.contains("movies/") -> {
                categoryDeepLink.replace("categoryId", CategoryType.MOVIE.value.toString())
            }
            url.contains("web-series/") -> {
                categoryDeepLink.replace("categoryId", CategoryType.DRAMA_SERIES.value.toString())
            }
            url.contains("live-tv/") -> {
                commonDeepLink.replace("pagelink", "tv_channels")
            }
            url.contains("explore/") -> {
                commonDeepLink.replace("pagelink", "explore")
            }
            url.contains("/all-drama") -> {
                categoryDeepLink.replace("categoryId", "18")
            }
            url.contains("/activities") -> {
                commonDeepLink.replace("pagelink", "activities")
            }
            url.contains("/my-favorite") -> {
                commonDeepLink.replace("pagelink", "favorites")
            }
            url.contains("subscription/") -> {
                commonDeepLink.replace("pagelink", "subscription")
            }
            url.contains("/home") -> {
                commonDeepLink.replace("pagelink", "home")
            }
            url.contains("/playlist-content") -> {
                getWebPlaylistShare(url)
            }
            else -> url
        }
    }
    
    private fun getWebPlaylistShare(url:String):String{
        val ownerId = url.substringAfter("owner_id=").substringBefore("&").toInt()
        val isOwner= if(ownerId==mPref.customerId) 1 else 0
        val playlistId = url.substringAfter("pl_id=").substringBefore("&").toInt()
        val playlistName = url.substringAfter("name=").substringBefore("&")
        val newUrl ="https://toffeelive.com/#video/data="
        
        val sharableData = ShareableData("playlist",0,null,null,
        0, isOwner, ownerId, playlistId, playlistName)
        val json = gson.toJson(sharableData,ShareableData::class.java)
        val shareableJsonData = EncryptionUtil.encryptRequest(json).trimIndent()
        return newUrl+shareableJsonData
    }
    
    private fun playPlaylistShareable() {
        observe(viewModel.playlistShareableLiveData) { response ->
            when (response) {
                is Success -> {
                    if (shareableData != null) {
                        response.data.channels?.let {
                            val playlistInfo = PlaylistPlaybackInfo(
                                shareableData?.playlistId ?: 0,
                                shareableData?.channelOwnerId ?: 0,
                                shareableData?.name ?: response.data.name ?: "" ,
                                response.data.totalCount,
                                playlistShareableUrl,
                                1,
                                shareableData?.isUserPlaylist == 1,
                                0,
                                it[0],
                                shareableData?.isOwner ?: 1,
                                true,
                            )
                            viewModel.addToPlayListMutableLiveData.postValue(
                                AddToPlaylistData(playlistInfo.getPlaylistIdLong(), it)
                            )
                            if (shareableData?.isUserPlaylist == 1) {
                                cacheManager.clearCacheByUrl(ApiRoutes.GET_USER_PLAYLIST_VIDEOS)
                            } else {
                                cacheManager.clearCacheByUrl(ApiRoutes.GET_MY_CHANNEL_PLAYLIST_VIDEOS)
                            }
                            viewModel.playContentLiveData.postValue(playlistInfo)
                        } ?: showToast("This playlist does not have any video")
                    } else {
                        showToast("Something went wrong")
                    }
                }
                is Failure -> {
                    ToffeeAnalytics.logEvent(
                        ToffeeEvents.EXCEPTION, bundleOf(
                            "api_name" to ApiNames.GET_PLAYLIST_SHAREABLE,
                            FirebaseParams.BROWSER_SCREEN to BrowsingScreens.HOME_PAGE,
                            "error_code" to response.error.code,
                            "error_description" to response.error.msg
                        )
                    )
                }
            }
        }
        shareableData?.let { viewModel.getPlaylistShareableVideos(it) }
    }
    
    private fun playShareableWebSeries() {
        observe(viewModel.webSeriesShareableLiveData) { response ->
            when (response) {
                is Success -> {
                    if (shareableData != null) {
                        response.data.channels?.let {
                            val seriesInfo = SeriesPlaybackInfo(
                                shareableData?.serialSummaryId ?: 0,
                                shareableData?.name ?: "",
                                shareableData?.seasonNo ?: 1,
                                shareableData?.activeSeason?.size ?: 0,
                                shareableData?.activeSeason,
                                webSeriesShareableUrl,
                                it[0].id.toInt(),
                                it[0],
                            )
                            viewModel.addToPlayListMutableLiveData.postValue(
                                AddToPlaylistData(seriesInfo.playlistId(), it)
                            )
                            viewModel.playContentLiveData.postValue(seriesInfo)
                        }
                    } else {
                        showToast("Something went wrong")
                    }
                }
                is Failure -> {
                    ToffeeAnalytics.logEvent(
                        ToffeeEvents.EXCEPTION, bundleOf(
                            "api_name" to ApiNames.GET_WEB_SERIES_BY_SEASON,
                            FirebaseParams.BROWSER_SCREEN to BrowsingScreens.HOME_PAGE,
                            "error_code" to response.error.code,
                            "error_description" to response.error.msg
                        )
                    )
                }
            }
        }
        shareableData?.let { viewModel.getShareableEpisodesBySeason(it) }
    }
    
    private suspend fun handleInAppDeepLink(url: String): Boolean {
        var isDeepLinkHandled = false
        val route = inAppMessageParser.parseUrlV2(url)
        route?.let {
            ToffeeAnalytics.logBreadCrumb("Trying to open ${it.name}")
            when (it.destId) {
                is Uri -> navController.navigate(it.destId, it.options, it.navExtra)
                is Int -> {
                    if(it.destId==R.id.menu_favorites
                        || it.destId==R.id.menu_activities
                        || it.destId==R.id.menu_subscriptions){
                        checkVerification {
                            navController.navigate(it.destId, it.args, it.options, it.navExtra)
                        }
                    }else{
                        navController.navigate(it.destId, it.args, it.options, it.navExtra)
                    }
                }
            }
            isDeepLinkHandled = true
        }
        return isDeepLinkHandled
    }
    
    private fun observeShareableContent(hash: String, type: String? = null) {
        observe(viewModel.getShareableContent(hash, type)) { channelResource ->
            if (channelResource is Success) {
                channelResource.data?.let {
                    onDetailsFragmentLoad(it)
                }
                mPref.shareableHashLiveData.value = null
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            query?.let { handleVoiceSearchEvent(it) }
        }
        if (intent.hasExtra(INTENT_PACKAGE_SUBSCRIBED)) {
            handlePackageSubscribe()
        }
        if (intent.hasExtra(ROW_ID)) {
            val actionName = intent.getIntExtra(ACTION_NAME, DISMISS)
            val pubSubId = intent.getStringExtra(PUB_SUB_ID) ?: "0"
            val notificationId = intent.getIntExtra(NOTIFICATION_ID, -1)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getSystemService(NotificationManager::class.java)
            } else {
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }.cancel(notificationId)
            
            if (actionName == CONTENT_VIEW || actionName == WATCH_NOW) {
                PubSubMessageUtil.sendNotificationStatus(pubSubId, PUBSUBMessageStatus.OPEN)
            }
        }
//        try {
//            val url = intent.data?.fragment?.takeIf { it.contains("fwplayer=") }?.removePrefix("fwplayer=")
//            url?.let {
//                FwSDK.play(it)
//                return
//            }
//        } catch (e: Exception) {
//            Log.e("FwSDK", "FireworkDeeplinkPlayException")
//        }
        handleSharedUrl(intent)
    }
    
    private fun navigateToSearch(query: String?) {
        ToffeeAnalytics.logEvent(
            ToffeeEvents.SEARCH, bundleOf("search_query" to query)
        )
        navController.popBackStack(R.id.searchFragment, true)
//        navController.navigate(Uri.parse("app.toffee://search/$query"))
        navController.navigate(R.id.searchFragment, Bundle().apply {
            putString(SearchFragment.SEARCH_KEYWORD, query)
        })
    }
    
    private fun handleVoiceSearchEvent(query: String) {
        if (!TextUtils.isEmpty(query)) {
            navigateToSearch(query)
        }
        if (searchView != null) {
            searchView!!.setQuery(query.lowercase(), false)
            searchView!!.clearFocus()
        }
    }
    
    private fun handlePackageSubscribe() {
        //Clean up stack upto landingPageFragment inclusive
        supportFragmentManager.popBackStack(
            LandingPageFragment::class.java.name, POP_BACK_STACK_INCLUSIVE
        )
    }
    
    private fun loadChannel(channelInfo: ChannelInfo) {
        viewModel.sendViewContentEvent(channelInfo)
        if (channelInfo.isLinear) {
            viewModel.addTvChannelToRecent(channelInfo)
            allChannelViewModel.selectedChannel.postValue(channelInfo)
        }
        addChannelToPlayList(channelInfo)
    }
    
    private fun loadPlayListItem(playbackInfo: PlaylistPlaybackInfo) {
        playIndex(playbackInfo.playIndex)
        playlistManager.getCurrentChannel()?.let {
            viewModel.sendViewContentEvent(it)
        }
    }
    
    private fun loadDramaSeasonInfo(seasonInfo: SeriesPlaybackInfo) {
        playChannelId(seasonInfo.channelId)
        playlistManager.getCurrentChannel()?.let {
            viewModel.sendViewContentEvent(it)
        }
    }
    
    private fun onDetailsFragmentLoad(detailsInfo: Any?) {
        val channelInfo = when (detailsInfo) {
            is ChannelInfo -> {
                detailsInfo
            }
            is PlaylistPlaybackInfo -> {
                detailsInfo.currentItem
            }
            is SeriesPlaybackInfo -> {
                detailsInfo.currentItem
            }
            else -> null
        }
        MedalliaDigital.disableIntercept()
        channelInfo?.let {
            when {
                it.urlTypeExt == PAYMENT -> {
                    checkVerification {
                        when {
                            mPref.isPaidUser -> playInNativePlayer(detailsInfo, it)
                            it.urlType == PLAY_IN_WEB_VIEW -> playInWebView(it)
                            it.urlType == OPEN_IN_EXTERNAL_BROWSER -> openInExternalBrowser(it)
                        }
                    }
                }
                it.urlType == PLAY_IN_WEB_VIEW && it.urlTypeExt == NON_PAYMENT -> {
                    playInWebView(it)
                }
                it.urlType == OPEN_IN_EXTERNAL_BROWSER && it.urlTypeExt == NON_PAYMENT -> {
                    openInExternalBrowser(it)
                }
                it.urlType == PLAY_IN_NATIVE_PLAYER && it.urlTypeExt == NON_PAYMENT -> {
                    playInNativePlayer(detailsInfo, it)
                }
                it.urlType == STINGRAY_CONTENT && it.urlTypeExt == NON_PAYMENT -> {
                    playInNativePlayer(detailsInfo, it)
                }
            }
        }
    }
    
    private fun playInNativePlayer(detailsInfo: Any?, it: ChannelInfo) {
        ToffeeAnalytics.logEvent(
            ToffeeEvents.CONTENT_CLICK, bundleOf(
                "content_id" to it.id,
                "content_title" to it.program_name,
                "content_category" to it.category,
                "content_partner" to it.content_provider_name,
            )
        )
        if (player is CastPlayer) {
            maximizePlayer()
        }
        ConvivaHelper.endPlayerSession(true)
        playerEventHelper.startContentPlayingSession(it.id)
        if (!isPlayerVisible()) {
            playerEventHelper.startPlayerSession()
        }
        
        when (detailsInfo) {
            is PlaylistPlaybackInfo -> {
                ConvivaHelper.setConvivaVideoMetadata(detailsInfo.currentItem!!, mPref.customerId)
                loadPlayListItem(detailsInfo)
            }
            is SeriesPlaybackInfo -> {
                ConvivaHelper.setConvivaVideoMetadata(detailsInfo.currentItem!!, mPref.customerId)
                loadDramaSeasonInfo(detailsInfo)
            }
            else -> {
                ConvivaHelper.setConvivaVideoMetadata(it, mPref.customerId)
                loadChannel(it)
            }
        }
        loadDetailFragment(detailsInfo)
    }
    
    private fun openInExternalBrowser(it: ChannelInfo) {
        it.getHlsLink()?.let { url ->
            startActivity(
                Intent(
                    Intent.ACTION_VIEW, Uri.parse(url)
                )
            )
        } ?: ToffeeAnalytics.logException(NullPointerException("External browser url is null"))
    }
    
    private fun playInWebView(it: ChannelInfo) {
        it.getHlsLink()?.let { url ->
            viewModel.sendViewContentEvent(it.copy(id = "0"))
            val shareableUrl = if (it.urlType == PLAY_IN_WEB_VIEW && it.urlTypeExt == PAYMENT) it.video_share_url else null
            launchActivity<Html5PlayerViewActivity> {
                putExtra(Html5PlayerViewActivity.CONTENT_URL, url)
                putExtra(Html5PlayerViewActivity.SHAREABLE_URL, shareableUrl)
            }
        } ?: ToffeeAnalytics.logException(NullPointerException("External browser url is null"))
    }
    
    override fun playNext() {
        super.playNext()
        if (playlistManager.playlistId == -1L) {
            viewModel.playContentLiveData.postValue(playlistManager.getCurrentChannel())
            return
        }
        ConvivaHelper.endPlayerSession()
//        resetPlayer()
        val info = playlistManager.getCurrentChannel()
        playerEventHelper.startContentPlayingSession(info!!.id)
        ConvivaHelper.setConvivaVideoMetadata(info, mPref.customerId)
        loadDetailFragment(
            PlaylistItem(playlistManager.playlistId, playlistManager.getCurrentChannel()!!)
        )
    }
    
    override fun playPrevious() {
        super.playPrevious()
        ConvivaHelper.endPlayerSession()
//        resetPlayer()
        val info = playlistManager.getCurrentChannel()
        playerEventHelper.startContentPlayingSession(info!!.id)
        ConvivaHelper.setConvivaVideoMetadata(info, mPref.customerId)
        loadDetailFragment(
            PlaylistItem(playlistManager.playlistId, playlistManager.getCurrentChannel()!!)
        )
    }
    
//    private fun resetPlayer() {
//        releasePlayer()
//        initializePlayer()
//        setPlayerInPlayerView()
//    }
    
    override fun playIndex(index: Int) {
        super.playIndex(index)
        loadDetailFragment(
            PlaylistItem(playlistManager.playlistId, playlistManager.getCurrentChannel()!!)
        )
    }
    
    override fun playChannelId(channelId: Int) {
        super.playChannelId(channelId)
        loadDetailFragment(
            PlaylistItem(playlistManager.playlistId, playlistManager.getCurrentChannel()!!)
        )
    }
    
    private fun loadDetailFragment(info: Any?) {
        val fragment = supportFragmentManager.findFragmentById(R.id.details_viewer)
        if (info is ChannelInfo) {
            if (info.isStingray) {
                if (fragment !is StingrayChannelFragmentNew) {
                    loadFragmentById(
                        R.id.details_viewer, StingrayChannelFragmentNew()
                    )
                }
            } else if (info.isLive) {
                if (fragment !is ChannelFragmentNew) {
                    loadFragmentById(
                        R.id.details_viewer, ChannelFragmentNew()
                    )
                }
            } else {
                loadFragmentById(
                    R.id.details_viewer, CatchupDetailsFragment.createInstance(info)
                )
            }
        } else if (info is PlaylistPlaybackInfo) {
            when {
                /*(fragment !is MyChannelPlaylistVideosFragment || fragment.getPlaylistId() != info.getPlaylistIdLong()) &&*/ !info.isUserPlaylist -> {
                    loadFragmentById(
                        R.id.details_viewer, MyChannelPlaylistVideosFragment.newInstance(info)
                    )
                }
                /*(fragment !is UserPlaylistVideosFragment || fragment.getPlaylistId() != info.getPlaylistIdLong()) &&*/ info
                .isUserPlaylist -> {
                    loadFragmentById(
                        R.id.details_viewer, UserPlaylistVideosFragment.newInstance(info)
                    )
                }
                fragment is MyChannelPlaylistVideosFragment -> {
                    fragment.setCurrentChannel(info.currentItem)
                }
                fragment is UserPlaylistVideosFragment -> {
                    fragment.setCurrentChannel(info.currentItem)
                }
            }
        } else if (info is PlaylistItem) {
            when (fragment) {
                is MyChannelPlaylistVideosFragment -> {
                    fragment.setCurrentChannel(info.channelInfo)
                }
                is UserPlaylistVideosFragment -> {
                    fragment.setCurrentChannel(info.channelInfo)
                }
                is EpisodeListFragment -> {
                    fragment.setCurrentChannel(info.channelInfo)
                }
            }
        } else if (info is SeriesPlaybackInfo) {
            if (fragment !is EpisodeListFragment || fragment.getSeriesId() != info.seriesId || fragment.getSeasonNo() != info.seasonNo) {
                loadFragmentById(
                    R.id.details_viewer, EpisodeListFragment.newInstance(info)
                )
            } else {
                fragment.setCurrentChannel(info.currentItem)
            }
        }
    }
    
    private fun loadFragmentById(id: Int, fragment: Fragment, tag: String) {
        supportFragmentManager.popBackStack(
            LandingPageFragment::class.java.name, 0
        )
        supportFragmentManager.beginTransaction().replace(id, fragment).addToBackStack(tag).commit()
    }
    
    private fun loadFragmentById(id: Int, fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(id, fragment).commit()
    }
    
    private fun initializeDraggableView() {
        binding.draggableView.addOnPositionChangedListener(this)
        binding.draggableView.addOnPositionChangedListener(binding.playerView)
        binding.draggableView.visibility = View.GONE
        binding.draggableView.isClickable = true
    }
    
    private fun initDrawer() {
        drawerHelper = DrawerHelper(this, mPref, bindingUtil, binding)
        drawerHelper.initDrawer()
    }
    
    private fun initSideNav() {
        if (mPref.isBanglalinkNumber != "true" || !mPref.showBuyInternetForAndroid) {
            val subMenu = binding.sideNavigation.menu.findItem(R.id.ic_menu_internet_packs)
            subMenu?.isVisible = false
        }
        binding.sideNavigation.menu.findItem(R.id.menu_tv).isVisible = mPref.isAllTvChannelMenuEnabled
        if (mPref.isVerifiedUser) {
            val login = binding.sideNavigation.menu.findItem(R.id.menu_login)
            login?.isVisible = false
        } else {
            val logout = binding.sideNavigation.menu.findItem(R.id.menu_logout)
            logout?.isVisible = false
        }
        val sideNav = binding.sideNavigation.menu.findItem(R.id.menu_change_theme)
        sideNav?.let { themeMenu ->
            val isDarkEnabled = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
            if (cPref.appThemeMode == 0) {
                cPref.appThemeMode = if (isDarkEnabled) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
            }
            val parser: XmlPullParser = resources.getXml(R.xml.custom_switch)
            var switch: View? = null
            try {
                parser.next()
                parser.nextTag()
                val attr: AttributeSet = Xml.asAttributeSet(parser)
                switch = SwitchButton(this, attr)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                switch = SwitchMaterial(this)
            } finally {
                themeMenu.actionView = switch
                when (themeMenu.actionView) {
                    is SwitchButton -> {
                        (themeMenu.actionView as SwitchButton).let {
                            val param = LinearLayout.LayoutParams(36.px, 22.px)
                            param.topMargin = 30
                            it.layoutParams = param
                            it.isChecked = isDarkEnabled
                            it.setOnCheckedChangeListener { _, isChecked ->
                                heartBeatManager.triggerEventViewingContentStop()
                                changeAppTheme(isChecked)
                            }
                        }
                    }
                    is SwitchMaterial -> {
                        (themeMenu.actionView as SwitchMaterial).let {
                            it.isChecked = isDarkEnabled
                            it.setOnCheckedChangeListener { _, isChecked ->
                                heartBeatManager.triggerEventViewingContentStop()
                                changeAppTheme(isChecked)
                            }
                        }
                    }
                }
            }
        }
    }
    
    private fun changeAppTheme(isDarkEnabled: Boolean) {
        ConvivaHelper.endPlayerSession()
        ToffeeAnalytics.logEvent(ToffeeEvents.DARK_MODE_THEME)
        if (isDarkEnabled) {
            cPref.appThemeMode = Configuration.UI_MODE_NIGHT_YES
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            cPref.appThemeMode = Configuration.UI_MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
    
    private fun observeInAppMessage() {
        ToffeeAnalytics.logEvent(ToffeeEvents.TRIGGER_INAPP_MESSAGING, null, true)
        FirebaseInAppMessaging.getInstance().triggerEvent(ToffeeEvents.TRIGGER_INAPP_MESSAGING)
    }
    
    private fun initLandingPageFragmentAndListenBackStack() {
        supportFragmentManager.addOnBackStackChangedListener(this)
    }
    
    private fun showRedeemMessageIfPossible() {
        //show referral redeem msg if possible
        val msg = intent.getStringExtra(INTENT_REFERRAL_REDEEM_MSG)
        msg?.let {
            showDisplayMessageDialog(this, it)
        }
    }
    
    override fun onPlayerMinimize() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding.playerView.clearDebugWindow()
    }
    
    override fun onControllerVisible() {
        if (playlistManager.getCurrentChannel()?.isLinear == true && resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        } else {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }
    
    override fun onControllerInVisible() {
        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }
    
    override fun onPlayerMaximize() {
        MedalliaDigital.disableIntercept()
        requestedOrientation =
            if (binding.playerView.isAutoRotationEnabled && !binding.playerView.isVideoPortrait) ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
            else {
                ActivityInfo.SCREEN_ORIENTATION_LOCKED
            }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        observe(mPref.playerOverlayLiveData) {
            if (it?.contentId == "all" || it?.contentId == playlistManager.getCurrentChannel()?.id) {
                it?.let { showPlayerOverlay(it) }
            }
        }
//        showPlayerOverlay()
    }
    
    private fun showPlayerOverlay(playerOverlayData: PlayerOverlayData? = null) {
        lifecycleScope.launch {
            /*val playerOverlayData = Gson().fromJson("""
                    {
                        "id": 234,
                        "content_id": "254896",
                        "function": "notification_on_top_of_player",
                        "timestamp": "2021-03-08 ",
                        "parameters": {
                            "show": ["msisdn", "user_name", "device_id", "user_id", "device_type", "content_id", "public_ip", "location"],
                            "custom_text": "",
                            "bg_color_code": "#77989908",
                            "font_color_code": "",
                            "font_size": "10px",
                            "opacity": "",
                            "position": "floating",
                            "duration": 30,
                            "from_position": [0.0,0.0],
                            "to_position": [1.0,1.0]
                        }
                    }
                """.trimIndent(), PlayerOverlayData::class.java)*/
            binding.playerView.showDebugOverlay(playerOverlayData!!, playlistManager.getCurrentChannel()?.id ?: "")
            
            val debugOverlayView = binding.playerView.getDebugOverLay()
//            debugOverlayView?.parent?.let { 
//                if (it is View) {
//                    it.setBackgroundColor(Color.TRANSPARENT)
//                }
//            }
            if (playerOverlayData.params.position == "floating") {
                debugOverlayView?.let {
                    val observer = object : ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            if (it.measuredWidth > 0) {
                                val viewWidth = it.measuredWidth.toFloat() + (8.px * 2)
                                val viewHeight = it.measuredHeight.toFloat() + (8.px * 2)
                                
                                val playerWidth = binding.playerView.measuredWidth.toFloat()
                                val playerHeight = binding.playerView.measuredHeight.toFloat()
                                
                                val fromPosition = playerOverlayData.params.fromPosition ?: listOf(0.0F, 0.0F)
                                val toPosition = playerOverlayData.params.toPosition ?: listOf(1.0F, 0.0F)
                                
                                val fromPositionX = fromPosition.first().coerceAtMost(1.0F)
                                val fromPositionY = fromPosition.last().coerceAtMost(1.0F)
                                
                                val toPositionX = toPosition.first().coerceAtMost(1.0F)
                                val toPositionY = toPosition.last().coerceAtMost(1.0F)
                                
                                val startX = playerWidth * fromPositionX
                                val startY = playerHeight * fromPositionY
                                
                                val endX = (playerWidth - viewWidth) * toPositionX
                                val endY = (playerHeight - viewHeight) * toPositionY
                                
                                val path = Path().apply {
                                    moveTo(startX, startY)
                                    lineTo(endX, endY)
                                }
                                ObjectAnimator.ofFloat(it, View.X, View.Y, path).apply {
                                    duration = playerOverlayData.params.duration * 1_000
                                    repeatMode = ValueAnimator.REVERSE
                                    repeatCount = 2
                                }.start()
                                it.viewTreeObserver.removeOnGlobalLayoutListener(this)
                            }
                        }
                    }
                    it.viewTreeObserver.addOnGlobalLayoutListener(observer)
                }
            }
        }
    }
    
    override fun onRotationLock(isAutoRotationEnabled: Boolean) {
        if (isAutoRotationEnabled && !binding.playerView.isVideoPortrait) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
            showToast(getString(R.string.auto_rotation_on))
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
            showToast(getString(R.string.auto_rotation_off))
        }
    }
    
    override fun onPlayerDestroy() {
        playerEventHelper.endContentPlayingSession()
        playerEventHelper.endPlayerSession()
        ConvivaHelper.endPlayerSession()
//        releasePlayer()
        if (mPref.isMedalliaActive) {
            MedalliaDigital.enableIntercept()
        }
        allChannelViewModel.selectedChannel.postValue(null)
        clearChannel()
        heartBeatManager.triggerEventViewingContentStop()
        binding.draggableView.animation = AnimationUtils.loadAnimation(this, android.R.anim.fade_out)
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        binding.draggableView.visibility = View.GONE
        binding.draggableView.resetImmediately()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
    
    override fun onDestroy() {
        mqttService.destroy()
        viewModelStore.clear()
        playerEventHelper.release()
        appUpdateManager.unregisterListener(appUpdateListener)
        navController.removeOnDestinationChangedListener(destinationChangeListener)
        ConvivaHelper.release()
        super.onDestroy()
    }
    
    fun handleExitApp() {
        AlertDialog.Builder(this, R.style.AlertDialogTheme)
            .setMessage(String.format(getString(R.string.exit_from_app_msg), getString(R.string.app_name)))
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                observeLogout()
                viewModel.logoutUser()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun observeLogout() {
        observe(viewModel.logoutLiveData) {
            when (it) {
                is Success -> {
                    if (!it.data.verifyStatus) {
                        mPref.mqttHost = ""
                        mPref.phoneNumber = ""
                        mPref.channelName = ""
                        mPref.channelLogo = ""
                        mPref.customerDOB = ""
                        mPref.customerNID = ""
                        mPref.mqttClientId = ""
                        mPref.mqttUserName = ""
                        mPref.mqttPassword = ""
                        mPref.customerName = ""
                        mPref.customerEmail = ""
                        mPref.isPaidUser = false
                        mPref.userImageUrl = null
                        mPref.customerAddress = ""
                        cacheManager.clearAllCache()
                        mPref.isVerifiedUser = false
                        mPref.isChannelDetailChecked = false
                        appScope.launch { favoriteDao.deleteAll() }
                        mqttService.destroy()
                        navController.popBackStack(R.id.menu_feed, false).let {
                            recreate()
                        }
                    }
                }
                is Failure -> {
                    ToffeeAnalytics.logEvent(
                        ToffeeEvents.EXCEPTION, bundleOf(
                            "api_name" to ApiNames.UN_VERIFY_USER,
                            FirebaseParams.BROWSER_SCREEN to BrowsingScreens.HOME_PAGE,
                            "error_code" to it.error.code,
                            "error_description" to it.error.msg
                        )
                    )
                    showToast(it.error.msg)
                }
            }
        }
    }
    
    override fun onDrawerButtonPressed(): Boolean {
        binding.drawerLayout.openDrawer(GravityCompat.END, true)
        return true
    }
    
    override fun onMinimizeButtonPressed(): Boolean {
        binding.draggableView.minimize()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        return true
    }
    
    override fun isVideoPortrait() = binding.playerView.isVideoPortrait
    
    override fun onFullScreenButtonPressed(): Boolean {
        super.onFullScreenButtonPressed()
        requestedOrientation = if (!binding.playerView.isAutoRotationEnabled || binding.playerView.isFullScreen || binding.playerView.isVideoPortrait) {
            ActivityInfo.SCREEN_ORIENTATION_LOCKED
        } else {
            ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        }
        updateFullScreenState()
        return true
    }
    
    override fun onBackStackChanged() {
        if (supportFragmentManager.backStackEntryCount == 0) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(false)
            drawerHelper.toggle.isDrawerIndicatorEnabled = true
        } else if (supportFragmentManager.backStackEntryCount == 1) { //home
            closeSearchBarIfOpen()
        }
    }
    
    private fun closeSearchBarIfOpen() {
        if (searchView?.isIconified == false) {
            searchView?.onActionViewCollapsed()
        }
    }
    
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if(player?.isPlaying == true && Build.VERSION.SDK_INT >= 24 && hasPip()) {
            enterPipMode()
        }
    }
    
    @Suppress("DEPRECATION")
    @RequiresApi(24)
    private fun enterPipMode() {
        toggleNavigation(true)
        maximizePlayer()
        if(Build.VERSION.SDK_INT < 26) {
            enterPictureInPictureMode()
        } else {
            enterPictureInPictureMode(
                PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(binding.playerView.width, binding.playerView.height))
                    .build()
            )
        }
    }
    
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration?) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        pipChanged(isInPictureInPictureMode)
    }
    
    private fun pipChanged(isInPip: Boolean) {
        if(isInPip) {
            toggleNavigation(true)
            binding.draggableView.maximize()
            binding.draggableView.visibility = View.VISIBLE
            maximizePlayer()
        }
        binding.playerView.onPip(isInPip)
    }
    
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        } else if (resources.configuration.orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else if (binding.playerView.isVideoPortrait && binding.playerView.isFullScreenPortrait()) {
            binding.playerView.isFullScreen = false
            updateFullScreenState()
        } else if (binding.draggableView.isMaximized() && binding.draggableView.visibility == View.VISIBLE) {
            if (mPref.isEnableFloatingWindow) {
                minimizePlayer()
            } else {
                destroyPlayer()
            }
        } else if (searchView?.isIconified == false) {
            closeSearchBarIfOpen()
        } else if(player?.isPlaying == true && Build.VERSION.SDK_INT >= 24 && hasPip()) {
            enterPipMode()
        } else {
            super.onBackPressed()
        }
    }
    
    private fun hasPip() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        mPref.isPipEnabled && packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
    } else {
        false
    }
    
    private fun minimizePlayer() {
        binding.draggableView.minimize()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
    
    private fun destroyPlayer() {
        binding.draggableView.destroyView()
        mPref.playerOverlayLiveData.removeObservers(this)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
    
    override fun maximizePlayer() {
        binding.draggableView.maximize()
        binding.draggableView.visibility = View.VISIBLE
//        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
    }
    
    override fun isPlayerVisible(): Boolean {
        return binding.draggableView.isVisible
    }
    
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (mPref.isVerifiedUser) {
            observe(mPref.profileImageUrlLiveData) {
                menu?.findItem(R.id.action_avatar)?.actionView?.findViewById<ImageView>(R.id.view_avatar)?.let { profileImageView ->
                    bindingUtil.bindRoundImage(profileImageView, it)
                }
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                navController.navigate(R.id.menu_feed)
                return true
            }
            R.id.action_avatar -> {
                binding.drawerLayout.openDrawer(GravityCompat.END, true)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        val searchMenuItem = menu.findItem(R.id.action_search)
        val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager
        searchView = searchMenuItem.actionView as SearchView
        searchView?.apply {
            maxWidth = Integer.MAX_VALUE
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            setIconifiedByDefault(true)
        }
        searchView?.setOnCloseListener {
            navController.popBackStack(R.id.searchFragment, true)
            false
        }
        
        val searchBar: LinearLayout = searchView!!.findViewById(R.id.search_bar)
        searchBar.layoutTransition = LayoutTransition()
        
        val mic = searchView!!.findViewById(androidx.appcompat.R.id.search_voice_btn) as ImageView
        mic.setImageResource(R.drawable.ic_menu_microphone)
        
        val close = searchView!!.findViewById(androidx.appcompat.R.id.search_close_btn) as ImageView
        close.setImageResource(R.drawable.ic_close)
        
        val searchIv = searchView!!.findViewById(androidx.appcompat.R.id.search_button) as ImageView
        searchIv.setImageResource(R.drawable.ic_menu_search)
        
        val searchBadgeTv = searchView?.findViewById(androidx.appcompat.R.id.search_badge) as TextView
        searchBadgeTv.background = ContextCompat.getDrawable(this@HomeActivity, R.drawable.ic_menu_search)
        
        val searchAutoComplete: AutoCompleteTextView = searchView!!.findViewById(androidx.appcompat.R.id.search_src_text)
        searchAutoComplete.apply {
            textSize = 18f
            setTextColor(
                ContextCompat.getColor(
                    this@HomeActivity, R.color.searchview_input_text_color
                )
            )
            background = ContextCompat.getDrawable(this@HomeActivity, R.drawable.searchview_input_bg)
            hint = null
            setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_menu_search, 0)
            
            addTextChangedListener { text ->
                val rightIcon = if (text?.length ?: 0 <= 0) R.drawable.ic_menu_search else 0
                if (compoundPaddingRight != rightIcon) {
                    setCompoundDrawablesWithIntrinsicBounds(0, 0, rightIcon, 0)
                }
            }
        }
        
        searchView?.setOnSearchClickListener {
            val searchFrag = supportFragmentManager.currentNavigationFragment
            if (searchFrag is SearchFragment) {
                searchFrag.getSearchString()?.let {
                    searchAutoComplete.setText(it)
                    searchAutoComplete.setSelection(searchAutoComplete.text.length)
                }
            }
        }
        searchView?.setOnQueryTextFocusChangeListener { view, isFocused -> 
            if (isFocused) {
                binding.searchOverlay.show()
            } else {
                binding.searchOverlay.hide()
            }
        }
        val notificationActionView = menu.findItem(R.id.action_notification)?.actionView
        notificationBadge = notificationActionView?.findViewById<TextView>(R.id.notification_badge)
        notificationActionView?.setOnClickListener {
            if (navController.currentDestination?.id != R.id.notificationDropdownFragment) {
                navController.navigate(R.id.notificationDropdownFragment)
            }
        }
        searchView?.setOnQueryTextListener(this)
        
        val awesomeMenuItem = menu.findItem(R.id.action_avatar)
        val awesomeActionView = awesomeMenuItem.actionView
        awesomeActionView.setOnClickListener { binding.drawerLayout.openDrawer(GravityCompat.END, true) }
        
        observeNotification()
        return true
    }
    
    override fun onQueryTextSubmit(query: String?): Boolean {
        if (!query.isNullOrBlank()) {
            navigateToSearch(query)
            return true
        }
        return false
    }
    
    override fun onQueryTextChange(newText: String?): Boolean {
        return false
    }
    
    override fun onMediaItemChanged() {
        super.onMediaItemChanged()
        maximizePlayer()
        onPlayerMaximize()
        if (binding.playerView.isVideoPortrait && resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        updateFullScreenState()
    }
    
    private fun observeUpload2() {
        binding.homeMiniProgressContainer.addUploadInfoButton.setOnClickListener {
            viewModel.myChannelNavLiveData.value = MyChannelNavParams(mPref.customerId)
            binding.homeMiniProgressContainer.root.isVisible = false
        }
        
        binding.homeMiniProgressContainer.closeButton.setOnClickListener {
            lifecycleScope.launch {
                uploadRepo.getActiveUploadsList().let {
                    if (it.isNotEmpty()) {
                        uploadRepo.updateUploadInfo(it[0].apply {
                            this.status = UploadStatus.CLEARED.value
                        })
                    }
                }
            }
        }
        
        lifecycleScope.launchWhenStarted {
            uploadViewModel.getActiveUploadList().collectLatest {
                Log.i("UPLOAD 2", "Collecting ->>> ${it.size}")
                if (it.isNotEmpty()) {
                    binding.homeMiniProgressContainer.root.isVisible = true
                    val upInfo = it[0]
                    when (upInfo.status) {
                        UploadStatus.SUCCESS.value, UploadStatus.SUBMITTED.value -> {
                            binding.homeMiniProgressContainer.miniUploadProgress.progress = 100
                            binding.homeMiniProgressContainer.addUploadInfoButton.isVisible = true
                            binding.homeMiniProgressContainer.closeButton.isVisible = true
                            binding.homeMiniProgressContainer.uploadSizeText.isInvisible = true
                            binding.homeMiniProgressContainer.miniUploadProgressText.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_upload_done, 0, 0, 0
                            )
                            binding.homeMiniProgressContainer.miniUploadProgressText.text = "Upload complete"
                            cacheManager.clearCacheByUrl(ApiRoutes.GET_MY_CHANNEL_VIDEOS)
//                            if (navController.currentDestination?.id == R.id.myChannelHomeFragment) {
//                                myChannelReloadViewModel.reloadVideos.postValue(true)
//                            }
                        }
                        UploadStatus.ADDED.value, UploadStatus.STARTED.value -> {
                            binding.homeMiniProgressContainer.addUploadInfoButton.isInvisible = true
                            binding.homeMiniProgressContainer.uploadSizeText.isVisible = true
                            binding.homeMiniProgressContainer.closeButton.isInvisible = true
                            binding.homeMiniProgressContainer.miniUploadProgressText.text = "Uploading - ${upInfo.completedPercent}%"
                            binding.homeMiniProgressContainer.miniUploadProgress.progress = upInfo.completedPercent
                            binding.homeMiniProgressContainer.uploadSizeText.text = Utils.readableFileSize(upInfo.fileSize)
                        }
                    }
                } else {
                    binding.homeMiniProgressContainer.root.isVisible = false
                }
            }
        }
    }
    
    private fun observeMyChannelNavigation() {
        observe(viewModel.myChannelNavLiveData) {
            if (navController.currentDestination?.id != R.id.menu_channel || channelOwnerId != it.channelOwnerId) {
                navController.navigate(Uri.parse("app.toffee://ugc_channel/${it.channelOwnerId}/false"))
            } else {
                minimizePlayer()
            }
        }
    }
}