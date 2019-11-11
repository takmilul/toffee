package com.banglalink.toffee.ui.home

import android.animation.LayoutTransition
import android.app.AlertDialog
import android.app.SearchManager
import android.content.Context
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProviders
import coil.api.load
import coil.request.CachePolicy
import com.banglalink.toffee.R
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.databinding.ActivityMainMenuBinding
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.*
import com.banglalink.toffee.ui.about.AboutActivity
import com.banglalink.toffee.ui.channels.ChannelFragment
import com.banglalink.toffee.ui.common.HtmlPageViewActivity
import com.banglalink.toffee.ui.common.ParentLevelAdapter
import com.banglalink.toffee.ui.favorite.FavoriteFragment
import com.banglalink.toffee.ui.player.ChannelInfo
import com.banglalink.toffee.ui.player.PlayerActivity
import com.banglalink.toffee.ui.player.PlayerFragment2
import com.banglalink.toffee.ui.profile.ViewProfileActivity
import com.banglalink.toffee.ui.recent.RecentFragment
import com.banglalink.toffee.ui.settings.SettingsActivity
import com.banglalink.toffee.ui.widget.DraggerLayout
import com.banglalink.toffee.util.Utils
import com.google.android.material.navigation.NavigationView
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.layout_appbar.view.*
import java.util.ArrayList

class HomeActivity : PlayerActivity(), FragmentManager.OnBackStackChangedListener,
    NavigationView.OnNavigationItemSelectedListener, View.OnClickListener,
    ParentLevelAdapter.OnNavigationItemClickListener, DraggerLayout.OnPositionChangedListener {

    private var searchView: SearchView? = null
    private val DELAY_MILLIS = 10
    private val ID_PROFILE = 10
    private val ID_HOME = 11
    private val ID_CHANNEL = 12
    private val ID_RECENT = 13
    private val ID_FAV = 14
    private val ID_SUB = 15
    private val ID_SUB_VIDEO = 16
    private val ID_SETTINGS = 17
    private val ID_ABOUT = 18
    private val ID_LOGOUT = 19
    val ID_VIDEO = 20
    private val ID_VOD = 21
    private val ID_FAQ = 22

    private lateinit var toggle: ActionBarDrawerToggle
    lateinit var binding: ActivityMainMenuBinding

    private val viewModel by lazy {
        ViewModelProviders.of(this).get(HomeViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_menu)
        setSupportActionBar(binding.tbar.toolbar)
        mediaPlayer = supportFragmentManager.findFragmentById(R.id.player_view) as PlayerFragment2
        val landingPageFragment = LandingPageFragment()
        supportFragmentManager.beginTransaction().replace(R.id.content_viewer, landingPageFragment)
            .addToBackStack(LandingPageFragment::class.java.getName()).commit()
        binding.draggableView.setOnPositionChangedListener(this)
        initializeDraggableView()
        supportFragmentManager.addOnBackStackChangedListener(this)
        initDrawer()

//        EventBus.getDefault().register(this)

        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 1) { //home
                if (searchView != null && !searchView?.isIconified!!) {
                    searchView?.onActionViewCollapsed()
                }
            }
        }

//        handleSharedUrl(intent)
        observe(viewModel.categoryLiveData) {
            when (it) {
                is Resource.Success -> {
                    val parentAdapter = ParentLevelAdapter(
                        this,
                        generateNavMenu(),
                        this,
                        binding.navMenuList
                    )
                    binding.navMenuList.setAdapter(parentAdapter)
                    var menu: NavigationMenu
                    try {
                        menu = NavigationMenu(
                            ID_VIDEO,
                            "All Videos",
                            R.mipmap.ic_menu_vod,
                            it.data.vod
                        )
                        parentAdapter.insert(menu, 1)
                        binding.navMenuList.expandGroup(1)
                    } catch (e: Exception) {
                        menu = NavigationMenu(ID_VOD, "VoD", R.mipmap.ic_menu_vod, ArrayList())
                        parentAdapter.insert(menu, 3)
                    }

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
            supportFragmentManager.popBackStack(
                LandingPageFragment::class.java.getName(),
                0
            )
            supportFragmentManager.beginTransaction().replace(
                R.id.content_viewer,
                ChannelFragment.createInstance(
                    0,
                    "",
                    getString(R.string.menu_channel_text)
                )
            )
                .addToBackStack(ChannelFragment::class.java.getName())
                .commit()
            binding.drawerLayout.closeDrawers()
            minimizePlayer()
        }

    }

    private fun loadChannel(channelInfo: ChannelInfo) {
        if (mediaPlayer != null) {
            mediaPlayer.load(channelInfo)
        }
    }

    fun onDetailsFragmentLoad(channelInfo: ChannelInfo?) {
        if (channelInfo != null) {
            if (Integer.parseInt(channelInfo.individual_price) > 0 && channelInfo.individual_purchase || Integer.parseInt(
                    channelInfo.individual_price
                ) == 0 && channelInfo.subscription
            ) {
                maximizePlayer()
                loadChannel(channelInfo)
                if (channelInfo.isLive) {
                    val fragment = supportFragmentManager.findFragmentById(R.id.details_viewer)
                    var channelListFragment: ChannelFragment? = null
                    if (fragment !is ChannelFragment) {
                        channelListFragment = ChannelFragment.createInstance(
                            URL_ALL_CHANNEL_LIST,
                            getString(R.string.menu_channel_text)
                        )
                    } else {
                        channelListFragment = fragment
                        channelListFragment.updateUrl(URL_ALL_CHANNEL_LIST)
                    }
                    loadFragmentById(R.id.details_viewer, channelListFragment!!)
                } else if (channelInfo.isCatchup() || channelInfo.isVOD()) {
                    loadFragmentById(
                        R.id.details_viewer,
                        CatchupDetailsFragment.createInstance(channelInfo)
                    )
                }
            } else {
//                startActivity(Intent(this@MainMenuActivity, ContentPurchaseActivity::class.java))
                showToast("Purchase channel")
            }
        }
    }

    private fun initializeDraggableView() {
        binding.draggableView.visibility = View.GONE
        binding.draggableView.isClickable = true
    }

    private fun initDrawer() {
        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.tbar.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        //After instantiating your ActionBarDrawerToggle
        toggle.isDrawerIndicatorEnabled = false
        toggle.setHomeAsUpIndicator(R.drawable.ic_home)
        val parentAdapter =
            ParentLevelAdapter(this, generateNavMenu(), this, binding.navMenuList)
        binding.navMenuList.setAdapter(parentAdapter)
        binding.drawerLayout.setDrawerListener(toggle)
        toggle.syncState()
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        toggle.toolbarNavigationClickListener = View.OnClickListener {
            if (supportFragmentManager.backStackEntryCount > 1) {
                supportFragmentManager.popBackStack(LandingPageFragment::class.java.name, 0)
            }
        }

        setNameAndCredit()
        setProfileInfo()
    }

    private fun setNameAndCredit() {
        val header = binding.navView.getHeaderView(0)
        (header.findViewById<View>(R.id.credit_tv) as TextView).text =
            "Credit: " + Preference.getInstance().balance
        (header.findViewById<View>(R.id.customer_name_tv) as TextView).text =
            Preference.getInstance().customerName
        header.findViewById<View>(R.id.nav_bar_close).setOnClickListener {
            if (binding.drawerLayout != null) {
                binding.drawerLayout.closeDrawer(GravityCompat.END)
            }
        }
    }

    private fun setProfileInfo() {
        val header = binding.navView.getHeaderView(0)
        val profileName = header.findViewById(R.id.profile_name) as TextView
        if(!Preference.getInstance().customerName.isBlank()){
            profileName.text=Preference.getInstance().customerName
        }
        val profilePicture = header.findViewById(R.id.profile_picture) as ImageView

        if(!Preference.getInstance().userImageUrl.isNullOrBlank()){
            profilePicture.load(Preference.getInstance().userImageUrl){
                memoryCachePolicy(CachePolicy.DISABLED)
                diskCachePolicy(CachePolicy.ENABLED)
            }
        }

        profilePicture.setOnClickListener{
            launchActivity<ViewProfileActivity>()
        }
        profileName.setOnClickListener{
            launchActivity<ViewProfileActivity>()
        }
    }

    private fun generateNavMenu(): List<NavigationMenu> {
        val navigationMenuList = ArrayList<NavigationMenu>()
        navigationMenuList.add(
            NavigationMenu(
                ID_CHANNEL,
                "TV Channels",
                R.mipmap.ic_menu_channels,
                ArrayList(),
                true
            )
        )
        navigationMenuList.add(
            NavigationMenu(
                ID_RECENT,
                "Recent",
                R.mipmap.ic_menu_recent,
                ArrayList()
            )
        )
        navigationMenuList.add(
            NavigationMenu(
                ID_FAV,
                "Favorites",
                R.mipmap.ic_menu_favorites,
                ArrayList()
            )
        )
        navigationMenuList.add(
            NavigationMenu(
                ID_SUB_VIDEO,
                getString(R.string.menu_create_text),
                R.mipmap.ic_menu_create,
                ArrayList(),
                true
            )
        )
        navigationMenuList.add(
            NavigationMenu(
                ID_SUB,
                "Subscriptions",
                R.mipmap.ic_menu_subscriptions,
                ArrayList(),
                true
            )
        )
        navigationMenuList.add(
            NavigationMenu(
                ID_SETTINGS,
                "Settings",
                R.mipmap.ic_menu_settings,
                ArrayList()
            )
        )
        navigationMenuList.add(
            NavigationMenu(
                ID_ABOUT,
                "About",
                R.mipmap.ic_menu_about,
                ArrayList()
            )
        )
        navigationMenuList.add(
            NavigationMenu(
                ID_FAQ,
                getString(R.string.menu_faqs_text),
                R.drawable.ic_menu_faq,
                ArrayList()
            )
        )
        navigationMenuList.add(
            NavigationMenu(
                ID_LOGOUT,
                "Logout",
                R.mipmap.ic_menu_exit,
                ArrayList()
            )
        )
        return navigationMenuList
    }

    override fun onMenuClick(menu: NavigationMenu?) {
        when (menu?.id) {
            ID_VIDEO -> {
                val currentFragment = getCurrentContentFragment()
                if (CatchupFragment::class.java.name != currentFragment!!.tag) {

                    supportFragmentManager.popBackStack(
                        LandingPageFragment::class.java.getName(),
                        0
                    )
                    supportFragmentManager.beginTransaction().replace(
                        R.id.content_viewer,
                        CatchupFragment.createInstance(0, 0, "", "All Videos", menu.name, "VOD"),
                        CatchupFragment::class.java.name
                    ).addToBackStack(CatchupFragment::class.java.name)
                        .commit()
                } else {
                    val catchupFragment = currentFragment as CatchupFragment
                    catchupFragment.updateInfo(0, 0, "", "All Videos", menu.name, "VOD")
                }

                binding.drawerLayout.closeDrawers()
                minimizePlayer()
            }
            ID_RECENT -> {
                val currentFragment = getCurrentContentFragment()
                if (currentFragment !is RecentFragment) {
                    supportFragmentManager.popBackStack(
                        LandingPageFragment::class.java.name,
                        0
                    )
                    loadFragmentById(
                        R.id.content_viewer, RecentFragment(),
                        RecentFragment::class.java.name
                    )
                }
                binding.drawerLayout.closeDrawers()
                minimizePlayer()
            }
            ID_CHANNEL -> {
                val currentFragment = getCurrentContentFragment()
                if (currentFragment !is ChannelFragment) {
                    supportFragmentManager.popBackStack(
                        LandingPageFragment::class.java.getName(),
                        0
                    )
                    supportFragmentManager.beginTransaction().replace(
                        R.id.content_viewer,
                        ChannelFragment.createInstance(
                            0,
                            "",
                            getString(R.string.menu_channel_text)
                        )
                    )
                        .addToBackStack(ChannelFragment::class.java.getName())
                        .commit()
                }
                binding.drawerLayout.closeDrawers()
                minimizePlayer()
            }
            ID_FAV -> {
                val currentFragment = getCurrentContentFragment()
                if (currentFragment !is FavoriteFragment) {
                    supportFragmentManager.popBackStack(
                        LandingPageFragment::class.java.getName(),
                        0
                    )
                    loadFragmentById(
                        R.id.content_viewer, FavoriteFragment(),
                        FavoriteFragment::class.java.getName()
                    )
                }
                binding.drawerLayout.closeDrawers()
                minimizePlayer()
            }
            ID_SUB_VIDEO -> {
                launchActivity<HtmlPageViewActivity> {
                    putExtra(
                        HtmlPageViewActivity.TITLE_KEY,
                        getString(R.string.menu_create_text)
                    )
                    putExtra(HtmlPageViewActivity.CONTENT_KEY, MICRO_SITE_URL)
                }
                binding.drawerLayout.closeDrawers()
            }
            ID_SETTINGS -> {
                launchActivity<SettingsActivity>()
                binding.drawerLayout.closeDrawers()

            }
            ID_ABOUT -> {
                launchActivity<AboutActivity>()
                binding.drawerLayout.closeDrawers()
                binding.drawerLayout.closeDrawers()

            }
            ID_FAQ -> {
                launchActivity<HtmlPageViewActivity> {
                    putExtra(HtmlPageViewActivity.CONTENT_KEY, FAQ_URL)
                    putExtra(HtmlPageViewActivity.TITLE_KEY, getString(R.string.menu_faqs_text))
                }
                binding.drawerLayout.closeDrawers()

            }
            ID_LOGOUT->{
                handleExitApp()
            }
        }
    }

    private fun getCurrentContentFragment(): Fragment? {
        return supportFragmentManager.findFragmentById(R.id.content_viewer)
    }

    protected fun loadFragmentById(id: Int, fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(id, fragment).commit()
    }

    protected fun loadFragmentById(id: Int, fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(id, fragment).addToBackStack(tag).commit()
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

    private fun handleExitApp() {
        AlertDialog.Builder(this)
            .setMessage(String.format(EXIT_FROM_APP_MSG, getString(R.string.app_name)))
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                Preference.getInstance().clear()
                finish()
                this@HomeActivity.finish()
            }
            .setNegativeButton("No"
            ) { dialog, id -> dialog.cancel() }
            .show()
    }
    override fun onViewDestroy() {
        mediaPlayer.onPause()
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

    override fun onBackStackChanged() {
        if (supportFragmentManager.backStackEntryCount == 0) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(false)
            toggle.isDrawerIndicatorEnabled = true
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout != null && binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        } else if (Utils.isFullScreen(this)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else if (binding.draggableView.isMaximized && binding.draggableView.getVisibility() == View.VISIBLE) {
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

    private fun minimizePlayer() {
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

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onClick(p0: View?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCategoryClick(category: NavCategory?, parent: NavigationMenu?) {

    }

    override fun onSubCategoryClick(
        subcategory: NavSubcategory?,
        category: NavCategory?,
        parent: NavigationMenu?
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val imageUrl = Preference.getInstance().userImageUrl
        if(!imageUrl.isNullOrBlank()){
            menu?.findItem(R.id.action_avatar)?.actionView?.findViewById<ImageView>(R.id.view_avatar)?.load(imageUrl) {
                memoryCachePolicy(CachePolicy.DISABLED)
                diskCachePolicy(CachePolicy.ENABLED)
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
        searchView = searchMenuItem.actionView as SearchView
        searchView!!.setMaxWidth(Integer.MAX_VALUE)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView!!.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView!!.setIconifiedByDefault(true)
//        searchView.setOnCloseListener {
//            if (supportFragmentManager.backStackEntryCount > 1) {
//                supportFragmentManager.popBackStack(
//                    SearchListFragment::class.java!!.getName(),
//                    FragmentManager.POP_BACK_STACK_INCLUSIVE
//                )
//            }
//            false
//        }

        val searchBar = searchView!!.findViewById(R.id.search_bar) as LinearLayout
        searchBar.layoutTransition = LayoutTransition()
        //
        val searchAutoComplete =
            searchView!!.findViewById(androidx.appcompat.R.id.search_src_text) as AutoCompleteTextView
        val mic = searchView!!.findViewById(androidx.appcompat.R.id.search_voice_btn) as ImageView
        mic.setImageResource(R.drawable.microphone)
        val close = searchView!!.findViewById(androidx.appcompat.R.id.search_close_btn) as ImageView
        close.setImageResource(R.drawable.close)
        val searchIv = searchView!!.findViewById(androidx.appcompat.R.id.search_button) as ImageView
        searchIv.setImageResource(R.drawable.menu_search)

        val searchBadgetTv =
            searchView!!.findViewById(androidx.appcompat.R.id.search_badge) as TextView
        searchBadgetTv.background = resources.getDrawable(R.drawable.menu_search)

        searchAutoComplete.textSize = 18f
        searchAutoComplete.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.searchview_input_text_color
            )
        )
        searchAutoComplete.background = resources.getDrawable(R.drawable.searchview_input_bg)

        val awesomeMenuItem = menu.findItem(R.id.action_avatar)
        val awesomeActionView = awesomeMenuItem.actionView
        awesomeActionView.setOnClickListener { onOptionsItemSelected(awesomeMenuItem) }

        return super.onCreateOptionsMenu(menu)
    }
}
