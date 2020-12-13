package com.banglalink.toffee.ui.home

import android.animation.LayoutTransition
import android.app.SearchManager
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.HeartBeatManager
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.repository.NotificationInfoRepository
import com.banglalink.toffee.data.repository.UploadInfoRepository
import com.banglalink.toffee.databinding.ActivityMainMenuBinding
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.extension.loadProfileImage
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.*
import com.banglalink.toffee.ui.channels.AllChannelsViewModel
import com.banglalink.toffee.ui.channels.ChannelFragment
import com.banglalink.toffee.ui.common.Html5PlayerViewActivity
import com.banglalink.toffee.ui.landing.AllCategoriesFragment
import com.banglalink.toffee.ui.mychannel.MyChannelPlaylistVideosFragment
import com.banglalink.toffee.ui.player.PlayerPageActivity
import com.banglalink.toffee.ui.player.PlaylistItem
import com.banglalink.toffee.ui.search.SearchFragment
import com.banglalink.toffee.ui.splash.SplashScreenActivity
import com.banglalink.toffee.ui.subscription.PackageListActivity
import com.banglalink.toffee.ui.upload.UploadProgressViewModel
import com.banglalink.toffee.ui.upload.UploadStatus
import com.banglalink.toffee.ui.widget.DraggerLayout
import com.banglalink.toffee.ui.widget.showDisplayMessageDialog
import com.banglalink.toffee.ui.widget.showSubscriptionDialog
import com.banglalink.toffee.util.InAppMessageParser
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.UtilsKt
import com.google.android.exoplayer2.util.Util
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.inappmessaging.FirebaseInAppMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main_menu.*
import kotlinx.android.synthetic.main.home_mini_upload_progress.*
import kotlinx.android.synthetic.main.layout_appbar.view.*
import kotlinx.android.synthetic.main.player_bottom_sheet_layout.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

const val ID_CHANNEL = 12
const val ID_RECENT = 13
const val ID_FAV = 14
const val ID_SUBSCRIPTIONS = 15
const val ID_SUB_VIDEO = 16
const val ID_SETTINGS = 17
const val ID_ABOUT = 18
const val ID_LOGOUT = 19
const val ID_VIDEO = 20
const val ID_VOD = 21
const val ID_FAQ = 22
const val ID_INVITE_FRIEND = 23
const val ID_REDEEM_CODE = 24
const val ID_INTERNET_PACK = 25

const val PLAY_IN_WEB_VIEW = 1
const val OPEN_IN_EXTERNAL_BROWSER = 2
@AndroidEntryPoint
class HomeActivity :
    PlayerPageActivity(),
    FragmentManager.OnBackStackChangedListener,
    DraggerLayout.OnPositionChangedListener ,
    SearchView.OnQueryTextListener
{

    @Inject
    lateinit var uploadRepo: UploadInfoRepository

    @Inject
    lateinit var notificationRepo: NotificationInfoRepository

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private var notificationBadge: View? = null
    private var searchView: SearchView? = null
    lateinit var binding: ActivityMainMenuBinding
    private lateinit var drawerHelper: DrawerHelper
    private lateinit var inAppMessageParser: InAppMessageParser
//    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    companion object {
        const val INTENT_REFERRAL_REDEEM_MSG = "REFERRAL_REDEEM_MSG"
        const val INTENT_PACKAGE_SUBSCRIBED = "PACKAGE_SUBSCRIBED"
    }

    private lateinit var navController: NavController
    private lateinit var appbarConfig: AppBarConfiguration

    private val viewModel: HomeViewModel by viewModels()
    private val allChannelViewModel by viewModels<AllChannelsViewModel>()
    private val uploadViewModel by viewModels<UploadProgressViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //disable screen capture
//        if (!BuildConfig.DEBUG) {
//            window.setFlags(
//                WindowManager.LayoutParams.FLAG_SECURE,
//                WindowManager.LayoutParams.FLAG_SECURE
//            )
//        }
//        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_menu)
        setSupportActionBar(binding.tbar.toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        
        if(savedInstanceState == null) {
            setupNavController()
        }

        initializeDraggableView()
        initDrawer()
        initLandingPageFragmentAndListenBackStack()
        showRedeemMessageIfPossible()

        binding.uploadButton.setOnClickListener {
            if(navController.currentDestination?.id == R.id.uploadMethodFragment) {
                navController.popBackStack()
                return@setOnClickListener
            }
            lifecycleScope.launch {

                if(uploadRepo.getActiveUploadsList().isNotEmpty()) {
                    return@launch
                }

//                mPref.uploadId?.let {
//                    val uploads = uploadRepo.getUploadById(UtilsKt.stringToUploadId(it))
//                    if(uploads == null || uploads.status !in listOf(0, 1, 2, 3)) {
//                        mPref.uploadId = null
//                        navController.navigate(R.id.uploadMethodFragment)
//                        return@launch
//                    }
////                    if(uploads.status in listOf(0, 1, 2, 3) && UploadService.taskList.isEmpty()) {
////                        uploads.apply {
////                            status = UploadStatus.ERROR.value
////                            statusMessage = "Process killed"
////                        }.also { info ->
////                            uploadRepo.updateUploadInfo(info)
////                        }
////                        mPref.uploadId = null
////                        navController.navigate(R.id.uploadMethodFragment)
////                    }
//                    if(navController.currentDestination?.id != R.id.editUploadInfoFragment) {
//                        navController.navigate(R.id.editUploadInfoFragment)
//                    }
//                    return@launch
//                }
                navController.navigate(R.id.uploadMethodFragment)
            }
        }

        observe(viewModel.fragmentDetailsMutableLiveData) {
            onDetailsFragmentLoad(it)
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

        observe(viewModel.switchBottomTab) {
//            drawerHelper.onMenuClick(NavigationMenu(ID_CHANNEL, "All Videos", 0, listOf(), false))
            when(it) {
                1-> binding.tabNavigator.selectedItemId = R.id.menu_tv
                3-> binding.tabNavigator.selectedItemId = R.id.menu_explore
            }
//            binding.homeTabPager.currentItem = 1
        }

        observe(viewModel.viewAllCategories) {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.content_viewer)
            if (currentFragment !is AllCategoriesFragment) {
                loadFragmentById(
                    R.id.content_viewer, AllCategoriesFragment(), AllCategoriesFragment::class.java.getName()
                )
            }
            binding.drawerLayout.closeDrawers()
            minimizePlayer()
        }

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

        observe(viewModel.addToPlayListMutableLiveData) { item ->
//            val playListItems = item.filter {
//                !it.isLive
//            }.map {
//                val uriStr = Channel.createChannel(it).getContentUri(this)
//                MediaItem.fromUri(uriStr)
//            }

            setPlayList(item)
        }

        observe(viewModel.shareContentLiveData) { channelInfo ->
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            sharingIntent.putExtra(
                Intent.EXTRA_TEXT,
                channelInfo.video_share_url
            )
            startActivity(Intent.createChooser(sharingIntent, "Share via"))
        }

        if(intent.hasExtra(INTENT_PACKAGE_SUBSCRIBED)){
            handlePackageSubscribe()
        }

        lifecycle.addObserver(HeartBeatManager)
        observeInAppMessage()
        handleSharedUrl(intent)
        configureBottomSheet()
        observeUpload2()
    }

    private fun configureBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior.addBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if(newState == BottomSheetBehavior.STATE_COLLAPSED && binding.playerView.isControllerHidden) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
//                Log.e("SLIDE", slideOffset.toString())
                binding.playerView.moveController(slideOffset)
            }
        })
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

    private fun setupNavController() {
        Log.e("NAV", "SetupNavController")

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.home_nav_host) as NavHostFragment
        navController = navHostFragment.navController

        appbarConfig = AppBarConfiguration(
            setOf(
                R.id.menu_feed,
                R.id.menu_tv,
                R.id.menu_activities,
                R.id.menu_channel,

//                R.id.menu_all_tv_channel,
                R.id.menu_activities,
                R.id.menu_favorites
            ),
            binding.drawerLayout
        )
//        setupActionBarWithNavController(navController, appbarConfig)
//        NavigationUI.setupActionBarWithNavController(this, navController, appbarConfig)
        binding.tbar.toolbar.setupWithNavController(navController, appbarConfig)
        binding.tbar.toolbar.setNavigationIcon(R.drawable.ic_home)
        binding.sideNavigation.setupWithNavController(navController)
        binding.tabNavigator.setupWithNavController(navController)
        binding.sideNavigation.setNavigationItemSelectedListener {
            drawerHelper.handleMenuItemById(it)
        }

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            supportFragmentManager.popBackStack(R.id.content_viewer, POP_BACK_STACK_INCLUSIVE)

            if(binding.draggableView.isMaximized) {
                minimizePlayer()
            }
            binding.tbar.toolbar.setNavigationIcon(R.drawable.ic_home)
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
        resetPlayer()
        binding.playerView.resizeView(calculateScreenWidth())
    }

    override fun resetPlayer() {
        binding.playerView.setPlayer(player)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        setupNavController()
    }

    override fun onPause() {
        super.onPause()
        binding.playerView.clearListeners()
        if (Util.SDK_INT <= 23) {
            binding.playerView.setPlayer(null)
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            binding.playerView.setPlayer(null)
        }
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
            bottom_sheet.visibility = View.VISIBLE
            binding.playerView.scaleX = 1f
            binding.playerView.scaleY = 1f
        } else {
            bottom_sheet.visibility = View.GONE
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
            resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        Utils.setFullScreen(this, state)
        toggleNavigations(state)
        binding.playerView.resizeView(calculateScreenWidth())
        binding.playerView.onFullScreen(state)
    }

    private fun toggleNavigations(state: Boolean) {
        if(state) {
            supportActionBar?.hide()
            binding.bottomAppBar.performHide()
            binding.uploadButton.hide()
        } else {
            supportActionBar?.show()
            binding.bottomAppBar.performShow()
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
        val uri = intent.data
        if (uri != null) {
            val strUri = uri.toString()
             handleDeepLink(strUri)
        }
    }

    private fun handleDeepLink(url: String){
//        https://toffeelive.com/#video/0d52770e16b19486d9914c81061cf2da
        try{
            var isDeepLinkHandled = false
            val route = inAppMessageParser.parseUrl(url)
            route?.drawerId?.let {
                ToffeeAnalytics.logBreadCrumb("Trying to open menu item")
//                drawerHelper.handleMenuItemById(it)
                isDeepLinkHandled = true
            }
            route?.categoryId?.let {
                ToffeeAnalytics.logBreadCrumb("Trying to open category item")
////                drawerHelper.handleCategoryClick(ID_VIDEO, it, route.categoryName ?: "")
                isDeepLinkHandled = true
            }

            if(!isDeepLinkHandled){
                ToffeeAnalytics.logBreadCrumb("Trying to open individual item")
                val hash = url.substring(url.lastIndexOf("/") + 1)
                observe(viewModel.getShareableContent(hash)){ channelResource ->
                    when(channelResource){
                        is Resource.Success -> {
                            channelResource.data?.let {
                                onDetailsFragmentLoad(it)
                            }
                        }
                    }
                }
            }
        }catch (e: Exception){
            ToffeeAnalytics.logBreadCrumb("Failed to handle depplink $url")
            ToffeeAnalytics.logException(e)
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

    private fun handleVoiceSearchEvent(query: String){
        if (!TextUtils.isEmpty(query)) {
            loadFragmentById(
                R.id.content_viewer, SearchFragment.createInstance(query),
                SearchFragment::class.java.name
            )
        }
        if (searchView != null) {
            searchView!!.setQuery(query.toLowerCase(), false)
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
        supportFragmentManager.beginTransaction().replace(R.id.content_viewer, LandingPageFragment())
            .addToBackStack(LandingPageFragment::class.java.name).commit()
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

    private fun onDetailsFragmentLoad(detailsInfo: Any?) {
        val channelInfo = when (detailsInfo) {
            is ChannelInfo -> {
                detailsInfo
            }
            is PlaylistPlaybackInfo -> {
                detailsInfo.currentItem
            }
            else -> null
        }

        channelInfo?.let {
            when{
                it.urlType == PLAY_IN_WEB_VIEW->{
                    HeartBeatManager.triggerEventViewingContentStart(it.id.toInt(), it.type ?: "VOD")
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
                (it.isPurchased || it.isPaidSubscribed) && !it.isExpired(Date())->{
                    maximizePlayer()
                    if(detailsInfo is PlaylistPlaybackInfo) {
                        loadPlayListItem(detailsInfo)
                    } else {
                        loadChannel(it)
                    }
                    loadDetailFragment(detailsInfo)
                }
                else ->{
                    showSubscribePackDialog()
                }
            }
        }
    }

    private fun showSubscribePackDialog(){
        showSubscriptionDialog(this) {
            launchActivity<PackageListActivity>()
        }
    }

    override fun playNext() {
        super.playNext()
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

    private fun loadDetailFragment(info: Any?){
        if(info is ChannelInfo) {
            if (info.isLive) {
                val fragment = supportFragmentManager.findFragmentById(R.id.details_viewer)
                if (fragment !is ChannelFragment) {
                    loadFragmentById(
                        R.id.details_viewer, ChannelFragment.createInstance(
                            getString(R.string.menu_channel_text), showSelected = true
                        )
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
            if (fragment !is MyChannelPlaylistVideosFragment || fragment.getPlaylistId() != info.playlistId) {
                loadFragmentById(
                    R.id.details_viewer, MyChannelPlaylistVideosFragment.newInstance(
                        info
                    )
                )
            } else {
                fragment.setCurrentChannel(info.currentItem)
            }
        } else if(info is PlaylistItem) {
            val fragment = supportFragmentManager.findFragmentById(R.id.details_viewer)
            if (fragment is MyChannelPlaylistVideosFragment) {
                fragment.setCurrentChannel(info.channelInfo)
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
    fun loadFragmentById(id: Int, fragment: Fragment, tag: String) {
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

    private fun observeInAppMessage(){
        FirebaseAnalytics.getInstance(this).logEvent("trigger_inapp_messaging", null)
        inAppMessageParser = InAppMessageParser()
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
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onControllerVisible() {
        if(playlistManager.getCurrentChannel()?.isLive == true &&
            resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    override fun onControllerInVisible() {
        if(bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    override fun onViewMaximize() {
        requestedOrientation = if(binding.playerView.isAutoRotationEnabled)
            ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        else{
            ActivityInfo.SCREEN_ORIENTATION_LOCKED
        }
    }

    override fun onRotationLock(isAutoRotationEnabled: Boolean) {
       if(isAutoRotationEnabled){
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
        HeartBeatManager.triggerEventViewingContentStop()
        binding.draggableView.animation = AnimationUtils.loadAnimation(
            this,
            android.R.anim.fade_out
        )
        binding.draggableView.visibility = View.GONE
        binding.draggableView.resetImmediately()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    fun handleExitApp() {
        AlertDialog.Builder(this, R.style.AlertDialogTheme)
            .setMessage(String.format(EXIT_FROM_APP_MSG, getString(R.string.app_name)))
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                mPref.clear()
                launchActivity<SplashScreenActivity>()
                finish()
            }
            .setNegativeButton(
                "No"
            ) { dialog, _ -> dialog.cancel() }
            .show()
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

    override fun onFullScreenButtonPressed(): Boolean {
        super.onFullScreenButtonPressed()
        if(!binding.playerView.isAutoRotationEnabled)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED;

        return true
    }

    override fun onBackStackChanged() {
        if (supportFragmentManager.backStackEntryCount == 0) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(false)
            drawerHelper.toggle.isDrawerIndicatorEnabled = true
        }
        else if (supportFragmentManager.backStackEntryCount == 1) { //home
            if (searchView != null && !searchView?.isIconified!!) {
                searchView?.onActionViewCollapsed()
            }
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        } else if (resources.configuration.orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else if (binding.draggableView.isMaximized && binding.draggableView.visibility == View.VISIBLE) {
            minimizePlayer()
        } else if (supportFragmentManager.findFragmentById(R.id.content_viewer) is LandingPageFragment) {
            val landingPageFragment =
                supportFragmentManager.findFragmentById(R.id.content_viewer) as LandingPageFragment
            if (!landingPageFragment.onBackPressed())
                finish()
        } else {
            super.onBackPressed()
        }
    }

    fun minimizePlayer() {
        binding.draggableView.minimize()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun maximizePlayer() {
        binding.draggableView.maximize()
        binding.draggableView.visibility = View.VISIBLE
//        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        observe(mPref.profileImageUrlLiveData) {
            menu?.findItem(R.id.action_avatar)
                ?.actionView?.findViewById<ImageView>(R.id.view_avatar)?.loadProfileImage(it)
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
            if (supportFragmentManager.backStackEntryCount > 1) {
                supportFragmentManager.popBackStack(
                    SearchFragment::class.java.name,
                    POP_BACK_STACK_INCLUSIVE
                )
                return@setOnCloseListener true
            }
            false
        }


        val searchBar: LinearLayout = searchView!!.findViewById(R.id.search_bar)
        searchBar.layoutTransition = LayoutTransition()
        //

        val mic = searchView!!.findViewById(androidx.appcompat.R.id.search_voice_btn) as ImageView
        mic.setImageResource(R.drawable.microphone)

        val close = searchView!!.findViewById(androidx.appcompat.R.id.search_close_btn) as ImageView
        close.setImageResource(R.drawable.close)

        val searchIv = searchView!!.findViewById(androidx.appcompat.R.id.search_button) as ImageView
        searchIv.setImageResource(R.drawable.menu_search)


        val searchBadgeTv =
            searchView?.findViewById(androidx.appcompat.R.id.search_badge) as TextView
        searchBadgeTv.background =
            ContextCompat.getDrawable(this@HomeActivity, R.drawable.menu_search)

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
        if (!TextUtils.isEmpty(query)) {
            loadFragmentById(
                R.id.content_viewer, SearchFragment.createInstance(query!!),
                SearchFragment::class.java.name
            )
            return true
        }
        return false;
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return false
    }

    private fun observeUpload2() {
        add_upload_info_button.setOnClickListener {
            navController.navigate(R.id.myChannelHomeFragment)
        }

        close_button.setOnClickListener {
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
                    home_mini_progress_container.isVisible = true
                    val upInfo = it[0]
                    when(upInfo.status){
                        UploadStatus.SUCCESS.value,
                        UploadStatus.SUBMITTED.value -> {
                            mini_upload_progress.progress = 100
                            add_upload_info_button.isVisible = true
                            close_button.isVisible = true
                            upload_size_text.isInvisible = true
                            mini_upload_progress_text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_upload_done, 0, 0, 0)
                            mini_upload_progress_text.text = "Upload complete"
                        }
                        UploadStatus.ADDED.value,
                        UploadStatus.STARTED.value -> {
                            add_upload_info_button.isInvisible = true
                            upload_size_text.isVisible = true
                            close_button.isInvisible = true
                            mini_upload_progress_text.text = "Uploading - ${upInfo.completedPercent}%"
                            mini_upload_progress.progress = upInfo.completedPercent
                            upload_size_text.text = Utils.readableFileSize(upInfo.fileSize)
                        }
                    }
                } else {
                    home_mini_progress_container.isVisible = false
                }
            }
        }
    }
}
