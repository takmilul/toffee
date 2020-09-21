package com.banglalink.toffee.ui.home

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.banglalink.toffee.R
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.databinding.ActivityMainMenuBinding
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.extension.loadProfileImage
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.*
import com.banglalink.toffee.ui.about.AboutActivity
import com.banglalink.toffee.ui.channels.ChannelFragment
import com.banglalink.toffee.ui.common.HtmlPageViewActivity
import com.banglalink.toffee.ui.common.ParentLevelAdapter
import com.banglalink.toffee.ui.favorite.FavoriteFragment
import com.banglalink.toffee.ui.profile.ViewProfileActivity
import com.banglalink.toffee.ui.recent.RecentFragment
import com.banglalink.toffee.ui.redeem.RedeemCodeActivity
import com.banglalink.toffee.ui.refer.ReferAFriendActivity
import com.banglalink.toffee.ui.settings.SettingsActivity
import com.banglalink.toffee.ui.subscription.MySubscriptionActivity
import com.banglalink.toffee.ui.subscription.PackageListActivity
import kotlinx.android.synthetic.main.layout_appbar.view.*
import java.util.ArrayList

class DrawerHelper(val activity: HomeActivity,val binding:ActivityMainMenuBinding): ParentLevelAdapter.OnNavigationItemClickListener{

    lateinit var toggle: ActionBarDrawerToggle

    fun initDrawer() {
        toggle = ActionBarDrawerToggle(
            activity,
            binding.drawerLayout,
            binding.tbar.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        //After instantiating your ActionBarDrawerToggle
        toggle.isDrawerIndicatorEnabled = false
        toggle.setHomeAsUpIndicator(R.drawable.ic_home)
        val parentAdapter =
            ParentLevelAdapter(activity, generateNavMenu(), this, binding.navMenuList)
        binding.navMenuList.setAdapter(parentAdapter)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        toggle.toolbarNavigationClickListener = View.OnClickListener {
            if (activity.supportFragmentManager.backStackEntryCount > 1) {
                activity.supportFragmentManager.popBackStack(LandingPageFragment::class.java.name, 0)
            }
        }

        setProfileInfo()
    }

    private fun setProfileInfo() {
        val header = binding.sideNavigation.getHeaderView(0)
        val profileName = header.findViewById(R.id.profile_name) as TextView
        activity.observe(Preference.getInstance().customerNameLiveData){
            when{
                it.isBlank()->profileName.text =
                    activity.getString(R.string.profile)
                else->{
                    profileName.text=Preference.getInstance().customerName
                }
            }
        }
        val profilePicture = header.findViewById(R.id.profile_picture) as ImageView

        activity.observe(Preference.getInstance().profileImageUrlLiveData){
            profilePicture.loadProfileImage(it)
        }

        profilePicture.setOnClickListener{
            activity.launchActivity<ViewProfileActivity>()
        }
        profileName.setOnClickListener{
            activity.launchActivity<ViewProfileActivity>()
        }

        val navBarClose = header.findViewById<ImageView>(R.id.nav_bar_close)
        navBarClose.setOnClickListener{
            binding.drawerLayout.closeDrawers()
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
                activity.getString(R.string.menu_create_text),
                R.mipmap.ic_menu_create,
                ArrayList(),
                true
            )
        )
        navigationMenuList.add(
            NavigationMenu(
                ID_SUBSCRIPTIONS,
                "Subscriptions",
                R.mipmap.ic_menu_subscriptions,
                ArrayList(),
                true
            )
        )
        navigationMenuList.add(
            NavigationMenu(
                ID_INVITE_FRIEND,
                activity.getString(R.string.refer_a_friend_txt),
                R.mipmap.ic_menu_invite,
                ArrayList()
            )
        )

        navigationMenuList.add(
            NavigationMenu(
                ID_REDEEM_CODE,
                activity.getString(R.string.redeem_code_txt),
                R.mipmap.ic_menu_redeem,
                ArrayList()
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
                activity.getString(R.string.menu_faqs_text),
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
       menu?.let {
           handleMenuItemById(it.id)
       }
    }

    fun handleMenuItemById(id:Int){
        when (id) {
            ID_VIDEO -> {
                val currentFragment = getCurrentContentFragment()
                if (CatchupFragment::class.java.name != currentFragment!!.tag) {
                    activity.loadFragmentById( R.id.content_viewer,
                        CatchupFragment.createInstance(0, 0, "", "All Videos", "All Videos", "VOD")
                        ,CatchupFragment::class.java.name
                    )
                } else {
                    val catchupFragment = currentFragment as CatchupFragment
                    catchupFragment.updateInfo(0, 0, "", "All Videos", "All Videos", "VOD")
                }

                binding.drawerLayout.closeDrawers()
                activity.minimizePlayer()
            }
            ID_RECENT -> {
                val currentFragment = getCurrentContentFragment()
                if (currentFragment !is RecentFragment) {
                    activity.loadFragmentById(
                        R.id.content_viewer, RecentFragment(),
                        RecentFragment::class.java.name
                    )
                }
                binding.drawerLayout.closeDrawers()
                activity.minimizePlayer()
            }
            ID_CHANNEL -> {
                val currentFragment = getCurrentContentFragment()
                if (currentFragment !is ChannelFragment) {
                    activity.loadFragmentById( R.id.content_viewer, ChannelFragment.createInstance(
                        0,
                        "",
                        activity.getString(R.string.menu_channel_text)
                    ), ChannelFragment::class.java.getName())
                }
                binding.drawerLayout.closeDrawers()
                activity.minimizePlayer()
            }
            ID_FAV -> {
                val currentFragment = getCurrentContentFragment()
                if (currentFragment !is FavoriteFragment) {
                    activity.loadFragmentById(
                        R.id.content_viewer, FavoriteFragment(),
                        FavoriteFragment::class.java.getName()
                    )
                }
                binding.drawerLayout.closeDrawers()
                activity.minimizePlayer()
            }
            ID_SUB_VIDEO -> {
                activity.launchActivity<HtmlPageViewActivity> {
                    putExtra(
                        HtmlPageViewActivity.TITLE_KEY,
                        activity.getString(R.string.menu_create_text)
                    )
                    putExtra(HtmlPageViewActivity.CONTENT_KEY, MICRO_SITE_URL)
                }
                binding.drawerLayout.closeDrawers()
            }
            ID_SUBSCRIPTIONS->{
                binding.drawerLayout.closeDrawers()
                if(Preference.getInstance().isSubscriptionActive == "true"){
                    activity.launchActivity<PackageListActivity>()
                }
                else{
                    activity.launchActivity<MySubscriptionActivity>()
                }

            }
            ID_SETTINGS -> {
                activity.launchActivity<SettingsActivity>()
                binding.drawerLayout.closeDrawers()

            }
            ID_ABOUT -> {
                activity.launchActivity<AboutActivity>()
                binding.drawerLayout.closeDrawers()

            }
            ID_FAQ -> {
                activity.launchActivity<HtmlPageViewActivity> {
                    putExtra(HtmlPageViewActivity.CONTENT_KEY, FAQ_URL)
                    putExtra(HtmlPageViewActivity.TITLE_KEY, activity.getString(R.string.menu_faqs_text))
                }
                binding.drawerLayout.closeDrawers()

            }
            ID_LOGOUT->{
                activity.handleExitApp()
            }
            ID_INVITE_FRIEND->{
                activity.launchActivity<ReferAFriendActivity>()
                binding.drawerLayout.closeDrawers()
            }
            ID_REDEEM_CODE->{
                activity.launchActivity<RedeemCodeActivity> ()
                binding.drawerLayout.closeDrawers()
            }
        }
    }

    private fun getCurrentContentFragment(): Fragment? {
        return activity.supportFragmentManager.findFragmentById(R.id.content_viewer)
    }

    override fun onSubCategoryClick(
        subcategory: NavSubcategory?,
        category: NavCategory?,
        parent: NavigationMenu?
    ) {
        //Do nothing
    }

    override fun onCategoryClick(category: NavCategory, parent: NavigationMenu) {
        handleCategoryClick(parent.id,category.id,category.categoryName)
        binding.drawerLayout.closeDrawers()
    }

    fun handleCategoryClick(parentId:Int,categoryId:Int,categoryName:String){
        val currentFragment = getCurrentContentFragment()
        if (parentId == ID_VIDEO && currentFragment!=null) {
            if (CatchupFragment::class.java.name != currentFragment.tag) {
                activity.loadFragmentById(R.id.content_viewer,CatchupFragment.createInstance(
                    categoryId,
                    0,
                    "",
                    "",
                    categoryName,
                    "VOD"
                ), CatchupFragment::class.java.name)
            } else {
                val catchupFragment = currentFragment as CatchupFragment
                catchupFragment.updateInfo(
                   categoryId,
                    0,
                    "",
                    "",
                    categoryName,
                    "VOD"
                )
            }
            activity.minimizePlayer()
        }
    }

    fun updateAdapter(navCategoryList:List<NavCategory>) {
//        val parentAdapter = ParentLevelAdapter(
//            activity,
//            generateNavMenu(),
//            this,
//            binding.navMenuList
//        )
//        binding.navMenuList.setAdapter(parentAdapter)
//        var menu: NavigationMenu
//        try {
//            menu = NavigationMenu(
//                ID_VIDEO,
//                "All Videos",
//                R.mipmap.ic_menu_vod,
//                navCategoryList
//            )
//            parentAdapter.insert(menu, 1)
//            binding.navMenuList.expandGroup(1)
//        } catch (e: Exception) {
//            menu = NavigationMenu(ID_VOD, "VoD", R.mipmap.ic_menu_vod, ArrayList())
//            parentAdapter.insert(menu, 3)
//        }
    }
}