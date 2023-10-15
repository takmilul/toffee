package com.banglalink.toffee.ui.home

import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.NotificationManager
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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Path
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Xml
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.R.style
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.OnBackStackChangedListener
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.media3.cast.CastPlayer
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import coil.load
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.Constants.IN_APP_UPDATE_REQUEST_CODE
import com.banglalink.toffee.Constants.NON_PREMIUM
import com.banglalink.toffee.Constants.OPEN_IN_EXTERNAL_BROWSER
import com.banglalink.toffee.Constants.PLAY_CDN
import com.banglalink.toffee.Constants.PLAY_IN_NATIVE_PLAYER
import com.banglalink.toffee.Constants.PLAY_IN_WEB_VIEW
import com.banglalink.toffee.Constants.PREMIUM
import com.banglalink.toffee.Constants.STINGRAY_CONTENT
import com.banglalink.toffee.R
import com.banglalink.toffee.R.*
import com.banglalink.toffee.analytics.FirebaseParams
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.apiservice.ApiRoutes
import com.banglalink.toffee.apiservice.BrowsingScreens
import com.banglalink.toffee.data.database.dao.FavoriteItemDao
import com.banglalink.toffee.data.database.entities.CdnChannelItem
import com.banglalink.toffee.data.exception.AppDeprecatedError
import com.banglalink.toffee.data.network.response.CircuitBreakerData
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.data.repository.CdnChannelItemRepository
import com.banglalink.toffee.data.repository.NotificationInfoRepository
import com.banglalink.toffee.data.repository.TVChannelRepository
import com.banglalink.toffee.data.repository.UploadInfoRepository
import com.banglalink.toffee.databinding.ActivityHomeBinding
import com.banglalink.toffee.di.AppCoroutineScope
import com.banglalink.toffee.di.FirebaseInAppMessage
import com.banglalink.toffee.enums.*
import com.banglalink.toffee.enums.BubbleType.*
import com.banglalink.toffee.enums.PlayingPage.ALL_TV_CHANNEL
import com.banglalink.toffee.enums.PlayingPage.FM_RADIO
import com.banglalink.toffee.enums.PlayingPage.SPORTS_CATEGORY
import com.banglalink.toffee.enums.PlayingPage.STINGRAY
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.*
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.mqttservice.ToffeeMqttService
import com.banglalink.toffee.notification.PUBSUBMessageStatus.OPEN
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.notification.ToffeeMessagingService.Companion.ACTION_NAME
import com.banglalink.toffee.notification.ToffeeMessagingService.Companion.CONTENT_VIEW
import com.banglalink.toffee.notification.ToffeeMessagingService.Companion.DISMISS
import com.banglalink.toffee.notification.ToffeeMessagingService.Companion.NOTIFICATION_ID
import com.banglalink.toffee.notification.ToffeeMessagingService.Companion.PUB_SUB_ID
import com.banglalink.toffee.notification.ToffeeMessagingService.Companion.ROW_ID
import com.banglalink.toffee.notification.ToffeeMessagingService.Companion.WATCH_NOW
import com.banglalink.toffee.ui.bubble.BaseBubbleService
import com.banglalink.toffee.ui.bubble.BubbleServiceRamadan
import com.banglalink.toffee.ui.bubble.BubbleServiceV2
import com.banglalink.toffee.ui.category.music.stingray.StingrayChannelFragmentNew
import com.banglalink.toffee.ui.category.webseries.EpisodeListFragment
import com.banglalink.toffee.ui.channels.AllChannelsViewModel
import com.banglalink.toffee.ui.channels.ChannelFragmentNew
import com.banglalink.toffee.ui.common.Html5PlayerViewActivity
import com.banglalink.toffee.ui.fmradio.FmChannelFragmentNew
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
import com.banglalink.toffee.ui.widget.*
import com.banglalink.toffee.util.*
import com.banglalink.toffee.util.Utils.getActionBarSize
import com.banglalink.toffee.util.Utils.hasDefaultOverlayPermission
import com.conviva.apptracker.ConvivaAppAnalytics
import com.conviva.apptracker.controller.TrackerController
import com.conviva.sdk.ConvivaAnalytics
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.inappmessaging.FirebaseInAppMessaging
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.medallia.digital.mobilesdk.MedalliaDigital
import com.suke.widget.SwitchButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import net.gotev.uploadservice.UploadService
import org.xmlpull.v1.XmlPullParser
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.net.URLDecoder
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class HomeActivity : PlayerPageActivity(),
    SearchView.OnQueryTextListener,
    DraggerLayout.OnPositionChangedListener,
    OnBackStackChangedListener
{
    
    private val gson = Gson()
    private var dInfo: Any? = null
    private var channelOwnerId: Int = 0
    private var visibleDestinationId = 0
    private var cInfo: ChannelInfo? = null
    lateinit var binding: ActivityHomeBinding
    private var searchView: SearchView? = null
    private var notificationBadge: View? = null
    private var bubbleFifaIntent: Intent? = null
    @Inject lateinit var bindingUtil: BindingUtil
    private var bubbleRamadanIntent: Intent? = null
    private lateinit var drawerHelper: DrawerHelper
    @Inject lateinit var cacheManager: CacheManager
    private var playlistShareableUrl: String? = null
    private var shareableData: ShareableData? = null
    private var webSeriesShareableUrl: String? = null
    private lateinit var navController: NavController
    @Inject lateinit var favoriteDao: FavoriteItemDao
    @Inject lateinit var mqttService: ToffeeMqttService
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var navHostFragment: NavHostFragment
    @Inject lateinit var uploadRepo: UploadInfoRepository
    private lateinit var appbarConfig: AppBarConfiguration
    @Inject lateinit var uploadManager: UploadStateManager
    private lateinit var appUpdateManager: AppUpdateManager
    @Inject lateinit var tvChannelsRepo: TVChannelRepository
    @Inject lateinit var inAppMessageParser: InAppMessageParser
    private var shouldNavigateToPremiumPageAfterConfigChange = false
    @Inject @AppCoroutineScope lateinit var appScope: CoroutineScope
    @Inject lateinit var notificationRepo: NotificationInfoRepository
    @Inject lateinit var cdnChannelItemRepository: CdnChannelItemRepository
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    @Inject @FirebaseInAppMessage lateinit var inAppMessaging: FirebaseInAppMessaging
    private val profileViewModel by viewModels<ViewProfileViewModel>()
    private val uploadViewModel by viewModels<UploadProgressViewModel>()
    private val allChannelViewModel by viewModels<AllChannelsViewModel>()
    private val landingPageViewModel by viewModels<LandingPageViewModel>()
    private val progressDialog by unsafeLazy { ToffeeProgressDialog(this) }
    private val circuitBreakerDataList = mutableMapOf<String, CircuitBreakerData>()
    
    companion object {
        const val TAG = "HOME_TAG"
        const val INTENT_REFERRAL_REDEEM_MSG = "REFERRAL_REDEEM_MSG"
        const val INTENT_PACKAGE_SUBSCRIBED = "PACKAGE_SUBSCRIBED"
    }
    
    override val playlistManager: PlaylistManager
        get() = viewModel.getPlaylistManager()
    
    fun getHomeViewModel() = viewModel
    
    private fun calculateScreenWidth(): Point {
        return Utils.getRealScreenSize(this)
    }
    
    fun Int.toPx(context: Context) = this * context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT
    
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        super.onCreate(savedInstanceState)
        
        val isDisableScreenshot = (
            mPref.screenCaptureEnabledUsers.contains(cPref.deviceId) ||
            mPref.screenCaptureEnabledUsers.contains(mPref.customerId.toString()) ||
            mPref.screenCaptureEnabledUsers.contains(mPref.phoneNumber)
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
        initSideNav()
        observeTopBarBackground() // get the custom toolbar background image from splash screen and set in the home activity
        initLandingPageFragmentAndListenBackStack()
        showRedeemMessageIfPossible()
        observeCircuitBreaker() // restrict users from watching a specific content
        
        ToffeeAnalytics.logUserProperty(
            mapOf(
                "userId" to mPref.customerId.toString(),
                "user_type" to mPref.isBanglalinkNumber,
                "app_version" to BuildConfig.VERSION_CODE.toString()
            )
        )
        
        if (mPref.customerId != 0 && mPref.password.isNotBlank()) {
            if (mPref.isVastActive || mPref.isNativeAdActive) {
                viewModel.getVastTagV3()
            }
            handleSharedUrl(mPref.homeIntent.value ?: intent)
            mPref.homeIntent.value = null
        } else {
            mPref.homeIntent.value = intent
            finish()
            launchActivity<SplashScreenActivity> { flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK }
        }
        
        binding.uploadButton.setOnClickListener {
            ToffeeAnalytics.logEvent(ToffeeEvents.UPLOAD_CLICK)
            checkVerification {
                checkChannelDetailAndUpload()
            }
        }
        runCatching {
            val mqttClientId = EncryptionUtil.decryptResponse(mPref.mqttClientId)
            if (mqttClientId.isBlank() || mqttClientId.substringBefore("_") != mPref.phoneNumber) {
                mPref.mqttHost = ""
                mPref.mqttClientId = ""
                mPref.mqttUserName = ""
                mPref.mqttPassword = ""
            }
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
        observe(landingPageViewModel.featuredPartnerDeeplinkLiveData) {
            it?.let {
                if (mPref.isFeaturePartnerActive) {
                    onFeaturedPartnerClick(it)
                }
            }
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
        observe(viewModel.isBottomChannelScrolling) {
            if (!it) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }
        observe(mPref.loginDialogLiveData) {
            if (it) {
                navController.navigateTo(R.id.loginDialog)
            }
        }
        observe(mPref.messageDialogLiveData) { message ->
            ToffeeAlertDialogBuilder(this, title = "Notice", text = message, positiveButtonListener = {
                it?.dismiss()
            }).create().show()
        }
        observe(mPref.startBubbleService) {
            if (it) {
                startBubbleService()
            } else if(mPref.bubbleType == FIFA.value && mPref.isFifaBubbleActive) {
                bubbleFifaIntent?.let { stopService(it) }
            }
            else if(mPref.bubbleType == RAMADAN.value && mPref.isBubbleActive) {
                bubbleRamadanIntent?.let { stopService(it) }
            }
        }
        
        if (intent.hasExtra(INTENT_PACKAGE_SUBSCRIBED)) {
            handlePackageSubscribe()
        }
        
        lifecycle.addObserver(heartBeatManager)
        loadUserInfo()
        initMqtt()
        observeInAppMessage()
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
        
        val isAnyNativeSectionActive = mPref.nativeAdSettings.value?.find {
            it.isActive
        }?.isActive ?: false
        
        if (isAnyNativeSectionActive && mPref.isNativeAdActive) {
//            val testDeviceIds = listOf("33D01C3F0C238BE4407EB453A72FA7E4", "09B67C1ED8519418B65ECA002058C882")
//            val configuration =
//                RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
//            MobileAds.setRequestConfiguration(configuration)
            MobileAds.initialize(this)
        }
    
        startBubbleService()
        
        if (mPref.deleteDialogLiveData.value == true) {
            getNavController().navigateTo(R.id.completeDeleteProfileDataBottomSheetFragment)
            mPref.deleteDialogLiveData.value = false
        }
        
        observeLogout()
        onLoginSuccess()
//        showDeviceId()
//        showCustomDialog("Device ID", cPref.deviceId)
//        lifecycleScope.launch(IO) {
//            val installationId = FirebaseAnalytics.getInstance(this@HomeActivity).firebaseInstanceId
//            withContext(Main) {
//                showCustomDialog("Firebase Installation ID", installationId)
//            }
//        }
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
    
    override fun onResume() {
        super.onResume()
        inAppMessaging.setMessagesSuppressed(false)
        
        if (mPref.customerId == 0 || mPref.password.isBlank()) {
            finish()
            launchActivity<SplashScreenActivity> { flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK }
        }
        binding.playerView.setPlaylistListener(this)
        binding.playerView.addPlayerControllerChangeListener(this)
        if (Build.VERSION.SDK_INT >= 24) {
            pipChanged(isInPictureInPictureMode)
        }
        setPlayerInPlayerView()
        binding.playerView.resizeView(calculateScreenWidth())
        updateFullScreenState()
    }
    
    override fun onPause() {
        super.onPause()
        binding.playerView.clearListeners()
        if (Util.SDK_INT <= 23) {
            binding.playerView.player = null
        }
    }
    
    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            binding.playerView.player = null
        }
        playerEventHelper.appBackgrounded("app backgrounded")
        ConvivaAnalytics.reportAppBackgrounded()
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
    
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        /*
        viewDragHelper send onViewMinimize/Maximize event when start the transition
        it's not possible to get transition(animation) end listener
        If phone is already in landscape mode, it starts to move to full screen while drag transition is on going
        so player can't reset scale completely. Manually resetting player scale value
         */
        if (Build.VERSION.SDK_INT >= 24 && isInPictureInPictureMode) {
//            binding.playerView.resizeView(Point(newConfig.screenWidthDp.px, newConfig.screenHeightDp.px))
            pipChanged(isInPictureInPictureMode)
//            maximizePlayer()
//            toggleNavigations(true)
//            binding.playerView.onPip(isInPictureInPictureMode)
            return
        }
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (playlistManager.getCurrentChannel()?.isLinear == true) {
                if (viewModel.currentlyPlayingFrom.value != FM_RADIO) {
                    binding.homeBottomSheet.bottomSheet.visibility = View.VISIBLE
                }
                if (binding.playerView.isControllerVisible() && viewModel.currentlyPlayingFrom.value != FM_RADIO) {
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
        if (shouldNavigateToPremiumPageAfterConfigChange) {
            shouldNavigateToPremiumPageAfterConfigChange = false
            lifecycleScope.launch {
                delay(800)
                minimizePlayer()
                navigateToPremiumPackList()
            }
        }
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
    
    @SuppressLint("ClickableViewAccessibility")
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
//        searchView?.setOnCloseListener {
//            navController.popBackStack(R.id.searchFragment, true)
//            false
//        }
        
        val searchBar: LinearLayout = searchView!!.findViewById(R.id.search_bar)
        searchBar.layoutTransition = LayoutTransition()
        
        val mic = searchView!!.findViewById(androidx.appcompat.R.id.search_voice_btn) as ImageView
        mic.setImageResource(R.drawable.ic_menu_microphone)
        
        close = searchView!!.findViewById(androidx.appcompat.R.id.search_close_btn) as ImageView
        close.updateLayoutParams {
            width = 0.toPx(this@HomeActivity)
            height = 0.toPx(this@HomeActivity)
        }
        
        val searchIv = searchView!!.findViewById(androidx.appcompat.R.id.search_button) as ImageView
        searchIv.setImageResource(R.drawable.ic_menu_search)
        
        searchIv.setOnClickListener {
            searchView?.onActionViewExpanded()
            if (navController.currentDestination?.id != R.id.searchFragment) {
                navController.navigateTo(R.id.searchFragment, bundleOf(SearchFragment.SEARCH_KEYWORD to ""))
            }
        }
        
        val searchBadgeTv = searchView?.findViewById(androidx.appcompat.R.id.search_badge) as TextView
        searchBadgeTv.background = ContextCompat.getDrawable(this@HomeActivity, R.drawable.ic_menu_search)
        
        val searchAutoComplete: AutoCompleteTextView = searchView!!.findViewById(R.id.search_src_text)
        
        searchAutoComplete.apply {
            textSize = 16f
            setTextColor(
                ContextCompat.getColor(
                    context, R.color.searchview_input_text_color
                )
            )
            background = ContextCompat.getDrawable(context, R.drawable.searchview_input_bg)
            hint = "Search"
            setHintTextColor(ContextCompat.getColor(context, R.color.searchview_hint_text_color))
            compoundDrawablePadding = 10
            addTextChangedListener { text ->
                val leftIcon = R.drawable.ic_search_new
                val rightIcon = if ((text?.length ?: 0) <= 0) 0 else R.drawable.ic_clear_search
                if (compoundPaddingRight != rightIcon) {
                    setCompoundDrawablesWithIntrinsicBounds(leftIcon, 0, rightIcon, 0)
                }
                searchAutoComplete.setOnTouchListener(View.OnTouchListener { _, event ->
                    val DRAWABLE_LEFT = 0
                    val DRAWABLE_TOP = 1
                    val DRAWABLE_RIGHT = 2
                    val DRAWABLE_BOTTOM = 3
                    if (event.action == MotionEvent.ACTION_UP) {
                        if (searchAutoComplete.compoundDrawables[DRAWABLE_RIGHT] != null) {
                            if (event.rawX >= searchAutoComplete.right - searchAutoComplete.compoundDrawables[DRAWABLE_RIGHT].bounds.width()) {
                                searchAutoComplete.setText("")
                                setCompoundDrawablesWithIntrinsicBounds(leftIcon, 0, 0, 0)
                                return@OnTouchListener true
                            }
                        }
                    }
                    false
                })
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
                navController.navigateTo(R.id.notificationDropdownFragment)
            }
        }
        searchView?.setOnQueryTextListener(this)
        
        val awesomeMenuItem = menu.findItem(R.id.action_avatar)
        val awesomeActionView = awesomeMenuItem.actionView
        awesomeActionView?.setOnClickListener { binding.drawerLayout.openDrawer(GravityCompat.END, true) }
        
        observeNotification()
        return true
    }
    
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if (mPref.isVerifiedUser) {
            observe(mPref.profileImageUrlLiveData) {
                menu.findItem(R.id.action_avatar)?.actionView?.findViewById<ImageView>(R.id.view_avatar)?.let { profileImageView ->
                    when (it) {
                        is String -> bindingUtil.bindRoundImage(profileImageView, it)
                        is Int -> bindingUtil.loadImageFromResource(profileImageView, it)
                    }
                }
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                navController.navigatePopUpTo(resId = R.id.menu_feed)
                return true
            }
            R.id.action_avatar -> {
                binding.drawerLayout.openDrawer(GravityCompat.END, true)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
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
            getNavController().popBackStack(R.id.searchFragment, true)
//            try {
//                lifecycleScope.launch {
//                    delay(250)
//                    getNavController().popBackStack()
//                }
//            } catch (e:Exception) {
//
//            }
        } else if (player?.isPlaying == true && Build.VERSION.SDK_INT >= 24 && hasPip() && navController.currentDestination?.id == R.id.menu_feed) {
            enterPipMode()
        } else if (navController.currentDestination?.id == R.id.premiumPackListFragment) {
            navController.navigatePopUpTo(R.id.menu_feed)
        } else {
            super.onBackPressed()
        }
    }
    
    override fun onBackStackChanged() {
        if (supportFragmentManager.backStackEntryCount == 0) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(false)
            drawerHelper.toggle.isDrawerIndicatorEnabled = true
        } else if (supportFragmentManager.backStackEntryCount == 1) { //home
            closeSearchBarIfOpen()
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appbarConfig) || super.onSupportNavigateUp()
    }
    
    private fun initLandingPageFragmentAndListenBackStack() {
        supportFragmentManager.addOnBackStackChangedListener(this)
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
    
    var currentFragmentDestinationId: Int? = 0
    var bottomNavBarHideState = false
    
    private val destinationChangeListener = NavController.OnDestinationChangedListener { controller, _, _ ->
        if (binding.draggableView.isMaximized() || controller.currentDestination?.id == R.id.editUploadInfoFragment) {
            minimizePlayer()
        }
        if (visibleDestinationId == R.id.htmlPageViewDialogInApp && isPlayerVisible()) {
            maximizePlayer()
        }
        visibleDestinationId = controller.currentDestination?.id ?: 0

//        if(navController.currentDestination?.id != R.id.searchFragment){
        closeSearchBarIfOpen()
//        }
        // For firebase screenview logging
        if (controller.currentDestination is FragmentNavigator.Destination) {
            val currentFragmentClassName = (controller.currentDestination as FragmentNavigator.Destination).className.substringAfterLast(".")
            
            ToffeeAnalytics.logEvent(
                FirebaseAnalytics.Event.SCREEN_VIEW, bundleOf(
                    FirebaseAnalytics.Param.SCREEN_CLASS to currentFragmentClassName
                )
            )
            currentFragmentDestinationId = controller.currentDestination?.id
        }
        
        bottomNavBarHideState = currentFragmentDestinationId in listOf(id.premiumPackListFragment, id.packDetailsFragment)
        toggleBottomNavBar(bottomNavBarHideState)
//        binding.tbar.toolbar.setBackgroundResource(R.drawable.demotopbar)
        binding.tbar.toolbar.setNavigationIcon(if(bottomNavBarHideState) R.drawable.ic_arrow_back else R.drawable.ic_toffee)
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
    
    fun getNavController() = navController
    
    private fun initSideNav() {
        with(binding.sideNavigation.menu) {
            if (mPref.isBanglalinkNumber != "true" || !mPref.showBuyInternetForAndroid) {
                val subMenu = findItem(R.id.ic_menu_internet_packs)
                subMenu?.isVisible = false
            }
            binding.sideNavigation.getHeaderView(0).findViewById<LinearLayout>(R.id.menu_toffee_premium).isVisible = mPref.isSubscriptionActive
            findItem(R.id.menu_tv).isVisible = mPref.isAllTvChannelMenuEnabled
            findItem(R.id.menu_login).isVisible = !mPref.isVerifiedUser
            findItem(R.id.menu_logout).isVisible = mPref.isVerifiedUser
            val sideNav = findItem(R.id.menu_change_theme)
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
                    switch = SwitchButton(this@HomeActivity, attr)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                    switch = SwitchMaterial(this@HomeActivity)
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
    }
    
    private fun setupNavController() {
        navHostFragment = supportFragmentManager.findFragmentById(R.id.home_nav_host) as NavHostFragment
        navController = navHostFragment.navController
        
        appbarConfig = AppBarConfiguration(
            setOf(
                R.id.menu_feed,
                R.id.menu_tv,
                R.id.menu_activities,
                R.id.menu_channel,
                R.id.menu_explore,
                R.id.menu_favorites,
                R.id.menu_settings,
                R.id.menu_subscriptions,
                R.id.menu_invite,
                R.id.menu_redeem,
                R.id.menu_creators_policy,
                R.id.premiumPackListFragment
            ), binding.drawerLayout
        )
//        setupActionBarWithNavController(navController, appbarConfig)
//        NavigationUI.setupActionBarWithNavController(this, navController, appbarConfig)
        binding.tbar.toolbar.setupWithNavController(navController, appbarConfig)
        binding.tbar.toolbar.setNavigationIcon(R.drawable.ic_toffee)
        binding.sideNavigation.setupWithNavController(navController)
        binding.tabNavigator.setupWithNavController(navController)
        binding.sideNavigation.setBackgroundColor(ContextCompat.getColor(this, R.color.cardBgColor))
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomAppBar) { _, _ ->
            WindowInsetsCompat.CONSUMED
        }
        
        binding.sideNavigation.setNavigationItemSelectedListener { menuItem ->
            drawerHelper.handleMenuItemById(menuItem)
        }
        
        navController.addOnDestinationChangedListener(destinationChangeListener)
        binding.tabNavigator.setOnItemSelectedListener {
            closeSearchBarIfOpen()
            when (it.itemId) {
                R.id.menu_feed -> navController.navigatePopUpTo(R.id.menu_feed)
                R.id.menu_tv -> navController.navigateTo(R.id.menu_tv)
                R.id.menu_explore -> navController.navigateTo(R.id.menu_explore)
                R.id.menu_channel -> {
                    channelOwnerId = if (mPref.isVerifiedUser) mPref.customerId else 0
                    navController.navigateTo(R.id.menu_channel)
                }
            }
            return@setOnItemSelectedListener true
        }
//        binding.sideNavigation.setNavigationItemSelectedListener {
//            binding.drawerLayout.closeDrawers()
//            return@setNavigationItemSelectedListener false
//        }
    }
    
    private fun observeTopBarBackground() {
        val isActive = try {
            mPref.isTopBarActive && Utils.getDate(mPref.topBarStartDate).before(mPref.getSystemTime()) && Utils.getDate(mPref.topBarEndDate)
                .after(mPref.getSystemTime())
        } catch (e: Exception) {
            false
        }
        if (isActive) {
            if (mPref.topBarType == "png") {
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val imagePath =
                            if (cPref.appThemeMode == Configuration.UI_MODE_NIGHT_NO) mPref.topBarImagePathLight else mPref.topBarImagePathDark
                        if (!imagePath.isNullOrBlank()) {
                            binding.tbar.toolbarImageView.load(imagePath)
                        } else {
                            loadDefaultTopBarColor()
                        }
                    } catch (e: Exception) {
                        ToffeeAnalytics.logException(e)
                        loadDefaultTopBarColor()
                    }
                }
            }
        } else {
            loadDefaultTopBarColor()
        }
    }
    
    private fun loadDefaultTopBarColor() {
        binding.tbar.toolbar.background = ContextCompat.getDrawable(this, R.color.tool_bar_color)
        binding.tbar.toolbar.popupTheme = style.ThemeOverlay_AppCompat_Dark_ActionBar
    }
    
    private fun observeMyChannelNavigation() {
        observe(viewModel.myChannelNavLiveData) {
            if (navController.currentDestination?.id != R.id.menu_channel || channelOwnerId != it.channelOwnerId) {
                navController.navigateTo(Uri.parse("app.toffee://ugc_channel/${it.channelOwnerId}/false"))
            } else {
                minimizePlayer()
            }
            channelOwnerId = it.channelOwnerId
        }
    }
    
    private fun toggleNavigation(state: Boolean) {
        if (!bottomNavBarHideState) {
            if (state) {
                supportActionBar?.hide()
                binding.bottomAppBar.hide()
                binding.uploadButton.hide()
                binding.mainUiFrame.visibility = View.GONE
                mPref.bubbleVisibilityLiveData.postValue(false)
            } else {
                supportActionBar?.show()
                binding.bottomAppBar.show()
                binding.uploadButton.show()
                binding.mainUiFrame.visibility = View.VISIBLE
                mPref.bubbleVisibilityLiveData.postValue(true)
            }
        }
    }
    
    private fun toggleBottomNavBar(state: Boolean) {
        if (state) {
            binding.bottomAppBar.hide()
            binding.uploadButton.hide()
            binding.mainUiFrame.updateLayoutParams<RelativeLayout.LayoutParams> {
                bottomMargin = 0
            }
            binding.detailsViewer.updateLayoutParams<RelativeLayout.LayoutParams> {
                bottomMargin = 0
            }
        } else {
            binding.bottomAppBar.show()
            binding.uploadButton.show()
            binding.mainUiFrame.updateLayoutParams<RelativeLayout.LayoutParams> {
                bottomMargin = getActionBarSize(this@HomeActivity) + 12.dp
            }
            binding.detailsViewer.updateLayoutParams<RelativeLayout.LayoutParams> {
                bottomMargin = getActionBarSize(this@HomeActivity) + 12.dp
            }
        }
    }
    
    override fun getPlayerView(): PlayerView = binding.playerView
    
    override fun setPlayerInPlayerView() {
        binding.playerView.player = player
        if (player is CastPlayer) {
            val deviceName = castContext?.sessionManager?.currentCastSession?.castDevice?.friendlyName
            binding.playerView.showCastingText(true, deviceName)
        } else {
            binding.playerView.showCastingText(false)
        }
    }
    
    private fun loadChannel(channelInfo: ChannelInfo) {
        viewModel.sendViewContentEvent(channelInfo)
        if (channelInfo.isLinear) {
            viewModel.addTvChannelToRecent(channelInfo)
            allChannelViewModel.selectedChannel.postValue(channelInfo)
        } else {
            allChannelViewModel.selectedChannel.postValue(null)
        }
        if (channelInfo.categoryId == 16) {
            mPref.categoryName.value = channelInfo.category
            mPref.categoryId.value = channelInfo.categoryId
        }
        allChannelViewModel.isFromSportsCategory.value = channelInfo.isFromSportsCategory
        viewModel.currentlyPlayingFrom.value =
            if (channelInfo.isFromSportsCategory) {
                SPORTS_CATEGORY
            } else if (channelInfo.isStingray) {
                STINGRAY
            } else if (channelInfo.isFmRadio) {
                FM_RADIO
            } else {
                ALL_TV_CHANNEL
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
        circuitBreakerDataList[channelInfo?.id]?.let {
            if (it.isActive && mPref.getSystemTime().after(it.updatedAt) && mPref.getSystemTime().before(it.expiredAt)) {
                showDisplayMessageDialog(this, getString(string.circuit_breaker_alert_message))
                return
            }
        }
        MedalliaDigital.disableIntercept()
        
        channelInfo?.let {
            when {
                it.urlTypeExt == PREMIUM -> {
                    observeGetPackStatus()
                    observeMnpStatus()
                    lifecycleScope.launch {
                        /**
                         * if the user is not logged in and video is full screen: exit full screen, update player size and minimize the player. delay some time before minimizing the player otherwise the player will not be half minimized.
                         */
                        if (!mPref.isVerifiedUser && binding.playerView.isFullScreen) {
                            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            binding.playerView.toggleFullScreenStatus(false)
                            updateFullScreenState()
                            delay(800)
                            minimizePlayer()
                        }
                        checkVerification {
                            checkPurchaseBeforePlay(it, detailsInfo) {
                                cInfo = it
                                dInfo = detailsInfo
                                if (!mPref.isMnpStatusChecked && mPref.isVerifiedUser && mPref.isMnpCallForSubscription) {
                                    viewModel.getMnpStatus()
                                } else {
                                    viewModel.getPackStatus(channelInfo.getContentId().toInt())
                                }
                            }
                        }
                    }
                }
                it.urlType == PLAY_IN_WEB_VIEW && it.urlTypeExt == NON_PREMIUM -> {
                    playInWebView(it)
                }
                it.urlType == OPEN_IN_EXTERNAL_BROWSER && it.urlTypeExt == NON_PREMIUM -> {
                    openInExternalBrowser(it)
                }
                it.urlType == PLAY_IN_NATIVE_PLAYER && it.urlTypeExt == NON_PREMIUM -> {
                    playInNativePlayer(detailsInfo, it)
                }
                it.urlType == PLAY_CDN && it.urlTypeExt == NON_PREMIUM -> {
                    playInNativePlayer(detailsInfo, it)
                }
                it.urlType == STINGRAY_CONTENT && it.urlTypeExt == NON_PREMIUM -> {
                    playInNativePlayer(detailsInfo, it)
                }
                else -> {}
            }
        }
    }
    
    private fun observeGetPackStatus() {
        observe(viewModel.activePackListLiveData) { response ->
            runCatching {
                when (response) {
                    is Success -> {
                        if (response.data.isNotEmpty()) {
                            mPref.activePremiumPackList.value = response.data
                            cInfo?.let {
                                checkPurchaseBeforePlay(it, dInfo) {
                                    handleNavigateToPremiumPackList()
                                }
                            } ?: showToast(getString(R.string.try_again_message))
                        } else {
                            handleNavigateToPremiumPackList()
                        }
                    }
                    is Failure -> {
                        handleNavigateToPremiumPackList()
                    }
                }
            }.onFailure { showToast(getString(R.string.try_again_message)) }
        }
    }
    
    /**
     * set a flag if the player is in full screen. if full screen then exit fullscreen, update fullscreen state. After that when 
     * configuration changes navigate to the premium page or if not in fullscreen then navigate immediately.
     */
    private fun handleNavigateToPremiumPackList() {
        shouldNavigateToPremiumPageAfterConfigChange = if (binding.playerView.isFullScreen) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            binding.playerView.toggleFullScreenStatus(false)
            updateFullScreenState()
            true
        } else {
            navigateToPremiumPackList()
            false
        }
    }
    
    /**
     * keep the content in a live data. navigate to the premium page. if the user purchase the pack then play the content from the live data.
     */
    private fun navigateToPremiumPackList() {
        mPref.prePurchaseClickedContent.value = cInfo
        navController.navigatePopUpTo(
            resId = id.premiumPackListFragment,
            args = bundleOf(
                "contentId" to if (cInfo?.getContentId() is String) cInfo?.getContentId() else "0",
                "clickedFromChannelItem" to true
            )
        )
    }
    
    private fun checkPurchaseBeforePlay(
        channelInfo: ChannelInfo,
        detailsInfo: Any?,
        onFailure: (() -> Unit)? = null
    ) {
        mPref.activePremiumPackList.value.checkContentPurchase(
            contentId = channelInfo.getContentId(),
            systemDate = mPref.getSystemTime(),
            onSuccess = {
                playInNativePlayer(detailsInfo, channelInfo)
            },
            onFailure = {
                onFailure?.invoke()
            }
        )
    }
    
    private fun playInNativePlayer(detailsInfo: Any?, channelInfo: ChannelInfo) {
        ToffeeAnalytics.logEvent(
            ToffeeEvents.CONTENT_CLICK, bundleOf(
                "content_id" to channelInfo.id,
                "content_title" to channelInfo.program_name,
                "content_category" to channelInfo.category,
                "content_partner" to channelInfo.content_provider_name,
            )
        )
        if (channelInfo.urlType == PLAY_CDN && (channelInfo.cdnType == CdnType.SIGNED_URL.value || channelInfo.cdnType == CdnType
            .SIGNED_COOKIE.value)) {
            checkAndUpdateMediaCdnConfig(channelInfo) {
                playContent(detailsInfo, it)
            }
        } else if (channelInfo.urlType == PLAY_IN_NATIVE_PLAYER || channelInfo.urlType == STINGRAY_CONTENT) {
            playContent(detailsInfo, channelInfo)
        } else {
            // do nothing
        }
    }
    
    private fun checkAndUpdateMediaCdnConfig(channelInfo: ChannelInfo, onSuccess: (newItem: ChannelInfo) -> Unit) {
        cInfo = channelInfo
        lifecycleScope.launch {
            cdnChannelItemRepository.getCdnChannelItemByChannelId(channelInfo.getContentId().toLong())?.let { cdnChannelItem ->
                loadMediaCdnConfig(cdnChannelItem.channelInfo!!, onSuccess)
            } ?: run {
                cdnChannelItemRepository.insert(
                    CdnChannelItem(
                        channelInfo.getContentId().toLong(),
                        channelInfo.urlType,
                        channelInfo.signedUrlExpiryDate ?: channelInfo.signedCookieExpiryDate,
                        gson.toJson(channelInfo)
                    )
                )
                loadMediaCdnConfig(channelInfo, onSuccess)
            }
        }
    }
    
    private fun loadMediaCdnConfig(channelInfo: ChannelInfo, onSuccess: (newItem: ChannelInfo) -> Unit) {
        val isExpired = (channelInfo.signedUrlExpiryDate ?: channelInfo.signedCookieExpiryDate)?.isExpiredFrom(mPref.getSystemTime()) ?: false
        if (isExpired) {
            observe(viewModel.mediaCdnSignUrlData) { mediaCdnInfo ->
                when (mediaCdnInfo) {
                    is Success -> {
                        val mediaCdnData = mediaCdnInfo.data
                        val expiryDate = mediaCdnData?.signedUrlExpiryDate ?: mediaCdnData?.signedCookieExpiryDate
                        val newChannelInfo = cInfo?.apply {
                            signedUrlExpiryDate = mediaCdnData?.signedUrlExpiryDate?.let {
                                if (cInfo?.urlTypeExt == PREMIUM) {
                                    paidPlainHlsUrl = mediaCdnData.signedUrl
                                } else {
                                    hlsLinks = cInfo?.hlsLinks?.mapIndexed { index, hlsLinks ->
                                        if (index == 0) {
                                            hlsLinks.hls_url_mobile = mediaCdnData.signedUrl
                                        }
                                        hlsLinks
                                    }
                                }
                                it
                            }
                            signedCookieExpiryDate = mediaCdnData?.signedCookieExpiryDate?.let {
                                signedCookie = mediaCdnData.signedCookie
                                it
                            }
                        }?.copy()
                        lifecycleScope.launch {
                            newChannelInfo?.getContentId()?.toLong()?.let {
                                cdnChannelItemRepository.updateCdnChannelItemByChannelId(
                                    it, expiryDate, gson.toJson(newChannelInfo)
                                )
                            }
                        }
                        cInfo = null
                        newChannelInfo?.let { onSuccess(it) }
                    }
                    is Failure -> {
                        cInfo = null
                        showToast(getString(string.try_again_message))
                    }
                }
            }
            viewModel.getMediaCdnSignUrl(channelInfo.getContentId())
        } else {
            cInfo?.let {
                onSuccess(it)
            } ?: showToast(getString(string.try_again_message))
        }
    }
    
    private fun playContent(detailsInfo: Any?, it: ChannelInfo) {
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
        runCatching {
            it.getHlsLink()?.let { url ->
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW, Uri.parse(url)
                    )
                )
            } ?: ToffeeAnalytics.logException(NullPointerException("External browser url is null"))
        }.onFailure {
            showToast(it.message)
        }
    }
    
    private fun playInWebView(it: ChannelInfo) {
        showDebugMessage("URL: ${it.hlsLinks?.get(0)?.hls_url_mobile}")
        it.getHlsLink()?.let { url ->
            viewModel.sendViewContentEvent(it.copy(id = "0"))
            val shareableUrl = it.video_share_url
            val urlWithTheme = if (!url.contains("style=", ignoreCase = true)) url else url.plus(cPref.appTheme)
            launchActivity<Html5PlayerViewActivity> {
                putExtra(Html5PlayerViewActivity.CONTENT_URL, urlWithTheme)
                putExtra(Html5PlayerViewActivity.SHAREABLE_URL, shareableUrl)
                putExtra(Html5PlayerViewActivity.TITLE, it.program_name)
            }
        } ?: ToffeeAnalytics.logException(NullPointerException("External browser url is null"))
    }
    
    override fun updateMediaCdnConfig(channelInfo: ChannelInfo, onSuccess: (newItem: ChannelInfo) -> Unit) {
        loadMediaCdnConfig(channelInfo, onSuccess)
    }
    
    private fun loadFragmentById(id: Int, fragment: Fragment, tag: String) {
        supportFragmentManager.popBackStack(
            LandingPageFragment::class.java.name, 0
        )
        supportFragmentManager.beginTransaction().replace(id, fragment).addToBackStack(tag).commitAllowingStateLoss()
    }
    
    private fun loadFragmentById(id: Int, fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(id, fragment).commitAllowingStateLoss()
    }
    
    private fun loadDetailFragment(info: Any?) {
        val fragment = supportFragmentManager.findFragmentById(R.id.details_viewer)
        if (info is ChannelInfo) {
            if (info.isFmRadio){
                if (fragment !is FmChannelFragmentNew) {
                    loadFragmentById(
                        R.id.details_viewer, FmChannelFragmentNew()
                    )
                }
            } else if (info.isStingray) {
                if (fragment !is StingrayChannelFragmentNew) {
                    loadFragmentById(
                        R.id.details_viewer, StingrayChannelFragmentNew()
                    )
                }
            } else if (info.isLive && !info.isFromSportsCategory) {
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
                /*(fragment !is MyChannelPlaylistVideosFragment || fragment.getPlaylistId() != info.getPlaylistIdLong()) &&*/ 
                !info.isUserPlaylist -> {
                    loadFragmentById(
                        R.id.details_viewer, MyChannelPlaylistVideosFragment.newInstance(info)
                    )
                }
                /*(fragment !is UserPlaylistVideosFragment || fragment.getPlaylistId() != info.getPlaylistIdLong()) &&*/ 
                info.isUserPlaylist -> {
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
    
    /**
     * super.[playNext] method will call the [playNext] method from the [PlayerPageActivity]
     * there we will check if the next content is [PREMIUM] and not Paid then we will not activate the next
     * content in the [playlistManager] instead we sill pass the next content reference to the [HomeViewModel.playContentLiveData]
     * live data so that it can check the next content's payment and show the Premium Packs List.
     * Remember that [PlaylistManager.nextChannel] will activate the next
     * content but [PlaylistManager.getNextChannel] will not activate the next content instead it will only provide the
     * next content's reference.
     */
    override fun playNext(): Boolean {
        val isNextChannelPremium = super.playNext()
        
        if (isNextChannelPremium) {
            viewModel.playContentLiveData.postValue(playlistManager.getNextChannel())
            return true
        }
        if (playlistManager.playlistId == -1L) {
            viewModel.playContentLiveData.postValue(playlistManager.getCurrentChannel())
            return false
        }
        ConvivaHelper.endPlayerSession()
//        resetPlayer()
        val info = playlistManager.getCurrentChannel()
        Log.i("Next_", "playNext: ${info?.program_name}")
        playerEventHelper.startContentPlayingSession(info!!.id)
        ConvivaHelper.setConvivaVideoMetadata(info, mPref.customerId)
        loadDetailFragment(
            playlistManager.getCurrentChannel()?.let { PlaylistItem(playlistManager.playlistId, it) }
        )
        return false
    }
    
    override fun playPrevious(): Boolean {
        val isPreviousChannelPremium = super.playPrevious()
        
        if (isPreviousChannelPremium) {
            viewModel.playContentLiveData.postValue(playlistManager.getPreviousChannel())
            return true
        }
        ConvivaHelper.endPlayerSession()
//        resetPlayer()
        val info = playlistManager.getCurrentChannel()
        playerEventHelper.startContentPlayingSession(info!!.id)
        ConvivaHelper.setConvivaVideoMetadata(info, mPref.customerId)
        loadDetailFragment(
            playlistManager.getCurrentChannel()?.let { PlaylistItem(playlistManager.playlistId, it) }
        )
        return false
    }
    
    private fun resetPlayer() {
        releasePlayer()
        initializePlayer()
        setPlayerInPlayerView()
    }
    
    override fun playIndex(index: Int) {
        super.playIndex(index)
        loadDetailFragment(
            playlistManager.getCurrentChannel()?.let { PlaylistItem(playlistManager.playlistId, it) }
        )
    }
    
    override fun playChannelId(channelId: Int) {
        super.playChannelId(channelId)
        loadDetailFragment(
            playlistManager.getCurrentChannel()?.let { PlaylistItem(playlistManager.playlistId, it) }
        )
    }
    
    override fun isPlayerVisible(): Boolean {
        return binding.draggableView.isVisible
    }
    
    override fun isVideoPortrait() = binding.playerView.isVideoPortrait
    
    override fun onMinimizeButtonPressed(): Boolean {
        binding.draggableView.minimize()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        return true
    }
    
    private fun minimizePlayer() {
        binding.draggableView.minimize()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
    
    override fun onPlayerMinimize() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding.playerView.clearDebugWindow()
    }
    
    override fun maximizePlayer() {
        binding.draggableView.maximize()
        binding.draggableView.visibility = View.VISIBLE
//        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
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
    
    private fun destroyPlayer() {
        binding.draggableView.destroyView()
        mPref.playerOverlayLiveData.removeObservers(this)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
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
    
    override fun onControllerVisible() {
        if (playlistManager.getCurrentChannel()?.isLinear == true && resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        } else {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }
    
    override fun onControllerInVisible() {
        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN && viewModel.isBottomChannelScrolling.value == false) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }
    
    override fun onFullScreenButtonPressed(): Boolean {
        super.onFullScreenButtonPressed()
        requestedOrientation =
            if (!binding.playerView.isAutoRotationEnabled || binding.playerView.isFullScreen || binding.playerView.isVideoPortrait) {
                ActivityInfo.SCREEN_ORIENTATION_LOCKED
            } else {
                ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
            }
        updateFullScreenState()
        return true
    }
    
    private fun updateFullScreenState() {
        if (Build.VERSION.SDK_INT >= 24 && isInPictureInPictureMode) return
        val state = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE || binding.playerView.isFullScreen
        binding.playerView.onFullScreen(state)
        binding.playerView.resizeView(calculateScreenWidth(), state)
        setFullScreen(state)
        toggleNavigation(state)
//        val isInPremiumPage = currentFragmentDestinationId in listOf(id.premiumPackListFragment, id.packDetailsFragment)
//        toggleBottomNavBar(isInPremiumPage)
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
    
    override fun onRotationLock(isAutoRotationEnabled: Boolean) {
        if (isAutoRotationEnabled && !binding.playerView.isVideoPortrait) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
            showToast(getString(R.string.auto_rotation_on))
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
            showToast(getString(R.string.auto_rotation_off))
        }
    }
    
    override fun onDrawerButtonPressed(): Boolean {
        binding.drawerLayout.openDrawer(GravityCompat.END, true)
        return true
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
    
    override fun resumeCastSession(info: ChannelInfo) {
        maximizePlayer()
        loadDetailFragment(info)
    }
    
    override fun channelCannotBePlayedDueToSettings() {
        binding.playerView.showWifiOnlyMessage()
    }
    
    override fun onContentExpired() {
        binding.playerView.showContentExpiredMessage()
    }
    
    override fun showPlayerCustomErrorMessage(errorMessage: String?) {
        binding.playerView.showCustomErrorMessage(errorMessage)
    }
    
    override fun onTrackerDialogDismissed() {
        updateFullScreenState()
    }
    
    private fun configureBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.homeBottomSheet.bottomSheet)
        if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT || viewModel.currentlyPlayingFrom.value == FM_RADIO) {
            binding.homeBottomSheet.bottomSheet.hide()
        } else {
            binding.homeBottomSheet.bottomSheet.show()
        }
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior.addBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback() {
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
    
    @Throws(MalformedURLException::class, IOException::class)
    fun getDrawableFromUrl(url: String?): Bitmap? {
        val connection: HttpURLConnection = URL(url).openConnection() as HttpURLConnection
        connection.setRequestProperty("User-agent", "Mozilla/4.0")
        connection.connect()
        val input: InputStream = connection.inputStream
        return BitmapFactory.decodeStream(input)
    }
    
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (player?.isPlaying == true && Build.VERSION.SDK_INT >= 24 && hasPip()) {
            enterPipMode()
        }
    }
    
    @Suppress("DEPRECATION")
    @RequiresApi(24)
    private fun enterPipMode() {
        try {
            if (Build.VERSION.SDK_INT < 26) {
                enterPictureInPictureMode()
            } else {
                enterPictureInPictureMode(
//                PictureInPictureParams.Builder()
//                    .setAspectRatio(Rational(binding.playerView.width, binding.playerView.height))
//                    .build()
                )
            }
        } catch (e: Exception) {
            ToffeeAnalytics.logException(e)
        }
    }
    
    @SuppressLint("MissingSuperCall", "NewApi")
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        pipChanged(isInPictureInPictureMode)
        if (lifecycle.currentState == Lifecycle.State.CREATED) {
            if (Settings.canDrawOverlays(this)) {
                startBubbleService()
            }
        }
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
    }
    
    private fun pipChanged(isInPip: Boolean) {
        if (isInPip) {
            toggleNavigation(true)
            val fragment = navHostFragment.childFragmentManager.fragments.last()
            if (fragment is DialogFragment) {
                progressDialog.dismiss()
                fragment.dismiss()
            }
            binding.draggableView.maximize()
            binding.draggableView.visibility = View.VISIBLE
            maximizePlayer()
            binding.homeBottomSheet.bottomSheet.hide()
        }
        binding.playerView.onPip(isInPip)
    }
    
    private fun hasPip() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        mPref.isPipEnabled && packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
    } else {
        false
    }
    
    @SuppressLint("MissingSuperCall")
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
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            }.cancel(notificationId)
            
            if (actionName == CONTENT_VIEW || actionName == WATCH_NOW) {
                PubSubMessageUtil.sendNotificationStatus(pubSubId, OPEN)
            }
        }
        observeAppVersionUpdate(intent)
        viewModel.checkForUpdateStatus()
    }
    
    private fun handleIntent(intent: Intent) {
        var newIntent = intent
        if (mPref.homeIntent.value != null) {
            newIntent = mPref.homeIntent.value!!
            mPref.homeIntent.value = null
        }
        if (mPref.customerId != 0 && mPref.password.isNotBlank()) {
            handleSharedUrl(newIntent)
        } else {
            mPref.homeIntent.value = newIntent
            finish()
            launchActivity<SplashScreenActivity> { flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK }
        }
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
                        val encryptedUrl = EncryptionUtil.decryptResponse(newHash).trimIndent()
                        shareableData = gson.fromJson(encryptedUrl, ShareableData::class.java)
                        when (shareableData?.type) {
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
                            SharingType.FM_RADIO.value -> {
                                pair = Pair(shareableData?.fmRadioShareUrl, SharingType.FM_RADIO.value)
                            }
                        }
                    } else {
                        pair = Pair(hash, null)
                    }
                    if (hash.contains("promotions")) {
//                        https://toffeelive.com/promotions?pid=1001&pname="promotionNameHere"&forward="forwardingUrlHere"
                        runCatching {
                            val uri = Uri.parse(hash)
                            val promotionId = uri.getQueryParameter("pid")
                            val promotionName = uri.getQueryParameter("pname")?.replace("\"", "")
                            val forwardUrl = hash.substringAfter("forward=", "").replace("\"", "")
                            if (forwardUrl.isNotBlank()) {
                                Log.i("IAM_", "promotionId: $promotionId, promotionName: $promotionName, forwardingUrl: $forwardUrl")
                                ToffeeAnalytics.logEvent(
                                    ToffeeEvents.PROMOTION, bundleOf(
                                        "promotion_id" to promotionId,
                                        "promotion_name" to promotionName,
                                        "msisdn" to mPref.phoneNumber,
                                        "device_id" to cPref.deviceId,
                                        "device_type" to "1",
                                        "ad_id" to mPref.adId,
                                        "redirection_link" to forwardUrl,
                                        "app_version" to cPref.appVersionName,
                                        "timestamp" to currentDateTime
                                    )
                                )
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(forwardUrl))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                            }
                        }
                    } else {
                        mPref.shareableHashLiveData.value = pair
                    }
                }
            } catch (e: Exception) {
                ToffeeAnalytics.logBreadCrumb("2. Failed to handle depplink $url")
                ToffeeAnalytics.logException(e)
            }
        }
    }
    
    private suspend fun handleInAppDeepLink(url: String): Boolean {
        var isDeepLinkHandled = false
        val route = inAppMessageParser.parseUrlV2(url)
        route?.let {
            ToffeeAnalytics.logBreadCrumb("Trying to open ${it.name}")
            when (it.destId) {
                is Uri -> navController.navigate(it.destId, it.options, it.navExtra)
                is Int -> {
                    if (it.name == "Featured Partner") {
                        landingPageViewModel.loadFeaturedPartnerList(it.destId)
                    } else if (it.destId == R.id.menu_favorites || it.destId == R.id.menu_activities || it.destId == R.id.menu_subscriptions) {
                        checkVerification {
                            navController.navigate(it.destId, it.args, it.options, it.navExtra)
                        }
                    } else {
                        navController.navigate(it.destId, it.args, it.options, it.navExtra)
                    }
                }
            }
            isDeepLinkHandled = true
        }
        return isDeepLinkHandled
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
    
    private fun getWebPlaylistShare(url: String): String {
        val ownerId = url.substringAfter("owner_id=").substringBefore("&").toInt()
        val isOwner = if (ownerId == mPref.customerId) 1 else 0
        val playlistId = url.substringAfter("pl_id=").substringBefore("&").toInt()
        val playlistName = url.substringAfter("name=").substringBefore("&")
        val newUrl = "https://toffeelive.com/#video/data="
        
        val sharableData = ShareableData(
            "playlist", 0, null, null, 0, isOwner, ownerId, playlistId, playlistName
        )
        val json = gson.toJson(sharableData, ShareableData::class.java)
        val shareableJsonData = EncryptionUtil.encryptRequest(json).trimIndent()
        return newUrl + shareableJsonData
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
                                shareableData?.name ?: response.data.name ?: "",
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
    
    private fun handlePackageSubscribe() {
        //Clean up stack upto landingPageFragment inclusive
        supportFragmentManager.popBackStack(
            LandingPageFragment::class.java.name, POP_BACK_STACK_INCLUSIVE
        )
    }

    private fun observeMnpStatus() {
        observe(viewModel.mnpStatusBeanLiveData) { response ->
            when (response) {
                is Success -> {
                    if (response.data?.mnpStatus == 200){
                        mPref.isMnpStatusChecked = true
                        cInfo?.let {
                            viewModel.getPackStatus(it.getContentId().toInt())
                        }
                    }
                }
                is Failure -> {
                    baseContext.showToast(response.error.msg)
                }
            }
        }
    }

    fun hideSearchOverlay() {
        binding.searchOverlay.hide()
    }
    
    lateinit var close: ImageView
    
    override fun onQueryTextSubmit(query: String?): Boolean {
        if (!query.isNullOrBlank()) {
            navigateToSearch(query)
            // openSearchBarIfClose()
            return true
        }
        return false
    }
    
    override fun onQueryTextChange(newText: String?): Boolean {
        close.hide()
        return false
    }
    
    fun openSearchBarIfClose() {
        if (searchView?.isIconified == true) {
            searchView?.onActionViewExpanded()
        }
    }
    
    private fun navigateToSearch(query: String?) {
        ToffeeAnalytics.logEvent(ToffeeEvents.SEARCH, bundleOf("search_query" to query))
        navController.navigatePopUpTo(R.id.searchFragment, bundleOf(SearchFragment.SEARCH_KEYWORD to query))
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
    
    fun closeSearchBarIfOpen() {
        if (searchView?.isIconified == false) {
            searchView?.onActionViewCollapsed()
        }
    }
    
    fun clearSearViewFocus() {
        searchView?.clearFocus()
    }
    
    private fun loadUserInfo() {
        if (!isChannelComplete() && mPref.isVerifiedUser) {
            viewModel.getChannelDetail(mPref.customerId)
            observe(profileViewModel.loadCustomerProfile()) {
                if (it is Success) {
                    profileViewModel.profileForm.value = it.data
                }
            }
        }
    }
    
    private fun showCustomDialog(title: String, message: String) {
        ToffeeAlertDialogBuilder(this, title = title, text = message, positiveButtonTitle = "copy", positiveButtonListener = {
            val clipboard: ClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText(title, message)
            clipboard.setPrimaryClip(clip)
            showToast("copied to clipboard")
            it?.dismiss()
        }, negativeButtonTitle = "Close", negativeButtonListener = { it?.dismiss() }).create().show()
    }
    
    private fun onFeaturedPartnerClick(item: FeaturedPartner) {
        if (item.isLoginRequired) {
            checkVerification {
                openFeaturePartner(item)
            }
        } else {
            openFeaturePartner(item)
        }
    }
    
    private fun openFeaturePartner(featuredPartner: FeaturedPartner) {
        if (featuredPartner.url_type == 1){
            navController.navigateTo(R.id.fmRadioFragment)
        } else{
            if (navController.currentDestination?.id != R.id.htmlPageViewDialog_Home) {
                featuredPartner.webViewUrl?.let { url ->
                    landingPageViewModel.sendFeaturePartnerReportData(
                        partnerName = featuredPartner.featurePartnerName.toString(), partnerId = featuredPartner.id
                    )
                    
                    navController.navigateTo(
                        resId = R.id.htmlPageViewDialog_Home,
                        args = bundleOf(
                            "myTitle" to getString(string.back_to_toffee_text),
                            "url" to url,
                            "isHideBackIcon" to false,
                            "isHideCloseIcon" to true
                        )
                    )
                } ?: ToffeeAnalytics.logException(NullPointerException("External browser url is null"))
            }
        }
    }
    
    private fun isChannelComplete() =
        mPref.customerName.isNotBlank() && mPref.customerEmail.isNotBlank() && mPref.customerAddress.isNotBlank() && mPref.customerDOB.isNotBlank() && mPref.customerNID.isNotBlank() && mPref.channelName.isNotBlank() && mPref.channelLogo.isNotBlank() && mPref.isChannelDetailChecked
    
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
                navController.navigateTo(R.id.uploadMethodFragment)
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
                navController.navigateTo(R.id.bottomSheetUploadFragment)
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
    
    private fun showRedeemMessageIfPossible() {
        //show referral redeem msg if possible
        val msg = intent.getStringExtra(INTENT_REFERRAL_REDEEM_MSG)
        msg?.let {
            showDisplayMessageDialog(this, it)
        }
    }
    
    private fun customCrashReport() {
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory()
        FirebaseCrashlytics.getInstance().setCustomKey("heap_size", "$maxMemory")
    }
    
    private val appUpdateListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADED -> {
                popupSnackbarForCompleteUpdate()
            }
            InstallStatus.INSTALLED -> {
                showToast("Toffee updated successfully")
            }
            else -> {}
        }
    }
    
    private fun inAppUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateManager.registerListener(appUpdateListener)
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
    }
    
    private fun popupSnackbarForCompleteUpdate() {
        binding.root.snack(getString(string.in_app_update_msg)) {
            action("RESTART", ContextCompat.getColor(this@HomeActivity, R.color.colorAccent2)) {
                appUpdateManager.completeUpdate()
            }
        }
    }
    
    private fun observeInAppMessage() {
        ToffeeAnalytics.logEvent(ToffeeEvents.TRIGGER_INAPP_MESSAGING, null, true)
        FirebaseInAppMessaging.getInstance().triggerEvent(ToffeeEvents.TRIGGER_INAPP_MESSAGING)
    }
    
    var newIntent: Intent? = null
    
    private fun observeAppVersionUpdate(intent: Intent) {
        newIntent = intent
        observe(viewModel.updateStatusLiveData) {
            when (it) {
                is Success -> {
                    newIntent?.let {
                        handleIntent(it)
                    }
                }
                is Failure -> {
                    if (it.error is AppDeprecatedError) {
                        finish()
                        launchActivity<SplashScreenActivity> { flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK }
                    } else {
                        newIntent?.let {
                            handleIntent(it)
                        }
                    }
                }
            }
        }
    }
    
    private fun initMqtt() {
        if (mPref.isVerifiedUser && mPref.mqttIsActive && mPref.isMqttRealtimeSyncActive) {
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
    }
    
    private fun initConvivaSdk() {
        runCatching {
            if (BuildConfig.DEBUG) {
//                val settings: Map<String, Any> = mutableMapOf(
//                    ConvivaSdkConstants.GATEWAY_URL to BuildConfig.CONVIVA_GATEWAY_URL,
//                    ConvivaSdkConstants.LOG_LEVEL to ConvivaSdkConstants.LogLevel.DEBUG
//                )
//                ConvivaAnalytics.init(applicationContext, BuildConfig.CONVIVA_CUSTOMER_KEY_TEST, settings)
//                val tracker: TrackerController? = ConvivaAppAnalytics.createTracker(
//                    applicationContext,
//                    BuildConfig.CONVIVA_CUSTOMER_KEY_TEST,
//                    "Toffee Android"
//                )
//                tracker?.subject?.userId = mPref.customerId.toString()
            } else {
                ConvivaAnalytics.init(applicationContext, BuildConfig.CONVIVA_CUSTOMER_KEY_PROD)
                val tracker: TrackerController? = ConvivaAppAnalytics.createTracker(
                    applicationContext,
                    BuildConfig.CONVIVA_CUSTOMER_KEY_PROD,
                    "Toffee Android"
                )
                tracker?.subject?.userId = mPref.customerId.toString()
            }
            ConvivaHelper.init(applicationContext, true)
        }
    }
    
    private fun startBubbleService() {
        if (!BaseBubbleService.isForceClosed && mPref.isBubbleActive && mPref.isBubbleEnabled) {
            if (mPref.bubbleType == FIFA.value && mPref.isFifaBubbleActive) {
                startFifaBubbleService()
            } else if (mPref.bubbleType == RAMADAN.value) {
                startRamadanBubbleService()
            }
        }
    }
    
    private fun startFifaBubbleService() {
        runCatching {
            bubbleFifaIntent = Intent(this, BubbleServiceV2::class.java)
            if (!hasDefaultOverlayPermission() && !Settings.canDrawOverlays(this) && mPref.bubbleDialogShowCount < 5) {
                displayMissingOverlayPermissionDialog()
            } else {
                bubbleFifaIntent?.let { stopService(it) }
            }
        }
    }
    
    private fun startRamadanBubbleService() {
        observe(viewModel.ramadanScheduleLiveData) {
            when(it) {
                is Success -> {
                    it.data.ifNotNullOrEmpty { ramadanSchedules ->
                        ramadanSchedules.find {
                            Utils.dateToStr(mPref.getSystemTime()) == Utils.dateToStr(Utils.getDate(it.sehriStart))
                        }?.let {
                            runCatching {
                                mPref.ramadanScheduleLiveData.value = ramadanSchedules.toList()
                                bubbleRamadanIntent = Intent(this, BubbleServiceRamadan::class.java)
                                if (!hasDefaultOverlayPermission() && !Settings.canDrawOverlays(this) && mPref.bubbleDialogShowCount < 5) {
                                    displayMissingOverlayPermissionForRamadanDialog()
                                } else {
                                    bubbleRamadanIntent?.let { startService(it) }
                                }
                            }
                        }
                    }
                }
                is Failure -> {
                    Log.i(TAG, "startRamadanBubbleService: ${it.error.msg}")
                }
            }
        }
        viewModel.getRamadanScheduleList()
    }
    
    private fun displayMissingOverlayPermissionDialog() {
        mPref.bubbleDialogShowCount++
        ToffeeAlertDialogBuilder(this,
            title = getString(R.string.missing_overlay_permission_dialog_title),
            text = getString(R.string.missing_overlay_permission_dialog_message),
            icon = R.drawable.ic_not_verified,
            positiveButtonTitle = "Allow",
            positiveButtonListener = {
                requestOverlayPermission()
                it?.dismiss()
            },
            negativeButtonTitle = "Cancel",
            negativeButtonListener = {
                it?.dismiss()
            }).create().show()
    }
    
    private fun displayMissingOverlayPermissionForRamadanDialog() {
        mPref.bubbleDialogShowCount++
        ToffeeAlertDialogBuilderTypeThree(this,
            title = getString(R.string.missing_overlay_permission_Ramadan_dialog_title),
            text = getString(R.string.missing_overlay_permission_Ramadan_dialog_message),
            icon = R.drawable.ic_error_ramadan,
            positiveButtonTitle = "Allow",
            positiveButtonListener = {
                requestOverlayRamadanPermission()
                it?.dismiss()
            },
            negativeButtonTitle = "Cancel",
            negativeButtonListener = {
                it?.dismiss()
            }).create().show()
    }
    
    private val startForOverlayPermission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (!hasDefaultOverlayPermission() && !Settings.canDrawOverlays(this)) {
            if (mPref.bubbleDialogShowCount < 5) {
                displayMissingOverlayPermissionDialog()
            }
        } else {
            bubbleFifaIntent?.let { startService(it) }
        }
    }
    
    private val startForOverlayRamadanPermission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (!hasDefaultOverlayPermission() && !Settings.canDrawOverlays(this)) {
            if (mPref.bubbleDialogShowCount < 5) {
                displayMissingOverlayPermissionForRamadanDialog()
            }
        } else {
            bubbleRamadanIntent?.let { startService(it) }
        }
    }
    
    private fun requestOverlayPermission() {
        runCatching {
            if (!hasDefaultOverlayPermission() && !Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startForOverlayPermission.launch(intent)
            }
        }
    }
    
    private fun requestOverlayRamadanPermission() {
        runCatching {
            if (!hasDefaultOverlayPermission() && !Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startForOverlayRamadanPermission.launch(intent)
            }
        }
    }
    
    /**
     * restrict users from watching specific channel/video if the server threshold exceeds.
     * this is configured from firebase firestore to restrict users for specific content and time.
     */
    private fun observeCircuitBreaker() {
        if (mPref.isCircuitBreakerActive) {
            mPref.circuitBreakerFirestoreCollectionName?.ifNotNullOrBlank {
                lifecycleScope.launch {
                    runCatching {
                        val db = Firebase.firestore
                        db.collection(it).addSnapshotListener { value, error ->
                            error?.let {
                                return@addSnapshotListener
                            }
                            circuitBreakerDataList.clear()
                            value?.let {
                                for (doc in value) {
                                    val contentId = doc.getLong("content_id")?.toString()
                                    val isActive = doc.getBoolean("is_active")
                                    val updatedAt = doc.getDate("updated_at")
                                    val expiredAt = doc.getDate("expired_at")
                                    if (contentId != null && isActive != null && updatedAt != null && expiredAt != null) {
                                        val data = CircuitBreakerData(isActive, updatedAt, expiredAt)
                                        circuitBreakerDataList[contentId] = data
                                    }
                                }
                            }
                        }
                    }.onFailure {
                        val message = it.message
                        Log.i(TAG, "observeCircuitBreaker: $message")
                    }
                }
            }
        }
    }
    
    private fun onLoginSuccess() {
        observe(viewModel.postLoginEvent) {
            initDrawer()
            initSideNav()
            loadUserInfo()
            observe(mPref.profileImageUrlLiveData) {
                binding.root.findViewById<View>(R.id.action_avatar)?.findViewById<ImageView>(R.id.view_avatar)?.let { profileImageView ->
                    when (it) {
                        is String -> bindingUtil.bindRoundImage(profileImageView, it)
                        is Int -> bindingUtil.loadImageFromResource(profileImageView, it)
                    }
                }
            }
            lifecycleScope.launch {
                if (mPref.doActionBeforeReload.value == true) {
                    mPref.postLoginEventAction.value?.invoke()
                    delay(300)
                    if (mPref.shouldReloadAfterLogin.value == true) {
                        handleRefreshPageOnLogin()
                    }
                } else {
                    if (mPref.shouldReloadAfterLogin.value == true) {
                        handleRefreshPageOnLogin()
                    }
                    mPref.postLoginEventAction.value?.invoke()
                }
                mPref.shouldReloadAfterLogin.value = false
            }
            initMqtt()
        }
    }
    
    private fun handleRefreshPageOnLogin() {
        mPref.preLoginDestinationId.value?.let {
            if (it == id.menu_channel) {
                navController.popBackStack(it, true)
                val isMyChannel = channelOwnerId == mPref.customerId
                navController.navigateTo(Uri.parse("app.toffee://ugc_channel/$channelOwnerId/$isMyChannel"))
            } else {
                navController.popBackStack(it, true)
                navController.navigateTo(it)
            }
        }
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
                        clearDataOnLogOut()
                        
                        if (mPref.shouldIgnoreReloadAfterLogout.value != true) {
                            handleReloadPageOnLogout()
                        }
                        mPref.shouldIgnoreReloadAfterLogout.value = false
                        viewModel.isLogoutCompleted.value = true
                        if (player?.currentMediaItem?.getChannelMetadata(player)?.urlTypeExt == PREMIUM) {
                            destroyPlayer()
                        }
                        
//                        navController.popBackStack(R.id.menu_feed, false).let {
//                            recreate()
//                        }
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
    
    private fun clearDataOnLogOut() {
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
        mPref.lastLoginDateTime = ""
        cacheManager.clearAllCache()
        mPref.isVerifiedUser = false
        mPref.isChannelDetailChecked = false
        mPref.isMnpStatusChecked = false
        lifecycleScope.launch {
            tvChannelsRepo.deleteAllRecentItems()
        }
        appScope.launch { favoriteDao.deleteAll() }
        mqttService.destroy()
        
        UploadService.stopAllUploads()
        initSideNav()
        mPref.profileImageUrlLiveData.postValue(drawable.ic_menu_profile)
    }
    
    private fun handleReloadPageOnLogout() {
        when (navController.currentDestination?.id) {
            id.menu_channel -> {
                if (channelOwnerId == mPref.customerId || channelOwnerId == 0) {
                    reloadCurrentPage()
                } else {
                    navController.popBackStack()
                    val isMyChannel = channelOwnerId == mPref.customerId
                    navController.navigateTo(Uri.parse("app.toffee://ugc_channel/$channelOwnerId/$isMyChannel"))
                }
            }
            
            id.menu_playlist,
            id.menu_favorites,
            id.menu_activities,
            id.profileFragment,
            id.upload_minimize,
            id.menu_subscriptions,
            id.editUploadInfoFragment,
            id.myChannelEditDetailFragment,
            id.myChannelVideosEditFragment,
            -> {
                navController.popBackStack()
                reloadCurrentPage()
            }
            
            id.editProfileFragment,
            id.userPlaylistVideos,
            -> {
                navController.popBackStack()
                navController.popBackStack()
                reloadCurrentPage()
            }
            
            else -> {
                reloadCurrentPage()
            }
        }
    }
    
    private fun reloadCurrentPage() {
        navController.currentDestination?.id?.let {
            navController.popBackStack()
            navController.navigateTo(it)
        }
    }
}