package com.banglalink.toffee.ui.home

import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Path
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.util.Xml
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
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
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.apiservice.ApiRoutes
import com.banglalink.toffee.data.database.dao.FavoriteItemDao
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.data.repository.NotificationInfoRepository
import com.banglalink.toffee.data.repository.UploadInfoRepository
import com.banglalink.toffee.databinding.ActivityMainMenuBinding
import com.banglalink.toffee.di.AppCoroutineScope
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.*
import com.banglalink.toffee.model.Resource.*
import com.banglalink.toffee.mqttservice.ToffeeMqttService
import com.banglalink.toffee.ui.category.drama.EpisodeListFragment
import com.banglalink.toffee.ui.channels.AllChannelsViewModel
import com.banglalink.toffee.ui.channels.ChannelFragmentNew
import com.banglalink.toffee.ui.common.Html5PlayerViewActivity
import com.banglalink.toffee.ui.mychannel.MyChannelPlaylistVideosFragment
import com.banglalink.toffee.ui.player.PlayerPageActivity
import com.banglalink.toffee.ui.player.PlaylistItem
import com.banglalink.toffee.ui.player.PlaylistManager
import com.banglalink.toffee.ui.profile.ViewProfileViewModel
import com.banglalink.toffee.ui.search.SearchFragment
import com.banglalink.toffee.ui.splash.SplashScreenActivity
import com.banglalink.toffee.ui.upload.UploadProgressViewModel
import com.banglalink.toffee.ui.upload.UploadStateManager
import com.banglalink.toffee.ui.upload.UploadStatus
import com.banglalink.toffee.ui.widget.DraggerLayout
import com.banglalink.toffee.ui.widget.showDisplayMessageDialog
import com.banglalink.toffee.ui.widget.showSubscriptionDialog
import com.banglalink.toffee.util.*
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.util.Util
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
import com.suke.widget.SwitchButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import net.gotev.uploadservice.UploadService
import org.xmlpull.v1.XmlPullParser
import java.util.*
import javax.inject.Inject

const val PLAY_IN_WEB_VIEW = 1
const val OPEN_IN_EXTERNAL_BROWSER = 2
const val IN_APP_UPDATE_REQUEST_CODE = 0x100

@AndroidEntryPoint
class HomeActivity :
    PlayerPageActivity(),
    SearchView.OnQueryTextListener,
    DraggerLayout.OnPositionChangedListener,
    FragmentManager.OnBackStackChangedListener
{
    private var channelOwnerId: Int = 0
    private var searchView: SearchView? = null
    private var notificationBadge: View? = null
    lateinit var binding: ActivityMainMenuBinding
    private lateinit var drawerHelper: DrawerHelper
    @Inject lateinit var cacheManager: CacheManager
    private lateinit var navController: NavController
    @Inject lateinit var favoriteDao: FavoriteItemDao
    @Inject lateinit var mqttService: ToffeeMqttService
    private val viewModel: HomeViewModel by viewModels()
    @Inject lateinit var uploadRepo: UploadInfoRepository
    private lateinit var appbarConfig: AppBarConfiguration
    @Inject lateinit var uploadManager: UploadStateManager
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isDisableScreenshot = !(mPref.screenCaptureEnabledUsers.contains(cPref.deviceId) || mPref.screenCaptureEnabledUsers.contains(mPref.customerId.toString()))
        //disable screen capture
        if (! BuildConfig.DEBUG && isDisableScreenshot) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
        cPref.isAlreadyForceLoggedOut = false
//        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.tbar.toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

//        if(savedInstanceState == null) {
            setupNavController()
//        }

        initializeDraggableView()
        initDrawer()
        initLandingPageFragmentAndListenBackStack()
        showRedeemMessageIfPossible()

        binding.uploadButton.setOnClickListener {
            checkVerification {
                checkChannelDetailAndUpload()
            }
        }
        
        if(mPref.mqttClientId.startsWith("_") || mPref.mqttClientId.substringBefore("_") != mPref.phoneNumber) {
            mPref.mqttIsActive = false
            mPref.mqttHost = ""
            mPref.mqttClientId = ""
            mPref.mqttUserName = ""
            mPref.mqttPassword = ""
        }
        
        observe(viewModel.fragmentDetailsMutableLiveData) {
            val cp = player
//            if(cp is CastPlayer) {
//                cp.getItem()
//            }
//            if(player is CastPlayer &&
//                !(player?.playbackState != Player.STATE_ENDED &&
//                        player?.playbackState != Player.STATE_IDLE)) {
//                val channelInfo = when (it) {
//                    is ChannelInfo -> {
//                        it
//                    }
//                    is PlaylistPlaybackInfo -> {
//                        it.currentItem
//                    }
//                    is SeriesPlaybackInfo -> {
//                        it.currentItem
//                    }
//                    else -> null
//                }
//
//                Log.e("CAST_T", "${player?.currentMediaItem?.playbackProperties?.tag}")
//
//                VelBoxAlertDialogBuilder(this).apply {
//                    setTitle("Remote play")
//                    setText("Play ${channelInfo?.program_name} on remote player?")
//                    setPositiveButtonListener("Play") { dialog->
//                        onDetailsFragmentLoad(it)
//                        dialog?.dismiss()
//                    }
//                    setNegativeButtonListener("Cancel") { dialog->
//                        dialog?.dismiss()
//                    }
//                }
//                .create()
//                .show()
//            } else {
                onDetailsFragmentLoad(it)
//            }
        }

        /*observe(viewModel.userChannelMutableLiveData) {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.content_viewer)
            if (currentFragment !is UserChannelHomeFragment) {
                loadFragmentById( R.id.content_viewer, UserChannelHomeFragment()
                    , UserChannelHomeFragment::class.java.name
                )
            }
            binding.drawerLayout.closeDrawers()
            minimizePlayer()
        }*/


//        observe(viewModel.viewAllCategories) {
//            val currentFragment = supportFragmentManager.findFragmentById(R.id.content_viewer)
//            if (currentFragment !is AllCategoriesFragment) {
//                loadFragmentById(
//                    R.id.content_viewer, AllCategoriesFragment(), AllCategoriesFragment::class.java.getName()
//                )
//            }
//            binding.drawerLayout.closeDrawers()
//            minimizePlayer()
//        }

//        observe(viewModel.openCategoryLiveData) {
//            val currentFragment = supportFragmentManager.findFragmentById(R.id.content_viewer)
//            if (currentFragment !is CategoryDetailsFragment) {
//                loadFragmentById( R.id.content_viewer, CategoryDetailsFragment.newInstance(it)
//                    , CategoryDetailsFragment::class.java.getName())
//            }
//            binding.drawerLayout.closeDrawers()
//            minimizePlayer()
//        }

        observe(viewModel.viewAllVideoLiveData) {
//            drawerHelper.onMenuClick(NavigationMenu(ID_VIDEO, "All Videos", 0, listOf(), false))
        }

        observe(mPref.sessionTokenLiveData){
            if(binding.draggableView.visibility == View.VISIBLE){
                updateStartPosition()//we are saving the player start position so that we can start where we left off for VOD.
                reloadChannel()
            }
        }

        observe(mPref.viewCountDbUrlLiveData){
            if(it.isNotEmpty()){
                viewModel.populateViewCountDb(it)
            }
        }

        observe(mPref.reactionStatusDbUrlLiveData){
            if(it.isNotEmpty()){
                viewModel.populateReactionStatusDb(it)
            }
        }
        
        observe(mPref.subscriberStatusDbUrlLiveData){
            if(it.isNotEmpty()){
                viewModel.populateSubscriptionCountDb(it)
            }
        }
        
        observe(mPref.shareCountDbUrlLiveData){
            if(it.isNotEmpty()){
                viewModel.populateShareCountDb(it)
            }
        }
        
        observe(mPref.forceLogoutUserLiveData){
            if (it) {
                mPref.clear()
                UploadService.stopAllUploads()
                launchActivity<SplashScreenActivity>()
                finish()
            }
        }
        
//        observe(mPref.reactionDbUrlLiveData){
//        if(!mPref.hasReactionDb){
//            viewModel.populateReactionDb("url")
//        }
//        }

        observe(viewModel.addToPlayListMutableLiveData) { item ->
//            val playListItems = item.filter {
//                !it.isLive
//            }.map {
//                val uriStr = Channel.createChannel(it).getContentUri(this)
//                MediaItem.fromUri(uriStr)
//            }

            setPlayList(item)
        }

        observe(viewModel.notificationUrlLiveData){
            handleDeepLink(it)
        }
        
        observe(viewModel.shareContentLiveData) { channelInfo ->
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            sharingIntent.putExtra(
                Intent.EXTRA_TEXT,
                channelInfo.video_share_url
            )
            startActivity(Intent.createChooser(sharingIntent, "Share via"))
            viewModel.sendShareLog(channelInfo)
        }
        
        if (!isChannelComplete() && mPref.isVerifiedUser) {
            viewModel.getChannelDetail(mPref.customerId)
            observe(profileViewModel.loadCustomerProfile()) {
                if (it is Success) {
                    profileViewModel.profileForm.value = it.data
                }
            }
        }
        
        if(intent.hasExtra(INTENT_PACKAGE_SUBSCRIBED)){
            handlePackageSubscribe()
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
                        
                            if (mPref.mqttIsActive) {
                                appScope.launch {
                                    val mqttDir = withContext(Dispatchers.IO + Job()) {
                                        val mqttTag = "MqttConnection"
                                        var tempDir = getExternalFilesDir(mqttTag)
                                        if (tempDir == null) {
                                            tempDir = getDir(mqttTag, Context.MODE_PRIVATE)
                                        }
                                        tempDir
                                    }
                                    if(mqttDir != null) {
                                        mqttService.initialize()
                                    }
                                }
                            }
                        }
                    }
                    is Failure -> {
                        Log.e("MQTT_", "onCreate: ${it.error.msg}")
                    }
                }
            }
            viewModel.getMqttCredential()
        }
        else {
            if (mPref.mqttIsActive) {
                mqttService.initialize()
            }
        }
    }

    private lateinit var appUpdateManager: AppUpdateManager
    
    private val appUpdateListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            showToast("Toffee updated successfully")
        }
    }

    private fun inAppUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask: Task<AppUpdateInfo> = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.FLEXIBLE,
                        this,
                        IN_APP_UPDATE_REQUEST_CODE)
                }
                catch (e: SendIntentException) {
                    e.printStackTrace()
                }
            }
        }

        appUpdateManager.registerListener(appUpdateListener)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IN_APP_UPDATE_REQUEST_CODE) {
            Log.e(TAG, "Start Download")
            if (resultCode != RESULT_OK) {
                Log.e(TAG, "Download Failed")
            }
        }
    }
    
    fun checkChannelDetailAndUpload() {
        if (!mPref.isChannelDetailChecked) {
            observe(viewModel.myChannelDetailResponse) {
                when(it) {
                    is Success -> showUploadDialog()
                    is Failure -> showToast("Operation failed")
                }
            }
            viewModel.getChannelDetail(mPref.customerId)
        }
        else {
            showUploadDialog()
        }
    }
    
    private fun showUploadDialog(): Boolean {
        if (isChannelComplete()){
            if (navController.currentDestination?.id == R.id.uploadMethodFragment) {
                navController.popBackStack()
                return true
            }
            lifecycleScope.launch {

                if (uploadRepo.getActiveUploadsList().isNotEmpty()) {
                    return@launch
                }
                navController.navigate(R.id.uploadMethodFragment)
            }
        }
        else{
            if (navController.currentDestination?.id == R.id.bottomSheetUploadFragment) {
                navController.popBackStack()
                return true
            }
            lifecycleScope.launch {

                if (uploadRepo.getActiveUploadsList().isNotEmpty()) {
                    return@launch
                }
                navController.navigate(R.id.bottomSheetUploadFragment)
            }
        }

        return false
    }

    @ExperimentalCoroutinesApi
    private fun watchConnectionChange() {
        lifecycleScope.launch {
            uploadManager.checkUploadStatus(false)
        }
        lifecycleScope.launch {
            connectionWatcher.watchNetwork().collect {
                if(it) {
                    uploadManager.checkUploadStatus(true)
                }
            }
        }
    }

    private fun configureBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.homeBottomSheet.bottomSheet)
        if(requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            binding.homeBottomSheet.bottomSheet.hide()
        } else {
            binding.homeBottomSheet.bottomSheet.show()
        }
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED && binding.playerView.isControllerHidden) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
//                Log.e("SLIDE", slideOffset.toString())
                binding.playerView.moveController(slideOffset)
            }
        })

        window.decorView.setOnSystemUiVisibilityChangeListener {
//            toggleNavigations(it and View.SYSTEM_UI_FLAG_FULLSCREEN == View.SYSTEM_UI_FLAG_FULLSCREEN)
            val isFullScreen = it and View.SYSTEM_UI_FLAG_FULLSCREEN == View.SYSTEM_UI_FLAG_FULLSCREEN
            if(!isFullScreen) {
                updateFullScreenState()
            }
        }
    }

    private fun observeNotification() {
        lifecycleScope.launchWhenStarted {
            notificationRepo.getUnseenNotificationCount().collect {
                if(it > 0) {
                    notificationBadge?.visibility = View.VISIBLE
                } else {
                    notificationBadge?.visibility = View.GONE
                }
            }
        }
    }

    fun rotateFab(isRotate: Boolean) {
        ViewCompat.animate(binding.uploadButton)
            .rotation(if (isRotate) 135.0F else 0.0F)
            .withEndAction {
                if (isRotate) {
                    val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.menuColorSecondaryDark))
                    binding.uploadButton.backgroundTintList = colorStateList
                    binding.uploadButton.imageTintList = colorStateList
                } else {
                    val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorAccent2))
                    binding.uploadButton.backgroundTintList = colorStateList
                    binding.uploadButton.imageTintList = colorStateList
                }
            }
            .withLayer()
            .setDuration(300L)
            .setInterpolator(AccelerateInterpolator())
            .start()
    }

    fun getNavController() = navController
    fun getHomeViewModel() = viewModel

    private val destinationChangeListener =
        NavController.OnDestinationChangedListener { controller, _, _ ->
//            supportFragmentManager.popBackStack(R.id.content_viewer, POP_BACK_STACK_INCLUSIVE)

            if(binding.draggableView.isMaximized()) {
                minimizePlayer()
            }
            closeSearchBarIfOpen()

            // For firebase screenview logging
            if (controller.currentDestination is FragmentNavigator.Destination) {
                val currentFragmentClassName =
                    (controller.currentDestination as FragmentNavigator.Destination)
                        .className
                        .substringAfterLast(".")

                val bundle = Bundle()
                bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, currentFragmentClassName)
                FirebaseAnalytics.getInstance(this@HomeActivity)
                    .logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
            }

            binding.tbar.toolbar.setNavigationIcon(R.drawable.ic_toffee)
        }

    private fun setupNavController() {
        Log.e("NAV", "SetupNavController")

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.home_nav_host) as NavHostFragment
        navController = navHostFragment.navController

        appbarConfig = AppBarConfiguration(
            setOf(
                R.id.menu_feed,
                R.id.menu_tv,
                R.id.menu_activities,
                R.id.myChannelHomeFragment,

//                R.id.menu_all_tv_channel,
                R.id.menu_favorites,
                R.id.menu_settings,
                R.id.menu_subscriptions,
                R.id.menu_invite,
                R.id.menu_redeem,
                R.id.menu_creators_policy,
            ),
            binding.drawerLayout
        )
//        setupActionBarWithNavController(navController, appbarConfig)
//        NavigationUI.setupActionBarWithNavController(this, navController, appbarConfig)
        binding.tbar.toolbar.setupWithNavController(navController, appbarConfig)
        binding.tbar.toolbar.setNavigationIcon(R.drawable.ic_toffee)
        binding.sideNavigation.setupWithNavController(navController)
        binding.tabNavigator.setupWithNavController(navController)
//        binding.tabNavigator.setOnNavigationItemReselectedListener {
//
//        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomAppBar) { _, _ ->
             WindowInsetsCompat.CONSUMED
        }

        binding.sideNavigation.setNavigationItemSelectedListener {
            drawerHelper.handleMenuItemById(it)
        }

        navController.addOnDestinationChangedListener(destinationChangeListener)

//        binding.sideNavigation.setNavigationItemSelectedListener {
//            binding.drawerLayout.closeDrawers()
//            return@setNavigationItemSelectedListener false
//        }
    }

    override fun onResume() {
        super.onResume()
        binding.playerView.setPlaylistListener(this)
        binding.playerView.addPlayerControllerChangeListener(this)
        resetPlayer()
        binding.playerView.resizeView(calculateScreenWidth())
        updateFullScreenState()
    }

    override fun resetPlayer() {
        binding.playerView.setPlayer(player)
        if(player is CastPlayer) {
            val deviceName = castContext?.sessionManager?.currentCastSession?.castDevice?.friendlyName
            binding.playerView.showCastingText(true, deviceName)
        } else {
            binding.playerView.showCastingText(false)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
//        setupNavController()
    }

    override fun onPause() {
        super.onPause()
        binding.playerView.clearListeners()
        if (Util.SDK_INT <= 23) {
            binding.playerView.setPlayer(null)
        }
    }

    override fun onStart() {
        super.onStart()
        if(playlistManager.getCurrentChannel() != null) {
            maximizePlayer()
            loadDetailFragment(
                if(playlistManager.playlistId >= 0) {
                    PlaylistItem(playlistManager.playlistId, playlistManager.getCurrentChannel()!!)
                } else {
                    playlistManager.getCurrentChannel()!!
                }
            )
        }
        if (mPref.isVerifiedUser) {
            initMqtt()
        }
    }

    override fun resumeCastSession(info: ChannelInfo) {
        maximizePlayer()
        loadDetailFragment(info)
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            binding.playerView.setPlayer(null)
        }
        mqttService.destroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        /*
        viewDragHelper send onViewMinimize/Maximize event when start the transition
        it's not possible to get transition(animation) end listener
        If phone is already in landscape mode, it starts to move to full screen while drag transition is on going
        so player can't reset scale completely. Manually resetting player scale value
         */
        /*
        viewDragHelper send onViewMinimize/Maximize event when start the transition
        it's not possible to get transition(animation) end listener
        If phone is already in landscape mode, it starts to move to full screen while drag transition is on going
        so player can't reset scale completely. Manually resetting player scale value
         */
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if(playlistManager.getCurrentChannel()?.isLive == true) {
                binding.homeBottomSheet.bottomSheet.visibility = View.VISIBLE
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
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
        return NavigationUI.navigateUp(navController, appbarConfig)
                || super.onSupportNavigateUp()
    }

    private fun updateFullScreenState() {
        val state =
            resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE ||
                    binding.playerView.isFullScreen

        binding.playerView.onFullScreen(state)
        binding.playerView.resizeView(calculateScreenWidth(), state)
        Utils.setFullScreen(this, state)// || binding.playerView.channelType != "LIVE")
        toggleNavigation(state)
    }

    private fun toggleNavigation(state: Boolean) {
        if(state) {
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
        val display: Display = windowManager.defaultDisplay
        val size = Point()
        display.getRealSize(size)
        return size
    }


    private fun handleSharedUrl(intent: Intent) {
        lifecycleScope.launch {
            var appLinkUriStr: String? = null
            try{
                val appLinkUri = AppLinks.getTargetUrlFromInboundIntent(this@HomeActivity, intent)
                if(appLinkUri != null && appLinkUri.host != "toffeelive.com") {
                    appLinkUriStr = viewModel.fetchRedirectedDeepLink(appLinkUri.toString())
                }
            } catch (ex: Exception) {
                ToffeeAnalytics.logException(ex)
            }
            if(!appLinkUriStr.isNullOrEmpty()) {
                handleDeepLink(appLinkUriStr)
            }
            else {
                val uri = intent.data
                if (uri != null) {
                    val strUri = uri.toString()
                    handleDeepLink(strUri)
                }
            }
        }
    }

    private fun handleDeepLink(url: String){
//        https://toffeelive.com/#video/0d52770e16b19486d9914c81061cf2da
        lifecycleScope.launch {
            try{
                var isDeepLinkHandled = false
                val route = inAppMessageParser.parseUrlV2(url)
                route?.let {
                    ToffeeAnalytics.logBreadCrumb("Trying to open ${it.name}")
                    when(it.destId) {
                        is Uri -> navController.navigate(it.destId, it.options, it.navExtra)
                        is Int -> navController.navigate(it.destId, it.args, it.options, it.navExtra)
                    }
                    isDeepLinkHandled = true
                }

                if(!isDeepLinkHandled){
                    ToffeeAnalytics.logBreadCrumb("Trying to open individual item")
                    val hash = url.substring(url.lastIndexOf("/") + 1)
                    observe(viewModel.getShareableContent(hash)){ channelResource ->
                        when(channelResource){
                            is Success -> {
                                channelResource.data?.let {
                                    onDetailsFragmentLoad(it)
                                }
                            }
                        }
                    }
                }
            }catch (e: Exception){
                ToffeeAnalytics.logBreadCrumb("2. Failed to handle depplink $url")
                ToffeeAnalytics.logException(e)
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            query?.let { handleVoiceSearchEvent(it) }

        }
        if(intent.hasExtra(INTENT_PACKAGE_SUBSCRIBED)){
            handlePackageSubscribe()
        }
        handleSharedUrl(intent)
    }

    private fun navigateToSearch(query: String?) {
//        if(navController.currentDestination?.id != R.id.searchFragment) {
        navController.popBackStack(R.id.searchFragment, true)
        navController.navigate(Uri.parse("app.toffee://search/$query"))
//        navController.navigate(R.id.searchFragment, Bundle().apply {
//            putString(SearchFragment.SEARCH_KEYWORD, query)
//        })
//        }
    }

    private fun handleVoiceSearchEvent(query: String){
        if (!TextUtils.isEmpty(query)) {
            navigateToSearch(query)
        }
        if (searchView != null) {
            searchView!!.setQuery(query.lowercase(), false)
            searchView!!.clearFocus()
        }
    }

    private fun handlePackageSubscribe(){
//        viewModel.getChannelByCategory(0)//reload the channels
        //Clean up stack upto landingPageFragment inclusive
        supportFragmentManager.popBackStack(
            LandingPageFragment::class.java.name,
            POP_BACK_STACK_INCLUSIVE
        )
        //Reload it again so that we can get updated VOD/Popular channels/Feature contents
//        supportFragmentManager.beginTransaction().replace(R.id.content_viewer, LandingPageFragment())
//            .addToBackStack(LandingPageFragment::class.java.name).commit()
    }

    private fun loadChannel(channelInfo: ChannelInfo) {
        viewModel.sendViewContentEvent(channelInfo)
        if(channelInfo.isLive) {
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

        channelInfo?.let {
            when{
                it.urlType == PLAY_IN_WEB_VIEW->{
                    heartBeatManager.triggerEventViewingContentStart(it.id.toInt(), it.type ?: "VOD")
                    viewModel.sendViewContentEvent(it)
                    launchActivity<Html5PlayerViewActivity> {
                        putExtra(
                            Html5PlayerViewActivity.CONTENT_URL,
                            it.getHlsLink()
                        )
                    }
                }
                it.urlType == OPEN_IN_EXTERNAL_BROWSER ->{
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(it.getHlsLink())
                        )
                    )
                }
                !((it.isPurchased || it.isPaidSubscribed) && !it.isExpired(Date())) && mPref.isSubscriptionActive == "true" ->{
                    showSubscribePackDialog()
                }
                else ->{
                    if(player is CastPlayer) {
                        maximizePlayer()
                    }
                    when (detailsInfo) {
                        is PlaylistPlaybackInfo -> {
                            loadPlayListItem(detailsInfo)
                        }
                        is SeriesPlaybackInfo -> {
                            loadDramaSeasonInfo(detailsInfo)
                        }
                        else -> {
                            loadChannel(it)
                        }
                    }
                    loadDetailFragment(detailsInfo)
                }
            }
        }
    }

    private fun showSubscribePackDialog(){
        showSubscriptionDialog(this) {
//            launchActivity<PackageListFragment>()
            if(navController.currentDestination?.id != R.id.menu_subscriptions) {
                navController.navigate(R.id.menu_subscriptions)
            }
        }
    }

    override fun playNext() {
        super.playNext()
        if(playlistManager.playlistId == -1L) {
            viewModel.fragmentDetailsMutableLiveData.postValue(playlistManager.getCurrentChannel())
            return
        }
        loadDetailFragment(
            PlaylistItem(playlistManager.playlistId, playlistManager.getCurrentChannel()!!)
        )
    }

    override fun playPrevious() {
        super.playPrevious()
        loadDetailFragment(
            PlaylistItem(playlistManager.playlistId, playlistManager.getCurrentChannel()!!)
        )
    }

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

    private fun loadDetailFragment(info: Any?){
        if(info is ChannelInfo) {
            if (info.isLive) {
                val fragment = supportFragmentManager.findFragmentById(R.id.details_viewer)
                if (fragment !is ChannelFragmentNew) {
                    loadFragmentById(
                        R.id.details_viewer, ChannelFragmentNew()
                    )
                }
            } else {
                loadFragmentById(
                    R.id.details_viewer,
                    CatchupDetailsFragment.createInstance(info)
                )
            }
        } else if(info is PlaylistPlaybackInfo) {
            val fragment = supportFragmentManager.findFragmentById(R.id.details_viewer)
            if (fragment !is MyChannelPlaylistVideosFragment || fragment.getPlaylistId() != info.getPlaylistIdLong()) {
                loadFragmentById(
                    R.id.details_viewer, MyChannelPlaylistVideosFragment.newInstance(info)
                )
            } else {
                fragment.setCurrentChannel(info.currentItem)
            }
        } else if(info is PlaylistItem) {
            val fragment = supportFragmentManager.findFragmentById(R.id.details_viewer)
            if (fragment is MyChannelPlaylistVideosFragment) {
                fragment.setCurrentChannel(info.channelInfo)
            } else if(fragment is EpisodeListFragment) {
                fragment.setCurrentChannel(info.channelInfo)
            }
        } else if(info is SeriesPlaybackInfo) {
            val fragment = supportFragmentManager.findFragmentById(R.id.details_viewer)
            if(fragment !is EpisodeListFragment
                || fragment.getSeriesId() != info.seriesId) {
                loadFragmentById(
                    R.id.details_viewer, EpisodeListFragment.newInstance(
                    info
                )
                )
            } else {
                fragment.setCurrentChannel(info.currentItem)
            }
        }
//        val frag = supportFragmentManager.findFragmentById(R.id.details_viewer)
//        if(frag !is ChannelViewFragment) {
//            supportFragmentManager.commit {
//                replace(
//                    R.id.details_viewer,
//                    ChannelViewFragment.newInstance(channelInfo)
//                )
//            }
//        }
    }
    private fun loadFragmentById(id: Int, fragment: Fragment, tag: String) {
        supportFragmentManager.popBackStack(
            LandingPageFragment::class.java.name,
            0
        )
        supportFragmentManager.beginTransaction()
            .replace(id, fragment).addToBackStack(tag).commit()
    }

    private fun loadFragmentById(id: Int, fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(id, fragment).commit()
    }

    private fun initializeDraggableView() {
        binding.draggableView.addOnPositionChangedListener(this)
        binding.draggableView.addOnPositionChangedListener(binding.playerView)

        binding.draggableView.visibility = View.GONE
        binding.draggableView.isClickable = true
    }

    private fun initDrawer(){
        drawerHelper = DrawerHelper(this, mPref, binding)
        drawerHelper.initDrawer()
    }

    private fun initSideNav() {
        val isBanglalinkNumber = mPref.isBanglalinkNumber
        if(isBanglalinkNumber != "true") {
            val subMenu = binding.sideNavigation.menu.findItem(R.id.ic_menu_internet_packs)
            subMenu?.isVisible = false
        }
        binding.sideNavigation.menu.findItem(R.id.menu_tv).isVisible = mPref.isAllTvChannelMenuEnabled
        if (!mPref.isVerifiedUser) {
            val logout = binding.sideNavigation.menu.findItem(R.id.menu_logout)
            logout?.isVisible = false
        }
//        else {
//            val verify = binding.sideNavigation.menu.findItem(R.id.menu_verfication)
//            verify?.isVisible = false
//        }
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
            }
            catch (e: java.lang.Exception) {
                e.printStackTrace()
                switch = SwitchMaterial(this)
            }
            finally {
                themeMenu.actionView = switch
                when(themeMenu.actionView){
                    is SwitchButton -> {
                        (themeMenu.actionView as SwitchButton).let {
                            val param = LinearLayout.LayoutParams(36.px, 22.px)
                            param.topMargin = 30
                            it.layoutParams = param
                            it.isChecked = isDarkEnabled
                            it.setOnCheckedChangeListener { _, isChecked ->
                                changeAppTheme(isChecked)
                            }
                        }
                    }
                    is SwitchMaterial -> {
                        (themeMenu.actionView as SwitchMaterial).let {
                            it.isChecked = isDarkEnabled
                            it.setOnCheckedChangeListener { _, isChecked ->
                                changeAppTheme(isChecked)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun changeAppTheme(isDarkEnabled: Boolean){
        if (isDarkEnabled) {
            cPref.appThemeMode = Configuration.UI_MODE_NIGHT_YES
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            cPref.appThemeMode = Configuration.UI_MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun observeInAppMessage(){
        FirebaseAnalytics.getInstance(this).logEvent("trigger_inapp_messaging", null)
        FirebaseInAppMessaging.getInstance().triggerEvent("trigger_inapp_messaging")
    }

    private fun initLandingPageFragmentAndListenBackStack(){
//        supportFragmentManager.beginTransaction().replace(R.id.content_viewer, LandingPageFragment())
//            .addToBackStack(LandingPageFragment::class.java.name).commit()

//        supportFragmentManager.beginTransaction().replace(R.id.content_viewer, UserActivitiesMainFragment())
//            .addToBackStack(UserActivitiesMainFragment::class.java.name).commit()

        supportFragmentManager.addOnBackStackChangedListener(this)
    }

    private fun showRedeemMessageIfPossible(){
        //show referral redeem msg if possible
        val msg = intent.getStringExtra(INTENT_REFERRAL_REDEEM_MSG)
        msg?.let {
            showDisplayMessageDialog(this, it)
        }
    }

    override fun onViewMinimize() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding.playerView.clearDebugWindow()
    }

    override fun onControllerVisible() {
        if(playlistManager.getCurrentChannel()?.isLive == true &&
            resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        } else {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    override fun onControllerInVisible() {
        if(bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    override fun onViewMaximize() {
        requestedOrientation = if(binding.playerView.isAutoRotationEnabled
            && !binding.playerView.isVideoPortrait)
            ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        else{
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
            delay(1_000)
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
                            "duration": 30
                        }
                    }
                """.trimIndent(), PlayerOverlayData::class.java)*/
            binding.playerView.showDebugOverlay(playerOverlayData!!, playlistManager.getCurrentChannel()?.id ?: "")
        
            if (playerOverlayData.params.position == "floating") {
                val debugOverlayView = binding.playerView.getDebugOverLay()
                debugOverlayView?.let {
                    val observer = object : ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            if (it.measuredWidth > 0) {
                                val width = it.measuredWidth.toFloat() + (10.px * 2)
                                val height = it.measuredHeight.toFloat() + (10.px * 2)
                            
                                val path = Path().apply {
                                    moveTo(0f, 0f)
                                    lineTo(binding.playerView.measuredWidth.toFloat() - width, 0f /*binding.playerView.measuredHeight.toFloat() - height*/)
                                }
                                ObjectAnimator.ofFloat(it.parent as View, View.X, View.Y, path).apply {
                                    duration = playerOverlayData.params.duration * 1_000
                                    repeatMode = ValueAnimator.REVERSE
                                    repeatCount = 2
                                    start()
                                }
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
       if(isAutoRotationEnabled && !binding.playerView.isVideoPortrait){
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
            showToast(getString(R.string.auto_rotation_on))
        } else{
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
            showToast(getString(R.string.auto_rotation_off))
        }
    }

    override fun onViewDestroy() {
        allChannelViewModel.selectedChannel.postValue(null)
        clearChannel()
        heartBeatManager.triggerEventViewingContentStop()
        binding.draggableView.animation = AnimationUtils.loadAnimation(
            this,
            android.R.anim.fade_out
        )
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        binding.draggableView.visibility = View.GONE
        binding.draggableView.resetImmediately()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
    
    override fun onDestroy() {
//        mqttService.destroy()
        navController.removeOnDestinationChangedListener(destinationChangeListener)
        appUpdateManager.unregisterListener(appUpdateListener)
        super.onDestroy()
    }
    
    fun handleExitApp() {
        AlertDialog.Builder(this, R.style.AlertDialogTheme)
            .setMessage(String.format(EXIT_FROM_APP_MSG, getString(R.string.app_name)))
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                observeLogout()
                viewModel.logoutUser()
            }
            .setNegativeButton(
                "No"
            ) { dialog, _ -> dialog.cancel() }
            .show()
    }

    private fun observeLogout() {
        observe(viewModel.logoutLiveData) {
            when(it) {
                is Success -> {
                    if (!it.data.verifyStatus) {
                        mPref.phoneNumber = ""
                        mPref.channelName = ""
                        mPref.channelLogo = ""
                        mPref.customerName = ""
                        mPref.customerEmail = ""
                        mPref.customerAddress = ""
                        mPref.customerDOB = ""
                        mPref.customerNID = ""
                        mPref.userImageUrl = null
                        mPref.isVerifiedUser = false
                        mPref.isChannelDetailChecked = false
                        mPref.mqttIsActive = false
                        mPref.mqttHost = ""
                        mPref.mqttClientId = ""
                        mPref.mqttUserName = ""
                        mPref.mqttPassword = ""
                        cacheManager.clearAllCache()
                        appScope.launch { favoriteDao.deleteAll() }
                        navController.popBackStack(R.id.menu_feed, false).let { 
                            recreate()
                        }
                    }
                }
                is Failure -> {
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
        requestedOrientation =
        if(!binding.playerView.isAutoRotationEnabled
            || binding.playerView.isFullScreen
            || binding.playerView.isVideoPortrait) {
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
        }
        else if (supportFragmentManager.backStackEntryCount == 1) { //home
            closeSearchBarIfOpen()
        }
    }

    private fun closeSearchBarIfOpen() {
        if(searchView?.isIconified == false) {
            searchView?.onActionViewCollapsed()
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        } else if (resources.configuration.orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else if(binding.playerView.isVideoPortrait && binding.playerView.isFullScreenPortrait()) {
            binding.playerView.isFullScreen = false
            updateFullScreenState()
        } else if (binding.draggableView.isMaximized() && binding.draggableView.visibility == View.VISIBLE) {
            if(mPref.isEnableFloatingWindow) {
                minimizePlayer()
            }
            else {
                destroyPlayer()
            }
        } else if(searchView?.isIconified == false) {
            closeSearchBarIfOpen()
        }
//        else if (supportFragmentManager.findFragmentById(R.id.content_viewer) is LandingPageFragment) {
//            val landingPageFragment =
//                supportFragmentManager.findFragmentById(R.id.content_viewer) as LandingPageFragment
//            if (!landingPageFragment.onBackPressed())
//                finish()
//        }
        else {
            super.onBackPressed()
        }
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

    private fun maximizePlayer() {
        binding.draggableView.maximize()
        binding.draggableView.visibility = View.VISIBLE
//        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (mPref.isVerifiedUser) {
            observe(mPref.profileImageUrlLiveData) {
                menu?.findItem(R.id.action_avatar)
                    ?.actionView?.findViewById<ImageView>(R.id.view_avatar)?.loadProfileImage(it)
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        
        if (item.itemId == android.R.id.home) {
            navController.navigate(R.id.menu_feed)
//            supportFragmentManager.popBackStack()
            return true
        } else if (item.itemId == R.id.action_avatar) {
            binding.drawerLayout.openDrawer(GravityCompat.END, true)
            return true
        }
        else if(item.itemId == R.id.action_notification){

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
//            if (supportFragmentManager.backStackEntryCount > 1) {
//                supportFragmentManager.popBackStack(
//                    SearchFragment::class.java.name,
//                    POP_BACK_STACK_INCLUSIVE
//                )
//                return@setOnCloseListener true
//            }
            navController.popBackStack(R.id.searchFragment, true)
            false
        }


        val searchBar: LinearLayout = searchView!!.findViewById(R.id.search_bar)
        searchBar.layoutTransition = LayoutTransition()
        //

        val mic = searchView!!.findViewById(androidx.appcompat.R.id.search_voice_btn) as ImageView
        mic.setImageResource(R.drawable.ic_menu_microphone)

        val close = searchView!!.findViewById(androidx.appcompat.R.id.search_close_btn) as ImageView
        close.setImageResource(R.drawable.ic_close)

        val searchIv = searchView!!.findViewById(androidx.appcompat.R.id.search_button) as ImageView
        searchIv.setImageResource(R.drawable.ic_menu_search)


        val searchBadgeTv =
            searchView?.findViewById(androidx.appcompat.R.id.search_badge) as TextView
        searchBadgeTv.background =
            ContextCompat.getDrawable(this@HomeActivity, R.drawable.ic_menu_search)

        val searchAutoComplete: AutoCompleteTextView =
            searchView!!.findViewById(androidx.appcompat.R.id.search_src_text)
        searchAutoComplete.apply {
            textSize = 18f
            setTextColor(
                ContextCompat.getColor(
                    this@HomeActivity,
                    R.color.searchview_input_text_color
                )
            )
            background =
                ContextCompat.getDrawable(this@HomeActivity, R.drawable.searchview_input_bg)
            hint = null
            setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_menu_search, 0)

            addTextChangedListener { text->
                val rightIcon = if(text?.length ?: 0 <= 0) R.drawable.ic_menu_search else 0
                if(compoundPaddingRight != rightIcon) {
                    setCompoundDrawablesWithIntrinsicBounds(0, 0, rightIcon, 0)
                }
            }
        }

        searchView?.setOnSearchClickListener {
            val searchFrag = supportFragmentManager.currentNavigationFragment
            if(searchFrag is SearchFragment) {
                searchFrag.getSearchString()?.let {
                    searchAutoComplete.setText(it)
                    searchAutoComplete.setSelection(searchAutoComplete.text.length)
                }
            }
        }

        val notificationActionView = menu.findItem(R.id.action_notification)?.actionView
        notificationBadge = notificationActionView?.findViewById<TextView>(R.id.notification_badge)
        notificationActionView?.setOnClickListener {
            if(navController.currentDestination?.id != R.id.notificationDropdownFragment) {
                navController.navigate(R.id.notificationDropdownFragment)
            }
        }
        searchView?.setOnQueryTextListener(this)

        val awesomeMenuItem = menu.findItem(R.id.action_avatar)
        val awesomeActionView = awesomeMenuItem.actionView
        awesomeActionView.setOnClickListener {  binding.drawerLayout.openDrawer(GravityCompat.END, true) }

        observeNotification()
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (!query.isNullOrBlank()) {
            navigateToSearch(query)
//            loadFragmentById(
//                R.id.content_viewer, SearchFragment.createInstance(query!!),
//                SearchFragment::class.java.name
//            )
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
        onViewMaximize()
        if(binding.playerView.isVideoPortrait && resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        updateFullScreenState()
    }

    private fun observeUpload2() {
        binding.homeMiniProgressContainer.addUploadInfoButton.setOnClickListener {
            viewModel.myChannelNavLiveData.value = MyChannelNavParams(mPref.customerId)
            /*if(navController.currentDestination?.id != R.id.myChannelHomeFragment) {
                navController.navigate(R.id.myChannelHomeFragment)
            }*/
        }

        binding.homeMiniProgressContainer.closeButton.setOnClickListener {
            lifecycleScope.launch {
                uploadRepo.getActiveUploadsList().let {
                    if(it.isNotEmpty()) {
                        uploadRepo.updateUploadInfo(it[0].apply {
                            this.status = UploadStatus.CLEARED.value
                        })
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            uploadViewModel.getActiveUploadList().collectLatest {
                Log.e("UPLOAD 2", "Collecting ->>> ${it.size}")
                if(it.isNotEmpty()) {
                    binding.homeMiniProgressContainer.root.isVisible = true
                    val upInfo = it[0]
                    when(upInfo.status){
                        UploadStatus.SUCCESS.value,
                        UploadStatus.SUBMITTED.value -> {
                            binding.homeMiniProgressContainer.miniUploadProgress.progress = 100
                            binding.homeMiniProgressContainer.addUploadInfoButton.isVisible = true
                            binding.homeMiniProgressContainer.closeButton.isVisible = true
                            binding.homeMiniProgressContainer.uploadSizeText.isInvisible = true
                            binding.homeMiniProgressContainer.miniUploadProgressText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_upload_done, 0, 0, 0)
                            binding.homeMiniProgressContainer.miniUploadProgressText.text = "Upload complete"
                            cacheManager.clearCacheByUrl(ApiRoutes.GET_MY_CHANNEL_VIDEOS)
                        }
                        UploadStatus.ADDED.value,
                        UploadStatus.STARTED.value -> {
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
    
    private fun observeMyChannelNavigation(){
        observe(viewModel.myChannelNavLiveData) {
            if (navController.currentDestination?.id != R.id.myChannelHomeFragment || channelOwnerId != it.channelOwnerId) {
                navController.navigate(Uri.parse("app.toffee://ugc_channel/${it.channelOwnerId}"))
//                channelOwnerId = it.channelOwnerId
//                navController.navigate(R.id.myChannelHomeFragment, Bundle().apply {
//                    putString(MyChannelHomeFragment.PAGE_TITLE, it.pageTitle)
//                    putInt(MyChannelHomeFragment.CHANNEL_OWNER_ID, it.channelOwnerId)
//                })
            } else{
                minimizePlayer()
            }
        }
    }
}
