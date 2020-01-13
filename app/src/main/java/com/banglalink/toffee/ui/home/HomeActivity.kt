package com.banglalink.toffee.ui.home

import android.animation.LayoutTransition
import android.app.AlertDialog
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProviders
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.R
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.databinding.ActivityMainMenuBinding
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.extension.loadProfileImage
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.*
import com.banglalink.toffee.ui.channels.ChannelFragment
import com.banglalink.toffee.ui.login.SigninByPhoneActivity
import com.banglalink.toffee.ui.player.PlayerActivity
import com.banglalink.toffee.ui.player.PlayerFragment2
import com.banglalink.toffee.ui.search.SearchFragment
import com.banglalink.toffee.ui.widget.DraggerLayout
import com.banglalink.toffee.ui.widget.showAlertDialog
import com.banglalink.toffee.ui.widget.showDisplayMessageDialog
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.unsafeLazy
import kotlinx.android.synthetic.main.layout_appbar.view.*

const val ID_CHANNEL = 12
const val ID_RECENT = 13
const val ID_FAV = 14
const val ID_SUBSCIPTIONS = 15
const val ID_SUB_VIDEO = 16
const val ID_SETTINGS = 17
const val ID_ABOUT = 18
const val ID_LOGOUT = 19
const val ID_VIDEO = 20
const val ID_VOD = 21
const val ID_FAQ = 22
const val ID_INVITE_FRIEND = 23

class HomeActivity : PlayerActivity(), FragmentManager.OnBackStackChangedListener, DraggerLayout.OnPositionChangedListener {

    private var searchView: SearchView? = null
    lateinit var binding: ActivityMainMenuBinding
    lateinit var drawerHelper: DrawerHelper

    companion object{
        const val INTENT_REFERRAL_REDEEM_MSG = "REFERRAL_REDEEM_MSG"
    }

    private val viewModel by unsafeLazy {
        ViewModelProviders.of(this).get(HomeViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //disable screen capture
        if(!BuildConfig.DEBUG) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_menu)
        setSupportActionBar(binding.tbar.toolbar)
        mediaPlayer = supportFragmentManager.findFragmentById(R.id.player_view) as PlayerFragment2
        val landingPageFragment = LandingPageFragment()
        supportFragmentManager.beginTransaction().replace(R.id.content_viewer, landingPageFragment)
            .addToBackStack(LandingPageFragment::class.java.getName()).commit()
        binding.draggableView.setOnPositionChangedListener(this)
        initializeDraggableView()
        supportFragmentManager.addOnBackStackChangedListener(this)
        drawerHelper = DrawerHelper(this,binding)
        drawerHelper.initDrawer()

        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 1) { //home
                if (searchView != null && !searchView?.isIconified!!) {
                    searchView?.onActionViewCollapsed()
                }
            }
        }

        handleSharedUrl(intent)
        observe(viewModel.categoryLiveData) {
            when (it) {
                is Resource.Success -> {
                   drawerHelper.updateAdapter(it.data.vod)
                }
                is Resource.Failure -> {
                    showToast(it.error.msg)
                }
            }
        }

        observe(viewModel.fragmentDetailsMutableLiveData) {
            onDetailsFragmentLoad(it)
        }
        observe(viewModel.viewAllChannelLiveData){
            drawerHelper.onMenuClick(NavigationMenu(ID_CHANNEL,"All Videos",0, listOf(),false))
        }

        observe(viewModel.viewAllVideoLiveData){
            drawerHelper.onMenuClick(NavigationMenu(ID_VIDEO,"All Videos",0, listOf(),false))
        }

        //Observing any changes in session token....
        observe(Preference.getInstance().sessionTokenLiveData){
            if (mediaPlayer != null && mediaPlayer.isVisible && mediaPlayer.channelInfo != null) {
                mediaPlayer.load(mediaPlayer.channelInfo)
            }
        }


        //show referral redeem msg if possible
        val msg = intent.getStringExtra(INTENT_REFERRAL_REDEEM_MSG)
        msg?.let {
            showDisplayMessageDialog(this,it)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopHeartBeatTimer()
    }

    override fun onResume() {
        super.onResume()
        viewModel.startHeartBeatTimer()
    }

    private fun handleSharedUrl(intent: Intent){
        val uri = intent.data
        if (uri != null) {
            val strUri = uri.toString()
            val hash = strUri.substring(strUri.lastIndexOf("/") + 1)
            observe(viewModel.getShareableContent(hash)){ channelResource ->
                when(channelResource){
                    is Resource.Success->{
                        channelResource.data?.let {
                            onDetailsFragmentLoad(it)
                        }
                    }
                }
            }
        }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            if (!TextUtils.isEmpty(query)) {
                loadFragmentById(
                    R.id.content_viewer, SearchFragment.createInstance(query),
                    SearchFragment::class.java.getName()
                )
            }
            if (searchView != null) {
                searchView!!.setQuery(query.toLowerCase(), false)
                searchView!!.clearFocus()
            }
        }
        handleSharedUrl(intent)
    }
    private fun loadChannel(channelInfo: ChannelInfo) {
        if (mediaPlayer != null) {
            mediaPlayer.load(channelInfo)
        }
    }

    fun onDetailsFragmentLoad(channelInfo: ChannelInfo?) {
        if (channelInfo != null) {
            if (channelInfo.isPurchased || channelInfo.isFree) {
                maximizePlayer()
                loadChannel(channelInfo)
                if (channelInfo.isLive) {
                    val fragment = supportFragmentManager.findFragmentById(R.id.details_viewer)
                    if (fragment !is ChannelFragment) {
                       loadFragmentById(R.id.details_viewer, ChannelFragment.createInstance(
                           getString(R.string.menu_channel_text)
                       ))
                    }
                } else {
                    loadFragmentById(
                        R.id.details_viewer,
                        CatchupDetailsFragment.createInstance(channelInfo)
                    )
                }
            } else {
                launchActivity<ContentPurchaseActivity>()
            }
        }
    }

    fun loadFragmentById(id: Int, fragment: Fragment, tag: String) {
        supportFragmentManager.popBackStack(
            LandingPageFragment::class.java.getName(),
            0
        )
        supportFragmentManager.beginTransaction()
            .replace(id, fragment).addToBackStack(tag).commit()
    }

    fun loadFragmentById(id: Int, fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(id, fragment).commit()
    }

    private fun initializeDraggableView() {
        binding.draggableView.visibility = View.GONE
        binding.draggableView.isClickable = true
    }

    override fun onViewMinimize() {
        if (mediaPlayer != null) {
            mediaPlayer.onMinimizePlayer()
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    override fun onViewMaximize() {
        if (mediaPlayer != null) {
            mediaPlayer.onMaximizePlayer()
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        }
    }

    fun handleExitApp() {
        AlertDialog.Builder(this)
            .setMessage(String.format(EXIT_FROM_APP_MSG, getString(R.string.app_name)))
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                Preference.getInstance().clear()
                launchActivity<SigninByPhoneActivity>()
                finish()
            }
            .setNegativeButton("No"
            ) { dialog, id -> dialog.cancel() }
            .show()
    }
    override fun onViewDestroy() {
        mediaPlayer.pausePlayer()
        binding.draggableView.animation = AnimationUtils.loadAnimation(
            this,
            android.R.anim.fade_out
        )
        binding.draggableView.visibility = View.GONE
        binding.draggableView.resetImmediately()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onDrawerButtonPressed(): Boolean {
        binding.drawerLayout.openDrawer(GravityCompat.END, true)
        return super.onDrawerButtonPressed()
    }

    override fun onMinimizeButtonPressed(): Boolean {
        minimizePlayer()
        return true
    }

    override fun onBackStackChanged() {
        if (supportFragmentManager.backStackEntryCount == 0) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(false)
            drawerHelper.toggle.isDrawerIndicatorEnabled = true
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout != null && binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        } else if (Utils.isFullScreen(this)) {
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
        if (binding.draggableView != null) {
            binding.draggableView.minimize()
            mediaPlayer.onMinimizePlayer()
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    private fun maximizePlayer() {
        if (binding.draggableView != null) {
            binding.draggableView.maximize()
            binding.draggableView.visibility = View.VISIBLE
            mediaPlayer.onMaximizePlayer()
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val imageUrl = Preference.getInstance().userImageUrl
        if(!imageUrl.isNullOrBlank()){
            observe(Preference.getInstance().profileImageUrlLiveData){
                menu?.findItem(R.id.action_avatar)?.actionView?.findViewById<ImageView>(R.id.view_avatar)?.loadProfileImage(it)
            }

        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home) {
            supportFragmentManager.popBackStack()
        } else if (item.itemId == R.id.action_avatar) {
            binding.drawerLayout.openDrawer(GravityCompat.END, true)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        val searchMenuItem = menu.findItem(R.id.action_search)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
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
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
            }
            false
        }

        val searchBar:LinearLayout = searchView!!.findViewById(R.id.search_bar)
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
        searchBadgeTv.background = ContextCompat.getDrawable(this@HomeActivity,R.drawable.menu_search)

        val searchAutoComplete:AutoCompleteTextView =
            searchView!!.findViewById(androidx.appcompat.R.id.search_src_text)
        searchAutoComplete.apply {
            textSize = 18f
            setTextColor(
                ContextCompat.getColor(
                    this@HomeActivity,
                    R.color.searchview_input_text_color
                )
            )
            background = ContextCompat.getDrawable(this@HomeActivity,R.drawable.searchview_input_bg)
        }


        val awesomeMenuItem = menu.findItem(R.id.action_avatar)
        val awesomeActionView = awesomeMenuItem.actionView
        awesomeActionView.setOnClickListener { onOptionsItemSelected(awesomeMenuItem) }

        return super.onCreateOptionsMenu(menu)
    }
}
